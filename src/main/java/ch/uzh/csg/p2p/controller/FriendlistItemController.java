package ch.uzh.csg.p2p.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class FriendlistItemController {

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

}
