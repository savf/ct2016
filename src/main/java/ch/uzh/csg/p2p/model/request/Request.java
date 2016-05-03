package ch.uzh.csg.p2p.model.request;

import java.io.Serializable;

public abstract class Request implements Serializable {
	private static final long serialVersionUID = 3325627567053761196L;
	private RequestType type;
	
	public Request(RequestType type){
	  setType(type);
	}

	public Request() {
		type = null;
	}
	
	public RequestType getType() {
	  return type;
	}

	public void setType(RequestType type) {
	  this.type = type;
	}

}
