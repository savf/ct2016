package ch.uzh.csg.p2p;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.controller.MainWindowController;
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
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class Node {

	protected final String BOOTSTRAPNODE = "Bootstrapnode";

	private Logger log;

	private MainWindowController mainWindowController;
	protected PeerDHT peer;
	private VideoApplication va;

	public Node(int nodeId, int localPort, String knownIP, int knownPort, String username,
			MainWindowController mainWindowController)
			throws IOException, LineUnavailableException, ClassNotFoundException {

		log = LoggerFactory.getLogger("Node form user: " + username);

		this.mainWindowController = mainWindowController;

		// if not a BootstrapNode
		if (knownIP != null) {
			createPeer(nodeId, localPort, username, false);
			connectToNode(knownIP, knownPort);
		}
		;

	}

	public void sendMessageToAddress(String username, String message) {
		final String msg = message;
		FutureGet futureGet = peer.get(Number160.createHash(username)).start();
		futureGet.addListener(new BaseFutureListener<FutureGet>() {

			public void exceptionCaught(Throwable arg0) throws Exception {
			}

			public void operationComplete(FutureGet future) throws Exception {
				if (future.isSuccess() && future.data() != null) {
					PeerAddress peerAddress = (PeerAddress) future.data().object();
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

	protected void createPeer(int nodeId, int localPort, String username, Boolean isBootstrapNode) throws IOException {
		Bindings b = new Bindings().listenAny();
		peer = new PeerBuilderDHT(new PeerBuilder(new Number160(nodeId)).ports(localPort).bindings(b).start()).start();
		peer.put(Number160.createHash(username)).data(new Data(peer.peerAddress())).start();
		peer.put(Number160.createHash(peer.peerAddress().toString())).data(new Data(username)).start();
		if (isBootstrapNode) {
			peer.put(Number160.createHash(BOOTSTRAPNODE)).data(new Data(peer.peerAddress())).start();
		}
		peer.peer().objectDataReply(new ObjectDataReply() {

			public Object reply(PeerAddress peerAddress, Object object) throws Exception {
				return handleMessage(peerAddress, object);
			}
		});
	}

	protected Object handleMessage(PeerAddress peerAddress, Object object) throws IOException {
		log.info("received message: " + object.toString() + " from: " + peerAddress.toString());
		final Object obj = object;
		if (object instanceof String) {
			if (object.toString().equals("start video")) {
				peer.peer().sendDirect(peerAddress).object("Send you my VideoData").start();
			}
			FutureGet futureGet = peer.get(Number160.createHash(peerAddress.toString())).start();
			futureGet.addListener(new BaseFutureListener<FutureGet>() {

				public void exceptionCaught(Throwable arg0) throws Exception {
				}

				public void operationComplete(FutureGet future) throws Exception {
					if (future.isSuccess() && future.data() != null) {
						mainWindowController.addReceivedMessage(future.data().object().toString(), obj.toString());
					} else {
						if (!future.isSuccess()) {
							log.error("FutureGet was unsuccessful: " + future.failedReason());
						} else {
							log.error("FutureGet was successful, but data is null!");
						}
					}

				}
			});

		} else {
			System.out.println("else");
		}
		return 0;
	}

	private void connectToNode(String knownIP, int knownPort) throws ClassNotFoundException, IOException {
		InetAddress address = Inet4Address.getByName(knownIP);
		FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports(knownPort).start();
		futureDiscover.awaitUninterruptibly();
		FutureBootstrap futureBootstrap = peer.peer().bootstrap().inetAddress(address).ports(knownPort).start();
		futureBootstrap.awaitUninterruptibly();

		FutureGet futureGet = peer.get(Number160.createHash(BOOTSTRAPNODE)).start();// TODO
		futureGet.addListener(new BaseFutureListener<FutureGet>() {

			public void exceptionCaught(Throwable arg0) throws Exception {
			}

			public void operationComplete(FutureGet future) throws Exception {
				if (future.isSuccess() && future.data() != null) {
					PeerAddress bootstrapNodePeerAddress = (PeerAddress) future.data().object();
					peer.peer().sendDirect(bootstrapNodePeerAddress).object("<sys>newnode</sys>").start();
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

	public void shutdown() {
		log.info("Shutting down gracefully.");
		if (peer != null) {
			peer.shutdown();
		}
	}

}
