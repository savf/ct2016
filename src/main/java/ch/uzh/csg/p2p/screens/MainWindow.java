package ch.uzh.csg.p2p.screens;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.controller.MainWindowController;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainWindow {

	private final String TITLE = "SkypeClone - Main";

	private Stage stage;
	private int id;
	private String ip;
	private String username;
	private String password;

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
		AnchorPane pane = loader.load();

		final MainWindowController mainWindowController = loader.getController();
		mainWindowController.setMainWindow(this);
		mainWindowController.startNode(id, ip, username, password);

		Scene scene = new Scene(pane);

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

}
