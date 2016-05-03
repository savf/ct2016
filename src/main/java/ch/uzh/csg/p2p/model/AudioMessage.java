package ch.uzh.csg.p2p.model;

import java.util.Date;
import java.util.List;

public class AudioMessage extends Message {
	private static final long serialVersionUID = -1954865919293641873L;
	private List<byte[]> data;

	public AudioMessage(String sender, String receiver, Date date, List<byte[]> message) {
		super(sender, receiver, date);
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
