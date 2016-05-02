package ch.uzh.csg.p2p.model.request;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PutBuilder;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.AutomaticFuture;
import net.tomp2p.p2p.JobScheduler;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;
import net.tomp2p.p2p.Shutdown;


public class RequestHandler {
  
  private static Logger log = LoggerFactory.getLogger("RequestHandler");
  private final static int DEFAULTPORT = 54000;
  private final static String BOOTSTRAPNODE = "Bootstrapnode";
  
  public RequestHandler(){
  }
  
  public static Object handleRequest(Request request, Node node){
    switch(request.getType()){
      case RECEIVE:
        return handleReceiveMessage(request, node);
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
          return handleSendMessage(request, node);
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
        }
      default:
        return null;
    }
  }

  private static Boolean handleStore(Request request, Node node) throws IOException {
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

  private static Boolean handleSendMessage(Request request, Node node) throws ClassNotFoundException, IOException {
    final Node n = node;
      if(request instanceof MessageRequest){
        final MessageRequest r = (MessageRequest) request;
        UserRequest req = new UserRequest();
        User u = new User(r.getMessage().getReceiverID(), null, null);
        req.setUser(u);
        User user = retrieveUser(req, node);
        if(user!= null){
          PeerAddress peerAddress = user.getPeerAddress();
          log.info(r.getMessage().getSenderID() + "sent Message of type " +r.getMessage().getClass()+ " " +r.getType().toString() + "to: " + peerAddress.toString());
          n.getPeer().peer().sendDirect(peerAddress).object(r.getMessage()).start();
        }
        else{
          log.error("FutureGet for user was successful, but data is null!");
        }
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
      //d.h. Operation ausgeführt, sonst nichts
      return true;
  }

  private static Object handleRetrieve(Request request, Node node) throws ClassNotFoundException, IOException {
    if(request instanceof UserRequest){
      UserRequest r = (UserRequest) request;
      return retrieveUser(r, node);
    }
    if(request instanceof MessageRequest){
      MessageRequest r = (MessageRequest) request;
    }
    if(request instanceof FriendRequest){
      FriendRequest r = (FriendRequest) request;
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

  private static Message handleReceiveMessage(Request request, Node node) {
    if(request instanceof MessageRequest){
      MessageRequest r = (MessageRequest) request;
      Message msg = r.getMessage();
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
    
    return null;
  }
  
  private static User retrieveUser(UserRequest r, Node node) throws ClassNotFoundException, IOException{
    FutureGet futureGet =
        node.getPeer().get(Number160.createHash(LoginHelper.USER_PREFIX + r.getUser().getUsername())).start();
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
  
}
