package ch.uzh.csg.p2p.model;

import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.List;

public class AudioMessage extends Message {
 
  private static final long serialVersionUID = -1954865919293641873L;
  private List<ByteBuffer> data;
  
  public AudioMessage(String sender, String receiver, Timestamp timestamp,List<ByteBuffer> message){
    super(sender, receiver, timestamp);
    setData(message);
  }  
  
  public void setData(List<ByteBuffer> message) {
    this.data = message;
  }

  public List<ByteBuffer> getData(){
    return data;
  }
  
}
