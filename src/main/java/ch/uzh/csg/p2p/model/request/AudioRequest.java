package ch.uzh.csg.p2p.model.request;

import net.tomp2p.peers.PeerAddress;

public class AudioRequest extends Request {

	private static final long serialVersionUID = 1L;

	public AudioRequest() {
		super();
	}

	public AudioRequest(RequestType type, RequestStatus status, PeerAddress address, String receiverName, String senderName) {
      super(type, status, address, receiverName, senderName);
  }
}
