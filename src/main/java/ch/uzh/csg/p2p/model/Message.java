package ch.uzh.csg.p2p.model;
import java.util.Date;
import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

public abstract class Message implements Serializable{

  private static final long serialVersionUID = 1L;
  private String senderID;
  private String receiverID;
  private PeerAddress receiverAddress;
  private Date date;
  
  public Message(){}
  
  public Message(String sender, String receiver, Date date){
    setSenderID(sender);
    setReceiverID(receiver);
    setDate(date);
  }
  
  public Message(String sender, String receiver, PeerAddress address, Date date){
    setSenderID(sender);
    setReceiverID(receiver);
    setDate(date);
    setReceiverAddress(address);
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

  public PeerAddress getReceiverAddress() {
    return receiverAddress;
  }

  public void setReceiverAddress(PeerAddress receiverAddress) {
    this.receiverAddress = receiverAddress;
  }
 
}
