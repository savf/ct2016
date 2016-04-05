package main.java.ch.uzh.csg.p2p;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.ch.uzh.csg.p2p.multimedia.VideoApplication;
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
import net.tomp2p.storage.Data;

public class Node {
	
	private final String KEY = "test";
	
	private Logger log;
	
	private PeerDHT peer;
	private VideoApplication va;
	
	
	public Node(int nodeId, int localPort, String knownIP, int knownPort) throws IOException, LineUnavailableException{
		
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
						PeerAddress peerAddress = (PeerAddress) future.data().object();
						log.info(peerAddress.toString());
						peer.peer().sendDirect(peerAddress).object("start video").start();
					} else {
						log.error("FutureGet was unsuccessful: " + future.failedReason());
					}
					
				}
			});
		}else{
			log.info("I'm First Node");
			peer.put(Number160.createHash(KEY)).data(new Data(peer.peerAddress())).start();
			log.info("Put my IP with key: "+KEY);
			va = new VideoApplication();
			va.initialize();
		}
	}
	
	private void createPeer(int nodeId, int localPort) throws IOException{
		Bindings b = new Bindings().listenAny();
    	peer = new PeerBuilderDHT(new PeerBuilder(new Number160(nodeId)).ports(localPort).bindings(b).start()).start();
    	peer.peer().objectDataReply(new ObjectDataReply() {
			
			public Object reply(PeerAddress peerAddress, Object object) throws Exception {
				return handleMessage(peerAddress, object);
			}
		});
	}
	
	private Object handleMessage(PeerAddress peerAddress, Object object) throws IOException{
		log.info("received message: "+object.toString()+" from: "+peerAddress.toString());
		if(object instanceof String){
			if(object.toString().equals("start video")){
				if(va != null){
					List<BufferedImage> list = va.getVideoData().subList(0, 10);
					
					
					ByteBuf byteBuf = new EmptyByteBuf(null);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
				    ObjectOutputStream oos = new ObjectOutputStream(bos);
				    oos.writeObject(list);
				    byte[] bytes = bos.toByteArray();
				    byteBuf.writeBytes(bytes);
				    Buffer buffer = new Buffer(byteBuf);
				    peer.peer().sendDirect(peerAddress).buffer(buffer).start();
				    
					peer.peer().sendDirect(peerAddress).object(list).start();
				}else{
					peer.peer().sendDirect(peerAddress).object("No Video Data available!").start();
				}
			}
		}else if(object instanceof List){
			System.out.println("hiere");
			showReceivedVideoData((List)object);
		}else{
			System.out.println("else");
		}
		return 0;
	}
	
	private void showReceivedVideoData(List<BufferedImage> dataList){
		System.out.println("received VideoData, datalength: "+dataList.size());
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
