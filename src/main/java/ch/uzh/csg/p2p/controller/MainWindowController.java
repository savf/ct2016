package ch.uzh.csg.p2p.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.AudioUtils;
import ch.uzh.csg.p2p.helper.EncoderUtils;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.AudioMessage;
import ch.uzh.csg.p2p.model.ChatMessage;
import ch.uzh.csg.p2p.model.Friend;
import ch.uzh.csg.p2p.model.Message;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import ch.uzh.csg.p2p.model.request.MessageRequest;
import ch.uzh.csg.p2p.model.request.RequestStatus;
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
			for(Friend f : node.getFriendList()){
			  addUserToFriendList(f);
			}
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
	      PeerAddress receiverAddress = node.getFriend(currentChatPartner).getPeerAddress();
	      String message = messageText.getText();
	      
	      Date date = new Date();
	      ChatMessage m = new ChatMessage(sender, receiver, receiverAddress, date, message);
	      
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
			boolean alreadyFriend = checkAlreadyFriend(user.getUsername());
			Button button = new Button("Send friend request");
			button.getStyleClass().add("btn");
			button.getStyleClass().add("friendRequestBtn");
			button.setOnAction(new EventHandler<ActionEvent>() {

				public void handle(ActionEvent event) {
				    sendFriendRequest(user, node);
					searchResultList.getChildren().clear();
					rightPane.setTop(infoPane);
				}
			});
	         if(alreadyFriend){
	             button.setDisable(true); 
	             button.setOpacity(0.5);
	             button.setVisible(false);
	            }
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
			AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.ABORTED, currentChatPartner, node.getFriend(currentChatPartner).getPeerAddress(), node.getUser().getUsername());
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
		// TODO: evtl können wir ohne retrieveUser arbeiten
		User receiver = LoginHelper.retrieveUser(currentChatPartner, node);
		audioUtils = new AudioUtils(node, user, receiver);
		AudioRequest request = new AudioRequest(RequestType.SEND, currentChatPartner, receiver.getPeerAddress(), node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
	}

	   public void askAudioCall(String username) throws IOException {
	      // TODO: Fehlerbehebung
	    //  AudioRequest request = new AudioRequest(RequestType.SEND, username, node.getUser().getUsername());
	     // RequestHandler.handleRequest(request, node);
	      makeAudioCallDialog(username);
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
	    
	    public void acceptAudioCall(String username)
            throws ClassNotFoundException, IOException, LineUnavailableException {
        mainPane.setTop(null);
        if (audioUtils != null) {
            audioUtils.endAudio();
        }
        AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.ACCEPTED, username, node.getFriend(username).getPeerAddress(), node.getUser().getUsername());
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
        AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.REJECTED, username, node.getFriend(username).getPeerAddress(),  node.getUser().getUsername());
        RequestHandler.handleRequest(request, node);
        log.info("rejected audio call with: " + username);
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
		AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.ABORTED, currentChatPartner, node.getFriend(currentChatPartner).getPeerAddress(), node.getUser().getUsername());
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
	
	
	/*
     * Friend PART
     */
	
	public void askFriend(FriendRequest request){
	  final FriendRequest r = request;
	  Platform.runLater(new Runnable() {
        public void run() {
            mainPane.setTop(requestPane);
            requestWindowLabel.setText("Do you want to be friends with " + r.getSenderName() + "?");
        }
    });
    requestWindowAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {

        public void handle(ActionEvent event) {
            try {
                acceptFriendship(r);
            } catch (Exception e) {
                log.error("Cannot accept friendship request: " + e);
            }
        }
    });
    requestWindowRejectBtn.setOnAction(new EventHandler<ActionEvent>() {

        public void handle(ActionEvent event) {
            try {
                rejectFriendship(r);
            } catch (Exception e) {
                log.error("Cannot reject friendship request: " + e);
            }
        }
    });
	}
	
	private void sendFriendRequest(User user, Node node) {
	   FriendRequest request = new FriendRequest(node.getPeer().peerAddress(), node.getUser().getUsername(), user.getUsername(), RequestType.SEND);
	   RequestHandler.handleRequest(request, node);
	 }

	protected void rejectFriendship(FriendRequest r) {
	  mainPane.setTop(null);
	  FriendRequest request = new FriendRequest(node.getPeer().peerAddress(), node.getUser().getUsername(), r.getSenderName(), RequestType.SEND);    
      request.setStatus(RequestStatus.REJECTED);
      RequestHandler.handleRequest(request, node);
  }

  protected void acceptFriendship(FriendRequest r) {
    mainPane.setTop(null);
    FriendRequest request = new FriendRequest(node.getPeer().peerAddress(), node.getUser().getUsername(), r.getSenderName(), RequestType.SEND);
    request.setStatus(RequestStatus.ACCEPTED);
    RequestHandler.handleRequest(request, node);
    Friend friend = new Friend(r.getSenderPeerAddress(), r.getSenderName());
    storeFriend(friend);
    addUserToFriendList(friend);
    node.addFriend(friend);
  }
  
  private void storeFriend(Friend f){
    FriendRequest r = new FriendRequest(f.getPeerAddress(), f.getName(), null, RequestType.STORE);
    RequestHandler.handleRequest(r, node);   
  }
  
  public void friendshipRejected(){
    // TODO: show a message with username rejected your friendship request
    Platform.runLater(new Runnable() {
      public void run() {
          rightPane.setTop(null);
      }
  });
  }
  
  public void friendshipAccepted(Friend f){
    // TODO: add a message with username accepted your friendship
    final Friend friend = f;
    Platform.runLater(new Runnable() {
      public void run() {
        mainPane.setTop(null);
        storeFriend(friend);
        addUserToFriendList(friend);
        node.addFriend(friend);
      }
  });
  }
  
  private boolean checkAlreadyFriend(String username) throws UnsupportedEncodingException {
    for(String n : node.getUser().getFriendStorage()){
      if((username).equals(n)){
        return true;
      }
    }
    return false;
  }

	/*
	 * VIDEO PART
	 */

	@FXML
	public void startVideoHandler() {
		// TODO
	}

	private void addUserToFriendList(Friend friend) {
		HBox hBox = new HBox();
		hBox.setSpacing(40);
		final String labelname = friend.getName();
		Label label = new Label(labelname);
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
	
	public Object handleReceiveMessage(Message m){
	  log.info("received message: " + m.toString() + " from: " + m.getSenderID());
      if(m instanceof ChatMessage){
        ChatMessage chatMessage = (ChatMessage) m;
        addReceivedMessage(chatMessage.getSenderID(), chatMessage.getData(), chatMessage.getDate());
      }
      else if(m instanceof AudioMessage){
        AudioMessage audioMessage = (AudioMessage) m;
        try {
          AudioUtils.playAudio(EncoderUtils.byteArrayToByteBuffer(audioMessage.getData()));
        } catch (LineUnavailableException e) {
          e.printStackTrace();
        }
      }
      else{
        
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

}
