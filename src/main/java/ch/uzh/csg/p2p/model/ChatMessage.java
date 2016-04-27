package ch.uzh.csg.p2p.model;

import java.sql.Timestamp;

public class ChatMessage extends Message {

  private static final long serialVersionUID = 668487259223050593L;
  private String data;

  public ChatMessage(String sender, String receiver, Timestamp timestamp,String message){
    super(sender, receiver, timestamp);
    setData(message);
  }

  public void setData(String message) {
    this.data = message;
  }

  @Override
  public String getData() {
    return data;
  }
  
}
