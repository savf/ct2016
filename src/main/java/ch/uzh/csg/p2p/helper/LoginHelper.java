package ch.uzh.csg.p2p.helper;

import java.io.IOException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.UserRequest;

public class LoginHelper {

	public final static String USER_PREFIX = "user_";
	public final static String ADDRESS_PREFIX = "address_";

	public static boolean usernamePasswordCorrect(Node node, String username, String password) {	        
	        User user = retrieveUser(username, node);
	        if(user == null){
	          return true;
	        }
			if (user.getPassword().equals(password)) {
				return true;
			} else {
				return false;
			}
	}

	public static void saveUsernamePassword(Node node, String username, String password)
			throws IOException {
	  User user = new User(username, password, node.getPeer().peerAddress());
	  storeUser(user, node);
	}

	public static void updatePeerAddress(Node node, String username)
			throws ClassNotFoundException, IOException {
	   // zuerst das bestehende User-Objekt laden
	    User user = retrieveUser(username, node);
        if(user == null){
          user = new User(username, "", null);
        }
		user.setPeerAddress(node.getPeer().peerAddress());
		storeUser(user, node);
	}

	public static Boolean userExists(Node node, String username) {
	  User user = retrieveUser(username, node);
		if (user != null) {
			return true;
		}
		return false;
	}

	private static User retrieveUser(String username, Node node){
	  User userToRetrieve = new User(username, "", null);
      UserRequest requestRetrieve = new UserRequest(userToRetrieve, RequestType.RETRIEVE);
      User user = (User)RequestHandler.handleRequest(requestRetrieve, node);
      return user;
	}
	
	private static void storeUser(User user, Node node){
	   UserRequest requestStore = new UserRequest(user, RequestType.STORE);
	   RequestHandler.handleRequest(requestStore, node);
	}
	
}
