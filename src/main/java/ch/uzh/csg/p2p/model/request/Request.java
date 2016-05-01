package ch.uzh.csg.p2p.model.request;

import java.io.Serializable;

public abstract class Request implements Serializable {
	private static final long serialVersionUID = 3325627567053761196L;

	private REQUEST_TYPE type;

	public Request() {
		type = null;
	}

	public Request(REQUEST_TYPE type) {
		setType(type);
	}

	public REQUEST_TYPE getType() {
		return type;
	}

	public void setType(REQUEST_TYPE type) {
		this.type = type;
	}

}
