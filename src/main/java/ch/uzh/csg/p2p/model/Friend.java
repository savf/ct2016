package ch.uzh.csg.p2p.model;

public class Friend {

  private String peerID;
  private String name;
  private ONLINE_STATUS onlineStatus;
  private FRIENDSHIP_STATUS friendshipStatus;
  
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
  public ONLINE_STATUS getStatus() {
    return onlineStatus;
  }
  public void setStatus(ONLINE_STATUS status) {
    this.onlineStatus = status;
  }
  public FRIENDSHIP_STATUS getFriendStatus() {
    return friendshipStatus;
  }
  
  public void hasAccepted(){
    friendshipStatus = FRIENDSHIP_STATUS.ACCEPTED;
  }
}
