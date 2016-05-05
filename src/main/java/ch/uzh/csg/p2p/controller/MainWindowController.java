package ch.uzh.csg.p2p.controller;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.AudioUtils;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.helper.VideoUtils;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.VideoRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class MainWindowController {

	private Logger log = LoggerFactory.getLogger(MainWindowController.class);

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

	/*
	 * Audio request dialog
	 */
	@FXML
	private Label requestWindowLabel;
	@FXML
	private Button requestWindowAcceptBtn;
	@FXML
	private Button requestWindowRejectBtn;

	/*
	 * Audio
	 */
	@FXML
	private HBox btnWrapperAudio;
	@FXML
	private Button muteBTn;
	@FXML
	private Label microphoneLbl;
	@FXML
	private HBox audioUserWrapper;
	@FXML
	private Label audioUser1;

	/*
	 * Video
	 */
	@FXML
	private HBox btnWrapperVideo;
	@FXML
	private Button muteBtnVideo;
	@FXML
	private Button muteVideoBtn;
	@FXML
	private Label microphoneLblAudio;
	@FXML
	private Label cameraLbl;
	@FXML
	private HBox videoUserWrapper;
	@FXML
	private ImageView videoUser1;
	@FXML
	private ImageView meImageView;
	@FXML
	private Button hideMyselfBtn;
	private Boolean cameraMute;
	private VideoUtils videoUtils;

	private Node node;
	private MainWindow mainWindow;
	private User user;

	private BorderPane mainPane;
	private BorderPane rightPane;
	private AnchorPane infoPane;
	private AnchorPane chatPane;
	private AnchorPane audioPane;
	private AnchorPane videoPane;
	private AnchorPane friendlistPane;
	private AnchorPane friendsearchResultPane;
	private AnchorPane requestPane;

	private Boolean microphoneMute;
	private AudioUtils audioUtils;

	private MediaPlayer mediaPlayer;

	private String currentChatPartner;

	public void setUser(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		user = LoginHelper.retrieveUser(username, node);
	}

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

	public void startNode(int id, String ip, String username, String password)
			throws IOException, LineUnavailableException, ClassNotFoundException {
		node = new Node(id, ip, username, password, this);
	}

	@FXML
	public void handleSendMessage()
			throws ClassNotFoundException, IOException, LineUnavailableException {
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
			chatText.setText(chatText.getText() + "\n" + "[" + f.format(date) + "] ME: " + message);
			messageText.setText("");
		}
	}

	@FXML
	public void searchFriendHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		rightPane.setTop(friendsearchResultPane);
		final User user = FriendlistHelper.findUser(node, friendSearchText.getText());
		// TODO: grey out button if User already added to friendlist!
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
					sendFriendRequest(user, node);
					addUserToFriendList(user);
					searchResultList.getChildren().clear();
					rightPane.setTop(infoPane);
				}
			});
			hBox.getChildren().add(button);

			searchResultList.getChildren().add(hBox);
		}
		friendSearchText.setText("");
	}

	@FXML
	public void leaveChatHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		rightPane.setTop(infoPane);
		rightPane.setBottom(null);

		if (audioUtils != null) {
			audioUtils.endAudio();
			AudioRequest request = new AudioRequest(RequestType.ABORTED, RequestStatus.CLOSED,
					currentChatPartner, node.getUser().getUsername());
			RequestHandler.handleRequest(request, node);
			// audioUtils.sendRequest(RequestType.ABORTED, currentChatPartner);
			audioUtils = null;
		}
	}

	@FXML
	public void addUserHandler() {
		// TODO
	}

	/*
	 * AUDIO PART
	 */

	@FXML
	public void startAudioHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		rightPane.setTop(audioPane);
		showChatBtns("audio");
		microphoneMute = false;
		Image image = new Image(MainWindowController.class.getResourceAsStream("/microphone.png"));
		microphoneLbl.setGraphic(new ImageView(image));
		muteBTn.setText("Mute microphone");
		audioUser1.setText(currentChatPartner + " calling...");
		audioUtils = new AudioUtils(node, user, LoginHelper.retrieveUser(currentChatPartner, node));
		AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.WAITING,
				currentChatPartner, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
	}

	public void startAudioCall() throws LineUnavailableException {
		Platform.runLater(new Runnable() {
			public void run() {
				microphoneMute = false;
				Image image = new Image(
						MainWindowController.class.getResourceAsStream("/microphone.png"));
				microphoneLbl.setGraphic(new ImageView(image));
				muteBTn.setText("Mute microphone");
				audioUser1.setText(currentChatPartner);
			}
		});
		audioUtils.startAudio();
	}

	public void audioCallRejected() {
		Platform.runLater(new Runnable() {
			public void run() {
				rightPane.setTop(null);
				showChatBtns("chat");
			}
		});
		audioUtils = null;
	}

	public void audioCallAborted()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		Platform.runLater(new Runnable() {
			public void run() {
				rightPane.setTop(null);
				showChatBtns("chat");
			}
		});
		audioUtils.endAudio();
		audioUtils = null;
	}

	@FXML
	public void endAudioHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		rightPane.setTop(null);
		showChatBtns("chat");
		microphoneLbl.setGraphic(null);
		microphoneMute = true;
		audioUtils.endAudio();

		audioUtils = null;
	}

	@FXML
	public void muteHandler() throws LineUnavailableException {
		System.out.println("muteHandler");
		microphoneMute = !microphoneMute;
		Image image;
		if (microphoneMute) {
			image = new Image(
					MainWindowController.class.getResourceAsStream("/microphone_mute.png"));
			System.out.println(muteBTn.getText());
			muteBTn.setText("Unmute microphone");
			System.out.println(muteBTn.getText());
			audioUtils.mute();
		} else {
			image = new Image(MainWindowController.class.getResourceAsStream("/microphone.png"));
			System.out.println(muteBTn.getText());
			muteBTn.setText("Mute microphone");
			System.out.println(muteBTn.getText());
			audioUtils.unmute();
		}
		microphoneLbl.setGraphic(new ImageView(image));
	}

	public void askAudioCall(String username) throws IOException {
		makeAudioCallDialog(username);
	}

	public void acceptAudioCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainPane.setTop(null);
		if (audioUtils != null) {
			audioUtils.endAudio();
		}
		AudioRequest request = new AudioRequest(RequestType.ACCEPTED, RequestStatus.ACCEPTED,
				username, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);

		audioUtils = new AudioUtils(node, user, LoginHelper.retrieveUser(username, node));
		startAudioCall();

		// set chatpane and audiopane
		currentChatPartner = username;
		rightPane.setBottom(chatPane);
		rightPane.setTop(audioPane);

		log.info("accept audio call with: " + username);
	}

	public void rejectAudioCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainPane.setTop(null);
		AudioRequest request = new AudioRequest(RequestType.REJECTED, RequestStatus.REJECTED,
				username, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
		log.info("rejected audio call with: " + username);
	}

	private void makeAudioCallDialog(final String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				mainPane.setTop(requestPane);
				requestWindowLabel.setText("Do you want to start an audio call with " + username);
			}
		});
		requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					acceptAudioCall(username);
				} catch (Exception e) {
					log.error("Cannot accept audio call: " + e);
				}
				mediaPlayer.stop();
			}
		});
		requestWindowRejectBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					rejectAudioCall(username);
				} catch (Exception e) {
					log.error("Cannot reject audio call: " + e);
				}
				mediaPlayer.stop();
			}
		});

		String musicFile = "resources/ring.mp3";
		Media sound = new Media(new File(musicFile).toURI().toString());
		mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.play();
	}

	/*
	 * VIDEO PART
	 */

	@FXML
	public void startVideoHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		rightPane.setTop(videoPane);
		showChatBtns("video");
		cameraMute = false;
		Image image = new Image(getClass().getResourceAsStream("/camera.png"));
		cameraLbl.setGraphic(new ImageView(image));
		muteVideoBtn.setText("Mute camera");
		microphoneMute = false;
		Image imageMicrophone = new Image(getClass().getResourceAsStream("/microphone.png"));
		microphoneLblAudio.setGraphic(new ImageView(imageMicrophone));
		muteBtnVideo.setText("Mute microphone");
		hideMyselfBtn.setText("Hide myself");

		// Initialize Audio utils
		if (audioUtils == null) {
			audioUtils =
					new AudioUtils(node, user, LoginHelper.retrieveUser(currentChatPartner, node));
		}

		// Initialize Video utils
		videoUtils = new VideoUtils(node, user, LoginHelper.retrieveUser(currentChatPartner, node));

		// Ask for Video call
		// videoUtils.sendRequest(RequestType.SEND, currentChatPartner);
		VideoRequest request = new VideoRequest(RequestType.SEND, RequestStatus.WAITING,
				currentChatPartner, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
	}

	@FXML
	public void endVideoHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		rightPane.setTop(null);
		showChatBtns("chat");
		microphoneLbl.setGraphic(null);
		microphoneMute = true;
		audioUtils.endAudio();
		audioUtils = null;

		cameraLbl.setGraphic(null);
		cameraMute = true;
		videoUtils.endVideo();
		AudioRequest request = new AudioRequest(RequestType.ABORTED, RequestStatus.CLOSED,
				currentChatPartner, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
		videoUtils = null;
	}

	@FXML
	public void muteHandlerVideo() {
		microphoneMute = !microphoneMute;
		Image image;
		if (microphoneMute) {
			image = new Image(
					MainWindowController.class.getResourceAsStream("/microphone_mute.png"));
			muteBtnVideo.setText("Unmute microphone");
			audioUtils.mute();
		} else {
			image = new Image(MainWindowController.class.getResourceAsStream("/microphone.png"));
			muteBtnVideo.setText("Mute microphone");
			audioUtils.unmute();
		}
		microphoneLblAudio.setGraphic(new ImageView(image));
	}

	@FXML
	public void muteVideoHandler() {
		cameraMute = !cameraMute;
		Image image;
		if (cameraMute) {
			image = new Image(getClass().getResourceAsStream("/camera_mute.png"));
			muteVideoBtn.setText("Unmute camera");
			videoUtils.mute();
		} else {
			image = new Image(getClass().getResourceAsStream("/camera.png"));
			muteVideoBtn.setText("Mute camera");
			videoUtils.unmute();
		}
		cameraLbl.setGraphic(new ImageView(image));
	}

	@FXML
	public void hideMyselfHandler() {
		if (meImageView.isVisible()) {
			meImageView.setVisible(false);
			hideMyselfBtn.setText("Show myself");
		} else {
			meImageView.setVisible(true);
			hideMyselfBtn.setText("Hide myself");
		}
	}

	public void askVideoCall(String username) throws IOException {
		makeVideoCallDialog(username);
	}

	public void startVideoCall() throws LineUnavailableException {
		videoUtils.startVideo(meImageView);
	}

	public void videoCallRejected() {
		Platform.runLater(new Runnable() {
			public void run() {
				rightPane.setTop(null);
				showChatBtns("chat");
			}
		});
		audioUtils = null;
		videoUtils = null;
	}

	public void videoCallAborted()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		Platform.runLater(new Runnable() {
			public void run() {
				rightPane.setTop(null);
				showChatBtns("chat");
			}
		});
		audioUtils.endAudio();
		audioUtils = null;

		videoUtils.endVideo();
		videoUtils = null;
	}

	private void makeVideoCallDialog(final String username) {
		Platform.runLater(new Runnable() {
			public void run() {
				mainPane.setTop(requestPane);
				requestWindowLabel.setText("Do you want to start a video call with " + username);
			}
		});
		requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					acceptVideoCall(username);
				} catch (Exception e) {
					log.error("Cannot accept video call: " + e);
					e.printStackTrace();
				}
				mediaPlayer.stop();
			}
		});
		requestWindowRejectBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				try {
					rejectVideoCall(username);
				} catch (Exception e) {
					log.error("Cannot reject video call: " + e);
				}
				mediaPlayer.stop();
			}
		});

		String musicFile = "resources/ring.mp3";
		Media sound = new Media(new File(musicFile).toURI().toString());
		mediaPlayer = new MediaPlayer(sound);
		mediaPlayer.play();
	}

	private void acceptVideoCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainPane.setTop(null);

		showChatBtns("video");
		cameraMute = false;
		Image image = new Image(getClass().getResourceAsStream("/camera.png"));
		cameraLbl.setGraphic(new ImageView(image));
		muteVideoBtn.setText("Mute camera");
		microphoneMute = false;
		Image imageMicrophone = new Image(getClass().getResourceAsStream("/microphone.png"));
		microphoneLblAudio.setGraphic(new ImageView(imageMicrophone));
		muteBtnVideo.setText("Mute microphone");

		// set chatpane and audiopane
		currentChatPartner = username;
		rightPane.setBottom(chatPane);
		rightPane.setTop(videoPane);

		if (audioUtils != null) {
			audioUtils.endAudio();
		}
		audioUtils = new AudioUtils(node, user, LoginHelper.retrieveUser(username, node));
		audioUtils.startAudio();

		if (videoUtils != null) {
			videoUtils.endVideo();
		}
		videoUtils = new VideoUtils(node, user, LoginHelper.retrieveUser(username, node));
		VideoRequest request = new VideoRequest(RequestType.ACCEPTED, RequestStatus.ACCEPTED,
				currentChatPartner, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
		videoUtils.startVideo(meImageView);

		log.info("accept audio call with: " + username);
	}

	private void rejectVideoCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainPane.setTop(null);
		if (videoUtils == null) {
			videoUtils = new VideoUtils(node, user, null);
			VideoRequest request = new VideoRequest(RequestType.REJECTED, RequestStatus.REJECTED,
					currentChatPartner, node.getUser().getUsername());
			RequestHandler.handleRequest(request, node);
			videoUtils = null;
		} else {
			VideoRequest request = new VideoRequest(RequestType.REJECTED, RequestStatus.REJECTED,
					currentChatPartner, node.getUser().getUsername());
			RequestHandler.handleRequest(request, node);
		}
		log.info("rejected video call with: " + username);
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

	public Object handleReceiveMessage(PeerAddress peerAddress, Object object) {
		log.info("received message: " + object.toString() + " from: " + peerAddress.toString());
		Message m = (Message) object;
		if (m instanceof ChatMessage) {
			ChatMessage chatMessage = (ChatMessage) m;
			addReceivedMessage(chatMessage.getSenderID(), chatMessage.getData(),
					chatMessage.getDate());
		} else {
			// TODO!
		}

		return 0;
	}

	public void addReceivedMessage(String sender, String message, Date date) {
		// TODO: Change to a fitting date format
		DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		chatText.setText(
				chatText.getText() + "\n" + "[" + f.format(date) + "] " + sender + ": " + message);
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
	public void showChatBtns(String pane) {
		if (pane == null) {
			btnWrapperChat.setVisible(false);
			btnWrapperAudio.setVisible(false);
			btnWrapperVideo.setVisible(false);
		} else if (pane.equals("chat")) {
			btnWrapperChat.setVisible(true);
			btnWrapperAudio.setVisible(false);
			btnWrapperVideo.setVisible(false);
		} else if (pane.equals("audio")) {
			btnWrapperChat.setVisible(false);
			btnWrapperAudio.setVisible(true);
			btnWrapperVideo.setVisible(false);
		} else if (pane.equals("video")) {
			btnWrapperChat.setVisible(false);
			btnWrapperAudio.setVisible(false);
			btnWrapperVideo.setVisible(true);
		}
	}

	private void sendFriendRequest(User user, Node node) {
		// TODO Send a friend Request and add Friend with Status Waiting
		Friend friend = new Friend();
		FriendRequest request = new FriendRequest(user.getUsername(), friend, RequestType.SEND);
	}

}
