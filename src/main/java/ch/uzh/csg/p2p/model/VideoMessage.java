package ch.uzh.csg.p2p.model;

import java.util.Date;
import java.util.List;

import net.tomp2p.peers.PeerAddress;

public class VideoMessage extends Message {

	private static final long serialVersionUID = 1L;
	private List<byte[]> data;

	public VideoMessage(String sender, String receiver, Date date, List<byte[]> data) {
		super(sender, receiver, date);
		setData(data);
	}
	
	public VideoMessage(String sender, String receiver, PeerAddress receiverAddress, Date date, List<byte[]> message) {
      super(sender, receiver, receiverAddress, date);
      setData(message);
  }

	public void setData(List<byte[]> data) {
		this.data = data;
	}

	@Override
	public List<byte[]> getData() {
		return data;
	}

	@Override
	public String toString() {
		return "[sender=" + getSenderID() + ", receiver=" + getReceiverID() + ", date="
				+ getDate().toString() + ", videoData=" + getData().toString() + "]";
	}

}
