package ch.uzh.csg.p2p.model.request;

import ch.uzh.csg.p2p.Node;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureListener;

public class RequestListener<Type> implements BaseFutureListener<FutureGet>{

	private Type variableToSet;
	protected Node node;
	
	public RequestListener(Type variableToSet) {
		this.variableToSet = variableToSet;
	}
	
	public RequestListener(Node node) {
		this.node = node;
	}
	
	public RequestListener() {
	}
	
	public void operationComplete(FutureGet futureGet) throws Exception {
		if(futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
			this.variableToSet = (Type) futureGet.data().object();
		}
		else {
			this.variableToSet = null;
		}
	}

	public void exceptionCaught(Throwable t) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public void shutdownNode() {
		if(node != null) {
			node.shutdown();
		}
	}

}
