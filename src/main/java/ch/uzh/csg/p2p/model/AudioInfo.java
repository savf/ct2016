package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.Date;

public class AudioInfo implements Serializable {

	private static final long serialVersionUID = 7580302456484382160L;
	private String sendername;
	private String receivername;
	private Date receivedOn;

	public AudioInfo() {
		sendername = "";
		receivername = "";
		receivedOn = new Date();
	}

	public AudioInfo(String sendername, String receivername) {
		setSendername(sendername);
		setReceivername(receivername);
		receivedOn = new Date();
	}

	public void setSendername(String sendername) {
		this.sendername = sendername;
	}

	public String getSendername() {
		return sendername;
	}

	public void setReceivername(String receivername) {
		this.receivername = receivername;
	}

	public String getReceivername() {
		return receivername;
	}

	public void setReceivedOn(Date receivedOn) {
		this.receivedOn = receivedOn;
	}

	public Date getReceivedOn() {
		return receivedOn;
	}
}
