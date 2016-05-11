package ch.uzh.csg.p2p.helper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.UserRequest;

public class FriendlistHelper {

	private static Logger log = LoggerFactory.getLogger(FriendlistHelper.class);
	private Node node;
	
	public FriendlistHelper(Node node) {
		this.node = node;
	}
	
	public static User findUser(Node node, String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
	  User user = retrieveUser(username,node);
	  if(user != null){
	    if(user.getPeerAddress().equals(node.getPeer().peerAddress())){
	      log.info("User with username: {} found, but is same user", username);
	      return null;
	    }
	    log.info("User with username: {} found", username);
	    return user;
	  } else {
	 // No data available --> user does not exists
	    log.info("User with username: {} not found", username);
        return null;
	  }
	}
	
	private static User retrieveUser(String username, Node node) throws UnsupportedEncodingException{
      User userToRetrieve = new User(username, null, null);
      UserRequest requestRetrieve = new UserRequest(userToRetrieve, RequestType.RETRIEVE);
      User user = (User)RequestHandler.handleRequest(requestRetrieve, node);
      return user;
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
