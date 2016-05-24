package ch.uzh.csg.p2p;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.sound.sampled.LineUnavailableException;

import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.FriendshipStatus;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.OnlineStatus;
import ch.uzh.csg.p2p.model.OnlineStatusTask;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.UserInfo;
import ch.uzh.csg.p2p.model.request.BootstrapRequest;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.OnlineStatusRequest;
import ch.uzh.csg.p2p.model.request.Request;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;

public class Node extends Observable {

  protected static final int MAX_TRIES = 0;
  protected final String BOOTSTRAPNODE = "Bootstrapnode";
  private final int DEFAULTPORT = 54000;
  private final long TIMERDELAY = 10000;
  private boolean running = true;

  private Logger log;
  private User user;
  private ObservableList<Friend> friendList = FXCollections.observableList(new ArrayList<Friend>());
  private Timer onlineStatusTaskTimer;
  protected PeerDHT peer;
  
  private ObservableList<FriendRequest> requestsWhileAway = FXCollections.observableList(new ArrayList<FriendRequest>());

  public Node(int nodeId, String ip, String username, String password, boolean bootstrapNode,
      Observer nodeReadyObserver) throws IOException, LineUnavailableException,
      ClassNotFoundException {
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

  public void loadStoredDataFromDHT() throws UnsupportedEncodingException, LineUnavailableException {
    // TODO load messages, calls, friendrequests...
    loadFriendlistFromDHTAndAnnounceOnlineStatus();
    loadMessagesFromDHT();
  }

  private void loadMessagesFromDHT() {
    // TODO Auto-generated method stub
  }

  private void initiateUser(final String username, final String password)
      throws ClassNotFoundException, LineUnavailableException, IOException {
    Node node = this;
    BaseFutureListener<FutureGet> userExistsListener = new BaseFutureListener<FutureGet>() {
      @Override
      public void operationComplete(FutureGet futureGet) throws Exception {
        if (futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
          // user already exist --> only update address
          LoginHelper.updatePeerAddress(node, username);
        } else {
          // user does not exist --> add user
          LoginHelper.saveUsernamePassword(node, username, password);
          UserInfo newUser = new UserInfo(node.getPeer().peerAddress(), username, password);
          node.setUserInfo(newUser);
        }
        nodeReady();
      }

      @Override
      public void exceptionCaught(Throwable t) throws Exception {}
    };

    LoginHelper.retrieveUserInfo(username, this, userExistsListener);
  }

  private void loadFriendlistFromDHTAndAnnounceOnlineStatus() throws UnsupportedEncodingException,
      LineUnavailableException {
    FriendRequest request = new FriendRequest();
    request.setType(RequestType.RETRIEVE);
    request.setSenderName(user.getUsername());

    BaseFutureListener<FutureGet> listener = new BaseFutureListener<FutureGet>() {

      @Override
      public void operationComplete(FutureGet future) throws Exception {
        if (future != null && future.isSuccess()) {
          Iterator<Data> i = future.dataMap().values().iterator();
          while (i.hasNext()) {
            Friend f = (Friend) i.next().object();
            if (f != null) {
              if (f.getFriendshipStatus().equals(FriendshipStatus.ACCEPTED)) {
                addFriend(f);
              } else if (f.getFriendshipStatus().equals(FriendshipStatus.REJECTED)
                  || f.getFriendshipStatus().equals(FriendshipStatus.ABORTED)) {
                getRejected(f);
              } else if (f.getFriendshipStatus().equals(FriendshipStatus.WAITING)) {
                addFriendRequestWhileAway(f);
              }
            }
          }
          announceChangedToOnlineStatus();
        }
      }

      @Override
      public void exceptionCaught(Throwable t) throws Exception {
        log.error(t.getMessage());
      }

    };
    RequestHandler.handleRequest(request, this, listener);
  }

  protected void addFriendRequestWhileAway(Friend f) {
    FriendRequest request = new FriendRequest();
    request.setReceiverName(user.getUsername());
    request.setReceiverAddress(peer.peerAddress());
    request.setSenderName(f.getName());
    request.setSenderPeerAddress(f.getPeerAddress());
    request.setStatus(RequestStatus.WAITING);
    request.setType(RequestType.RECEIVE);
    requestsWhileAway.add(request);
  }

  protected void getRejected(Friend f) {
    FriendlistHelper helper = new FriendlistHelper(this);
    helper.removeFriend(f, user.getUsername());
    FriendRequest req = new FriendRequest();
    req.setReceiverName(user.getUsername());
    req.setReceiverAddress(peer.peerAddress());
    req.setSenderName(f.getName());
    req.setSenderPeerAddress(f.getPeerAddress());
    req.setStatus(RequestStatus.REJECTED);
    req.setType(RequestType.RECEIVE);
    RequestHandler.handleRequest(req, this);
  }

  protected void startOnlineStatusTask() {
    OnlineStatusTask onlineStatusTask = new OnlineStatusTask(this);
    onlineStatusTaskTimer = new Timer();
    onlineStatusTaskTimer.scheduleAtFixedRate(onlineStatusTask, 0, TIMERDELAY);
  }

  protected void stopOnlineStatusTask() {
    if (onlineStatusTaskTimer != null) {
      onlineStatusTaskTimer.cancel();
    }
  }

  public void registerForFriendListUpdates(ListChangeListener<Friend> listener) {
    friendList.addListener(listener);
  }
  
  public void registerForFriendRequestWhileAwayUpdates(ListChangeListener<FriendRequest> listener){
    requestsWhileAway.addListener(listener);
  }

  protected void createPeerAndInitiateUser(int nodeId, String username, String password,
      boolean bootstrapNode, String ip) throws IOException, ClassNotFoundException,
      LineUnavailableException {
    Bindings b = new Bindings().listenAny();
    peer =
        new PeerBuilderDHT(new PeerBuilder(new Number160(nodeId)).ports(getPort()).bindings(b)
            .start()).start();

    if (bootstrapNode) {
      // Bootstrapnode
      BootstrapRequest request = new BootstrapRequest(RequestType.STORE);
      request.setBootstrapNodeIP(ip);
      BaseFutureListener<FuturePut> futurePutListener = new BaseFutureListener<FuturePut>() {
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
      RequestHandler.handleRequest(request, this, futurePutListener);
    } else {
      peer.peer().objectDataReply(new ObjectDataReply() {
        public Object reply(PeerAddress peerAddress, Object object) throws Exception {
          return handleReceivedData(peerAddress, object);
        }
      });
      new IndirectReplication(peer).autoReplication(true).replicationFactor(3).start();

      connectToNode(ip, username, password);
    }
  }

  protected Object handleReceivedData(PeerAddress peerAddress, Object object) throws IOException,
      LineUnavailableException, ClassNotFoundException {

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
     * RequestHandler.handleRequest(messageRequest, this); } else if (object instanceof ChatMessage)
     * { messageRequest.setType(RequestType.RECEIVE); messageRequest.setMessage((ChatMessage)
     * object); RequestHandler.handleRequest(messageRequest, this);}
     */
    /*
     * else if (object instanceof AudioRequest) { AudioRequest audioRequest = (AudioRequest) object;
     * audioRequest.setType(RequestType.RECEIVE); RequestHandler.handleRequest(audioRequest, this);
     * }
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

  private void connectToNode(String knownIP, String username, String password)
      throws ClassNotFoundException, IOException, LineUnavailableException {
    BootstrapRequest request =
        new BootstrapRequest(RequestType.SEND, user.getPeerAddress(), knownIP);
    BaseFutureListener<FutureBootstrap> futureBootstrapListener =
        new BaseFutureListener<FutureBootstrap>() {

          @Override
          public void operationComplete(FutureBootstrap future) throws Exception {

            log.info(getUser().getUsername() + " knows: " + getPeer().peerBean().peerMap().all()
                + " unverified: " + getPeer().peerBean().peerMap().allOverflow());
            log.info("Waiting for maintenance ping");
            if(future.isSuccess()){
            initiateUser(username, password);
            }
            else{
              tryAgain(request, this);
            }
          }

          @Override
          public void exceptionCaught(Throwable t) throws Exception {
            log.error(t.getMessage());
          }

        };
    RequestHandler.handleRequest(request, this, futureBootstrapListener);
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

  public void setUserInfo(UserInfo info) {
    if (user != null && info != null) {
      user.setUsername(info.getUserName());
      user.setPassword(info.getPassword());
      user.setPeerAddress(info.getPeerAddress());
    } else if (info != null) {
      User newUser = new User(info.getUserName(), info.getPassword(), info.getPeerAddress());
      this.user = newUser;
    }
  }

  public List<Friend> getFriendList() {
    return friendList;
  }
  
  public List<FriendRequest> getRequestsWhileAway(){
    return requestsWhileAway;
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

  public void announceChangedToOnlineStatus() throws LineUnavailableException {
    for (Friend f : friendList) {
      OnlineStatusRequest request =
          new OnlineStatusRequest(f.getPeerAddress(), peer.peerAddress(), user.getUsername(),
              f.getName(), RequestType.SEND);
      request.setChangedPeerAddress(true);
      request.setOnlineStatus(OnlineStatus.ONLINE);
      // send new OnlineRequest, act like this is an answer to a request, and send a positive answer
      // so status goes online
      request.setStatus(RequestStatus.ACCEPTED);
      BaseFutureListener<FutureDirect> futureDirectListener =
          new BaseFutureListener<FutureDirect>() {
            @Override
            public void operationComplete(FutureDirect future) throws Exception {
              if (future != null && future.isSuccess()) {
                f.setStatus(OnlineStatus.ONLINE);
              } else {
                f.setStatus(OnlineStatus.OFFLINE);
              }
            }

            @Override
            public void exceptionCaught(Throwable t) throws Exception {
              log.error(t.getMessage());
            }
          };
      RequestHandler.handleRequest(request, this, futureDirectListener);
    }
  }

  private void announceChangedToOfflineStatus() throws LineUnavailableException {
    if (friendList.isEmpty()) {
      peer.shutdown();
    } else {
      int i = 1;
      for (Friend f : friendList) {
        OnlineStatusRequest request =
            new OnlineStatusRequest(f.getPeerAddress(), peer.peerAddress(), user.getUsername(),
                f.getName(), RequestType.SEND);
        // act like we received an OnlineStatusRequest and answer with Aborted so Status goes to
        // offline
        request.setOnlineStatus(OnlineStatus.OFFLINE);
        request.setStatus(RequestStatus.ABORTED);
        boolean lastInLine = (i == friendList.size());
        BaseFutureListener<FutureDirect> futureDirectListener =
            new BaseFutureListener<FutureDirect>() {
              @Override
              public void operationComplete(FutureDirect future) throws Exception {
                if(future.isSuccess()){
                if (lastInLine) {
                  peer.shutdown();
                }
                }
                else{
                  tryAgain(request, this);
                }
              }

              @Override
              public void exceptionCaught(Throwable t) throws Exception {
                log.error(t.getMessage());
              }
            };
        RequestHandler.handleRequest(request, this, futureDirectListener);
        i++;
      }
    }
  }

  protected void tryAgain(Request request,
      BaseFutureListener baseFutureListener) throws LineUnavailableException, InterruptedException {
    Thread.sleep(500);
    RequestHandler.tryAgain(request, this, baseFutureListener); 
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
    boolean containsFriend = false;
    for (Friend f : friendList) {
      if (f.getName().equals(friend.getName())) {
        f.setFriendshipStatus(FriendshipStatus.ACCEPTED);
        friend = f;
        containsFriend = true;
        break;
      }
    }
    if (!containsFriend) {
      friendList.add(friend);
    }

/*    OnlineStatusRequest req =
        new OnlineStatusRequest(friend.getPeerAddress(), peer.peerAddress(), user.getUsername(),
            friend.getName(), RequestType.SEND);
    req.setOnlineStatus(OnlineStatus.ONLINE);
    req.setStatus(RequestStatus.ACCEPTED);
    BaseFutureListener<FutureDirect> futureDirectListener = new BaseFutureListener<FutureDirect>() {
      @Override
      public void operationComplete(FutureDirect future) throws Exception {
        if (future.isCompleted() && future.isSuccess()) {
          friend.setStatus(OnlineStatus.ONLINE);
        } else {
          friend.setStatus(OnlineStatus.OFFLINE);
        }
      }

      @Override
      public void exceptionCaught(Throwable t) throws Exception {
        log.error(t.getMessage());
      }
    };
    try {
      RequestHandler.handleRequest(req, this, futureDirectListener);
    } catch (LineUnavailableException e) {
      e.printStackTrace();
    }*/
  }

  public void removeFriend(Friend f) {
    for (Friend friend : friendList) {
      if (friend.getName().equals(f.getName())) {
        friendList.remove(friend);
        break;
      }
    }
  }

}
