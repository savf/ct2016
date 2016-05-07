package ch.uzh.csg.p2p.model;

import java.util.Date;

public class ChatMessage extends Message {

  private static final long serialVersionUID = 668487259223050593L;
  private String data;

  public ChatMessage(String sender, String receiver, Date date,String message){
    super(sender, receiver, date);
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
