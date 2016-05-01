package ch.uzh.csg.p2p.screens;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.controller.MainWindowController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainWindow {

	private final String TITLE = "SkypeClone - Main";

	private Stage stage;
	private int id;
	private String ip;
	private String username;
	private String password;

	private MainWindowController mainWindowController;

	private BorderPane mainPane;
	private BorderPane rightPane;
	private AnchorPane infoPane;
	private AnchorPane chatPane;
	private AnchorPane audioPane;
	private AnchorPane friendlistPane;
	private AnchorPane friendsearchResultPane;
	private AnchorPane requestPane;

	public void start(Stage stage, int id, String ip, String username, String password)
			throws Exception {
		this.stage = stage;
		this.id = id;
		this.ip = ip;
		this.username = username;
		this.password = password;

		initialiseWindow();
	}

	private void initialiseWindow()
			throws IOException, LineUnavailableException, ClassNotFoundException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("MainWindow.fxml"));
		mainWindowController = new MainWindowController();
		loader.setController(mainWindowController);
		mainPane = loader.load();

		initializeRightPane();
		initializeInfoPane();
		initializeChatPane();
		initializeAudioPane();
		initializeFriendlistPane();
		initializeFriendsearchResultPane();
		initializeRequestPane();

		mainPane.setLeft(friendlistPane);
		mainPane.setRight(rightPane);
		rightPane.setTop(infoPane);

		mainWindowController.setMainWindow(this);
		mainWindowController.setMainPane(mainPane);
		mainWindowController.setRightPane(rightPane);
		mainWindowController.setInfoPane(infoPane);
		mainWindowController.setChatPane(chatPane);
		mainWindowController.setAudioPane(audioPane);
		mainWindowController.setFriendlistPane(friendlistPane);
		mainWindowController.setFriendsearchResultPane(friendsearchResultPane);
		mainWindowController.setRequestPane(requestPane);
		mainWindowController.startNode(id, ip, username, password);
		mainWindowController.setUser(username);

		Scene scene = new Scene(mainPane);

		String css = LoginWindow.class.getResource("basic.css").toExternalForm();
		scene.getStylesheets().add(css);

		stage.setTitle(TITLE);
		stage.setScene(scene);
		stage.setFullScreen(true);

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			public void handle(WindowEvent event) {
				mainWindowController.shutdownNode();
				stage.close();
			}
		});
	}

	public void showSearchResults() {
		mainPane.setRight(friendsearchResultPane);
	}

	private void initializeRightPane() throws IOException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("RightPane.fxml"));
		loader.setController(mainWindowController);
		rightPane = loader.load();
	}

	private void initializeInfoPane() throws IOException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("InfoPane.fxml"));
		loader.setController(mainWindowController);
		infoPane = loader.load();
	}

	private void initializeChatPane() throws IOException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("ChatPane.fxml"));
		loader.setController(mainWindowController);
		chatPane = loader.load();
	}

	private void initializeAudioPane() throws IOException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("AudioPane.fxml"));
		loader.setController(mainWindowController);
		audioPane = loader.load();
	}

	private void initializeFriendlistPane() throws IOException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("FriendlistPane.fxml"));
		loader.setController(mainWindowController);
		friendlistPane = loader.load();
	}

	private void initializeFriendsearchResultPane() throws IOException {
		FXMLLoader loader =
				new FXMLLoader(LoginWindow.class.getResource("FriendsearchResultPane.fxml"));
		loader.setController(mainWindowController);
		friendsearchResultPane = loader.load();
	}

	private void initializeRequestPane() throws IOException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("RequestWindow.fxml"));
		loader.setController(mainWindowController);
		requestPane = loader.load();
	}

}
