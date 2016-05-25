package ch.uzh.csg.p2p.helper;

import java.util.Date;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import net.tomp2p.dht.FutureRemove;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestType;

public class ChatHelper {


	public static void sendMessage(Node node, String message, List<String> users)
			throws LineUnavailableException {
		String sender = node.getUser().getUsername();

		for (String chatPartner : users) {
			Date date = new Date();
			PeerAddress receiverAddress = node.getUser().getFriend(chatPartner).getPeerAddress();
			ChatMessage m = new ChatMessage(sender, chatPartner, receiverAddress, date, message);

			MessageRequest request = new MessageRequest(m, RequestType.SEND);
			RequestHandler.handleRequest(request, node);
		}
	}

	public static void removeStoredMessageFrom(String sender, String recipient, Date date, Node node) {
		FutureRemove futureRemove =
				node.getPeer().remove(Number160.createHash(recipient))
						.domainKey(Number160.createHash("message"))
						.contentKey(Number160.createHash(sender + date.getTime())).start();
	}

}
