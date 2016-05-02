package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public abstract class Message implements Serializable{

  private static final long serialVersionUID = 1L;
  private String senderID;
  private String receiverID;
  private Date date;
  
  public Message(){}
  
  public Message(String sender, String receiver, Date date){
    setSenderID(sender);
    setReceiverID(receiver);
    setDate(date);
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

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
  
  public abstract Object getData();
  
  public abstract String toString();
 
}
