package ch.uzh.csg.p2p;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.OnlineStatus;
import ch.uzh.csg.p2p.model.OnlineStatusTask;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.BootstrapRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.OnlineStatusRequest;
import ch.uzh.csg.p2p.model.request.Request;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.FutureGetListener;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.UserRequest;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.ObjectDataReply;

public class Node extends Observable {

	protected final String BOOTSTRAPNODE = "Bootstrapnode";
	private final int DEFAULTPORT = 54000;
	private final long TIMERDELAY = 10000;
	private boolean running = true;

	private Logger log;
	private User user;
	private ObservableList<Friend> friendList =
			FXCollections.observableList(new ArrayList<Friend>());
	private Timer onlineStatusTaskTimer;
	protected PeerDHT peer;

	public Node(int nodeId, String ip, String username, String password, boolean bootstrapNode, Observer nodeReadyObserver)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		log = LoggerFactory.getLogger("Node of user " + username);
		user = new User(username, password, null);
		int id = ((Long) System.currentTimeMillis()).intValue();

		if (nodeReadyObserver != null) {
			addObserver(nodeReadyObserver);
		}
		// if not a BootstrapNode
		createPeerAndInitiateUser(id, username, password, bootstrapNode, ip);
	}

	private void nodeReady() {
		setChanged();
		notifyObservers(this);
		startOnlineStatusTask();
	}

	public void loadStoredDataFromDHT()
			throws UnsupportedEncodingException, LineUnavailableException {
		// TODO load messages, calls, friendrequests...
		loadFriendlistFromDHTAndAnnounceOnlineStatus();
	}

	private void initiateUser(final String username, final String password)
			throws ClassNotFoundException, LineUnavailableException, IOException {
		FutureGetListener<User> userExistsListener = new FutureGetListener<User>(this) {
			@Override
			public void operationComplete(FutureGet futureGet) throws Exception {
				if (futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
					// user already exist --> only update address
					LoginHelper.updatePeerAddress(this.node, username);
				} else {
					// user does not exist --> add user
					LoginHelper.saveUsernamePassword(this.node, username, password);
					User newUser = new User(username, password, this.node.getPeer().peerAddress());
					this.node.setUser(newUser);
				}
				nodeReady();
			}
		};

		LoginHelper.retrieveUser(username, this, userExistsListener);
	}

	private void loadFriendlistFromDHTAndAnnounceOnlineStatus()
			throws UnsupportedEncodingException, LineUnavailableException {

		for (String friendName : user.getFriendStorage()) {
			User user = new User(friendName, null, null);
			FutureGetListener<User> requestListener = new FutureGetListener<User>(this) {
				@Override
				public void operationComplete(FutureGet futureGet) throws Exception {
					if (futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
						User user = (User) futureGet.data().object();
						Friend friend = new Friend(user.getPeerAddress(), user.getUsername());
						this.node.friendList.add(friend);
					}
					announceChangedToOnlineStatus();
				}
			};

			LoginHelper.retrieveUser(friendName, this, requestListener);
		}
	}

	protected void startOnlineStatusTask() {
    OnlineStatusTask onlineStatusTask = new OnlineStatusTask(this);
    onlineStatusTaskTimer = new Timer();
    onlineStatusTaskTimer.scheduleAtFixedRate(onlineStatusTask, 0, TIMERDELAY);
  }
	
	protected void stopOnlineStatusTask(){
	  if(onlineStatusTaskTimer!= null){
	  onlineStatusTaskTimer.cancel();
	  }
	}

  public void registerForFriendListUpdates(ListChangeListener<Friend> listener) {
		friendList.addListener(listener);
	}

	protected void createPeerAndInitiateUser(int nodeId, String username, String password, boolean bootstrapNode, String ip)
			throws IOException, ClassNotFoundException, LineUnavailableException {
		Bindings b = new Bindings().listenAny();
		peer = new PeerBuilderDHT(
				new PeerBuilder(new Number160(nodeId)).ports(getPort()).bindings(b).start())
						.start();

		if (bootstrapNode) {
		  // Bootstrapnode
			BootstrapRequest request = new BootstrapRequest(RequestType.STORE);
			request.setBootstrapNodeIP(ip);
			BaseFutureListener<FuturePut> futurePutListener = new BaseFutureListener<FuturePut>(){
            @Override
            public void operationComplete(FuturePut future) throws Exception {
              peer.peer().objectDataReply(new ObjectDataReply() {
                public Object reply(PeerAddress peerAddress, Object object) throws Exception {
                    return handleReceivedData(peerAddress, object);
                }
            });
            // TODO: Indirect Replication or direct? Replication factor?
            new IndirectReplication(peer).autoReplication(true).replicationFactor(3).start();    
            
            initiateUser(username, password);
            }   

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
              log.error(t.getMessage());
            }
			  
			};
			RequestHandler.handleRequest(request, this, null, futurePutListener);
		}
		else{
		  peer.peer().objectDataReply(new ObjectDataReply() {
            public Object reply(PeerAddress peerAddress, Object object) throws Exception {
                return handleReceivedData(peerAddress, object);
            }
        });
		  new IndirectReplication(peer).autoReplication(true).replicationFactor(3).start(); 
		  
		  connectToNode(ip, username, password);
		}
	}

	protected Object handleReceivedData(PeerAddress peerAddress, Object object)
			throws IOException, LineUnavailableException, ClassNotFoundException {

		log.info("received message: " + object.toString() + " from: " + peerAddress.toString());

		if (object instanceof Message) {
			MessageRequest messageRequest = new MessageRequest();
			messageRequest.setType(RequestType.RECEIVE);
			messageRequest.setMessage((Message) object);
			RequestHandler.handleRequest(messageRequest, this);
		}
		/*
		 * if (object instanceof AudioMessage) { messageRequest.setType(RequestType.RECEIVE);
		 * messageRequest.setMessage((AudioMessage) object);
		 * RequestHandler.handleRequest(messageRequest, this); } else if (object instanceof
		 * ChatMessage) { messageRequest.setType(RequestType.RECEIVE);
		 * messageRequest.setMessage((ChatMessage) object);
		 * RequestHandler.handleRequest(messageRequest, this);}
		 */
		/*
		 * else if (object instanceof AudioRequest) { AudioRequest audioRequest = (AudioRequest)
		 * object; audioRequest.setType(RequestType.RECEIVE);
		 * RequestHandler.handleRequest(audioRequest, this); }
		 */
		else if (object instanceof Request) {
			Request r = (Request) object;
			r.setType(RequestType.RECEIVE);
			RequestHandler.handleRequest(r, this);
		} else {
			System.out.println("else");
		}

		return 0;
	}

	private void connectToNode(String knownIP, String username, String password) throws ClassNotFoundException, IOException, LineUnavailableException {
		BootstrapRequest request =
				new BootstrapRequest(RequestType.SEND, user.getPeerAddress(), knownIP);
		BaseFutureListener<FutureBootstrap> futureBootstrapListener = new BaseFutureListener<FutureBootstrap>() {

      @Override
      public void operationComplete(FutureBootstrap future) throws Exception {
        
        log.info(getUser().getUsername() + " knows: "
                + getPeer().peerBean().peerMap().all() + " unverified: "
                + getPeer().peerBean().peerMap().allOverflow());
        log.info("Waiting for maintenance ping");
         
        initiateUser(username, password);
      }

      @Override
      public void exceptionCaught(Throwable t) throws Exception {
        log.error(t.getMessage());
      }
  
		};
		RequestHandler.handleRequest(request, this, null, futureBootstrapListener);
	}

	private int getPort() {
		int port = DEFAULTPORT;
		while (portIsOpen("127.0.0.1", port, 200)) {
			port++;
		}
		log.info("Free port found, port is: " + port);
		return port;
	}

	private boolean portIsOpen(String ip, int port, int timeout) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), timeout);
			socket.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public PeerDHT getPeer() {
		return peer;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		if (user != null) {
			this.user = user;
		} else {
			User newUser = new User(this.user.getUsername(), this.user.getPassword(),
					this.getPeer().peerAddress());
			this.user = newUser;
		}
	}

	public List<Friend> getFriendList() {
		return friendList;
	}

	public void shutdown() {
		log.info("Shutting down gracefully.");
		running = false;
		if (peer != null) {
		  stopOnlineStatusTask();
		    try {
		      announceChangedToOfflineStatus();
        } catch (LineUnavailableException e) {
          e.printStackTrace();
        }
		}
	}

	public void announceChangedToOnlineStatus() throws LineUnavailableException{
	  for(Friend f : friendList){
	      OnlineStatusRequest request = new OnlineStatusRequest(f.getPeerAddress(),
	          peer.peerAddress(), user.getUsername(), f.getName(), RequestType.SEND);
	      request.setChangedPeerAddress(true);
	      request.setOnlineStatus(OnlineStatus.ONLINE);
	   // send new OnlineRequest, act like this is an answer to a request, and send a positive answer so status goes online	       
	      request.setStatus(RequestStatus.ACCEPTED);
	      BaseFutureListener<FutureDirect> futureDirectListener = new BaseFutureListener<FutureDirect>(){
	        @Override
	        public void operationComplete(FutureDirect future) throws Exception {
	          if(future != null && future.isSuccess()) {
	              f.setStatus(OnlineStatus.ONLINE);
	          }
	          else {
	          f.setStatus(OnlineStatus.OFFLINE);  
	          }
	      }

          @Override
          public void exceptionCaught(Throwable t) throws Exception {
            log.error(t.getMessage());
          }
	  };
	      RequestHandler.handleRequest(request, this, null, futureDirectListener);
	  }
	}
	
	private void announceChangedToOfflineStatus() throws LineUnavailableException {
	  if(friendList.isEmpty()){
	    peer.shutdown();
	  }
	  else{
	    int i = 1;
	  for(Friend f : friendList){
      OnlineStatusRequest request = new OnlineStatusRequest(f.getPeerAddress(),
          peer.peerAddress(), user.getUsername(), f.getName(), RequestType.SEND);
   // act like we received an OnlineStatusRequest and answer with Aborted so Status goes to offline
      request.setOnlineStatus(OnlineStatus.OFFLINE);
      request.setStatus(RequestStatus.ABORTED);
      boolean lastInLine = (i == friendList.size());
      BaseFutureListener<FutureDirect> futureDirectListener = new BaseFutureListener<FutureDirect>(){
        @Override
        public void operationComplete(FutureDirect future) throws Exception {
          if(lastInLine){
            peer.shutdown();
          }
      }

        @Override
        public void exceptionCaught(Throwable t) throws Exception {
          log.error(t.getMessage());
        }
  };
      RequestHandler.handleRequest(request, this, null, futureDirectListener);
      i++;
	    }
	  }
  }

  public Friend getFriend(String currentChatPartner) {
		for (Friend f : friendList) {
			if (f.getName().equals(currentChatPartner)) {
				return f;
			}
		}
		return null;
	}

	public void addFriend(Friend friend){
	  if(!friendList.contains(friend)){
		friendList.add(friend);
	  }
	  
		OnlineStatusRequest req = new OnlineStatusRequest(friend.getPeerAddress(), 
	          peer.peerAddress(), user.getUsername(), friend.getName(), RequestType.SEND);
	      req.setOnlineStatus(OnlineStatus.ONLINE);
	      req.setStatus(RequestStatus.ACCEPTED);
	      BaseFutureListener<FutureDirect> futureDirectListener = new BaseFutureListener<FutureDirect>(){
	        @Override
	        public void operationComplete(FutureDirect future) throws Exception {
	          if(future.isCompleted() && future.isSuccess()){
	          friend.setStatus(OnlineStatus.ONLINE);
	          }
	          else{
	            friend.setStatus(OnlineStatus.OFFLINE);
	          }
	      }

          @Override
          public void exceptionCaught(Throwable t) throws Exception {
            log.error(t.getMessage());
          }
	  };
	      try {
	        RequestHandler.handleRequest(req, this, null, futureDirectListener);
	      } catch (LineUnavailableException e) {
	        e.printStackTrace();
	      }
	}

}
