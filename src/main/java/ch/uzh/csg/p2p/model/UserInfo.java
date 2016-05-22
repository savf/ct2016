package ch.uzh.csg.p2p.model;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

public class UserInfo implements Serializable{

  private static final long serialVersionUID = -6968180176792187246L;
  private PeerAddress peerAddress;
  private String userName;
  private String password;
  
  public UserInfo(){
    peerAddress = null;
    userName = "";
    password = "";
  }
  
  public UserInfo(PeerAddress address, String name, String pw){
    setPeerAddress(address);
    setUserName(name);
    setPassword(pw);
  }
  
  public PeerAddress getPeerAddress() {
    return peerAddress;
  }
  public void setPeerAddress(PeerAddress peerAddress) {
    this.peerAddress = peerAddress;
  }
  public String getUserName() {
    return userName;
  }
  public void setUserName(String userName) {
    this.userName = userName;
  }
  public String getPassword() {
    return password;
  }
  public void setPassword(String password) {
    this.password = password;
  }

}
