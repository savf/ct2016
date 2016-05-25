package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.AudioHelper;
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
import net.tomp2p.dht.FuturePut;
import net.tomp2p.futures.BaseFutureAdapter;

public class AudioPaneController {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Node node;
	private MainWindowController mainWindowController;
	private boolean microphoneMuted = false;
	private HashMap<String, Label> audioUsersMap = new HashMap<String, Label>();
	private AudioHelper audioUtils;

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
		audioUtils = new AudioHelper(node, node.getUser());
	}

	@FXML
	public void startAudioHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showAudioPane();
		microphoneMuted = false;
		Image image = new Image(getClass().getResourceAsStream("/microphone.png"));
		microphoneLbl.setGraphic(new ImageView(image));
		muteBtn.setText("Mute microphone");

		audioUsersMap.clear();
		audioUserWrapper.getChildren().clear();
		audioUtils.endAudio();

		for (String chatPartner : mainWindowController.currentChatPartners) {
			addChatPartner(chatPartner, " ringing...");
			AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.WAITING,
					node.getUser().getFriend(chatPartner).getPeerAddress(), chatPartner,
					node.getUser().getUsername());
			RequestHandler.handleRequest(request, node, new BaseFutureAdapter<FuturePut>() {

				@Override
				public void operationComplete(FuturePut futurePut) throws Exception {
					if (futurePut.isSuccess()) {
						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								Label userLabel = (Label) audioUsersMap.get(chatPartner);
								if (userLabel != null) {
									userLabel.getStyleClass().add("audioUserRejected");
									userLabel.setText(chatPartner + " offline");
								}
							}

						});
					}
				}

			});
		}

		for (Map.Entry<String, Label> audioUser : audioUsersMap.entrySet()) {
			audioUserWrapper.getChildren().add(audioUser.getValue());
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
		for (String chatPartner : mainWindowController.currentChatPartners) {
			AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.ABORTED,
					node.getUser().getFriend(chatPartner).getPeerAddress(), chatPartner,
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
	public void leaveChatHandler()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.showNotificationPane();
		mainWindowController.chatPaneController.leaveChatHandler();
		audioUtils.endAudio();
	}

	@FXML
	public void addUserHandler() {
		// TODO: Implement
	}

	public void startAudioCall()
			throws ClassNotFoundException, IOException, LineUnavailableException {
		Platform.runLater(new Runnable() {
			public void run() {
				audioUserWrapper.getChildren().clear();

				for (Map.Entry<String, Label> audioUser : audioUsersMap.entrySet()) {
					audioUser.getValue().setText(audioUser.getKey());
					audioUserWrapper.getChildren().add(audioUser.getValue());
				}
			}
		});

		audioUtils.startAudio();
	}

	public void acceptAudioCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		mainWindowController.addChatPartner(username);

		audioUsersMap.clear();
		audioUserWrapper.getChildren().clear();
		audioUtils.endAudio();

		AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.ACCEPTED,
				node.getUser().getFriend(username).getPeerAddress(), username,
				node.getUser().getUsername());
		RequestHandler.handleRequest(request, node);

		addChatPartner(username, "");
		startAudioCall();
		mainWindowController.chatPaneController.startChatSessionWith(username);

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

	public void rejectAudioCall(String username)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		AudioRequest request = new AudioRequest(RequestType.SEND, RequestStatus.REJECTED,
				node.getUser().getFriend(username).getPeerAddress(), username,
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

		audioUtils.removeReceiver(node.getUser().getFriend(audioRequest.getSenderName()));
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
		audioUtils.addReceiver(node.getUser().getFriend(username));
	}

}
