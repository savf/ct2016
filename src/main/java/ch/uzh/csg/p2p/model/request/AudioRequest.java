package ch.uzh.csg.p2p.model.request;

public class AudioRequest extends Request {

	String receiverName;
	String senderName;

	public AudioRequest() {
		super();
		receiverName = "";
		senderName = "";
	}

	public AudioRequest(REQUEST_TYPE type, String receiverName, String senderName) {
		super(type);
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
