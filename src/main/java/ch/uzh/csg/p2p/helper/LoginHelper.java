package ch.uzh.csg.p2p.helper;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestListener;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.UserRequest;
import net.tomp2p.dht.FutureGet;

public class LoginHelper {

	public final static String USER_PREFIX = "user_";
	public final static String ADDRESS_PREFIX = "address_";

	public static void saveUsernamePassword(Node node, String username, String password)
			throws IOException, LineUnavailableException {
		User user = new User(username, password, node.getPeer().peerAddress());
		storeUser(user, node);
	}

	public static void updatePeerAddress(Node node, String username)
			throws ClassNotFoundException, LineUnavailableException {
		RequestListener<User> requestListener = new RequestListener<User>(node){
			@Override
			public void operationComplete(FutureGet futureGet) throws Exception {
				User user;
				if(futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
					user = (User) futureGet.data().object();
				}
				else {
					user = new User(username, "", null);
				}
				user.setPeerAddress(this.node.getPeer().peerAddress());
				LoginHelper.storeUser(user, this.node);
			}
		};
		retrieveUser(username, node, requestListener);
	}

	public static void retrieveUser(String username, Node node,
			RequestListener<User> requestListener) throws LineUnavailableException {
		User userToRetrieve = new User(username, "", null);
		UserRequest requestRetrieve = new UserRequest(userToRetrieve, RequestType.RETRIEVE);
		RequestHandler.handleRequest(requestRetrieve, node, requestListener);
	}

	private static void storeUser(User user, Node node) throws LineUnavailableException {
		UserRequest requestStore = new UserRequest(user, RequestType.STORE);
		RequestHandler.handleRequest(requestStore, node);
	}

}
