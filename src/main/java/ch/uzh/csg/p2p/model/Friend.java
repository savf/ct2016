package ch.uzh.csg.p2p.model;

public class Friend {

  private String peerID;
  private String name;
  private OnlineStatus onlineStatus;
  private FriendshipStatus friendshipStatus;
  
  public String getPeerID() {
    return peerID;
  }
  public void setPeerID(String peerID) {
    this.peerID = peerID;
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
  public FriendshipStatus getFriendStatus() {
    return friendshipStatus;
  }
  
  public void hasAccepted(){
    friendshipStatus = FriendshipStatus.ACCEPTED;
  }
}
