package ch.uzh.csg.p2p.controller;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.BootstrapNode;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;

public class MainWindowController {

	@FXML
	private TextField usernameText;
	@FXML
	private TextField messageText;
	@FXML
	private TextArea chatText;

	private Node node;
	private MainWindow mainWindow;

	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
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
		if (usernameText.getText().equals("") || messageText.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Empty informations");
			String s = "Username and message cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		} else {
			node.sendMessageToAddress(usernameText.getText(), messageText.getText());
			chatText.setText(chatText.getText() + "\n" + "ME: " + messageText.getText());
			usernameText.setText("");
			messageText.setText("");
		}
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

}
