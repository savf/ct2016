package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.model.request.FriendRequest;

public class User implements Serializable {
	private static final long serialVersionUID = 1057801283816805651L;

	String username;
	String password;
	PeerAddress peerAddress;
	private ObservableList<FriendRequest> friendRequestStorage;
	private ObservableList<ChatMessage> chatMessageStorage;
	private ObservableList<AudioInfo> audioInfoStorage;

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
		setFriendRequestStorage(FXCollections.observableList(new ArrayList<FriendRequest>()));
		chatMessageStorage = FXCollections.observableList(new ArrayList<ChatMessage>());
		audioInfoStorage = FXCollections.observableList(new ArrayList<AudioInfo>());
	}

	public void registerForAudioInfoUpdates(ListChangeListener<AudioInfo> listener) {
		audioInfoStorage.addListener(listener);
	}

	public List<AudioInfo> getAudioInfoStorage() {
		return audioInfoStorage;
	}

	public void addAudioInfo(AudioInfo audioInfo) {
		audioInfoStorage.add(audioInfo);
	}

	public void registerForChatMessageUpdates(ListChangeListener<ChatMessage> listener) {
		chatMessageStorage.addListener(listener);
	}

	public List<ChatMessage> getChatMessageStorage() {
		return chatMessageStorage;
	}

	public void addChatMessage(ChatMessage chatMessage) {
		chatMessageStorage.add(chatMessage);
	}

	public void removeMessagesFromUser(String username) {
		for (Iterator<ChatMessage> iterator = chatMessageStorage.iterator(); iterator.hasNext();) {
			ChatMessage chatMessage = iterator.next();
			if (chatMessage.getSenderID().equals(username)) {
				iterator.remove();
			}
		}
	}

	public ObservableList<FriendRequest> getFriendRequestStorage() {
		return friendRequestStorage;
	}

	public void setFriendRequestStorage(ObservableList<FriendRequest> friendRequestStorage) {
		this.friendRequestStorage = friendRequestStorage;
	}

	public void registerForFriendRequestWhileAwayUpdates(ListChangeListener<FriendRequest> listener) {
		friendRequestStorage.addListener(listener);
	}

	public void addFriendRequest(FriendRequest request) {
		for (Iterator<FriendRequest> iterator = friendRequestStorage.iterator(); iterator.hasNext();) {
			FriendRequest friendRequest = iterator.next();
			if (!friendRequest.getSenderName().equals(request.getSenderName())) {
				friendRequestStorage.add(request);
			}
		}
	}

	public void removeFriendRequest(FriendRequest request) {
		for (Iterator<FriendRequest> iterator = friendRequestStorage.iterator(); iterator.hasNext();) {
			FriendRequest friendRequest = iterator.next();
			if (!friendRequest.getSenderName().equals(request.getSenderName())) {
				iterator.remove();
			}
		}
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
	public String toString() {
		return "[username=" + username + ", password=" + password + ", peerAddress=" + peerAddress
				+ "]";
	}
}
