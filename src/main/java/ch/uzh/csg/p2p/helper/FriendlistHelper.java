package ch.uzh.csg.p2p.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestType;

public class FriendlistHelper {

	private static Logger log = LoggerFactory.getLogger(FriendlistHelper.class);
	private Node node;

	public FriendlistHelper(Node node) {
		this.node = node;
	}

	public boolean checkAlreadyFriend(String username) {
		for (String n : node.getUser().getFriendStorage()) {
			if ((username).equals(n)) {
				return true;
			}
		}
		return false;
	}

	public void storeFriend(Friend f) {
		FriendRequest r =
				new FriendRequest(f.getPeerAddress(), f.getName(), null, RequestType.STORE);
		RequestHandler.handleRequest(r, node);
	}

}
