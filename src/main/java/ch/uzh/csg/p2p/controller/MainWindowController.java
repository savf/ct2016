package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

import javax.sound.sampled.LineUnavailableException;

import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.BootstrapNode;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class MainWindowController {

	@FXML
	private TextField messageText;
	@FXML
	private TextArea chatText;
	@FXML
	private TextField friendSearchText;
	@FXML
	private VBox friendlist;
	@FXML
	private VBox searchResultList;

	private Node node;
	private MainWindow mainWindow;

	private BorderPane mainPane;
	private BorderPane rightPane;
	private AnchorPane infoPane;
	private AnchorPane chatPane;
	private AnchorPane friendlistPane;
	private AnchorPane friendsearchResultPane;

	private String currentChatPartner;

	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void setMainPane(BorderPane mainPane) {
		this.mainPane = mainPane;
	}

	public void setRightPane(BorderPane rightPane) {
		this.rightPane = rightPane;
	}

	public void setInfoPane(AnchorPane infoPane) {
		this.infoPane = infoPane;
	}

	public void setChatPane(AnchorPane chatPane) {
		this.chatPane = chatPane;
	}

	public void setFriendlistPane(AnchorPane friendlistPane) {
		this.friendlistPane = friendlistPane;
	}

	public void setFriendsearchResultPane(AnchorPane friendsearchResultPane) {
		this.friendsearchResultPane = friendsearchResultPane;
	}

	public void startNode(int id, String ip, String username, String password)
			throws IOException, LineUnavailableException, ClassNotFoundException {
			node = new Node(id, ip, username, password, this);
	}

	@FXML
	public void handleSendMessage() {	  
		if (messageText.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Empty informations");
			String s = "Message cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		} else {
		  String sender = node.getUser().getUsername();
	      String receiver = currentChatPartner;
	      String message = messageText.getText();
	      
	      Date date = new Date();
	      ChatMessage m = new ChatMessage(sender, receiver, date, message);
	      
	      MessageRequest request = new MessageRequest(m, RequestType.SEND);
	      // TODO: change to a fitting Date format!
	      DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
	      RequestHandler.handleRequest(request, node);
			chatText.setText(chatText.getText() + "\n" +"[" + f.format(date) +"] ME: " + message);
			messageText.setText("");
		}
	}

	@FXML
	public void searchFriendHandler() throws ClassNotFoundException, IOException {
		rightPane.setTop(friendsearchResultPane);
		final User user = FriendlistHelper.findUser(node, friendSearchText.getText());
		if (user != null) {
			HBox hBox = new HBox();
			hBox.setSpacing(40);

			Label label = new Label(user.getUsername());
			label.getStyleClass().add("label");
			hBox.getChildren().add(label);

			Button button = new Button("Send friend request");
			button.getStyleClass().add("btn");
			button.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent event) {
					addUserToFriendList(user);
					searchResultList.getChildren().clear();
					rightPane.setTop(infoPane);
				}
			});
			hBox.getChildren().add(button);

			searchResultList.getChildren().add(hBox);
		}
	}

	private void addUserToFriendList(User user) {
		HBox hBox = new HBox();
		hBox.setSpacing(40);

		Label label = new Label(user.getUsername());
		label.getStyleClass().add("label");
		label.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				Label label = (Label) event.getSource();
				currentChatPartner = label.getText();
				rightPane.setBottom(chatPane);
			}
		});
		hBox.getChildren().add(label);

		friendlist.getChildren().add(label);
	}
	
	public Object handleReceiveMessage(PeerAddress peerAddress, Object object){
      Message m = (Message) object;
      if(m instanceof ChatMessage){
        ChatMessage chatMessage = (ChatMessage) m;
        addReceivedMessage(chatMessage.getSenderID(), chatMessage.getData(), chatMessage.getDate());
      }
      else{
        //TODO!
      }
  
      return 0;
	}

	private void addReceivedMessage(String sender, String message, Date date) {
	  //TODO: Change to a fitting date format
		DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
	  chatText.setText(chatText.getText() + "\n" +"[" + f.format(date) +"] "+ sender + ": " + message);
	}

	public void shutdownNode() {
		if (node != null) {
			node.shutdown();
		}
		System.exit(0);
	}

}
