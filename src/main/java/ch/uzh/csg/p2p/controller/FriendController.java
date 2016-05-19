package ch.uzh.csg.p2p.controller;

import javafx.scene.paint.Color;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.scene.layout.HBox;

public class FriendController {
  
  private MainWindowController mainWindowController;
  
  @FXML
  public Label friendName;

  @FXML
  private Circle onlineStatus;
  
  @FXML
  public HBox friendWrapper;
  
  public FriendController(MainWindowController mwc) {
    mainWindowController = mwc;
  }
  
  public void setOnline(){
    onlineStatus.setFill(Color.GREEN);
  }
  
  public void setOffline(){
    onlineStatus.setFill(Color.RED);
  }
 
  public void setName(String name){
    friendName.setText(name);
  }
  
  @FXML
  public void handleClickOnFriend(){
    String username = friendName.getText();
    mainWindowController.chatPaneController.startChatSessionWith(username);
    mainWindowController.showChatPane();
  }
  
}
