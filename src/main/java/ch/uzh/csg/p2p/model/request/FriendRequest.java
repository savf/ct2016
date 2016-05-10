package ch.uzh.csg.p2p.model.request;

import net.tomp2p.peers.PeerAddress;

public class FriendRequest extends Request {

  private static final long serialVersionUID = -2514333310601556161L;
  private PeerAddress senderPeerAddress;
  
  public FriendRequest(){
    super();
    //setFriend(new Friend());
    setReceiverName("");
  }
  
  public FriendRequest(PeerAddress senderPeerAddress, String sendername, String receiver, RequestType type){
    super(type);
    //setFriend(friend);
    setSenderName(sendername);
    setReceiverName(receiver);
    setSenderPeerAddress(senderPeerAddress);
  }

  public PeerAddress getSenderPeerAddress() {
    return senderPeerAddress;
  }

  public void setSenderPeerAddress(PeerAddress senderPeerAddress) {
    this.senderPeerAddress = senderPeerAddress;
  }

  /*public Friend getFriend() {
    return friend;
  }

  public void setFriend(Friend friend) {
    this.friend = friend;
  }*/
}
