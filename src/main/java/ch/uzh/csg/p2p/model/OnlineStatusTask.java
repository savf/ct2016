package ch.uzh.csg.p2p.model;

import java.util.List;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureDirect;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.request.OnlineStatusRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;

public class OnlineStatusTask extends TimerTask {
  private Node node;
  private Logger log;

  private OnlineStatusTask() {
    log = LoggerFactory.getLogger("Onlinestatus Task");
  }

  public OnlineStatusTask(Node node) {
    this();
    setNode(node);
  }

  private void setNode(Node node) {
    this.node = node;
  }

  @Override
  public void run() {
    try {
      OnlineStatusRequest r = new OnlineStatusRequest();
      r.setOnlineStatus(OnlineStatus.ONLINE);
      r.setSenderName(node.getUser().getUsername());
      r.setSenderAddress(node.getPeer().peerAddress());
      List<Friend> list = node.getFriendList();
      synchronized (list) {
        for (Friend f : list) {
          // only send OnlineStatusRequest when Friend was last online, since when they go offline &
          // online again they need to announce it
          if (f.getStatus().equals(OnlineStatus.ONLINE)) {
            r.setReceiverAddress(f.getPeerAddress());
            r.setReceiverName(f.getName());
            r.setType(RequestType.SEND);
            r.setStatus(RequestStatus.WAITING);
            BaseFutureListener<FutureDirect> futureDirectListener =
                new BaseFutureListener<FutureDirect>() {
                  @Override
                  public void operationComplete(FutureDirect future) throws Exception {
                    if (future != null && future.isSuccess()) {
                      // TODO: maybe just leave status?
                      // f.setStatus(OnlineStatus.ONLINE);
                    } else {
                      f.setStatus(OnlineStatus.OFFLINE);
                    }
                  }

                  @Override
                  public void exceptionCaught(Throwable t) throws Exception {
                    log.error(t.getMessage());
                  }
                };
            RequestHandler.handleRequest(r, node, futureDirectListener);
          }
        }
      }
    } catch (Exception ex) {
      System.out.println("Error running thread " + ex.getMessage());
    }
  }

}
