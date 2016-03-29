package ch.uzh.csg.p2p;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class Node {
	
	private final String KEY = "test";
	
	private Logger log;
	
	private PeerDHT peer;
	
	
	public Node(int nodeId, int localPort, String knownIP, int knownPort) throws IOException{
		
		log = LoggerFactory.getLogger("Node with id: "+nodeId);
		
		createPeer(nodeId, localPort);
		
		//if knownIP == null; this is first node in network and cannot connect to others
		if(knownIP != null){
			connectToNode(knownIP, knownPort);
			FutureGet futureGet = peer.get(Number160.createHash(KEY)).start();
			futureGet.addListener(new BaseFutureListener<FutureGet>() {

				public void exceptionCaught(Throwable arg0) throws Exception {
					// TODO Auto-generated method stub
				}

				public void operationComplete(FutureGet future) throws Exception {
					if(future.isSuccess() && future.data() != null) {
						log.info(future.data().object().toString());
					} else {
						log.error("FutureGet was unsuccessful: " + future.failedReason());
					}
					
				}
			});
		}else{
			log.info("I'm First Node");
			peer.put(Number160.createHash(KEY)).data(new Data(peer.peerAddress())).start();
			log.info("Put my IP with key: "+KEY);
		}
	}
	
	private void createPeer(int nodeId, int localPort) throws IOException{
		Bindings b = new Bindings().listenAny();
    	peer = new PeerBuilderDHT(new PeerBuilder(new Number160(nodeId)).ports(localPort).bindings(b).start()).start();
	}

	private void connectToNode(String knownIP, int knownPort) throws UnknownHostException{
		InetAddress address = Inet4Address.getByName(knownIP);
		FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports(knownPort).start();
		futureDiscover.awaitUninterruptibly();
		FutureBootstrap futureBootstrap = peer.peer().bootstrap().inetAddress(address).ports(knownPort).start();
		futureBootstrap.awaitUninterruptibly();
	}
	
	public void shutdown(){
		log.info("Shutting down gracefully.");
		if(peer != null) {
			peer.shutdown();
		}
	}
	
}
