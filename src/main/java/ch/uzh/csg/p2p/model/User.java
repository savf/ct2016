package ch.uzh.csg.p2p.model;

import java.io.Serializable;

import net.tomp2p.peers.PeerAddress;

public class User implements Serializable {
	private static final long serialVersionUID = 1057801283816805651L;
	
	String username;
	String password;
	PeerAddress peerAddress;

	public User(String username, String password, PeerAddress peerAddress) {
		super();
		this.username = username;
		this.password = password;
		this.peerAddress = peerAddress;
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
	
	@Override
	public String toString(){
		return "[username="+username+", password="+password+", peerAddress="+peerAddress+"]";
	}
}
