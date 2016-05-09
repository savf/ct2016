package ch.uzh.csg.p2p.model.request;

import net.tomp2p.peers.PeerAddress;

public class FriendRequest extends Request {

  private static final long serialVersionUID = -2514333310601556161L;
  private String senderName;
  private PeerAddress senderPeerAddress;
  private String receiverName;
  
  public FriendRequest(){
    super();
    //setFriend(new Friend());
    setReceiverName("");
  }
  
  public FriendRequest(PeerAddress senderPeerAddress, String sendername, String receiver, RequestType type){
    super(type);
    //setFriend(friend);
    setSenderPeerAddress(senderPeerAddress);
    setSenderName(sendername);
    setReceiverName(receiver);
  }
  
  public String getReceiverName() {
    return receiverName;
  }
  public void setReceiverName(String name) {
    this.receiverName = name;
  }

  public PeerAddress getSenderPeerAddress() {
    return senderPeerAddress;
  }

  public void setSenderPeerAddress(PeerAddress senderPeerAddress) {
    this.senderPeerAddress = senderPeerAddress;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setSenderName(String name) {
    this.senderName = name;
  }

  /*public Friend getFriend() {
    return friend;
  }

  public void setFriend(Friend friend) {
    this.friend = friend;
  }*/
}
