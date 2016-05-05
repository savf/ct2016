package ch.uzh.csg.p2p.model.request;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.controller.MainWindowController;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;


public class RequestHandler {
  
  private static Logger log = LoggerFactory.getLogger("RequestHandler");
  private static MainWindowController mainWindowController;
  private final static int DEFAULTPORT = 54000;
  private final static String BOOTSTRAPNODE = "Bootstrapnode";
  private static final String FRIEND_PREFIX = "Friend";
  
  public RequestHandler(){
  }
  
  public static Object handleRequest(Request request, Node node){
    switch(request.getType()){
      case RECEIVE:
        try {
          return handleReceive(request, node);
        } catch (ClassNotFoundException e2) {
          e2.printStackTrace();
        } catch (IOException e2) {
          e2.printStackTrace();
        } catch (LineUnavailableException e2) {
          e2.printStackTrace();
        }
      case RETRIEVE:
        try {
          return handleRetrieve(request, node);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      case SEND:
        try {
          return handleSend(request, node);
        } catch (ClassNotFoundException e1) {
          e1.printStackTrace();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      case STORE:
        try {
          return handleStore(request, node);
        } catch (IOException e) {
          e.printStackTrace();
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      default:
        return null;
    }
  }

  private static Object handleAbortRequest(Request request, Node node) throws ClassNotFoundException, IOException {
    if(request instanceof AudioRequest){
      // TODO: handle Audio Request
      AudioRequest r = (AudioRequest) request;
      final Node n = node;
      User user = new User(r.getReceiverName(), null, null);
      n.getPeer().peer().sendDirect(retrieveUser(user, node).getPeerAddress())
      .object(r).start();
      return true;
    }
    return null;
  }

  private static Object handleRejectRequest(Request request, Node node) throws ClassNotFoundException, IOException {
    if(request instanceof AudioRequest){
      AudioRequest r = (AudioRequest) request;
      final Node n = node;
      User user = new User(r.getReceiverName(), null, null);
      n.getPeer().peer().sendDirect(retrieveUser(user, node).getPeerAddress())
      .object(r).start();
      return true;
    }
    return null;
  }

  private static Object handleAcceptRequest(Request request, Node node) throws ClassNotFoundException, IOException {
    if(request instanceof AudioRequest){
      AudioRequest r = (AudioRequest) request;
      final Node n = node;
      User user = new User(r.getReceiverName(), null, null);
      n.getPeer().peer().sendDirect(retrieveUser(user, node).getPeerAddress())
      .object(r).start();
      return true;
    }
    return null;
  }

  private static Boolean handleStore(Request request, Node node) throws IOException, ClassNotFoundException {
    if(request instanceof UserRequest){
      UserRequest r = (UserRequest) request;
      User user = r.getUser();
      
      // TODO: Direct replication if wanted.. should look somewhat like this
  /*    PutBuilder putBuilder = node.getPeer().put(Number160.createHash(LoginHelper.USER_PREFIX + r.getUser().getUsername())).data(new Data(user));
      JobScheduler replication = new JobScheduler(node.getPeer().peer());
      Shutdown shutdown = replication.start(putBuilder, 1000, 3, new AutomaticFuture() {
          public void futureCreated(BaseFuture future) {
              System.out.println("put again...");
          }
      });

      shutdown.shutdown();

      replication.shutdown().awaitUninterruptibly(); */     
      
      node.getPeer().put(Number160.createHash(LoginHelper.USER_PREFIX + r.getUser().getUsername())).data(new Data(user))
              .start();
      return true;
    }
    if(request instanceof MessageRequest){
      MessageRequest r = (MessageRequest) request;
    }
    if(request instanceof FriendRequest){
      FriendRequest r = (FriendRequest) request;
      Friend friend = new Friend(r.getSenderPeerAddress(), r.getSenderName());
      Number160 hash = Number160.createHash(FRIEND_PREFIX + friend.getName());
      User user = node.getUser();
      user.addFriend(hash);
      node.getPeer().put(Number160.createHash(LoginHelper.USER_PREFIX + user.getUsername())).data(new Data(user))
      .start();
    }
    if(request instanceof BootstrapRequest){ 
      User user = new User(null, null, node.getPeer().peerAddress());
      node.getPeer().put(Number160.createHash(BOOTSTRAPNODE)).data(new Data(user))
      .start();
      log.info("BootstrapNode created from user: " + node.getUser().getUsername());
      return true;
    }
    return false;
  }

  private static Boolean handleSend(Request request, Node node) throws ClassNotFoundException, IOException {
    final Node n = node;
      if(request instanceof MessageRequest){
        final MessageRequest r = (MessageRequest) request;
        PeerAddress peerAddress = null;
        if(r.getMessage() != null && r.getMessage().getReceiverAddress() != null){
          peerAddress = r.getMessage().getReceiverAddress();
        }
        else{
        User u = new User(r.getMessage().getReceiverID(), null, null);
        User user = retrieveUser(u, node);
        if(user!= null){
          peerAddress = user.getPeerAddress();
          log.info(r.getMessage().getSenderID() + "sent Message of type " +r.getMessage().getClass()+ " " +r.getType().toString() + "to: " + peerAddress.toString());
          //n.getPeer().peer().sendDirect(peerAddress).object(r.getMessage()).start();
        }
        else{
          log.error("FutureGet for user was successful, but data is null!");
        }
        }
        n.getPeer().peer().sendDirect(peerAddress).object(r.getMessage()).start();
      }
      
      if(request instanceof BootstrapRequest){
        BootstrapRequest r = (BootstrapRequest) request;        
        
        InetAddress address = Inet4Address.getByName(r.getBootstrapNodeIP());       
        FutureDiscover futureDiscover =
                n.getPeer().peer().discover().inetAddress(address).ports(DEFAULTPORT).start();
        futureDiscover.awaitUninterruptibly();
        
        FutureBootstrap futureBootstrap =
            n.getPeer().peer().bootstrap().inetAddress(address).ports(DEFAULTPORT).start();
        futureBootstrap.awaitUninterruptibly();
        
        BootstrapRequest r2 = new BootstrapRequest(RequestType.RETRIEVE);
        User bootstrap = (User)handleRetrieve(r2, n);
        if(bootstrap != null && bootstrap.getPeerAddress()!=null){
          log.info(r.getSenderPeerAddress() + "sent Bootstrap Request of type " +r.getType()+ "to: " + bootstrap.getPeerAddress().toString());
          n.getPeer().peer().sendDirect(bootstrap.getPeerAddress()).object(r).start();
        }
        else{
          log.error("FutureGet was successful, but data is null!");
        }
        
      }      
      if(request instanceof AudioRequest){
        AudioRequest r = (AudioRequest) request;
        User user = new User(r.getReceiverName(), null, null);
        n.getPeer().peer().sendDirect(retrieveUser(user, node).getPeerAddress())
        .object(r).start();
        return true;
      }
      
      if(request instanceof FriendRequest){
        FriendRequest r = (FriendRequest) request;
        User user = new User(r.getReceiverName(), null, null);
        n.getPeer().peer().sendDirect(retrieveUser(user, node).getPeerAddress())
        .object(r).start();
        return true;
      }
      
      //d.h. Operation ausgeführt, sonst nichts
      return true;
  }

  private static Object handleRetrieve(Request request, Node node) throws ClassNotFoundException, IOException {
    if(request instanceof UserRequest){
      UserRequest r = (UserRequest) request;
      return retrieveUser(r.getUser(), node);
    }
    if(request instanceof MessageRequest){
      MessageRequest r = (MessageRequest) request;
    }
    if(request instanceof FriendRequest){
      FriendRequest r = (FriendRequest) request;
      return retrieveFriend(r.getSenderName(), node);
    }
    if(request instanceof BootstrapRequest){
      FutureGet futureGet = node.getPeer().get(Number160.createHash(BOOTSTRAPNODE)).start();
      futureGet.awaitUninterruptibly();
      User user = null;
      if(futureGet!= null && futureGet.data() != null){
        user = (User) futureGet.data().object();
      }
      else{
        log.error("FutureGet was unsuccessful.");
      }
      return user;
    }
    return null;
  }

  private static Friend retrieveFriend(String senderName, Node node) throws ClassNotFoundException, IOException {
    FutureGet futureGet =
        node.getPeer().get(Number160.createHash(FRIEND_PREFIX + senderName)).start();
        futureGet.awaitUninterruptibly();
        Friend result = null;
        if(futureGet!= null && futureGet.data() != null){
          result = (Friend) futureGet.data().object();
        }
        else{
          log.error("FutureGet was unsuccessful.");
        }
          return result;  
  }

  private static Message handleReceive(Request request, Node node) throws IOException, LineUnavailableException, ClassNotFoundException {
    if(request instanceof MessageRequest){
      MessageRequest r = (MessageRequest) request;
      Message msg = r.getMessage();
      mainWindowController.handleReceiveMessage(msg);
      log.info(msg.getReceiverID() + " received message: " + msg.toString() + " from: " + msg.getSenderID().toString());
      return msg;
    }   
    
    if(request instanceof BootstrapRequest){
      BootstrapRequest r = (BootstrapRequest) request;
      log.info("BootstrapNode received Bootstrap Request from: "
          + r.getSenderPeerAddress().toString());
      log.info("new node");
      FutureBootstrap futureBootstrap =
              node.getPeer().peer().bootstrap().peerAddress(r.getSenderPeerAddress()).start();
    }
    
    if(request instanceof AudioRequest){
      AudioRequest audioRequest = (AudioRequest) request;
      switch(audioRequest.getStatus()){
        case WAITING:
          mainWindowController.askAudioCall(audioRequest.getSenderName());
          break;
        case ACCEPTED:
          mainWindowController.startAudioCall();
          break;
        case REJECTED:
          mainWindowController.audioCallRejected();
          break;
        case ABORTED:
          mainWindowController.audioCallAborted();
          break;
          default:
            break;
      }
    }
    
    if(request instanceof FriendRequest){
      FriendRequest friendRequest = (FriendRequest) request;
      switch(friendRequest.getStatus()){
        case WAITING:
          mainWindowController.askFriend(friendRequest);
          break;
        case ACCEPTED:
          Friend f = new Friend(friendRequest.getSenderPeerAddress(), friendRequest.getSenderName());
          mainWindowController.friendshipAccepted(f);;
          break;
        case REJECTED:
          mainWindowController.friendshipRejected();;
          break;
        case ABORTED:
          mainWindowController.friendshipRejected();;
          break;
          default:
            break;
      }
    }
    
    return null;
  }
  
  private static User retrieveUser(User user, Node node) throws ClassNotFoundException, IOException{
    FutureGet futureGet =
        node.getPeer().get(Number160.createHash(LoginHelper.USER_PREFIX + user.getUsername())).start();
        futureGet.awaitUninterruptibly();
        User result = null;
        if(futureGet!= null && futureGet.data() != null){
          if(futureGet.data().object() instanceof User){
          result = (User) futureGet.data().object();
          }
        }
        else{
          log.error("FutureGet was unsuccessful.");
        }
          return result;        
  }

  public static void setMainWindowController(MainWindowController mWC) {
    mainWindowController = mWC;
  }
  
}
