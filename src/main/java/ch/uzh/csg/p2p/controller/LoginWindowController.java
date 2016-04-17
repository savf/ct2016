package ch.uzh.csg.p2p.controller;

import ch.uzh.csg.p2p.screens.LoginWindow;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginWindowController {

	@FXML
	private Label title;
	@FXML
	private TextField usernameText;
	@FXML
	private TextField localPortText;
	@FXML
	private TextField remoteIPText;
	@FXML
	private TextField remotePortText;

	private LoginWindow loginWindow;

	public void setLoginWindow(LoginWindow loginWindow) {
		this.loginWindow = loginWindow;
	}

	@FXML
	public void handleNewNetwork() throws Exception {
		int id = getId();
		if (!localPortText.getText().equals("")) {
			int localport = Integer.parseInt(localPortText.getText());
			startMainWindow(id, localport, null, 0);
		} else {
			startMainWindow(id, -1, null, 0);
		}
	}

	@FXML
	public void handleJoinNetwork() throws Exception {
		int id = getId();
		String ip = remoteIPText.getText();
		if (!ip.equals("") && !remotePortText.getText().equals("") && !localPortText.getText().equals("")) {
			int localPort = Integer.parseInt(localPortText.getText());
			int remotePort = Integer.parseInt(remotePortText.getText());
			startMainWindow(id, localPort, ip, remotePort);
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Empty informations");
			String s = "Remote IP, remote port and local port cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		}
	}

	private int getId() {
		int id = ((Long) System.currentTimeMillis()).intValue();
		return id;
	}

	private void startMainWindow(int id, int localPort, String ip, int remotePort) throws Exception {
		if (checkUsername(usernameText.getText())) {
			MainWindow mainWindow = new MainWindow();
			mainWindow.start(loginWindow.getStage(), id, localPort, ip, remotePort, usernameText.getText());
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Empty username");
			String s = "Username cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		}
	}

	private Boolean checkUsername(String username) {
		Boolean isCorrect = true;
		if (username.equals("")) {
			isCorrect = false;
		}
		return isCorrect;
	}

}
