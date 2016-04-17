package ch.uzh.csg.p2p.screens;

import java.io.IOException;

import ch.uzh.csg.p2p.controller.LoginWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginWindow extends Application {

	private final String TITLE = "SkypeClone - Login";
	private final int WIDTH = 400;
	private final int HEIGHT = 400;

	private Stage stage;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		initialiseWindow();
	}

	private void initialiseWindow() throws IOException {
		FXMLLoader loader = new FXMLLoader(LoginWindow.class.getResource("LoginWindow.fxml"));
		AnchorPane pane = loader.load();

		stage.setHeight(HEIGHT);
		stage.setWidth(WIDTH);

		LoginWindowController loginWindowController = loader.getController();
		loginWindowController.setLoginWindow(this);

		Scene scene = new Scene(pane);

		String css = LoginWindow.class.getResource("basic.css").toExternalForm();
		scene.getStylesheets().add(css);

		stage.setScene(scene);
		stage.setTitle(TITLE);
		stage.show();
	}

	public Stage getStage() {
		return stage;
	}

}
