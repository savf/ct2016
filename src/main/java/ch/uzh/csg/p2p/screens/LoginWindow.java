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

	private Stage stage;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		initialiseWindow();
	}


	private void initialiseWindow() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginWindow.fxml"));
		AnchorPane pane = loader.load();

		LoginWindowController loginWindowController = loader.getController();
		loginWindowController.setLoginWindow(this);

		Scene scene = new Scene(pane);

		scene.getStylesheets().add("basic.css");

		stage.setScene(scene);
		stage.setTitle(TITLE);
		stage.show();
	}

	public Stage getStage() {
		return stage;
	}

}
