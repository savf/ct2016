package ch.uzh.csg.p2p.controller;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.BootstrapNode;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.model.User;
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
	@FXML
	private HBox btnWrapperChat;

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
		if (ip == null) {
			node = new BootstrapNode(id, ip, username, password, this);
		} else {
			node = new Node(id, ip, username, password, this);
		}
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
			node.sendMessageToAddress(currentChatPartner, messageText.getText());
			chatText.setText(chatText.getText() + "\n" + "ME: " + messageText.getText());
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
			button.getStyleClass().add("friendRequestBtn");
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
	
	@FXML
	public void leaveChatHandler(){
		rightPane.setTop(infoPane);
		rightPane.setBottom(null);
	}
	
	@FXML
	public void addUserHandler(){
		//TODO
	}
	
	@FXML
	public void startAudioHandler(){
		//TODO
	}
	
	@FXML
	public void startVideoHandler(){
		//TODO
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

	public void addReceivedMessage(String sender, String message) {
		chatText.setText(chatText.getText() + "\n" + sender + ": " + message);
	}

	public void shutdownNode() {
		if (node != null) {
			node.shutdown();
		}
		System.exit(0);
	}
	
	/*
	 * shows buttons list for which pane (chat, audio, video), null for not show buttons somewhere
	 */
	public void showChatBtns(String pane){
		if(pane == null){
			btnWrapperChat.setVisible(false);
		}else if(pane.equals("chat")){
			btnWrapperChat.setVisible(true);
		}
	}

}
