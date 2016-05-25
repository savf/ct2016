package ch.uzh.csg.p2p.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.uzh.csg.p2p.model.request.FriendRequest;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import net.tomp2p.peers.PeerAddress;

public class User implements Serializable {
	private static final long serialVersionUID = 1057801283816805651L;

	String username;
	String password;
	PeerAddress peerAddress;
	private ObservableList<Friend> friendList =
			FXCollections.observableList(new ArrayList<Friend>());
	private ObservableList<FriendRequest> friendRequestStorage;
	private ObservableList<ChatMessage> chatMessageStorage;
	private ObservableList<AudioInfo> audioInfoStorage;
	private ObservableList<VideoInfo> videoInfoStorage;

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
		friendList = FXCollections.observableList(new ArrayList<Friend>());
		setFriendRequestStorage(FXCollections.observableList(new ArrayList<FriendRequest>()));
		chatMessageStorage = FXCollections.observableList(new ArrayList<ChatMessage>());
		audioInfoStorage = FXCollections.observableList(new ArrayList<AudioInfo>());
		videoInfoStorage = FXCollections.observableList(new ArrayList<VideoInfo>());
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

	public void registerForVideoInfoUpdates(ListChangeListener<VideoInfo> listener) {
		videoInfoStorage.addListener(listener);
	}

	public List<VideoInfo> getVideoInfoStorage() {
		return videoInfoStorage;
	}

	public void addVideoInfo(VideoInfo videoInfo) {
		videoInfoStorage.add(videoInfo);
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

	public void removeAudioCallsFromUser(String username) {
		for (Iterator<AudioInfo> iterator = audioInfoStorage.iterator(); iterator.hasNext();) {
			AudioInfo audioInfo = iterator.next();
			if (audioInfo.getSendername().equals(username)) {
				iterator.remove();
			}
		}
	}

	public void removeVideoCallsFromUser(String username) {
		for (Iterator<VideoInfo> iterator = videoInfoStorage.iterator(); iterator.hasNext();) {
			VideoInfo videoInfo = iterator.next();
			if (videoInfo.getSendername().equals(username)) {
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

	public void registerForFriendRequestWhileAwayUpdates(
			ListChangeListener<FriendRequest> listener) {
		friendRequestStorage.addListener(listener);
	}

	public void addFriendRequest(FriendRequest request) {
		for (Iterator<FriendRequest> iterator = friendRequestStorage.iterator(); iterator
				.hasNext();) {
			FriendRequest friendRequest = iterator.next();
			if (!friendRequest.getSenderName().equals(request.getSenderName())) {
				friendRequestStorage.add(request);
			}
		}
	}

	public void removeFriendRequest(FriendRequest request) {
		for (Iterator<FriendRequest> iterator = friendRequestStorage.iterator(); iterator
				.hasNext();) {
			FriendRequest friendRequest = iterator.next();
			if (!friendRequest.getSenderName().equals(request.getSenderName())) {
				iterator.remove();
			}
		}
	}

	public ObservableList<Friend> getFriendList() {
		return friendList;
	}

	public void setFriendList(ObservableList<Friend> friendList) {
		this.friendList = friendList;
	}

	public void registerForFriendListUpdates(ListChangeListener<Friend> listener) {
		friendList.addListener(listener);
	}

	public Friend getFriend(String currentChatPartner) {
		for (Friend f : friendList) {
			if (f.getName().equals(currentChatPartner)) {
				return f;
			}
		}
		return null;
	}

	public void addFriend(Friend friend) {
		boolean containsFriend = false;
		for (Friend f : friendList) {
			if (f.getName().equals(friend.getName())) {
				f.setFriendshipStatus(FriendshipStatus.ACCEPTED);
				friend = f;
				containsFriend = true;
				break;
			}
		}
		if (!containsFriend) {
			friendList.add(friend);
		}
	}

	public void removeFriend(Friend f) {
		for (Friend friend : friendList) {
			if (friend.getName().equals(f.getName())) {
				friendList.remove(friend);
				break;
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
