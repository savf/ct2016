package ch.uzh.csg.p2p.controller;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.AudioUtils;
import ch.uzh.csg.p2p.helper.VideoUtils;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import ch.uzh.csg.p2p.model.request.RequestStatus;
import ch.uzh.csg.p2p.model.request.RequestType;
import ch.uzh.csg.p2p.model.request.VideoRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class VideoPaneController {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Node node;
  private MainWindowController mainWindowController;
  private Boolean cameraOff;
  private Boolean microphoneMuted;
  private AudioUtils audioUtils;
  private VideoUtils videoUtils;

  @FXML
  public HBox btnWrapperVideo;
  @FXML
  private Button muteMicrophoneBtn;
  @FXML
  private Button muteVideoBtn;
  @FXML
  private Label microphoneLbl;
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


  public VideoPaneController(Node node, MainWindowController mainWindowController) {
    this.node = node;
    this.mainWindowController = mainWindowController;
    this.audioUtils = new AudioUtils(node, node.getUser());
    this.videoUtils = new VideoUtils(node, node.getUser());
  }

  @FXML
  public void leaveChatHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.showNotificationPane();

    audioUtils.endAudio();
    videoUtils.endVideo();
  }

  @FXML
  public void addUserHandler() {
    // TODO: Implement
  }

  @FXML
  public void startVideoHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.showVideoPane();
    cameraOff = false;
    Image image = new Image(getClass().getResourceAsStream("/camera.png"));
    cameraLbl.setGraphic(new ImageView(image));
    muteVideoBtn.setText("Turn camera off");
    microphoneMuted = false;
    Image imageMicrophone = new Image(getClass().getResourceAsStream("/microphone.png"));
    microphoneLbl.setGraphic(new ImageView(imageMicrophone));
    muteMicrophoneBtn.setText("Mute microphone");
    hideMyselfBtn.setText("Hide myself");

    if (audioUtils == null) {
      audioUtils = new AudioUtils(node, node.getUser());
    }

    videoUtils = new VideoUtils(node, node.getUser());

    for (String chatPartner : mainWindowController.currentChatPartners) {
      VideoRequest request =
          new VideoRequest(RequestType.SEND, RequestStatus.WAITING, node.getFriend(chatPartner)
              .getPeerAddress(), chatPartner, node.getUser().getUsername());
      RequestHandler.handleRequest(request, node);
      videoUtils.addReceiver(node.getFriend(chatPartner));
      audioUtils.addReceiver(node.getFriend(chatPartner));
    }

  }

  @FXML
  public void endVideoHandler() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.hideVideoPane();
    microphoneLbl.setGraphic(null);
    microphoneMuted = true;
    audioUtils.endAudio();
    audioUtils = null;

    cameraLbl.setGraphic(null);
    cameraOff = true;
    videoUtils.endVideo();
    for (String chatPartner : mainWindowController.currentChatPartners) {
      VideoRequest request =
          new VideoRequest(RequestType.SEND, RequestStatus.ABORTED, node.getFriend(chatPartner)
              .getPeerAddress(), chatPartner, node.getUser().getUsername());
      RequestHandler.handleRequest(request, node);
    }

    videoUtils = null;
  }

  @FXML
  public void muteMicrophoneHandler() {
    microphoneMuted = !microphoneMuted;
    Image image;
    if (microphoneMuted) {
      image = new Image(getClass().getResourceAsStream("/microphone_muted.png"));
      muteMicrophoneBtn.setText("Unmute microphone");
      audioUtils.mute();
    } else {
      image = new Image(getClass().getResourceAsStream("/microphone.png"));
      muteMicrophoneBtn.setText("Mute microphone");
      audioUtils.unmute();
    }
    microphoneLbl.setGraphic(new ImageView(image));
  }

  @FXML
  public void muteVideoHandler() {
    cameraOff = !cameraOff;
    Image image;
    if (cameraOff) {
      image = new Image(getClass().getResourceAsStream("/camera_off.png"));
      muteVideoBtn.setText("Turn camera off");
      videoUtils.mute();
    } else {
      image = new Image(getClass().getResourceAsStream("/camera.png"));
      muteVideoBtn.setText("Turn camera on");
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

  public void askVideoCall(VideoRequest videoRequest) throws IOException {
    System.out.println("askVideoCall");
    mainWindowController.makeVideoCallDialog(videoRequest.getSenderName());
  }

  public void startVideoCall() throws LineUnavailableException, IOException {
    videoUtils.setPartnerImageView(videoUser1);
    if (!videoUtils.videoIsRunning()) {
      videoUtils.startVideo(meImageView);
    }
  }

  public void videoCallRejected(final VideoRequest videoRequest) throws LineUnavailableException {
    Platform.runLater(new Runnable() {
      public void run() {
        mainWindowController.hideVideoPane();
      }
    });

    audioUtils.removeReceiver(node.getFriend(videoRequest.getSenderName()));
    videoUtils.removeReceiver(node.getFriend(videoRequest.getSenderName()));
  }

  public void videoCallAborted() throws ClassNotFoundException, IOException,
      LineUnavailableException {
    Platform.runLater(new Runnable() {
      public void run() {
        mainWindowController.hideVideoPane();
      }
    });
    audioUtils.endAudio();
    videoUtils.endVideo();
  }

  public void acceptVideoCall(String username) throws ClassNotFoundException, IOException,
      LineUnavailableException {
    mainWindowController.addChatPartner(username);
    if (audioUtils != null) {
      audioUtils.endAudio();
    }
    if (videoUtils != null) {
      videoUtils.endVideo();
    }

    cameraOff = false;
    Image image = new Image(getClass().getResourceAsStream("/camera.png"));
    cameraLbl.setGraphic(new ImageView(image));
    muteVideoBtn.setText("Turn camera off");
    microphoneMuted = false;
    Image imageMicrophone = new Image(getClass().getResourceAsStream("/microphone.png"));
    microphoneLbl.setGraphic(new ImageView(imageMicrophone));
    muteMicrophoneBtn.setText("Mute microphone");

    mainWindowController.showVideoAndChatPanes();

    videoUtils = new VideoUtils(node, node.getUser());
    audioUtils = new AudioUtils(node, node.getUser());

    VideoRequest request =
        new VideoRequest(RequestType.SEND, RequestStatus.ACCEPTED, node.getFriend(username)
            .getPeerAddress(), username, node.getUser().getUsername());
    RequestHandler.handleRequest(request, node);

    audioUtils.startAudio();

    for (String chatPartner : mainWindowController.currentChatPartners) {
      videoUtils.addReceiver(node.getFriend(chatPartner));
      audioUtils.addReceiver(node.getFriend(chatPartner));
    }

    videoUtils.setPartnerImageView(videoUser1);
    videoUtils.startVideo(meImageView);

    log.info("Accept audio call with: " + username);
  }

  public void rejectVideoCall(String username) throws ClassNotFoundException, IOException,
      LineUnavailableException {
    VideoRequest request =
        new VideoRequest(RequestType.SEND, RequestStatus.REJECTED, node.getFriend(username)
            .getPeerAddress(), username, node.getUser().getUsername());
    RequestHandler.handleRequest(request, node);

    log.info("Rejected video call with: " + username);
  }

}
