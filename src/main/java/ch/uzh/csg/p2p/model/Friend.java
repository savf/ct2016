package ch.uzh.csg.p2p.model;

import java.util.Observable;
import java.util.Observer;

import net.tomp2p.connection.PeerConnection;
import net.tomp2p.connection.PeerException;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerStatusListener;
import net.tomp2p.peers.RTT;

public class Friend extends Observable {

  private PeerAddress peerAddress;
  private String name;
  private OnlineStatus onlineStatus;
 // private FriendshipStatus friendshipStatus;
  
  public Friend(){
    setPeerAddress(null);
    setName("");
    setStatus(OnlineStatus.OFFLINE);
   // friendshipStatus = FriendshipStatus.WAITING;
  }
  
  public Friend(PeerAddress peerAddress, String name){
    setPeerAddress(peerAddress);
    setName(name);
    setStatus(OnlineStatus.OFFLINE);
    //onlineStatus = OnlineStatus.OFFLINE;
  //  friendshipStatus = FriendshipStatus.WAITING;
  }
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public OnlineStatus getStatus() {
    return onlineStatus;
  }
  public void setStatus(OnlineStatus status) {
    this.onlineStatus = status;
    setChanged();
    notifyObservers(status);
  }

  public PeerAddress getPeerAddress() {
    return peerAddress;
  }

  public void setPeerAddress(PeerAddress peerAddress) {
    this.peerAddress = peerAddress;
  }
  
}
