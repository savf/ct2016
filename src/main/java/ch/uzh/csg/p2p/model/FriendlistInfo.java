package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FriendlistInfo implements Serializable {

  private static final long serialVersionUID = -5114058158442667036L;
  private String username;
  private List<Friend> friendlist;
  
  public FriendlistInfo(){
    username = "";
    friendlist = new ArrayList<Friend>();
  }
  
  public FriendlistInfo(String username, List<Friend> friendlist){
    setUsername(username);
    setFriendlist(friendlist);
  }
  
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }
  public List<Friend> getFriendlist() {
    return friendlist;
  }
  public void setFriendlist(List<Friend> friendlist) {
    this.friendlist = friendlist;
  }
  
  public void addFriend(Friend friend){
    friendlist.add(friend);
  }
}
