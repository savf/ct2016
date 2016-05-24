package ch.uzh.csg.p2p.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tomp2p.dht.PeerDHT;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;

public class NotificationPaneController {

  private Logger log = LoggerFactory.getLogger(MainWindowController.class);
  private Node node;
  private MainWindowController mainWindowController;
  private int friendRequestCounter = 0;
  private ListChangeListener<FriendRequest> listener;
  
  @FXML
  private Label awayMessageCountLabel;
  @FXML
  private VBox awayMessageVBox;
  
  @FXML
  private Label awayCallCountLabel;
  @FXML
  private VBox awayMissedCallVBox;
  
  @FXML
  private Label awayFriendRequestCountLabel; 
  @FXML
  private VBox awayFriendRequestVBox;
  
  private NotificationPaneController(){
    setNode(null);
    setMainWindowController(null);
  }
  
  public NotificationPaneController(Node node, MainWindowController mainWindowController){
    setNode(node);
    setMainWindowController(mainWindowController);
    listener = new ListChangeListener<FriendRequest>() {
      public void onChanged(ListChangeListener.Change change) {
        initializeNotificationPane(node);
      }
    };
    node.registerForFriendRequestWhileAwayUpdates(listener);
  }
  
  public void initializeNotificationPane(Node node){
    startFriendRequestWhileAway(node.getRequestsWhileAway());
  }
  
  private void startFriendRequestWhileAway(List<FriendRequest> list) {
    if(list != null && !list.isEmpty()){
      Platform.runLater(new Runnable() {
        public void run() {
    friendRequestCounter = list.size();
    awayFriendRequestCountLabel.setText(Integer.toString(friendRequestCounter));
    for(FriendRequest r : list){
      Label label = new Label(r.getSenderName());
      label.getStyleClass().add("label");
      label.setOnMouseClicked(new EventHandler<MouseEvent>(){

        @Override
        public void handle(MouseEvent arg0) {
          friendRequestCounter--;
          RequestHandler.handleRequest(r, node);
          awayFriendRequestVBox.getChildren().remove(label);
          awayFriendRequestCountLabel.setText(Integer.toString(friendRequestCounter));      
        }
        
      });
      awayFriendRequestVBox.getChildren().add(label);
    } 
        }
      });
    }
  }

  public Node getNode() {
    return node;
  }
  public void setNode(Node node) {
    this.node = node;
  }
  public MainWindowController getMainWindowController() {
    return mainWindowController;
  }
  public void setMainWindowController(MainWindowController mainWindowController) {
    this.mainWindowController = mainWindowController;
  }
  
  @FXML
  public void showFriendRequest() {
    
  }
  
}
