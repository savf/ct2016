package ch.uzh.csg.p2p.model.request;

public class AudioRequest extends Request {

	private static final long serialVersionUID = 1L;
	String receiverName;
	String senderName;

	public AudioRequest() {
		super();
		receiverName = "";
		senderName = "";
	}

	public AudioRequest(RequestType type, String receiverName, String senderName) {
		super(type);
		this.receiverName = receiverName;
		this.senderName = senderName;
	}
	
	public AudioRequest(RequestType type, RequestStatus status, String receiverName, String senderName) {
      super(type, status);
      this.receiverName = receiverName;
      this.senderName = senderName;
  }

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderName() {
		return senderName;
	}
}
