package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class NotificationPaneController {

	private Logger log = LoggerFactory.getLogger(MainWindowController.class);
	private Node node;
	private MainWindowController mainWindowController;
	private int friendRequestCounter = 0;
	private ListChangeListener<FriendRequest> friendRequestListener;
	private ListChangeListener<ChatMessage> chatMessageListener;

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

	private NotificationPaneController() {
		setNode(null);
		setMainWindowController(null);
	}

	public NotificationPaneController(Node node, MainWindowController mainWindowController) {
		setNode(node);
		setMainWindowController(mainWindowController);
		friendRequestListener = new ListChangeListener<FriendRequest>() {
			public void onChanged(ListChangeListener.Change change) {
				startFriendRequestWhileAway(node.getRequestsWhileAway());
			}
		};
		node.registerForFriendRequestWhileAwayUpdates(friendRequestListener);

		chatMessageListener = new ListChangeListener<ChatMessage>() {

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends ChatMessage> c) {
				showMissedChatMessages(node.getUser().getChatMessageStorage());
			}

		};
		node.getUser().registerForChatMessageUpdates(chatMessageListener);
	}

	private void startFriendRequestWhileAway(List<FriendRequest> list) {
		if (list != null && !list.isEmpty()) {
			Platform.runLater(new Runnable() {
				public void run() {
					friendRequestCounter = list.size();
					awayFriendRequestCountLabel.setText(Integer.toString(friendRequestCounter));
					for (FriendRequest r : list) {
						Label label = new Label(r.getSenderName());
						label.getStyleClass().add("label");
						label.setOnMouseClicked(new EventHandler<MouseEvent>() {

							@Override
							public void handle(MouseEvent arg0) {
								friendRequestCounter--;
								RequestHandler.handleRequest(r, node);
								awayFriendRequestVBox.getChildren().remove(label);
								awayFriendRequestCountLabel
										.setText(Integer.toString(friendRequestCounter));
							}

						});
						awayFriendRequestVBox.getChildren().add(label);
					}
				}
			});
		}
	}

	private void showMissedChatMessages(List<ChatMessage> list) {
		if (list != null && !list.isEmpty()) {
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					awayMessageCountLabel.setText(Integer.toString(list.size()));
					for (ChatMessage m : list) {
						try {
							mainWindowController.chatPaneController
									.addReceivedMessage(m.getSenderID(), m.getData());
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						Function<String, Void> externalHandler = new Function<String, Void>() {

							@Override
							public Void apply(String sender) {
								awayMessageCountLabel.setText(Integer.toString(
										Integer.parseInt(awayMessageCountLabel.getText()) - 1));
								node.getUser().removeMessagesFromUser(sender);
								mainWindowController.chatPaneController
										.startChatSessionWith(sender);
								return null;
							}

						};

						FXMLLoader loader = new FXMLLoader(
								MainWindow.class.getResource("MissedMessageItem.fxml"));
						MissedItemController missedItemController =
								new MissedItemController(m.getSenderID(), externalHandler);
						loader.setController(missedItemController);
						missedItemController.setMessage(m.getData());
						missedItemController.setDateTime(m.getDate());
						try {
							AnchorPane missedMessageItem = (AnchorPane) loader.load();
							awayMessageVBox.getChildren().add(missedMessageItem);
						} catch (IOException e) {
							e.printStackTrace();
						}
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
