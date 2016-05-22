package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageStorageInfo implements Serializable {

  private static final long serialVersionUID = 3653241289168544444L;
  private String username;
  private ChatMessage message;
  
  public MessageStorageInfo(){
    username = "";
    message = new ChatMessage();
  }
  
  public MessageStorageInfo(String username){
    setUsername(username);
    message = new ChatMessage();
  }
  
  public MessageStorageInfo(String username, ChatMessage m){
    setUsername(username);
    setMessage(m);
  }
  
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public ChatMessage getMessage() {
    return message;
  }
  public void setMessage(ChatMessage message) {
    this.message = message;
  }
  
}
