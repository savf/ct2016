package ch.uzh.csg.p2p.model.request;

import net.tomp2p.peers.PeerAddress;

public class AudioRequest extends Request {

	String receiverName;
	PeerAddress receiverAddress;
  String senderName;

	public AudioRequest() {
		super();
		receiverName = "";
		senderName = "";
		receiverAddress = null;
	}

	public AudioRequest(RequestType type, String receiverName, PeerAddress address, String senderName) {
		super(type);
		this.receiverName = receiverName;
		this.senderName = senderName;
		setReceiverAddress(address);
	}

	public AudioRequest(RequestType type, RequestStatus status, String receiverName, PeerAddress address, String senderName) {
      super(type, status);
      this.receiverName = receiverName;
      this.senderName = senderName;
      setReceiverAddress(address);
  }
	
	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getReceiverName() {
		return receiverName;
	}
	
	public PeerAddress getReceiverAddress() {
	    return receiverAddress;
	  }

	  public void setReceiverAddress(PeerAddress receiverAddress) {
	    this.receiverAddress = receiverAddress;
	  }
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderName() {
		return senderName;
	}
}
