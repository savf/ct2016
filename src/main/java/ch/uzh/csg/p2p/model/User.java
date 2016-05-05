package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

public class User implements Serializable {
	private static final long serialVersionUID = 1057801283816805651L;
	
	String username;
	String password;
	PeerAddress peerAddress;
	private Set<Number160> friendStorage;
	private List<String> audioMessageStorage;
	private List<String> chatMessageStorage;
	
	public User(String username, String password, PeerAddress peerAddress) {
	     super();
	     this.username = username;
	     this.password = password;
	     this.peerAddress = peerAddress;
	     setFriendStorage(new HashSet<Number160>());
	     setAudioMessageStorage(new ArrayList<String>());
	     setChatMessageStorage(new ArrayList<String>());
	 }
	
	public Set<Number160> getFriendStorage() {
    return friendStorage;
  }

  public void setFriendStorage(Set<Number160> friendStorage) {
    this.friendStorage = friendStorage;
  }

  public List<String> getAudioMessageStorage() {
    return audioMessageStorage;
  }

  public void setAudioMessageStorage(List<String> audioMessageStorage) {
    this.audioMessageStorage = audioMessageStorage;
  }

  public List<String> getChatMessageStorage() {
    return chatMessageStorage;
  }

  public void setChatMessageStorage(List<String> chatMessageStorage) {
    this.chatMessageStorage = chatMessageStorage;
  }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public PeerAddress getPeerAddress() {
		return peerAddress;
	}

	public void setPeerAddress(PeerAddress peerAddress) {
		this.peerAddress = peerAddress;
	}
	
	public void addFriend(Number160 hash){
	  friendStorage.add(hash);
	}
	
	@Override
	public String toString(){
	  // TODO: maybe add the new attributes?
		return "[username="+username+", password="+password+", peerAddress="+peerAddress+"]";
	}
}
