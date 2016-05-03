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
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.RequestHandler;
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

	private Node node;
	private MainWindow mainWindow;
	private User user;

	private BorderPane mainPane;
	private BorderPane rightPane;
	private AnchorPane infoPane;
	private AnchorPane chatPane;
	private AnchorPane audioPane;
	private AnchorPane friendlistPane;
	private AnchorPane friendsearchResultPane;
	private AnchorPane requestPane;

	private Boolean microphoneMute;
	private AudioUtils audioUtils;

	private MediaPlayer mediaPlayer;

	private String currentChatPartner;

	public void setUser(String username) throws ClassNotFoundException, IOException {
		user = LoginHelper.retrieveUser(username, node);
	}

	public void setMainWindow(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void setMainPane(BorderPane mainPane) {
		System.out.println("setMainPane");
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
	public void handleSendMessage() {	  
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
			chatText.setText(chatText.getText() + "\n" +"[" + f.format(date) +"] ME: " + message);
			messageText.setText("");
		}
	}

	@FXML
	public void searchFriendHandler() throws ClassNotFoundException, IOException {
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
	public void leaveChatHandler() throws ClassNotFoundException, IOException {
		rightPane.setTop(infoPane);
		rightPane.setBottom(null);

		if (audioUtils != null) {
			audioUtils.endAudio();
			AudioRequest request = new AudioRequest(RequestType.ABORTED, currentChatPartner, node.getUser().getUsername());
			RequestHandler.handleRequest(request, node);
			//audioUtils.sendRequest(RequestType.ABORTED, currentChatPartner);
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
	public void startAudioHandler() throws ClassNotFoundException, IOException {
		rightPane.setTop(audioPane);
		showChatBtns("audio");
		microphoneMute = false;
		Image image = new Image(MainWindowController.class.getResourceAsStream("/microphone.png"));
		microphoneLbl.setGraphic(new ImageView(image));
		muteBTn.setText("Mute microphone");
		audioUser1.setText(currentChatPartner + " calling...");
		audioUtils = new AudioUtils(node, user, LoginHelper.retrieveUser(currentChatPartner, node));
		AudioRequest request = new AudioRequest(RequestType.SEND, currentChatPartner, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
	}

	public void startAudioCall() throws LineUnavailableException {
		Platform.runLater(new Runnable() {
			public void run() {
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

	public void audioCallAborted() throws ClassNotFoundException, IOException {
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
	public void endAudioHandler() throws ClassNotFoundException, IOException {
		rightPane.setTop(null);
		showChatBtns("chat");
		microphoneLbl.setGraphic(null);
		microphoneMute = true;
		audioUtils.endAudio();
		AudioRequest request = new AudioRequest(RequestType.ABORTED, currentChatPartner, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
		
		//audioUtils.sendRequest(RequestType.ABORTED, currentChatPartner);
		audioUtils = null;
	}

	@FXML
	public void muteHandler() throws LineUnavailableException {
		microphoneMute = !microphoneMute;
		Image image;
		if (microphoneMute) {
			image = new Image(MainWindowController.class.getResourceAsStream("/microphone_mute.png"));
			muteBTn.setText("Unmute microphone");
			audioUtils.mute();
		} else {
			image = new Image(MainWindowController.class.getResourceAsStream("/microphone.png"));
			muteBTn.setText("Mute microphone");
			audioUtils.unmute();
		}
		microphoneLbl.setGraphic(new ImageView(image));
	}

	public void askAudioCall(String username) throws IOException {
	  // TODO: Fehlerbehebung
    //  AudioRequest request = new AudioRequest(RequestType.SEND, username, node.getUser().getUsername());
     // RequestHandler.handleRequest(request, node);
	  makeAudioCallDialog(username);
	}

	public void acceptAudioCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainPane.setTop(null);
		if (audioUtils != null) {
			audioUtils.endAudio();
		}
		AudioRequest request = new AudioRequest(RequestType.ACCEPTED, username, node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
		
		audioUtils = new AudioUtils(node, user, LoginHelper.retrieveUser(username, node));
		startAudioCall();

		// set chatpane and audiopane
		currentChatPartner = username;
		rightPane.setBottom(chatPane);
		rightPane.setTop(audioPane);

		log.info("accept audio call with: " + username);
	}

	public void rejectAudioCall(String username) throws ClassNotFoundException, IOException {
		mainPane.setTop(null);
		AudioRequest request = new AudioRequest(RequestType.REJECTED, username, node.getUser().getUsername());
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
	public void startVideoHandler() {
		// TODO
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
	
	public Object handleReceiveMessage(PeerAddress peerAddress, Object object){
	  log.info("received message: " + object.toString() + " from: " + peerAddress.toString());
      Message m = (Message) object;
      if(m instanceof ChatMessage){
        ChatMessage chatMessage = (ChatMessage) m;
        addReceivedMessage(chatMessage.getSenderID(), chatMessage.getData(), chatMessage.getDate());
      }
      else{
        //TODO!
      }
  
      return 0;
	}

	public void addReceivedMessage(String sender, String message, Date date) {
	  //TODO: Change to a fitting date format
	  DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
	  chatText.setText(chatText.getText() + "\n" +"[" + f.format(date) +"] "+ sender + ": " + message);
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
		} else if (pane.equals("chat")) {
			btnWrapperChat.setVisible(true);
			btnWrapperAudio.setVisible(false);
		} else if (pane.equals("audio")) {
			btnWrapperChat.setVisible(false);
			btnWrapperAudio.setVisible(true);
		}
	}
	
	private void sendFriendRequest(User user, Node node) {
      // TODO Send a friend Request and add Friend with Status Waiting 
	  Friend friend = new Friend();
      FriendRequest request = new FriendRequest(user.getUsername(), friend, RequestType.SEND);
    }

}
