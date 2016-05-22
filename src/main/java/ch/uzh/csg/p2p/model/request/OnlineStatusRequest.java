package ch.uzh.csg.p2p.model.request;

import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.model.OnlineStatus;

public class OnlineStatusRequest extends Request {
  private OnlineStatus onlineStatus;
  private PeerAddress senderAddress;
  private boolean changedPeerAddress = false;
  
  public OnlineStatusRequest(){
    super();
    setOnlineStatus(OnlineStatus.ONLINE);
  }
  
  public OnlineStatusRequest(PeerAddress receiverAddress, String senderName, String receiverName, RequestType type){
    super(type, receiverAddress, receiverName, senderName);
  }
  
  public OnlineStatusRequest(PeerAddress receiverAddress, PeerAddress senderAddress, String senderName, String receiverName, RequestType type){
    super(type, receiverAddress, receiverName, senderName);
    setSenderAddress(senderAddress);
  }

  public OnlineStatus getOnlineStatus() {
    return onlineStatus;
  }

  public void setOnlineStatus(OnlineStatus status) {
    onlineStatus = status;
  }

  public PeerAddress getSenderAddress() {
    return senderAddress;
  }

  public void setSenderAddress(PeerAddress senderAddress) {
    this.senderAddress = senderAddress;
  }

  public boolean hasChangedPeerAddress() {
    return changedPeerAddress;
  }

  public void setChangedPeerAddress(boolean changedPeerAddress) {
    this.changedPeerAddress = changedPeerAddress;
  }
}
