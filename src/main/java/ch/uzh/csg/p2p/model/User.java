package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.tomp2p.peers.PeerAddress;

public class User implements Serializable {
	private static final long serialVersionUID = 1057801283816805651L;

	String username;
	String password;
	PeerAddress peerAddress;
	private List<Friend> friendList;
	private List<AudioInfo> audioMessageStorage;
	private List<ChatMessage> chatMessageStorage;

	public User(String username, String password, PeerAddress peerAddress) {
		this();
		this.username = username;
		this.password = password;
		this.peerAddress = peerAddress;
	}

	public User() {
	  super();
	  username = new String();
	  password = new String();
	  peerAddress = null;
	  setFriendList(new ArrayList<Friend>());
      setAudioMessageStorage(new ArrayList<AudioInfo>());
      setChatMessageStorage(new ArrayList<ChatMessage>());
  }

  public List<Friend> getFriendList() {
		return friendList;
	}

	public void setFriendList(List<Friend> friendStorage) {
		this.friendList = friendStorage;
	}

	public List<AudioInfo> getAudioMessageStorage() {
		return audioMessageStorage;
	}

	public void setAudioMessageStorage(List<AudioInfo> audioMessageStorage) {
		this.audioMessageStorage = audioMessageStorage;
	}

	public List<ChatMessage> getChatMessageStorage() {
		return chatMessageStorage;
	}

	public void setChatMessageStorage(List<ChatMessage> chatMessageStorage) {
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

	public void addFriend(Friend friend) {
		friendList.add(friend);
	}

	@Override
	public String toString() {
		// TODO: maybe add the new attributes?
		return "[username=" + username + ", password=" + password + ", peerAddress=" + peerAddress
				+ "]";
	}
}
