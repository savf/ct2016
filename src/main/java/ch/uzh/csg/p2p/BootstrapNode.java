package ch.uzh.csg.p2p;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.controller.MainWindowController;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.storage.Data;

public class BootstrapNode extends Node {

	private Logger log;

	private MainWindowController mainWindowController;

	public BootstrapNode(int nodeId, String ip, String username, String password,
			MainWindowController mainWindowController)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		super(nodeId, ip, username, password, mainWindowController);

		log = LoggerFactory.getLogger("BootstrapNode form user: " + username);

		this.mainWindowController = mainWindowController;
		createPeer(nodeId, username, password, true);
	}

	@Override
	protected Object handleMessage(PeerAddress peerAddress, Object object) throws IOException {
		log.info("BootstrapNode received message: " + object.toString() + " from: "
				+ peerAddress.toString());
		if (object instanceof String && ((String) object).equals("<sys>newnode</sys>")) {
			log.info("new node");
			FutureBootstrap futureBootstrap =
					peer.peer().bootstrap().peerAddress(peerAddress).start();
		} else {
			super.handleMessage(peerAddress, object);
		}
		return 0;
	}

}
