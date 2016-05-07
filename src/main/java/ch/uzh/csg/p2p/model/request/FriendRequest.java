package ch.uzh.csg.p2p.model.request;

import ch.uzh.csg.p2p.model.Friend;

public class FriendRequest extends Request {
	private static final long serialVersionUID = 1L;
	private Friend friend;
	private String receiverID;

	public FriendRequest() {
		super();
		setFriend(new Friend());
		setReceiverID("");
	}

	public FriendRequest(String receiver, Friend friend, RequestType type) {
		super(type);
		setFriend(friend);
		setReceiverID(receiver);
	}

	public String getReceiverID() {
		return receiverID;
	}

	public void setReceiverID(String receiverID) {
		this.receiverID = receiverID;
	}

	public Friend getFriend() {
		return friend;
	}

	public void setFriend(Friend friend) {
		this.friend = friend;
	}
}
