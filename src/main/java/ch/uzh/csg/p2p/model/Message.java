package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.sql.Timestamp;

public abstract class Message implements Serializable{

  private static final long serialVersionUID = 1L;
  private String senderID;
  private String receiverID;
  private Timestamp timestamp;
  
  public Message(){}
  
  public Message(String sender, String receiver, Timestamp timestamp){
    setSenderID(sender);
    setReceiverID(receiver);
    setTimestamp(timestamp);
  }

  public String getSenderID() {
    return senderID;
  }

  public void setSenderID(String senderID) {
    this.senderID = senderID;
  }

  public String getReceiverID() {
    return receiverID;
  }

  public void setReceiverID(String receiverID) {
    this.receiverID = receiverID;
  }

  public Timestamp getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Timestamp timestamp) {
    this.timestamp = timestamp;
  }
  
  public abstract Object getData();
 
}
