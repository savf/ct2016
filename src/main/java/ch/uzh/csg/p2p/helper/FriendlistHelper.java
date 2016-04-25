package ch.uzh.csg.p2p.helper;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.User;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.peers.Number160;

public class FriendlistHelper {

	private static Logger log = LoggerFactory.getLogger(FriendlistHelper.class);

	public static User findUser(Node node, String username)
			throws ClassNotFoundException, IOException {
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
	}

}
