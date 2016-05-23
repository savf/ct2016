package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.Observable;
import java.util.Observer;

import net.tomp2p.connection.PeerConnection;
import net.tomp2p.connection.PeerException;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerStatusListener;
import net.tomp2p.peers.RTT;

public class Friend extends Observable implements Serializable{

  private static final long serialVersionUID = 7449783766050626661L;
  private PeerAddress peerAddress;
  private String name;
  private OnlineStatus onlineStatus;
  private FriendshipStatus friendshipStatus;
  
  public Friend(){
    setPeerAddress(null);
    setName("");
    setStatus(OnlineStatus.ONLINE);
    setFriendshipStatus(FriendshipStatus.WAITING);
  }
  
  public Friend(PeerAddress peerAddress, String name){
    setPeerAddress(peerAddress);
    setName(name);
    setStatus(OnlineStatus.ONLINE);
    //onlineStatus = OnlineStatus.OFFLINE;
    setFriendshipStatus(FriendshipStatus.WAITING);
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
    notifyObservers(onlineStatus);
  }

  public PeerAddress getPeerAddress() {
    return peerAddress;
  }

  public void setPeerAddress(PeerAddress peerAddress) {
    this.peerAddress = peerAddress;
  }

  public FriendshipStatus getFriendshipStatus() {
    return friendshipStatus;
  }

  public void setFriendshipStatus(FriendshipStatus friendshipStatus) {
    this.friendshipStatus = friendshipStatus;
  }

  public void setFriendshipStatus(String string) {
   setFriendshipStatus(FriendshipStatus.valueOf(string));
  }
  
}
