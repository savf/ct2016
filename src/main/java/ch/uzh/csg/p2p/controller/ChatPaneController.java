package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.ChatHelper;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ChatPaneController {

	private Node node;
	private MainWindowController mainWindowController;

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
	}
	
	@FXML
	public void initialize() {
		messagesVBox.heightProperty().addListener(new ChangeListener<Number>(){

			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue) {
				messagesScrollPane.setVvalue( 1.0d );
			}
			
		});
	}

	@FXML
	public void leaveChatHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showInfoPane();

		messagesVBox.getChildren().clear();
		messageText.clear();
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
	public void handleSendMessage() throws LineUnavailableException, IOException {
		if (messageText.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Empty informations");
			String s = "Message cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		} else {
			ChatHelper.sendMessage(this.node, messageText.getText(),
					mainWindowController.currentChatPartners);
			addChatBubble(messageText.getText(), "", true);
		}
	}

	@FXML
	public void enterKeyHandler() throws LineUnavailableException, IOException {
		handleSendMessage();
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

				chatBubbleController.setMessage(message, messagesScrollPane.getWidth());
				if (!fromMe) {
					chatBubbleController.setSender(sender);
				}
				chatBubbleController.setDateTime();
				messagesVBox.getChildren().add(chatBubble);
				messageText.setText("");
				messagesScrollPane.setVvalue(messagesVBox.getHeight());
			}
		});
	}

	public void addReceivedMessage(String sender, String message) throws IOException {
		addChatBubble(message, sender, false);
	}

}
