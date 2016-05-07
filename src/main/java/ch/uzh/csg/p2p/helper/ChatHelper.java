package ch.uzh.csg.p2p.helper;

import java.util.Date;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestType;

public class ChatHelper {

	
	public static void sendMessage(Node node, String message, List<String> users)
			throws LineUnavailableException {
		String sender = node.getUser().getUsername();
		
		for(String chatPartner: users) {
			Date date = new Date();
			ChatMessage m = new ChatMessage(sender, chatPartner, date, message);
		  
			MessageRequest request = new MessageRequest(m, RequestType.SEND);
			RequestHandler.handleRequest(request, node);
		}
	}
	
}
