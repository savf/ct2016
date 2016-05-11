package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.ChatHelper;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MainWindowController {

	private Logger log = LoggerFactory.getLogger(MainWindowController.class);

	/*
	 * Audio request dialog
	 */
	@FXML
	private Label requestWindowLabel;
	@FXML
	private Button requestWindowAcceptBtn;
	@FXML
	private Button requestWindowRejectBtn;


	public Node node;
	public User user;
	private ChatHelper chatHelper;
	private FriendlistHelper friendlistHelper;
	public List<String> currentChatPartners;
	public Thread audioRingingThread;
	public Thread videoRingingThread;

	private MainWindow mainWindow;
	private BorderPane mainPane;
	private BorderPane rightPane;
	private AnchorPane infoPane;
	private AnchorPane chatPane;
	private AnchorPane audioPane;
	private AnchorPane videoPane;
	private AnchorPane friendlistPane;
	private AnchorPane friendsearchResultPane;
	private AnchorPane requestPane;

	public ChatPaneController chatPaneController;
	public AudioPaneController audioPaneController;
	public VideoPaneController videoPaneController;
	public FriendlistPaneController friendlistPaneController;

	public MainWindowController(Node node) {
		this.node = node;
		this.chatHelper = new ChatHelper();
		this.currentChatPartners = new ArrayList<String>();
		this.friendlistHelper = new FriendlistHelper(this.node);
	}

	public void initialiseFriendlist(Node node) {
		for (Friend f : node.getFriendList()) {
			friendlistPaneController.addUserToFriendList(f);
		}
	}

	/*
	 * SETUP
	 */

	public void setUser(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		user = LoginHelper.retrieveUser(username, node);
	}

	public void setChatPaneController(ChatPaneController chatPaneController) {
		this.chatPaneController = chatPaneController;
	}

	public void setAudioPaneController(AudioPaneController audioPaneController) {
		this.audioPaneController = audioPaneController;
	}

	public void setVideoPaneController(VideoPaneController videoPaneController) {
		this.videoPaneController = videoPaneController;
	}

	public void setFriendlistPaneController(FriendlistPaneController friendlistPaneController) {
		this.friendlistPaneController = friendlistPaneController;
	}

	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void setMainPane(BorderPane mainPane) {
		System.out.println("setMainPane");
		this.mainPane = mainPane;
	}

	public void setMainPaneTop(AnchorPane anchorPane) {
		mainPane.setTop(anchorPane);
	}

	public void setRightPane(BorderPane rightPane) {
		this.rightPane = rightPane;
	}

	public void setRightPaneTop(AnchorPane anchorPane) {
		rightPane.setTop(anchorPane);
	}

	public void setInfoPane(AnchorPane infoPane) {
		this.infoPane = infoPane;
	}

	public void setChatPane(AnchorPane chatPane) {
		this.chatPane = chatPane;
	}

	public void setAudioPane(AnchorPane audioPane) {
		this.audioPane = audioPane;
	}

	public void setVideoPane(AnchorPane videoPane) {
		this.videoPane = videoPane;
	}

	public void setFriendlistPane(AnchorPane friendlistPane) {
		this.friendlistPane = friendlistPane;
	}

	public void setFriendsearchResultPane(AnchorPane friendsearchResultPane) {
		this.friendsearchResultPane = friendsearchResultPane;
	}

	public void setRequestPane(AnchorPane requestPane) {
		this.requestPane = requestPane;
	}

	public void addChatPartner(String username) {
		if (!currentChatPartners.contains(username)
				&& friendlistHelper.checkAlreadyFriend(username)) {
			currentChatPartners.add(username);
		}
	}
	
	public void clearChatPartners() {
		currentChatPartners.clear();
	}

	/*
	 * NOTIFICATIONS
	 */

	public void showInfoPane() {
		rightPane.setTop(infoPane);
		rightPane.setBottom(null);
	}


	/*
	 * AUDIO PART
	 */

	public void showAudioPane() {
		rightPane.setTop(audioPane);
		showChatBtns("audio");
	}

	public void hideAudioPane() {
		rightPane.setTop(null);
		showChatBtns("chat");
	}

	public void showAudioAndChatPanes() {
		rightPane.setBottom(chatPane);
		rightPane.setTop(audioPane);
		showChatBtns("audio");
	}

	public void makeAudioCallDialog(final String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				mainPane.setTop(requestPane);
				requestWindowLabel
						.setText("Do you want to start an audio call with " + username + "?");
			}
		});
		requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					audioPaneController.acceptAudioCall(username);
				} catch (Exception e) {
					log.error("Cannot accept audio call: " + e);
				}
				audioRingingThread.stop();
			}
		});
		requestWindowRejectBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					audioPaneController.rejectAudioCall(username);
				} catch (Exception e) {
					log.error("Cannot reject audio call: " + e);
				}
				audioRingingThread.stop();
			}
		});

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ring.mp3");
		final Player player;
		try {
			player = new Player(inputStream);

			audioRingingThread = new Thread(new Runnable() {
				public void run() {
					try {
						player.play();
					} catch (JavaLayerException e) {
						e.printStackTrace();
					}

				}
			});
			audioRingingThread.start();
		} catch (JavaLayerException e1) {
			e1.printStackTrace();
		}
	}

	/*
	 * VIDEO PART
	 */

	public void showVideoPane() {
		rightPane.setTop(videoPane);
		showChatBtns("video");
	}

	public void hideVideoPane() {
		rightPane.setTop(null);
		showChatBtns("chat");
	}

	public void showVideoAndChatPanes() {
		rightPane.setBottom(chatPane);
		rightPane.setTop(videoPane);
		showChatBtns("video");
	}

	public void makeVideoCallDialog(final String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				mainPane.setTop(requestPane);
				requestWindowLabel
						.setText("Do you want to start a video call with " + username + "?");
			}
		});
		requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					videoPaneController.acceptVideoCall(username);
					videoRingingThread.stop();
				} catch (Exception e) {
					log.error("Cannot accept video call: " + e);
					e.printStackTrace();
				}
			}
		});
		requestWindowRejectBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					videoPaneController.rejectVideoCall(username);
					videoRingingThread.stop();
				} catch (Exception e) {
					log.error("Cannot reject video call: " + e);
				}
			}
		});

		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ring.mp3");
		final Player player;
		try {
			player = new Player(inputStream);

			videoRingingThread = new Thread(new Runnable() {
				public void run() {
					try {
						player.play();
					} catch (JavaLayerException e) {
						e.printStackTrace();
					}

				}
			});
			videoRingingThread.start();
		} catch (JavaLayerException e1) {
			e1.printStackTrace();
		}
	}

	/*
	 * FRIENDLIST
	 */

	public void showFriendSearchResultPane() {
		rightPane.setTop(friendsearchResultPane);
	}

	public void showChatPane() {
		rightPane.setBottom(chatPane);
	}

	public void askFriend(FriendRequest request) {
		final FriendRequest r = request;
		Platform.runLater(new Runnable() {
			public void run() {

				mainPane.setTop(requestPane);
				requestWindowLabel
						.setText("Do you want to be friends with " + r.getSenderName() + "?");
			}
		});
		requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					friendlistPaneController.acceptFriendship(r);
				} catch (Exception e) {
					log.error("Cannot accept friendship request: " + e);
				}
			}
		});
		requestWindowRejectBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					friendlistPaneController.rejectFriendship(r);
				} catch (Exception e) {
					log.error("Cannot reject friendship request: " + e);
				}
			}
		});
	}

	public void friendshipAccepted(final String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				// TODO: InformationPane with only OK as button
				mainPane.setTop(infoPane);
				requestWindowLabel.setText(username + " accepted your friendship request.");
			}
		});
		requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				setMainPaneTop(null);
			}
		});
	}

	public void friendshipRejected(final String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				// TODO: InformationPane with only OK as button
				mainPane.setTop(infoPane);
				requestWindowLabel.setText(username + " rejected your friendship request.");
			}
		});
		requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				setMainPaneTop(null);
			}
		});
	}

	/*
	 * shows buttons list for which pane (chat, audio, video), null for not show buttons somewhere
	 */
	public void showChatBtns(String pane) {
		if (pane == null) {
			chatPaneController.btnWrapperChat.setVisible(false);
			audioPaneController.btnWrapperAudio.setVisible(false);
			videoPaneController.btnWrapperVideo.setVisible(false);
		} else if (pane.equals("chat")) {
			chatPaneController.btnWrapperChat.setVisible(true);
			audioPaneController.btnWrapperAudio.setVisible(false);
			videoPaneController.btnWrapperVideo.setVisible(false);
		} else if (pane.equals("audio")) {
			chatPaneController.btnWrapperChat.setVisible(false);
			audioPaneController.btnWrapperAudio.setVisible(true);
			videoPaneController.btnWrapperVideo.setVisible(false);
		} else if (pane.equals("video")) {
			chatPaneController.btnWrapperChat.setVisible(false);
			audioPaneController.btnWrapperAudio.setVisible(false);
			videoPaneController.btnWrapperVideo.setVisible(true);
		}
	}

}
