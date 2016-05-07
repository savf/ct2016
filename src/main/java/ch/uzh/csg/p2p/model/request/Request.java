package ch.uzh.csg.p2p.model.request;

import java.io.Serializable;

public abstract class Request implements Serializable {
	private static final long serialVersionUID = 3325627567053761196L;
	private RequestType type;
	private RequestStatus status;
	
	public Request(RequestType type){
	  setType(type);
	  setStatus(RequestStatus.WAITING);
	}
	
	public Request(RequestType type, RequestStatus status){
	  setType(type);
	  setStatus(status);
	}

	public Request() {
		type = null;
		status = null;
	}
	
	public RequestType getType() {
	  return type;
	}

	public void setType(RequestType type) {
	  this.type = type;
	}

  public RequestStatus getStatus() {
    return status;
  }

  public void setStatus(RequestStatus status) {
    this.status = status;
  }

}
