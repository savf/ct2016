package ch.uzh.csg.p2p.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.FriendlistHelper;
import ch.uzh.csg.p2p.model.request.FriendRequest;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class MainWindowController {

  private Logger log = LoggerFactory.getLogger(MainWindowController.class);

  @FXML
  private AnchorPane leftPane;
  @FXML
  private AnchorPane rightTopPane;
  @FXML
  private AnchorPane rightBottomPane;
  @FXML
  private AnchorPane modalOverlayPane;

  public Node node;
  private Stage stage;
  private FriendlistHelper friendlistHelper;
  public List<String> currentChatPartners;
  public Thread audioRingingThread;
  public Thread videoRingingThread;

  private AnchorPane notificationPane;
  private AnchorPane chatPane;
  private AnchorPane audioPane;
  private AnchorPane videoPane;
  private AnchorPane friendlistPane;
  private AnchorPane friendsearchResultPane;
  private AnchorPane requestPane;
  private AnchorPane informPane;

  public ChatPaneController chatPaneController;
  public AudioPaneController audioPaneController;
  public VideoPaneController videoPaneController;
  public FriendlistPaneController friendlistPaneController;
  public RequestPaneController requestPaneController;

  public MainWindowController(Node node, Stage stage) {
    this.node = node;
    this.stage = stage;
    this.currentChatPartners = new ArrayList<String>();
    this.friendlistHelper = new FriendlistHelper(this.node);
  }

  /*
   * SETUP
   */

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

  public void setRequestPaneController(RequestPaneController requestPaneController) {
    this.requestPaneController = requestPaneController;
  }

  public void setLeftPane(AnchorPane anchorPane) {
    leftPane.getChildren().clear();
    if (anchorPane != null) {
      leftPane.getChildren().add(anchorPane);
    }
  }

  public void setRightTopPane(AnchorPane anchorPane) {
    rightTopPane.getChildren().clear();
    if (anchorPane != null) {
      rightTopPane.getChildren().add(anchorPane);
    }
  }

  public void setRightBottomPane(AnchorPane anchorPane) {
    rightBottomPane.getChildren().clear();
    if (anchorPane != null) {
      rightBottomPane.getChildren().add(anchorPane);
    }
  }

  public void setNotificationPane(AnchorPane infoPane) {
    this.notificationPane = infoPane;
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

  public void setInformPane(AnchorPane informPane) {
    this.informPane = informPane;
  }

  public void addChatPartner(String username) {
    if (!currentChatPartners.contains(username) && friendlistHelper.checkAlreadyFriend(username)) {
      currentChatPartners.add(username);
    }
  }

  public void clearChatPartners() {
    currentChatPartners.clear();
  }

  /*
   * REQUEST DIALOG
   */

  public void showRequestOverlay() {
    modalOverlayPane.setVisible(true);
    modalOverlayPane.getChildren().clear();
    modalOverlayPane.getChildren().add(requestPane);
  }

  public void showInformOverlay() {
    modalOverlayPane.setVisible(true);
    modalOverlayPane.getChildren().clear();
    modalOverlayPane.getChildren().add(informPane);
  }

  public void hideRequestOverlay() {
    modalOverlayPane.setVisible(false);
  }

  public void hideInformOverlay() {
    modalOverlayPane.setVisible(false);
  }

  /*
   * NOTIFICATIONS
   */

  public void showNotificationPane() {
    setRightTopPane(notificationPane);
    rightBottomPane.getChildren().clear();
  }


  /*
   * AUDIO
   */

  public void showAudioPane() {
    setRightTopPane(audioPane);
    showChatBtns("audio");
    stage.setWidth(1024);
    stage.setHeight(685);
    stage.centerOnScreen();
  }

  public void hideAudioPane() {
    rightTopPane.getChildren().clear();
    showChatBtns("chat");
    stage.setWidth(1024);
    stage.setHeight(640);
    stage.centerOnScreen();
  }

  public void showAudioAndChatPanes() {
    setRightTopPane(audioPane);
    setRightBottomPane(chatPane);
    showChatBtns("audio");
    stage.setWidth(1024);
    stage.setHeight(685);
    stage.centerOnScreen();
  }

  public void makeAudioCallDialog(final String username) {
    Platform.runLater(new Runnable() {
      public void run() {
        requestPaneController.makeDialog("Do you want to start an audio call with " + username
            + "?", new EventHandler<ActionEvent>() {

          public void handle(ActionEvent event) {
            try {
              audioPaneController.acceptAudioCall(username);
            } catch (Exception e) {
              log.error("Cannot accept audio call: " + e);
            }
            audioRingingThread.stop();
          }
        }, new EventHandler<ActionEvent>() {

          public void handle(ActionEvent event) {
            try {
              audioPaneController.rejectAudioCall(username);
            } catch (Exception e) {
              log.error("Cannot reject audio call: " + e);
            }
            audioRingingThread.stop();
          }
        });
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
   * VIDEO
   */

  public void showVideoPane() {
    setRightTopPane(videoPane);
    showChatBtns("video");
    stage.setWidth(1333);
    stage.setHeight(768);
    stage.centerOnScreen();
  }

  public void hideVideoPane() {
    rightTopPane.getChildren().clear();
    showChatBtns("chat");
    stage.setWidth(1024);
    stage.setHeight(640);
    stage.centerOnScreen();
  }

  public void showVideoAndChatPanes() {
    setRightTopPane(videoPane);
    setRightBottomPane(chatPane);
    showChatBtns("video");
    stage.setWidth(1333);
    stage.setHeight(768);
    stage.centerOnScreen();
  }

  public void makeVideoCallDialog(final String username) {
    Platform.runLater(new Runnable() {
      public void run() {
        if (!modalOverlayPane.isVisible()) {
          requestPaneController.makeDialog("Do you want to start a video call with " + username
              + "?", new EventHandler<ActionEvent>() {

            public void handle(ActionEvent event) {
              try {
                videoPaneController.acceptVideoCall(username);
                videoRingingThread.stop();
              } catch (Exception e) {
                log.error("Cannot accept video call: " + e);
                e.printStackTrace();
              }
            }
          }, new EventHandler<ActionEvent>() {

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
      }
    });
  }

  /*
   * FRIENDLIST
   */

  public void showFriendSearchResultPane() {
    setRightTopPane(friendsearchResultPane);
  }

  public void alertWidthHeight() {
    Alert alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Width and height of stage");
    String s =
        "The current width is: " + stage.getWidth() + " and the height is: " + stage.getHeight();
    alert.setContentText(s);
    alert.showAndWait();
  }

  public void showChatPane() {
    setRightTopPane(null);
    setRightBottomPane(chatPane);
    stage.setMinWidth(1024);
    stage.setMinHeight(640);
    stage.centerOnScreen();
  }

  public void askFriend(FriendRequest request) {
    final FriendRequest r = request;
    Platform.runLater(new Runnable() {
      public void run() {

        requestPaneController.makeDialog("Do you want to be friends with " + r.getSenderName()
            + "?", new EventHandler<ActionEvent>() {

          public void handle(ActionEvent event) {
            try {
              friendlistPaneController.acceptFriendship(r);
            } catch (Exception e) {
              log.error("Cannot accept friendship request: " + e);
            }
          }
        }, new EventHandler<ActionEvent>() {

          public void handle(ActionEvent event) {
            try {
              friendlistPaneController.rejectFriendship(r);
            } catch (Exception e) {
              log.error("Cannot reject friendship request: " + e);
            }
          }
        });
      }
    });
  }

  public void friendshipAccepted(final String username) {
    Platform.runLater(new Runnable() {
      public void run() {
        requestPaneController.inform(username + " accepted your friendship request.");
      }
    });
  }

  public void friendshipRejected(final String username) {
    Platform.runLater(new Runnable() {
      public void run() {
        requestPaneController.inform(username + " rejected your friendship request.");
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
