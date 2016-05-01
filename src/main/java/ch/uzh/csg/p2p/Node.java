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
import ch.uzh.csg.p2p.helper.AudioUtils;
import ch.uzh.csg.p2p.helper.EncoderUtils;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.AudioMessage;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.AudioRequest;
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
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.rpc.RawDataReply;
import net.tomp2p.storage.Data;

public class Node {

	protected final String BOOTSTRAPNODE = "Bootstrapnode";
	private final int DEFAULTPORT = 54000;

	private Logger log;

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
		} ;

	}

	public void sendMessageToAddress(String username, String message) {
		final String msg = message;
		FutureGet futureGet =
				peer.get(Number160.createHash(LoginHelper.USER_PREFIX + username)).start();
		futureGet.addListener(new BaseFutureListener<FutureGet>() {

			public void exceptionCaught(Throwable arg0) throws Exception {}

			public void operationComplete(FutureGet future) throws Exception {
				if (future.isSuccess() && future.data() != null) {
					User user = (User) future.data().object();
					PeerAddress peerAddress = user.getPeerAddress();
					log.info(peerAddress.toString());
					peer.peer().sendDirect(peerAddress).object(msg).start();
				} else {
					if (!future.isSuccess()) {
						log.error("FutureGet was unsuccessful: " + future.failedReason());
					} else {
						log.error("FutureGet was successful, but data is null!");
					}
				}

			}
		});
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

		// save address/username combination
		peer.put(Number160.createHash(LoginHelper.ADDRESS_PREFIX + peer.peerAddress().toString()))
				.data(new Data(username)).start();
		if (isBootstrapNode) {
			peer.put(Number160.createHash(BOOTSTRAPNODE)).data(new Data(peer.peerAddress()))
					.start();
		}
		peer.peer().objectDataReply(new ObjectDataReply() {

			public Object reply(PeerAddress peerAddress, Object object) throws Exception {
				return handleReceivedData(peerAddress, object);
			}
		});
	}

	protected Object handleReceivedData(PeerAddress peerAddress, Object object)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		log.info("received message: " + object.toString() + " from: " + peerAddress.toString());
		final Object obj = object;
		if (object instanceof String) {
			if (object.toString().equals("start video")) {
				peer.peer().sendDirect(peerAddress).object("Send you my VideoData").start();
			}
			FutureGet futureGet = peer
					.get(Number160.createHash(LoginHelper.ADDRESS_PREFIX + peerAddress.toString()))
					.start();
			futureGet.addListener(new BaseFutureListener<FutureGet>() {

				public void exceptionCaught(Throwable arg0) throws Exception {}

				public void operationComplete(FutureGet future) throws Exception {
					if (future.isSuccess() && future.data() != null) {
						mainWindowController.addReceivedMessage(future.data().object().toString(),
								obj.toString());
					} else {
						if (!future.isSuccess()) {
							log.error("FutureGet was unsuccessful: " + future.failedReason());
						} else {
							log.error("FutureGet was successful, but data is null!");
						}
					}
				}
			});

		} else if (object instanceof AudioMessage) {
			AudioMessage message = (AudioMessage) object;
			try {
				AudioUtils.playAudio(EncoderUtils.byteArrayToByteBuffer(message.getData()));
			} catch (LineUnavailableException e) {
				log.error("AudioMessage could not be played: " + e);
			}
		} else if (object instanceof AudioRequest) {
			AudioRequest audioRequest = (AudioRequest) object;
			handleAudioRequest(audioRequest);
		} else {
			System.out.println("else");
		}
		return 0;
	}

	private void handleAudioRequest(AudioRequest request)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		switch (request.getType()) {
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
		InetAddress address = Inet4Address.getByName(knownIP);
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
		});
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

	public void shutdown() {
		log.info("Shutting down gracefully.");
		if (peer != null) {
			peer.shutdown();
		}
	}

}
