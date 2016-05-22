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
	  if(node.getUser().getFriendList()==null){
	    return false;
	  }
		for (Friend f : node.getUser().getFriendList()) {
			if ((username).equals(f.getName())) {
				return true;
			}
		}
		return false;
	}

	public void storeFriend(Friend f) {
		FriendRequest r =
				new FriendRequest(node.getUser().getPeerAddress(), node.getUser().getUsername(), f.getName(), RequestType.STORE);
		r.setReceiverAddress(f.getPeerAddress());
		RequestHandler.handleRequest(r, node);
	}
}
