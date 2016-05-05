package ch.uzh.csg.p2p.model;

import java.sql.Timestamp;
import java.util.Date;

import net.tomp2p.peers.PeerAddress;

public class ChatMessage extends Message {

  private static final long serialVersionUID = 668487259223050593L;
  private String data;

  public ChatMessage(String sender, String receiver, PeerAddress receiverAddress, Date date,String message){
    super(sender, receiver, receiverAddress, date);
    setData(message);
  }

  public void setData(String message) {
    this.data = message;
  }

  @Override
  public String getData() {
    return data;
  }
  
  @Override
  public String toString(){
    return "[sender="+getSenderID()+", receiver="+getReceiverID()+", date="+getDate().toString()+", chatData="+getData().toString()+"]";
  }
  
}
