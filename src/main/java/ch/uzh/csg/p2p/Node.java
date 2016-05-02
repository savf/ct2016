package ch.uzh.csg.p2p;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.controller.MainWindowController;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.BootstrapRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.multimedia.VideoApplication;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class Node {

	protected final String BOOTSTRAPNODE = "Bootstrapnode";
	private final int DEFAULTPORT = 54000;

	private Logger log;
	private User user;

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
			  // TODO: Aufrufen RequestHandler
				return mainWindowController.handleReceiveMessage(peerAddress, object);
			}
		});
		// TODO: Indirect Replication or direct? Replication factor?
		new IndirectReplication(peer).autoReplication(true).replicationFactor(3).start();
	}

/*	protected Object handleMessage(PeerAddress peerAddress, Object object) throws IOException {
	  //TODO: Umwandeln mit RequestHandler!
		log.info("received message: " + object.toString() + " from: " + peerAddress.toString());
		Message m = (Message) object;
		if(m instanceof ChatMessage){
		  ChatMessage chatMessage = (ChatMessage) m;
		  mainWindowController.addReceivedMessage(chatMessage.getSenderID(), chatMessage.getData());
		}
		else{
		  //TODO!
		}
	
		return 0;
	} */

	private void connectToNode(String knownIP) throws ClassNotFoundException, IOException {
	  BootstrapRequest request = new BootstrapRequest(user.getPeerAddress(), knownIP ,RequestType.SEND);
	  RequestHandler.handleRequest(request, this);
	  
	  // TODO: BootstrapRequest
		/*InetAddress address = Inet4Address.getByName(knownIP);
		FutureDiscover futureDiscover =
				peer.peer().discover().inetAddress(address).ports(DEFAULTPORT).start();
		futureDiscover.awaitUninterruptibly();
		FutureBootstrap futureBootstrap =
				peer.peer().bootstrap().inetAddress(address).ports(DEFAULTPORT).start();
		futureBootstrap.awaitUninterruptibly();

		FutureGet futureGet = peer.get(Number160.createHash(BOOTSTRAPNODE)).start();
		futureGet.addListener(new BaseFutureListener<FutureGet>() {

			public void exceptionCaught(Throwable arg0) throws Exception {}

			public void operationComplete(FutureGet future) throws Exception {
				if (future.isSuccess() && future.data() != null) {
					PeerAddress bootstrapNodePeerAddress = (PeerAddress) future.data().object();
					peer.peer().sendDirect(bootstrapNodePeerAddress).object("<sys>newnode</sys>")
							.start();
				} else {
					if (!future.isSuccess()) {
						log.error("FutureGet was unsuccessful: " + future.failedReason());
					} else {
						log.error("FutureGet was successful, but data is null!");
					}
				}
			}
		});*/
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
