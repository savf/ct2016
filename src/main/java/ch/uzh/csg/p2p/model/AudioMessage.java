package ch.uzh.csg.p2p.model;

import java.util.Date;
import java.util.List;

import net.tomp2p.peers.PeerAddress;

public class AudioMessage extends Message {
	private static final long serialVersionUID = -1954865919293641873L;
	private List<byte[]> data;

	public AudioMessage(String sender, String receiver, PeerAddress receiverAddress,Date date, List<byte[]> message) {
		super(sender, receiver, receiverAddress, date);
		setData(message);
	}

	public void setData(List<byte[]> message) {
		this.data = message;
	}

	public List<byte[]> getData() {
		return data;
	}
  
  @Override
  public String toString(){
    return "[sender="+getSenderID()+", receiver="+getReceiverID()+", date="+getDate().toString()+", audioData="+getData().toString()+"]";
  }
  
}
