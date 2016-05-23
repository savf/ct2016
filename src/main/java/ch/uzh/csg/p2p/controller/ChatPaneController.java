package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.ChatHelper;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatPaneController {

  private Node node;
  private MainWindowController mainWindowController;
  private HashMap<String, List<AnchorPane>> chatHistories;

  @FXML
  private Label chatPartnerLbl;
  @FXML
  private TextField messageText;
  @FXML
  private VBox messagesVBox;
  @FXML
  private ScrollPane messagesScrollPane;
  @FXML
  public HBox btnWrapperChat;

  public ChatPaneController(Node node, MainWindowController mainWindowController) {
    this.node = node;
    this.mainWindowController = mainWindowController;
    this.chatHistories = new HashMap<String, List<AnchorPane>>();
  }

  @FXML
  public void initialize() {
    messagesVBox.heightProperty().addListener(new ChangeListener<Number>() {

      public void changed(ObservableValue<? extends Number> observable, Number oldValue,
          Number newValue) {
        messagesScrollPane.setVvalue(1.0d);
      }

    });
  }

  @FXML
  public void leaveChatHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.showNotificationPane();

    chatPartnerLbl.setText("Nobody");
    messagesVBox.getChildren().clear();
    messageText.clear();
    mainWindowController.currentChatPartners.clear();
  }

  @FXML
  public void addUserHandler() {
    // TODO: Implement
  }

  @FXML
  public void startAudioHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.audioPaneController.startAudioHandler();
  }

  @FXML
  public void startVideoHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.videoPaneController.startVideoHandler();
  }

  @FXML
  public void handleSendMessage() throws LineUnavailableException, IOException {
    if (messageText.getText().equals("")) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Empty informations");
      String s = "Message cannot be empty.";
      alert.setContentText(s);
      alert.showAndWait();
    } else {
      String message = messageText.getText();
      ChatHelper.sendMessage(this.node, message, mainWindowController.currentChatPartners);
      addChatBubble(message, "", true);
      messageText.setText("");
    }
  }

  public void startChatSessionWith(String username) {
    mainWindowController.clearChatPartners();
    mainWindowController.addChatPartner(username);
    mainWindowController.friendlistPaneController.friendlistItemControllerList.get(username)
        .clearUnreadMessages();
    chatPartnerLbl.setText(username);
    List<AnchorPane> chatHistory = this.chatHistories.get(username);
    messagesVBox.getChildren().clear();
    if (chatHistory != null) {
      messagesVBox.getChildren().addAll(chatHistory);
    }
  }

  private void addChatBubble(final String message, final String sender, final boolean fromMe) {
    Platform.runLater(new Runnable() {
      public void run() {
        FXMLLoader loader;
        if (fromMe) {
          loader = new FXMLLoader(MainWindow.class.getResource("ChatBubbleMe.fxml"));
        } else {
          loader = new FXMLLoader(MainWindow.class.getResource("ChatBubblePeer.fxml"));
        }
        ChatBubbleController chatBubbleController = new ChatBubbleController();
        loader.setController(chatBubbleController);
        AnchorPane chatBubble = new AnchorPane();
        try {
          chatBubble = loader.load();
        } catch (IOException e) {
          e.printStackTrace();
        }

        chatBubbleController.setMessage(message);
        chatBubbleController.setBackground(fromMe);
        if (!fromMe) {
          chatBubbleController.setSender(sender);
        }
        chatBubbleController.setDateTime();

        if (mainWindowController.currentChatPartners.size() <= 1) {
          String currentChatPartner =
              fromMe ? mainWindowController.currentChatPartners.get(0) : sender;
          if (!chatHistories.containsKey(currentChatPartner)) {
            chatHistories.put(currentChatPartner, new ArrayList<AnchorPane>());
          }
          chatHistories.get(currentChatPartner).add(chatBubble);
        }

        if (fromMe || mainWindowController.currentChatPartners.contains(sender)) {
          messagesVBox.getChildren().add(chatBubble);
        } else {
          mainWindowController.friendlistPaneController.friendlistItemControllerList.get(sender)
              .newUnreadMessage();
        }
      }
    });
  }

  public void addReceivedMessage(String sender, String message) throws IOException {
    addChatBubble(message, sender, false);
  }

}
