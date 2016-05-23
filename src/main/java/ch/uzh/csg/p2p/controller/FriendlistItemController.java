package ch.uzh.csg.p2p.controller;

import java.util.Observable;
import java.util.Observer;

import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.OnlineStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class FriendlistItemController implements Observer {

  private MainWindowController mainWindowController;
  private int unreadMessageCount = 0;

  @FXML
  public AnchorPane friendlistItemWrapper;

  @FXML
  public Label friendName;

  @FXML
  public Label unreadMessageLabel;

  @FXML
  private Circle onlineStatus;

  public FriendlistItemController(MainWindowController mwc) {
    mainWindowController = mwc;
  }

  public void setOnline() {
    onlineStatus.setFill(Color.GREEN);
  }

  public void setOffline() {
    onlineStatus.setFill(Color.RED);
  }

  private void setStatus(OnlineStatus status) {
    switch (status) {
      case OFFLINE:
        setOffline();
        break;
      case ONLINE:
        setOnline();
        break;
      default:
        break;
    }
  }

  public void setName(String name) {
    friendName.setText(name);
  }

  public void newUnreadMessage() {
    unreadMessageCount++;
    unreadMessageLabel.setVisible(true);
    unreadMessageLabel.setText(Integer.toString(unreadMessageCount));
  }

  public void clearUnreadMessages() {
    unreadMessageCount = 0;
    unreadMessageLabel.setVisible(false);
  }

  @FXML
  public void friendClickHandler() {
    String username = friendName.getText();
    mainWindowController.chatPaneController.startChatSessionWith(username);
    mainWindowController.showChatPane();
  }

  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof Friend) {
      Friend f = (Friend) o;
      OnlineStatus status = (OnlineStatus) arg;
      setStatus(status);
    }
  }

}
