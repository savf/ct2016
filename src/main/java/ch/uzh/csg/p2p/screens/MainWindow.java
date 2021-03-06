package ch.uzh.csg.p2p.screens;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.controller.AudioPaneController;
import ch.uzh.csg.p2p.controller.ChatPaneController;
import ch.uzh.csg.p2p.controller.FriendlistPaneController;
import ch.uzh.csg.p2p.controller.MainWindowController;
import ch.uzh.csg.p2p.controller.NotificationPaneController;
import ch.uzh.csg.p2p.controller.RequestPaneController;
import ch.uzh.csg.p2p.controller.VideoPaneController;
import ch.uzh.csg.p2p.model.request.RequestHandler;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainWindow {

  private final String TITLE = "Quack! - ";

  private Node node;

  private Stage stage;
  private int id;
  private String ip;
  private String username;
  private String password;
  private boolean bootstrapNode;

  private MainWindowController mainWindowController;
  private ChatPaneController chatPaneController;
  private AudioPaneController audioPaneController;
  private VideoPaneController videoPaneController;
  private FriendlistPaneController friendlistPaneController;
  private RequestPaneController requestPaneController;
  private NotificationPaneController notificationPaneController;

  private AnchorPane mainPane;
  private AnchorPane notificationPane;
  private AnchorPane chatPane;
  private AnchorPane audioPane;
  private AnchorPane videoPane;
  private AnchorPane friendlistPane;
  private AnchorPane friendsearchResultPane;
  private AnchorPane requestPane;
  private AnchorPane informPane;

  public void start(Stage stage, int id, String ip, String username, String password,
      boolean bootstrapNode) throws Exception {
    this.stage = stage;
    this.id = id;
    this.ip = ip;
    this.username = username;
    this.password = password;
    this.bootstrapNode = bootstrapNode;
    initialiseWindow();
  }

  public void startNode(int id, String ip, String username, String password, boolean bootstrapNode)
      throws IOException, LineUnavailableException, ClassNotFoundException {
    node = new Node(id, ip, username, password, bootstrapNode, null);
  }

  private void initialiseWindow() throws IOException, LineUnavailableException,
      ClassNotFoundException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
    startNode(id, ip, username, password, bootstrapNode);
    mainWindowController = new MainWindowController(node, stage);

    chatPaneController = new ChatPaneController(node, mainWindowController);
    mainWindowController.setChatPaneController(chatPaneController);
    audioPaneController = new AudioPaneController(node, mainWindowController);
    mainWindowController.setAudioPaneController(audioPaneController);
    videoPaneController = new VideoPaneController(node, mainWindowController);
    mainWindowController.setVideoPaneController(videoPaneController);
    friendlistPaneController = new FriendlistPaneController(node, mainWindowController);
    mainWindowController.setFriendlistPaneController(friendlistPaneController);
    requestPaneController = new RequestPaneController(mainWindowController);
    mainWindowController.setRequestPaneController(requestPaneController);
    notificationPaneController = new NotificationPaneController(node, mainWindowController);
    mainWindowController.setNotificationPaneController(notificationPaneController);
    loader.setController(mainWindowController);
    mainPane = loader.load();

    initializeNotificationPane();
    initializeChatPane();
    initializeAudioPane();
    initializeVideoPane();
    initializeFriendlistPane();
    initializeFriendsearchResultPane();
    initializeRequestPane();
    initializeInformPane();

    mainWindowController.setNotificationPane(notificationPane);
    mainWindowController.setChatPane(chatPane);
    mainWindowController.setAudioPane(audioPane);
    mainWindowController.setVideoPane(videoPane);
    mainWindowController.setFriendlistPane(friendlistPane);
    mainWindowController.setFriendsearchResultPane(friendsearchResultPane);
    mainWindowController.setRequestPane(requestPane);
    mainWindowController.setInformPane(informPane);

    mainWindowController.setLeftPane(friendlistPane);
    mainWindowController.setRightTopPane(notificationPane);

    Scene scene = new Scene(mainPane);

    scene.getStylesheets().add("basic.css");

    stage.setTitle(TITLE + username);
    stage.setScene(scene);
    stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));

    stage.setMinWidth(800);
    stage.setMinHeight(480);
    stage.centerOnScreen();

    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

      public void handle(WindowEvent event) {
        if (node != null) {
          node.shutdown();
        }
        if (mainWindowController.audioRingingThread != null) {
          mainWindowController.audioRingingThread.stop();
        }
        if (mainWindowController.videoRingingThread != null) {
          mainWindowController.videoRingingThread.stop();
        }

        stage.close();
        System.exit(0);
      }
    });

    RequestHandler.setMainWindowController(mainWindowController);
  }

  private void initializeNotificationPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("NotificationPane.fxml"));
    loader.setController(notificationPaneController);
    notificationPane = loader.load();
  }

  private void initializeChatPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPane.fxml"));
    loader.setController(chatPaneController);
    chatPane = loader.load();
  }

  private void initializeAudioPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("AudioPane.fxml"));
    loader.setController(audioPaneController);
    audioPane = loader.load();
  }

  private void initializeVideoPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("VideoPane.fxml"));
    loader.setController(videoPaneController);
    videoPane = loader.load();
  }

  private void initializeFriendlistPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("FriendlistPane.fxml"));
    loader.setController(friendlistPaneController);
    friendlistPane = loader.load();
  }

  private void initializeFriendsearchResultPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("FriendsearchResultPane.fxml"));
    loader.setController(friendlistPaneController);
    friendsearchResultPane = loader.load();
  }

  private void initializeRequestPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("RequestPane.fxml"));
    loader.setController(requestPaneController);
    requestPane = loader.load();
  }

  private void initializeInformPane() throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("InformPane.fxml"));
    loader.setController(requestPaneController);
    informPane = loader.load();
  }

}
