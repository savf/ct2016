package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.sound.sampled.LineUnavailableException;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.screens.LoginWindow;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginWindowController {

	private final String LOGINNODENAME = "loginnode";

	@FXML
	private Label title;
	@FXML
	private TextField usernameText;
	@FXML
	private PasswordField passwordText;
	@FXML
	private TextField ipText;

	private LoginWindow loginWindow;

	public void setLoginWindow(LoginWindow loginWindow) {
		this.loginWindow = loginWindow;
	}

	@FXML
	public void handleLogin() throws Exception {
		int id = getId();
		// check if ip not set --> is bootstrapnode
		if (ipText.getText().equals("")) {
			startMainWindow(id, null);
		} else {
			String ip = ipText.getText();
			if (!ip.equals("")) {
				startMainWindow(id, ip);
			}
		}
	}

	private int getId() {
		int id = ((Long) System.currentTimeMillis()).intValue();
		return id;
	}

	private void startMainWindow(int id, String ip) throws Exception {
		String password = getPassword();
		String username = usernameText.getText();
		if (checkUsernamePassword(username, password, id, ip)) {
			MainWindow mainWindow = new MainWindow();
			mainWindow.start(loginWindow.getStage(), id, ip, username, password);
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Wrong username/password");
			String s = "Username password combination not correct";
			alert.setContentText(s);
			alert.showAndWait();
		}
	}

	private Boolean checkUsernamePassword(String username, String password, int id, String ip)
			throws ClassNotFoundException, IOException, LineUnavailableException {
		Boolean isCorrect = true;
		if (username.equals("") || password.equals("")) {
			isCorrect = false;
		} else if (ip != null) {
			// if ip is null -> is first node in network --> no user exists
		  
			Node node = new Node(getId(), ip, LOGINNODENAME, "", null);
			isCorrect = LoginHelper.usernamePasswordCorrect(node, username, password);
			node.shutdown();
		}
		return isCorrect;
	}

	private String getPassword() throws NoSuchAlgorithmException {
		if (!passwordText.getText().equals("")) {
			return passwordText.getText();
		} else {
			return "";
		}
	}

}
