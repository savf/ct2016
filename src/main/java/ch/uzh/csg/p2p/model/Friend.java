package ch.uzh.csg.p2p.model;

import net.tomp2p.peers.PeerAddress;

public class Friend {

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
  }
  /*public FriendshipStatus getFriendStatus() {
    return friendshipStatus;
  }
  
  public void forRequest(){
    friendshipStatus = FriendshipStatus.RECEIVED;
  }
  
  public void hasRejected(){
    friendshipStatus = FriendshipStatus.REJECTED;
  }
  
  public void hasAccepted(){
    friendshipStatus = FriendshipStatus.ACCEPTED;
  }*/

  public PeerAddress getPeerAddress() {
    return peerAddress;
  }

  public void setPeerAddress(PeerAddress peerAddress) {
    this.peerAddress = peerAddress;
  }
}
