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
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.storage.Data;

public class Node {
	
	private final int PORT = 4000;
	private final String KEY = "test";
	
	private Logger log;
	
	private PeerDHT peer;
	
	
	public Node(int nodeId, String knownIP) throws IOException{
		
		log = LoggerFactory.getLogger("Node with id: "+nodeId);

		
		createPeer(nodeId);
		
		//if knownIP == null; this is first node in network and cannot connect to others
		if(knownIP != null){
			connectToNode(knownIP);
			FutureGet futureGet = peer.get(Number160.createHash(KEY)).start();
			futureGet.addListener(new BaseFutureListener<BaseFuture>() {

				public void exceptionCaught(Throwable arg0) throws Exception {
					// TODO Auto-generated method stub
				}

				public void operationComplete(BaseFuture baseFuture) throws Exception {
					if(baseFuture.isSuccess()){
						//TODO change to logger
						log.info(((FutureGet)baseFuture).data().object().toString());
					}else{
						log.error("BaseFutur not success: "+baseFuture.failedReason());
					}
					
				}
			});
		}else{
			log.info("I'm First Node");
			peer.put(Number160.createHash(KEY)).data(new Data(peer.peerAddress())).start();
			log.info("Put my IP with key: "+KEY);
		}
	}
	
	private void createPeer(int nodeId) throws IOException{
		Bindings b = new Bindings().listenAny();
    	peer = new PeerBuilderDHT(new PeerBuilder(new Number160(nodeId)).ports(PORT).bindings(b).start()).start();
	}

	private void connectToNode(String knownIP) throws UnknownHostException{
		InetAddress address = Inet4Address.getByName(knownIP);
		FutureDiscover futureDiscover = peer.peer().discover().inetAddress(address).ports(PORT).start();
		futureDiscover.awaitUninterruptibly();
		FutureBootstrap futureBootstrap = peer.peer().bootstrap().inetAddress(address).ports(PORT).start();
		futureBootstrap.awaitUninterruptibly();
	}
	
}
