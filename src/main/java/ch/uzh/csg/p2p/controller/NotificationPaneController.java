package ch.uzh.csg.p2p.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;

public class NotificationPaneController {

	private Logger log = LoggerFactory.getLogger(MainWindowController.class);
	private Node node;
	private MainWindowController mainWindowController;
	private ListChangeListener<FriendRequest> listener;
	private Set<String> friendRequestsFromWhileAway;

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
		friendRequestsFromWhileAway = new HashSet<String>();
		listener = new ListChangeListener<FriendRequest>() {
			public void onChanged(ListChangeListener.Change change) {
				initializeNotificationPane(node);
			}
		};
		node.registerForFriendRequestWhileAwayUpdates(listener);
	}

	public void initializeNotificationPane(Node node) {
		startFriendRequestWhileAway(node.getRequestsWhileAway());
	}

	private void startFriendRequestWhileAway(List<FriendRequest> list) {
		if (list != null && !list.isEmpty()) {
			Platform.runLater(new Runnable() {
				public void run() {
					awayFriendRequestCountLabel.setText(Integer.toString(list.size()));
					for (FriendRequest r : list) {
						if (!friendRequestsFromWhileAway.contains(r.getSenderName())) {

							Label label = new Label(r.getSenderName());
							label.getStyleClass().add("label");
							label.setOnMouseClicked(new EventHandler<MouseEvent>() {

								@Override
								public void handle(MouseEvent arg0) {
									friendRequestsFromWhileAway.remove(r.getSenderName());
									RequestHandler.handleRequest(r, node);
									awayFriendRequestVBox.getChildren().remove(label);
									awayFriendRequestCountLabel.setText(Integer
											.toString(friendRequestsFromWhileAway.size()));
								}

							});
							awayFriendRequestVBox.getChildren().add(label);
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
