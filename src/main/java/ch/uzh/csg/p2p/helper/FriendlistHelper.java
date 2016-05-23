package ch.uzh.csg.p2p.helper;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;

public class FriendlistHelper {

  private Node node;

  public FriendlistHelper(Node node) {
    this.node = node;
  }

  public boolean checkAlreadyFriend(String username) {
    if (node.getFriendList() == null) {
      return false;
    }
    for (Friend f : node.getFriendList()) {
      if ((username).equals(f.getName())) {
        return true;
      }
    }
    return false;
  }

  public void storeFriend(Friend f, String requester) {
    FriendRequest r = new FriendRequest(null, requester, f.getName(), RequestType.STORE);
    r.setReceiverAddress(f.getPeerAddress());
    r.setStatus(RequestStatus.valueOf(f.getFriendshipStatus().toString()));
    RequestHandler.handleRequest(r, node);
  }

  public void removeFriend(Friend f, String requester) {
    FriendRequest req = new FriendRequest(null, requester, f.getName(), RequestType.STORE);
    req.setStatus(RequestStatus.ABORTED);
    RequestHandler.handleRequest(req, node);
  }
}
