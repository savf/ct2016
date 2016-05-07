package ch.uzh.csg.p2p.helper;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.UserRequest;

public class FriendlistHelper {

	private static Logger log = LoggerFactory.getLogger(FriendlistHelper.class);

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
	  /*
		FutureGet futureGet = node.getPeer()
				.get(Number160.createHash(LoginHelper.USER_PREFIX + username)).start();
		futureGet.awaitUninterruptibly(); 
		if (futureGet.data() != null) {
			log.info("User with username: {} found", username);
			User user = (User) futureGet.data().object();
			return user;
		} else {
			// No data available --> user not exists
			log.info("User with username: {} not found", username);
			return null;
		}
		*/
	}
	
	private static User retrieveUser(String username, Node node)
			throws LineUnavailableException{
      User userToRetrieve = new User(username, "", null);
      UserRequest requestRetrieve = new UserRequest(userToRetrieve, RequestType.RETRIEVE);
      User user = (User)RequestHandler.handleRequest(requestRetrieve, node);
      return user;
    }

}
