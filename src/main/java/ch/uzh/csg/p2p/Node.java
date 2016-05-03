package ch.uzh.csg.p2p;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.controller.MainWindowController;
import ch.uzh.csg.p2p.helper.AudioUtils;
import ch.uzh.csg.p2p.helper.EncoderUtils;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.AudioMessage;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.BootstrapRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.Request;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.message.Buffer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.rpc.RawDataReply;
import net.tomp2p.storage.Data;

public class Node {

	protected final String BOOTSTRAPNODE = "Bootstrapnode";
	private final int DEFAULTPORT = 54000;

	private Logger log;
	private User user;

	//TODO: delete MWC from Node
	private MainWindowController mainWindowController;
	protected PeerDHT peer;

	public Node(int nodeId, String ip, String username, String password,
			MainWindowController mainWindowController)
			throws IOException, LineUnavailableException, ClassNotFoundException {

		log = LoggerFactory.getLogger("Node from user: " + username);

		this.mainWindowController = mainWindowController;

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
			throws IOException, ClassNotFoundException {
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
	  //TODO: Umwandeln mit RequestHandler!
	  
		log.info("received message: " + object.toString() + " from: " + peerAddress.toString());
		
		final Object obj = object;
		
		if (object instanceof AudioMessage) {
			AudioMessage message = (AudioMessage) object;
			try {
				AudioUtils.playAudio(EncoderUtils.byteArrayToByteBuffer(message.getData()));
			} catch (LineUnavailableException e) {
				log.error("AudioMessage could not be played: " + e);
			}
		} else if (object instanceof AudioRequest) {
			AudioRequest audioRequest = (AudioRequest) object;
			handleAudioRequest(audioRequest);
		} 
		else if(object instanceof ChatMessage){
		  ChatMessage chatMessage = (ChatMessage) object;
		  mainWindowController.addReceivedMessage(chatMessage.getSenderID(), chatMessage.getData(), new Date());
		}
		else {
          System.out.println("else");
		}    
      
		return 0;
	} 

	private void handleAudioRequest(AudioRequest request)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		switch (request.getType()) {
		  // TODO: let RequestHandler handle all P2P things!
			case SEND:
				mainWindowController.askAudioCall(request.getSenderName());
				break;
			case ACCEPTED:
				mainWindowController.startAudioCall();
				break;
			case REJECTED:
				mainWindowController.audioCallRejected();
				break;
			case ABORTED:
				mainWindowController.audioCallAborted();
				break;
			default:
				break;
		}
	}

	private void connectToNode(String knownIP) throws ClassNotFoundException, IOException {
	  BootstrapRequest request = new BootstrapRequest(user.getPeerAddress(), knownIP ,RequestType.SEND);
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
