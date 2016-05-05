package ch.uzh.csg.p2p.model;

import java.sql.Timestamp;
import java.util.List;

public class VideoMessage extends Message {

	private static final long serialVersionUID = 5905024972095013097L;
	private List<byte[]> data;

	public VideoMessage(String sender, String receiver, Timestamp timestamp, List<byte[]> data) {
		super(sender, receiver, timestamp);
		setData(data);
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
		 return "[sender="+getSenderID()+", receiver="+getReceiverID()+", date="+getDate().toString()+", videoData="+getData().toString()+"]";
	}

}
