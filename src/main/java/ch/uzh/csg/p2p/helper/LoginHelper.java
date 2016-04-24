package ch.uzh.csg.p2p.helper;

import java.io.IOException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.User;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class LoginHelper {

	public final static String USER_PREFIX = "user_";
	public final static String ADDRESS_PREFIX = "address_";

	public static boolean usernamePasswordCorrect(Node node, String username, String password)
			throws ClassNotFoundException, IOException {
		FutureGet futureGet =
				node.getPeer().get(Number160.createHash(USER_PREFIX + username)).start();
		futureGet.awaitUninterruptibly();
		try {
			User user = (User) futureGet.data().object();
			if (user.getPassword().equals(password)) {
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException e) {
			// No data available --> user not exists
			return true;
		}
	}

	public static void saveUsernamePassword(Node node, String username, String password)
			throws IOException {
		User user = new User(username, password, node.getPeer().peerAddress());
		node.getPeer().put(Number160.createHash(USER_PREFIX + username)).data(new Data(user))
				.start();
	}

	public static void updatePeerAddress(Node node, String username)
			throws ClassNotFoundException, IOException {
		FutureGet futureGet =
				node.getPeer().get(Number160.createHash(USER_PREFIX + username)).start();
		futureGet.awaitUninterruptibly();
		User user = (User) futureGet.data().object();
		user.setPeerAddress(node.getPeer().peerAddress());
		node.getPeer().put(Number160.createHash(USER_PREFIX + username)).data(new Data(user))
				.start();
	}

	public static Boolean userExists(Node node, String username) {
		FutureGet futureGet =
				node.getPeer().get(Number160.createHash(USER_PREFIX + username)).start();
		futureGet.awaitUninterruptibly();
		Data data = futureGet.data();
		if (data != null) {
			return true;
		}
		return false;
	}

}
