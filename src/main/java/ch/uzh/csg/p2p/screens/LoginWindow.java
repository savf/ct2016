package ch.uzh.csg.p2p.screens;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.controller.LoginWindowController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class LoginWindow extends Application {

  private final String TITLE = "Quack! - Login";
  private Logger log = LoggerFactory.getLogger(getClass());

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
    stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("icon.png")));

    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

      public void handle(WindowEvent event) {
        log.info("Login window has been closed - Shutting down.");
        stage.close();
        System.exit(0);
      }
    });

    stage.show();
  }

  public Stage getStage() {
    return stage;
  }

}
