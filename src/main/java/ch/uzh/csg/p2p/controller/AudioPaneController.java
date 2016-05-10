package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.AudioUtils;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.request.AudioRequest;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class AudioPaneController {

	private Logger log = LoggerFactory.getLogger(getClass());
	
	private Node node;
	private MainWindowController mainWindowController;
	private boolean microphoneMuted = false;
	private HashMap<String, Label> audioUsersMap = new HashMap<String, Label>();
	private AudioUtils audioUtils;
	
	@FXML
	public HBox btnWrapperAudio;
	@FXML
	private Button muteBtn;
	@FXML
	private Label microphoneLbl;
	@FXML
	private HBox audioUserWrapper;
	
	
	public AudioPaneController(Node node, MainWindowController mainWindowController) {
		this.node = node;
		this.mainWindowController = mainWindowController;
		audioUtils = new AudioUtils(node, mainWindowController.user);
	}
	
	@FXML
	public void startAudioHandler() throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showAudioPane();
		microphoneMuted = false;
		Image image = new Image(getClass().getResourceAsStream("/microphone.png"));
		microphoneLbl.setGraphic(new ImageView(image));
		muteBtn.setText("Mute microphone");
		
		audioUsersMap.clear();
		audioUserWrapper.getChildren().clear();
		audioUtils.endAudio();
		
		for(String chatPartner: mainWindowController.currentChatPartners) {
			addChatPartner(chatPartner, " ringing...");
			AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.WAITING, node.getFriend(chatPartner).getPeerAddress(), chatPartner, node.getUser().getUsername());
			RequestHandler.handleRequest(request, node);
		}
	}
	
	@FXML
	public void startVideoHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.videoPaneController.startVideoHandler();
	}
	
	@FXML
	public void endAudioHandler() throws LineUnavailableException {
		microphoneLbl.setGraphic(null);
		microphoneMuted = true;
		endAudio();
		for(String chatPartner: mainWindowController.currentChatPartners) {
			AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.ABORTED, node.getFriend(chatPartner).getPeerAddress(), chatPartner,
					node.getUser().getUsername());
			RequestHandler.handleRequest(request, node);
		}
		mainWindowController.hideAudioPane();
	}

	@FXML
	public void muteHandler() throws LineUnavailableException {
		microphoneMuted = !microphoneMuted;
		Image image;
		if (microphoneMuted) {
			image = new Image(getClass().getResourceAsStream("/microphone_muted.png"));
			muteBtn.setText("Unmute microphone");
			audioUtils.mute();
		} else {
			image = new Image(getClass().getResourceAsStream("/microphone.png"));
			muteBtn.setText("Mute microphone");
			audioUtils.unmute();
		}
		microphoneLbl.setGraphic(new ImageView(image));
	}
	
	@FXML
	public void leaveChatHandler() throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showInfoPane();

		audioUtils.endAudio();
	}
	
	@FXML
	public void addUserHandler() {
		// TODO: Implement
	}
	
	public void startAudioCall()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		//User user = new User(audioRequest.getReceiverName(), null, null);
	    //node.getPeer().peer().sendDirect(RequestHandler.retrieveUser(user, node).getPeerAddress())
	    //  .object(audioRequest).start();
	    
		Platform.runLater(new Runnable() {
			public void run() {
				audioUserWrapper.getChildren().clear();
				
				for(Map.Entry<String, Label> audioUser: audioUsersMap.entrySet()) {
					audioUser.getValue().setText(audioUser.getKey());
					audioUserWrapper.getChildren().add(audioUser.getValue());
				}
			}
		});
		
		audioUtils.startAudio();
	}
	
	public void acceptAudioCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.setMainPaneTop(null);
		mainWindowController.addChatPartner(username);
		
		audioUsersMap.clear();
		audioUserWrapper.getChildren().clear();
		audioUtils.endAudio();
		
		for(String chatPartner: mainWindowController.currentChatPartners) {
			addChatPartner(chatPartner, "");
		}
		
		AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.ACCEPTED, node.getFriend(username).getPeerAddress(), username, 
				node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
		
		startAudioCall();
		
		mainWindowController.showAudioAndChatPanes();
		microphoneMuted = false;
		Image image = new Image(MainWindowController.class.getResourceAsStream("/microphone.png"));
		microphoneLbl.setGraphic(new ImageView(image));
		muteBtn.setText("Mute microphone");

		log.info("Accepted audio call with: " + username);
	}
	
	public void askAudioCall(AudioRequest audioRequest) throws IOException, ClassNotFoundException {
		mainWindowController.makeAudioCallDialog(audioRequest.getSenderName());
	}
	
	public void rejectAudioCall(String username) throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.setMainPaneTop(null);
		AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.REJECTED, node.getFriend(username).getPeerAddress(), username,
				node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);
		log.info("Rejected audio call with: " + username);
	}
	
	public void audioCallRejected(final AudioRequest audioRequest)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		Platform.runLater(new Runnable() {
			public void run() {
				markRejectedChatPartner(audioRequest.getSenderName());
				mainWindowController.hideAudioPane();
			}
		});

		audioUtils.removeReceiver(node.getFriend(audioRequest.getSenderName()));
	}
	
	private void markRejectedChatPartner(String chatPartner) {
		Label chatPartnerLabel = audioUsersMap.get(chatPartner);
		chatPartnerLabel.getStyleClass().clear();
		chatPartnerLabel.getStyleClass().add("audioUserRejected");
		chatPartnerLabel.setText(chatPartner + " rejected");
	}
	
	public void audioCallAborted() {
		Platform.runLater(new Runnable() {
			public void run() {
				mainWindowController.hideAudioPane();
				endAudio();
			}
		});
	}
	
	private void endAudio() {
		audioUsersMap.clear();
		audioUserWrapper.getChildren().clear();
		try {
			audioUtils.endAudio();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	private void addChatPartner(String username, String labelPostfix)
			throws LineUnavailableException {
		Label audioUserLabel = new Label();
		audioUserLabel.setText(username + labelPostfix);
		audioUserLabel.getStyleClass().add("audioUser");
		audioUsersMap.put(username, audioUserLabel);
		audioUtils.addReceiver(node.getFriend(username));
	}
	
}