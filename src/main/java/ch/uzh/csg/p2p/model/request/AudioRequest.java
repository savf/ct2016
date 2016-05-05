package ch.uzh.csg.p2p.model.request;

public class AudioRequest extends Request {

	String receiverName;
	String senderName;
	RequestStatus status;

	public AudioRequest() {
		super();
		receiverName = "";
		senderName = "";
	}

	public AudioRequest(RequestType type, RequestStatus status, String receiverName, String senderName) {
		super(type);
		this.receiverName = receiverName;
		this.senderName = senderName;
		this.status = status;
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
	
	public void setStatus(RequestStatus status){
		this.status = status;
	}
	
	public RequestStatus getStatus(){
		return status;
	}
}
