package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import javax.sound.sampled.LineUnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.User;
import ch.uzh.csg.p2p.model.request.RequestListener;
import ch.uzh.csg.p2p.screens.LoginWindow;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.tomp2p.dht.FutureGet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginWindowController {

	private final String LOGINNODENAME = "loginnode";
	private Logger log = LoggerFactory.getLogger(getClass());

	@FXML
	private Label title;
	@FXML
	private TextField usernameText;
	@FXML
	private PasswordField passwordText;
	@FXML
	private TextField ipText;
	@FXML
	private CheckBox bootstrapCB;

	private LoginWindow loginWindow;

	public void setLoginWindow(LoginWindow loginWindow) {
		this.loginWindow = loginWindow;
	}

	@FXML
	public void handleLogin() throws Exception {
		int id = getId();
		// check if ip not set --> is bootstrapnode
		if (bootstrapCB.isSelected()) {
			startMainWindow(id, null);
		} else {
			String ip = ipText.getText();
			if (!ip.equals("")) {
				startMainWindow(id, ip);
			}else{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("IP address is missing");
				String s = "Set IP address of bootstrap node or start as bootstrap";
				alert.setContentText(s);
				alert.showAndWait();
			}
		}
	}
	
	@FXML public void handleBootstrapCB() throws UnknownHostException{
		if(bootstrapCB.isSelected()){
			ipText.setText("The local IP address is: "+InetAddress.getLocalHost().getHostAddress());
			ipText.setDisable(true);
		}else{
			ipText.setText("");
			ipText.setDisable(false);
		}
	}

	private int getId() {
		int id = ((Long) System.currentTimeMillis()).intValue();
		return id;
	}

	private void startMainWindow(int id, String ip) throws Exception {
		String password = getPassword();
		String username = usernameText.getText();
		
		if(username.equals("") || password.equals("")){
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Username or password empty");
			String s = "Username or password cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		}
		else {
			if (ip != null) {
				Node node = new Node(getId(), ip, LOGINNODENAME, "");
				RequestListener<User> userExistsListener = new RequestListener<User>(node){
					@Override
					public void operationComplete(FutureGet futureGet) throws Exception {
						if(futureGet != null && futureGet.isSuccess() && futureGet.data() != null) {
							if(futureGet.data().object() instanceof User) {
								User user = (User) futureGet.data().object();
								if (user.getPassword().equals(password)) {
									MainWindow mainWindow = new MainWindow();
									mainWindow.start(loginWindow.getStage(), id, ip, username, password);
								} else {
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Wrong username/password");
									String s = "Username/password combination is not correct";
									alert.setContentText(s);
									alert.showAndWait();
								}
								shutdownNode();
							}
						}
						if(futureGet.isSuccess()) {
							//  FutureGet was successful, but user does not yet exist
							MainWindow mainWindow = new MainWindow();
							mainWindow.start(loginWindow.getStage(), id, ip, username, password);
						}
					}
				};
				LoginHelper.retrieveUser(username, node, userExistsListener);
			} else {
				//  ip null means bootstrap node, no user check needed
				MainWindow mainWindow = new MainWindow();
				mainWindow.start(loginWindow.getStage(), id, ip, username, password);
			}
		}
	}

	private String getPassword() throws NoSuchAlgorithmException {
		// TODO: Add hash function
		if (!passwordText.getText().equals("")) {
			return passwordText.getText();
		} else {
			return "";
		}
	}

}
