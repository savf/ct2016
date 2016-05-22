package ch.uzh.csg.p2p;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.controller.LoginWindowController;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.OnlineStatus;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.BootstrapRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.OnlineStatusRequest;
import ch.uzh.csg.p2p.model.request.Request;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestListener;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.StatusListener;
import ch.uzh.csg.p2p.model.request.UserRequest;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.ObjectDataReply;

public class Node extends Observable{

	protected final String BOOTSTRAPNODE = "Bootstrapnode";
	private final int DEFAULTPORT = 54000;

	private Logger log;
	private User user;
	private ObservableList<Friend> friendList = FXCollections.observableList(new ArrayList<Friend>());

	protected PeerDHT peer;
	

	public Node(int nodeId, String ip, String username, String password, Observer nodeReadyObserver)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		log = LoggerFactory.getLogger("Node of user " + username);
		user = new User(username, password, null);
		int id = ((Long) System.currentTimeMillis()).intValue();
		
		if(nodeReadyObserver != null) {
			addObserver(nodeReadyObserver);
		}
		// if not a BootstrapNode
		if (ip != null) {
			createPeer(id, username, password, false);
			connectToNode(ip);
		} else {
			createPeer(nodeId, username, password, true);
		}
		initiateUser(username, password);
	}
	
	private void nodeReady() {
		setChanged();
		notifyObservers(this);
	}

	public void loadStoredDataFromDHT()
			throws UnsupportedEncodingException, LineUnavailableException {
		// TODO load messages, calls, friendrequests...
		loadFriendlistFromDHTAndAnnounceOnlineStatus();
	}

	private void initiateUser(final String username, final String password)
			throws ClassNotFoundException, LineUnavailableException, IOException {
		RequestListener<User> userExistsListener = new RequestListener<User>(this) {
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
			RequestListener<User> requestListener = new RequestListener<User>(this) {
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
	
	public void registerForFriendListUpdates(ListChangeListener<Friend> listener) {
		friendList.addListener(listener);
	}

	protected void createPeer(int nodeId, String username, String password, Boolean isBootstrapNode)
			throws IOException, ClassNotFoundException, LineUnavailableException {
		Bindings b = new Bindings().listenAny();
		peer = new PeerBuilderDHT(
				new PeerBuilder(new Number160(nodeId)).ports(getPort()).bindings(b).start())
						.start();

		if (isBootstrapNode) {
			BootstrapRequest request = new BootstrapRequest(RequestType.STORE);
			RequestHandler.handleRequest(request, this);
		}
		peer.peer().objectDataReply(new ObjectDataReply() {
			public Object reply(PeerAddress peerAddress, Object object) throws Exception {
				return handleReceivedData(peerAddress, object);
			}
		});
		// TODO: Indirect Replication or direct? Replication factor?
		new IndirectReplication(peer).autoReplication(true).replicationFactor(3).start();
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

	private void connectToNode(String knownIP) throws ClassNotFoundException, IOException {
		BootstrapRequest request =
				new BootstrapRequest(RequestType.SEND, user.getPeerAddress(), knownIP);
		RequestHandler.handleRequest(request, this);
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
		if (peer != null) {
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
	   // send new OnlineRequest, act like this is an answer to a request, and send a positive answer so status goes online
	      request.setOnlineStatus(OnlineStatus.ONLINE);
	      request.setStatus(RequestStatus.ACCEPTED);
	      StatusListener<OnlineStatus> statusListener = new StatusListener<OnlineStatus>(){
	        @Override
	        public void operationComplete(FutureDirect future) throws Exception {
	          if(future != null && future.isSuccess()) {
	              f.setStatus(OnlineStatus.ONLINE);
	          }
	          else {
	          f.setStatus(OnlineStatus.OFFLINE);  
	          }
	      }
	  };
	      RequestHandler.handleRequest(request, this, null, statusListener);
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
      StatusListener<OnlineStatus> statusListener = new StatusListener<OnlineStatus>(){
        @Override
        public void operationComplete(FutureDirect future) throws Exception {
          if(lastInLine){
            peer.shutdown();
          }
      }
  };
      RequestHandler.handleRequest(request, this, null, statusListener);
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

	public void addFriend(Friend friend) {
		friendList.add(friend);
	}

}
