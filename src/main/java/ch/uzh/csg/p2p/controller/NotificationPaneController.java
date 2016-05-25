package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.ChatHelper;
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
	private Set<String> missedFriendRequestsFrom;
	private ListChangeListener<FriendRequest> friendRequestListener;
	private ListChangeListener<ChatMessage> chatMessageListener;
	private HashMap<ChatMessage, MissedItemController> missedChatMessages =
			new HashMap<ChatMessage, MissedItemController>();

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

	public NotificationPaneController(Node node, MainWindowController mainWindowController) {
		setNode(node);
		setMainWindowController(mainWindowController);
		missedFriendRequestsFrom = new HashSet<String>();
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
					awayFriendRequestCountLabel.setText(Integer.toString(list.size()));
					for (FriendRequest r : list) {
						if (!missedFriendRequestsFrom.contains(r.getSenderName())) {
							missedFriendRequestsFrom.add(r.getSenderName());



							FXMLLoader loader = new FXMLLoader(
									MainWindow.class.getResource("MissedMessageItem.fxml"));
							MissedItemController missedItemController = new MissedItemController();
							loader.setController(missedItemController);

							EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>() {

								@Override
								public void handle(MouseEvent event) {
									missedFriendRequestsFrom.remove(r.getSenderName());
									RequestHandler.handleRequest(r, node);
									missedItemController.removeMyself();
									awayFriendRequestCountLabel.setText(
											Integer.toString(missedFriendRequestsFrom.size()));
								}

							};

							try {
								AnchorPane missedMessageItem = (AnchorPane) loader.load();
								missedItemController.setMessage(r.getSenderName());
								missedItemController.setClickHandler(clickHandler);
								awayFriendRequestVBox.getChildren().add(missedMessageItem);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

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
						if (!missedChatMessages.containsKey(m)) {
							try {
								mainWindowController.chatPaneController
										.addReceivedMessage(m.getSenderID(), m.getData());
							} catch (IOException e1) {
								e1.printStackTrace();
							}

							EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>() {

								@Override
								public void handle(MouseEvent event) {
									awayMessageCountLabel.setText(Integer.toString(
											Integer.parseInt(awayMessageCountLabel.getText()) - 1));
									mainWindowController.chatPaneController
											.startChatSessionWith(m.getSenderID());
									mainWindowController.showChatPane();
									removeChatMessagesFrom(m.getSenderID());
								}

							};

							FXMLLoader loader = new FXMLLoader(
									MainWindow.class.getResource("MissedMessageItem.fxml"));
							MissedItemController missedItemController = new MissedItemController();
							loader.setController(missedItemController);
							missedChatMessages.put(m, missedItemController);
							try {
								AnchorPane missedMessageItem = (AnchorPane) loader.load();
								missedItemController
										.setMessage(m.getSenderID() + ": " + m.getData());
								missedItemController.setDateTime(m.getDate());
								missedItemController.setClickHandler(clickHandler);
								awayMessageVBox.getChildren().add(missedMessageItem);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}

			});
		}
	}

	private void removeChatMessagesFrom(String sender) {
		for (Iterator<Map.Entry<ChatMessage, MissedItemController>> iterator =
				missedChatMessages.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<ChatMessage, MissedItemController> entry = iterator.next();
			if (entry.getKey().getSenderID().equals(sender)) {
				entry.getValue().removeMyself();
				iterator.remove();
				ChatHelper.removeStoredMessageFrom(sender, entry.getKey().getReceiverID(),
						entry.getKey().getDate(), node);
			}
		}
		node.getUser().removeMessagesFromUser(sender);

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
