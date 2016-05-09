package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.ChatHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class ChatPaneController {

	private Node node;
	private MainWindowController mainWindowController;
	
	@FXML
	private TextField messageText;
	@FXML
	private TextArea chatText;
	@FXML
	public HBox btnWrapperChat;
	
	public ChatPaneController(Node node, MainWindowController mainWindowController) {
		this.node = node;
		this.mainWindowController = mainWindowController;
	}
	
	@FXML
	public void leaveChatHandler() throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showInfoPane();

		messageText.clear();
		chatText.clear();
		mainWindowController.currentChatPartners.clear();
	}
	
	@FXML
	public void addUserHandler() {
		// TODO: Implement
	}
	
	@FXML
	public void startAudioHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.audioPaneController.startAudioHandler();
	}
	
	@FXML
	public void startVideoHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.videoPaneController.startVideoHandler();
	}
	
	@FXML
	public void handleSendMessage() throws LineUnavailableException {
		if (messageText.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Empty informations");
			String s = "Message cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		} else {
			ChatHelper.sendMessage(this.node, messageText.getText(),
					mainWindowController.currentChatPartners);
			DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
			chatText.setText(chatText.getText() + "\n" + "[" + f.format(new Date()) + "] ME: "
					+ messageText.getText());
			messageText.setText("");
		}
	}
	
	public void addReceivedMessage(String sender, String message, Date date) {
		  //TODO: Change to bubble messages
		  DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		  chatText.setText(chatText.getText() + "\n" +"[" + f.format(date) +"] "+ sender + ": " + message);
		}
	
}
