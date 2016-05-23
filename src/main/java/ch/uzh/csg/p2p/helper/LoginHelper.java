package ch.uzh.csg.p2p.helper;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureListener;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.UserInfo;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.UserInfoRequest;

public class LoginHelper {

  public static void saveUsernamePassword(Node node, String username, String password)
      throws IOException, LineUnavailableException {
    UserInfo user = new UserInfo(node.getPeer().peerAddress(), username, password);
    storeUserInfo(user, node);
  }

  public static void updatePeerAddress(Node node, final String username)
      throws ClassNotFoundException, LineUnavailableException {
    BaseFutureListener<FutureGet> requestListener = new BaseFutureListener<FutureGet>() {
      @Override
      public void operationComplete(FutureGet futureGet) throws Exception {
        UserInfo userInfo;
        if (futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
          userInfo = (UserInfo) futureGet.data().object();
        } else {
          userInfo = new UserInfo(null, username, "");
        }
        userInfo.setPeerAddress(node.getPeer().peerAddress());
        LoginHelper.storeUserInfo(userInfo, node);
        node.setUserInfo(userInfo);
        node.loadStoredDataFromDHT();
      }

      @Override
      public void exceptionCaught(Throwable t) throws Exception {}
    };
    retrieveUserInfo(username, node, requestListener);
  }

  public static void retrieveUserInfo(String username, Node node,
      BaseFutureListener<FutureGet> requestListener) throws LineUnavailableException {
    UserInfo userToRetrieve = new UserInfo(null, username, "");
    UserInfoRequest requestRetrieve = new UserInfoRequest(userToRetrieve, RequestType.RETRIEVE);
    RequestHandler.handleRequest(requestRetrieve, node, requestListener);
  }

  private static void storeUserInfo(UserInfo user, Node node) throws LineUnavailableException {
    UserInfoRequest requestStore = new UserInfoRequest(user, RequestType.STORE);
    RequestHandler.handleRequest(requestStore, node);
  }

}
