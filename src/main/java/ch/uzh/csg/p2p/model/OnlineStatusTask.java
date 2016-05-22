package ch.uzh.csg.p2p.model;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.request.OnlineStatusRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestListener;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.StatusListener;

public class OnlineStatusTask extends TimerTask {
  private Node node;
  
  private OnlineStatusTask(){
  }
  
  public OnlineStatusTask(Node node){
    setNode(node);
  }

  private void setNode(Node node) {
   this.node = node;
  }

  @Override
  public void run() {
   try{
     OnlineStatusRequest r = new OnlineStatusRequest();
     r.setOnlineStatus(OnlineStatus.ONLINE);
     r.setSenderName(node.getUser().getUsername());
     r.setSenderAddress(node.getPeer().peerAddress());
     List<Friend> list = node.getFriendList();
     synchronized(list){
       for(Friend f : list){
         // only send OnlineStatusRequest when Friend was last online, since when they go offline & online again they need to announce it
         if(f.getStatus().equals(OnlineStatus.ONLINE)){
           r.setReceiverAddress(f.getPeerAddress());
           r.setReceiverName(f.getName());
           r.setType(RequestType.SEND);
           r.setStatus(RequestStatus.WAITING);
           StatusListener<OnlineStatus> statusListener = new StatusListener<OnlineStatus>(){
             @Override
             public void operationComplete(FutureDirect future) throws Exception {
               if(future != null && future.isSuccess()) {
                 // TODO: maybe just leave status?
                 //  f.setStatus(OnlineStatus.ONLINE);
               }
               else {
               f.setStatus(OnlineStatus.OFFLINE);  
               }
           }
       };
        RequestHandler.handleRequest(r, node, null, statusListener);
         }
       }
     }
   }
   catch(Exception ex){
     System.out.println("Error running thread " + ex.getMessage());
   }
  }

}
