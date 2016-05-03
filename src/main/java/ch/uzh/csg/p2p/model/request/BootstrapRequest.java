package ch.uzh.csg.p2p.model.request;

import net.tomp2p.peers.PeerAddress;


public class BootstrapRequest extends Request {
    private PeerAddress senderPeerAddress;
    private String bootstrapIP;
    
    public BootstrapRequest(){
      super();
      setSenderPeerAddress(null);
      setBootstrapNodeIP("");
    }
    
    public BootstrapRequest(PeerAddress senderPeerAddress, String bootstrapIP, RequestType type){
      super(type);
      setSenderPeerAddress(senderPeerAddress);
      setBootstrapNodeIP(bootstrapIP);
    }
    
    public BootstrapRequest(RequestType type){
      super(type);
      setSenderPeerAddress(null);
      setBootstrapNodeIP(null);
    }

    public PeerAddress getSenderPeerAddress() {
      return senderPeerAddress;
    }

    public void setSenderPeerAddress(PeerAddress senderPeerAddress) {
      this.senderPeerAddress = senderPeerAddress;
    }

    public String getBootstrapNodeIP() {
      return bootstrapIP;
    }

    public void setBootstrapNodeIP(String bootstrapNodeIP) {
      this.bootstrapIP = bootstrapNodeIP;
    }
    
}
