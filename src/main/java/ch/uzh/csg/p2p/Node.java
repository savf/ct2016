package ch.uzh.csg.p2p;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.helper.VideoUtils;
import ch.uzh.csg.p2p.model.AudioMessage;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.VideoMessage;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.BootstrapRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.VideoRequest;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.ObjectDataReply;

public class Node {

	protected final String BOOTSTRAPNODE = "Bootstrapnode";
	private final int DEFAULTPORT = 54000;

	private Logger log;
	private User user;

	protected PeerDHT peer;

	public Node(int nodeId, String ip, String username, String password)
			throws IOException, LineUnavailableException, ClassNotFoundException {

		log = LoggerFactory.getLogger("Node from user: " + username);

		// if not a BootstrapNode
		if (ip != null) {
			createPeer(nodeId, username, password, false);
			connectToNode(ip);
		}
		else {
		  createPeer(nodeId, username, password, true);
		}

	}

	protected void createPeer(int nodeId, String username, String password, Boolean isBootstrapNode)
			throws IOException, ClassNotFoundException, LineUnavailableException {
		Bindings b = new Bindings().listenAny();
		peer = new PeerBuilderDHT(
				new PeerBuilder(new Number160(nodeId)).ports(getPort()).bindings(b).start())
						.start();

		if (LoginHelper.userExists(this, username)) {
			// user still exist --> only update address
			LoginHelper.updatePeerAddress(this, username);
		} else {
			// user not exist --> add user
			LoginHelper.saveUsernamePassword(this, username, password);
		}
		user = new User(username, password, peer.peerAddress());
		if (isBootstrapNode) {
		  BootstrapRequest request = new BootstrapRequest(RequestType.STORE);
		  RequestHandler.handleRequest(request, this);
		}
		peer.peer().objectDataReply(new ObjectDataReply() {
			public Object reply(PeerAddress peerAddress, Object object) throws Exception {
			  return handleReceivedData(peerAddress, object);			  
			  }
			});
		// TODO: Indirect Replication or direct? Replication factor?
		new IndirectReplication(peer).autoReplication(true).replicationFactor(3).start();
	}


	protected Object handleReceivedData(PeerAddress peerAddress, Object object)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		log.info("Received message: " + object.toString() + " from: " + peerAddress.toString());
		MessageRequest messageRequest = new MessageRequest();

		if (object instanceof AudioMessage) {
			messageRequest.setType(RequestType.RECEIVE);
			messageRequest.setMessage((AudioMessage) object);
			RequestHandler.handleRequest(messageRequest, this);
		} else if (object instanceof AudioRequest) {
			AudioRequest audioRequest = (AudioRequest) object;
			audioRequest.setType(RequestType.RECEIVE);
			RequestHandler.handleRequest(audioRequest, this);
		} else if (object instanceof VideoMessage) {
			VideoMessage message = (VideoMessage) object;
			VideoUtils.playVideo(message.getData());
		} else if (object instanceof VideoRequest) {
			VideoRequest videoRequest = (VideoRequest) object;
			videoRequest.setType(RequestType.RECEIVE);
			RequestHandler.handleRequest(videoRequest, this);
		} else if (object instanceof ChatMessage) {
			messageRequest.setType(RequestType.RECEIVE);
			messageRequest.setMessage((ChatMessage) object);
			RequestHandler.handleRequest(messageRequest, this);
		} else {
			System.out.println("else");
		}

		return 0;
	}

	private void connectToNode(String knownIP)
			throws ClassNotFoundException, IOException, LineUnavailableException {
	  BootstrapRequest request = new BootstrapRequest(RequestType.SEND, user.getPeerAddress(), knownIP);
	  RequestHandler.handleRequest(request, this);
	}

	private int getPort() {
		int port = DEFAULTPORT;
		while (portIsOpen("127.0.0.1", port, 200)) {
			port++;
		}
		log.info("Free port found, port is: " + port);
		return port;
	}

	private boolean portIsOpen(String ip, int port, int timeout) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), timeout);
			socket.close();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public PeerDHT getPeer() {
		return peer;
	}
	
	public User getUser(){
	  return user;
	}

	public void shutdown() {
		log.info("Shutting down gracefully.");
		if (peer != null) {
			peer.shutdown();
		}
	}

}
