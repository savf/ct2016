package ch.uzh.csg.p2p.controller;

import java.awt.Color;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.sound.sampled.LineUnavailableException;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.screens.MainWindow;

public class FriendlistPaneController {

  private Node node;
  private MainWindowController mainWindowController;

  @FXML
  private TextField friendSearchText;
  @FXML
  private VBox friendlist;
  @FXML
  private VBox searchResultList;
  
  private Label currentChatLabel;

  public FriendlistPaneController(Node node, MainWindowController mainWindowController) {
    this.node = node;
    this.mainWindowController = mainWindowController;
    currentChatLabel = null;
  }

  public void sendFriendRequest(User user, Node node) {
    FriendRequest request =
        new FriendRequest(node.getUser().getPeerAddress(), node.getUser().getUsername(),
            user.getUsername(), RequestType.SEND);
    RequestHandler.handleRequest(request, node);
  }

  @FXML
  public void searchFriendHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    Platform.runLater(new Runnable() {
        public void run() {
          mainWindowController.showFriendSearchResultPane();
          searchResultList.getChildren().clear();           
            try {
              final User user = FriendlistHelper.findUser(node, friendSearchText.getText());
              if (user != null) {
                HBox hBox = new HBox();
                hBox.setSpacing(40);

                Label label = new Label(user.getUsername());
                label.getStyleClass().add("label");
                hBox.getChildren().add(label);
                boolean alreadyFriend = true;
                try {
                  alreadyFriend = checkAlreadyFriend(user.getUsername());
                } catch (UnsupportedEncodingException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
                Button button = new Button("Send friend request");
                button.getStyleClass().add("btn");
                button.getStyleClass().add("friendRequestBtn");
                button.setOnAction(new EventHandler<ActionEvent>() {

                  public void handle(ActionEvent event) {
                    sendFriendRequest(user, node);
                    searchResultList.getChildren().clear();
                    mainWindowController.showInfoPane();
                  }
                });
                if (alreadyFriend) {
                  button.setDisable(true);
                  button.setVisible(false);
                }
                hBox.getChildren().add(button);

                searchResultList.getChildren().add(hBox);
              }
              friendSearchText.setText(""); 
            } catch (ClassNotFoundException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            } catch (IOException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            } catch (LineUnavailableException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
      }
  });
  }

  public void addUserToFriendList(Friend friend) {
    final Friend f = friend;
    Platform.runLater(new Runnable() {
      public void run() {
    HBox hBox = new HBox();
    hBox.setSpacing(40);

    Label label = new Label(f.getName());
    label.getStyleClass().add("friendLabel");
    label.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

      public void handle(MouseEvent event) {
        if(currentChatLabel != null){
          currentChatLabel.getStyleClass().remove("friendLabelBright");
          currentChatLabel.getStyleClass().add("friendLabel");
          mainWindowController.deleteChatPartners();
        }
        Label label = (Label) event.getSource();
        label.getStyleClass().remove("friendLabel");
        label.getStyleClass().add("friendLabelBright");
        currentChatLabel = label;
        mainWindowController.addChatPartner(label.getText());
        mainWindowController.showChatPane();
        }
    });
    
    hBox.getChildren().add(label);

    friendlist.getChildren().add(label);
      }
    });
  }

  protected void rejectFriendship(FriendRequest r) {
    final FriendRequest req = r;
    Platform.runLater(new Runnable() {
      public void run() {
    mainWindowController.setMainPaneTop(null);
    FriendRequest request =
        new FriendRequest(node.getPeer().peerAddress(), node.getUser().getUsername(),
            req.getSenderName(), RequestType.SEND);
    request.setStatus(RequestStatus.REJECTED);
    RequestHandler.handleRequest(request, node);
      }
    });
  }

  protected void acceptFriendship(FriendRequest r) {
    final FriendRequest req = r;
    Platform.runLater(new Runnable() {
      public void run() {
    mainWindowController.setMainPaneTop(null);
    FriendRequest request =
        new FriendRequest(node.getPeer().peerAddress(), node.getUser().getUsername(),
            req.getSenderName(), RequestType.SEND);
    request.setReceiverAddress(req.getSenderPeerAddress());
    request.setStatus(RequestStatus.ACCEPTED);
    RequestHandler.handleRequest(request, node);
    Friend friend = new Friend(req.getSenderPeerAddress(), req.getSenderName());
    storeFriend(friend);
    addUserToFriendList(friend);
    node.addFriend(friend);
      }
    });
  }

  private void storeFriend(Friend f) {
    FriendRequest r = new FriendRequest(f.getPeerAddress(), f.getName(), null, RequestType.STORE);
    RequestHandler.handleRequest(r, node);
  }

  public void friendshipRejected(Friend f) {
    Platform.runLater(new Runnable() {
      public void run() {
        mainWindowController.setRightPaneTop(null);
      }
    });
    mainWindowController.friendshipRejected(f.getName());
  }

  public void friendshipAccepted(Friend f) {
    final Friend friend = f;
    Platform.runLater(new Runnable() {
      public void run() {
        mainWindowController.setMainPaneTop(null);
        storeFriend(friend);
        addUserToFriendList(friend);
        node.addFriend(friend);
      }
    });
    mainWindowController.friendshipAccepted(f.getName());
  }

  private boolean checkAlreadyFriend(String username) throws UnsupportedEncodingException {
    for (String n : node.getUser().getFriendStorage()) {
      if ((username).equals(n)) {
        return true;
      }
    }
    return false;
  }


  @FXML
  public void leaveChatHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.showInfoPane();
  }
  
  public void selectChatPartner(){
    
  }


}
