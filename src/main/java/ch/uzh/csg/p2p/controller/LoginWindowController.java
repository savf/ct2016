package ch.uzh.csg.p2p.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.LineUnavailableException;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.uzh.csg.p2p.Node;
import ch.uzh.csg.p2p.helper.LoginHelper;
import ch.uzh.csg.p2p.model.UserInfo;
import ch.uzh.csg.p2p.screens.LoginWindow;
import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.futures.BaseFutureListener;

public class LoginWindowController implements Observer, Controller {

	private final String LOGINNODENAME = "loginnode";
	private Logger log = LoggerFactory.getLogger(getClass());

	private static final long TRY_AGAIN_TIME_WINDOW = 7000;

	private String username;
	private String password;
	private int nodeId;
	private String nodeIP;

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
	@FXML
	private AnchorPane modalOverlayPane;

	private LoginWindow loginWindow;
	private AnchorPane requestPane;
	private RequestPaneController requestPaneController;


	public void setLoginWindow(LoginWindow loginWindow) {
		this.loginWindow = loginWindow;
	}

	@FXML
	public void handleLogin() throws Exception {
		int id = getId();
		// check if ip not set --> is bootstrapnode
		if (bootstrapCB.isSelected()) {
			login(id, null);
		} else {
			String ip = ipText.getText();
			if (!ip.equals("")) {
				login(id, ip);
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("IP address is missing");
				String s = "Set IP address of bootstrap node or start as bootstrap";
				alert.setContentText(s);
				alert.showAndWait();
			}
		}
	}

	@FXML
	public void handleBootstrapCB() throws UnknownHostException {
		if (bootstrapCB.isSelected()) {
			ipText.setText(
					"The local IP address is: " + InetAddress.getLocalHost().getHostAddress());
			ipText.setDisable(true);
		} else {
			ipText.setText("");
			ipText.setDisable(false);
		}
	}


	private int getId() {
		int id = ((Long) System.currentTimeMillis()).intValue();
		return id;
	}

	private void login(final int id, final String ip) throws Exception {
		password = getPassword();
		username = usernameText.getText();
		this.nodeIP = ip;
		this.nodeId = id;
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher m = p.matcher(username);
		boolean usernameIsCorrectEMailAddress = m.matches();

		if (username.equals("") || password.equals("")) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Username or password empty");
			String s = "Username or password cannot be empty.";
			alert.setContentText(s);
			alert.showAndWait();
		} else if (!usernameIsCorrectEMailAddress) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Incorrect email format");
			String s = "The username has to be a valid email address.";
			alert.setContentText(s);
			alert.showAndWait();
		} else {
			if (ip != null) {
				// start a new node and check if the user already exists with the observer
				// update() method
				new Node(getId(), ip, LOGINNODENAME, "", false, this);
			} else {
				// ip null means bootstrap node, no user check needed
				final String ip2 = InetAddress.getLocalHost().getHostAddress();
				MainWindow mainWindow = new MainWindow();
				mainWindow.start(loginWindow.getStage(), id, ip2, username, password, true);
			}
		}
	}

	private String getPassword() throws NoSuchAlgorithmException {
		if (!passwordText.getText().equals("")) {
			return DigestUtils.shaHex(passwordText.getText() + LoginHelper.PASSWORD_SALT);
		} else {
			return "";
		}
	}

	public void update(Observable o, Object arg) {
		Node node = (Node) arg;
		final long time = System.currentTimeMillis();
		BaseFutureListener<FutureGet> userExistsListener = new BaseFutureListener<FutureGet>() {
			@Override
			public void operationComplete(FutureGet futureGet) throws ClassNotFoundException,
					IOException, LineUnavailableException, InterruptedException {
				if (futureGet.isSuccess()) {
					if (futureGet != null && futureGet.data() != null) {
						if (futureGet.data().object() instanceof UserInfo) {
							UserInfo user = (UserInfo) futureGet.data().object();
							if (user.getPassword().equals(password)) {
								shutdownNode(node);
								futureGet.removeListener(this);
								Platform.runLater(new Runnable() {
									public void run() {
										try {
											MainWindow mainWindow = new MainWindow();
											mainWindow.start(loginWindow.getStage(), nodeId, nodeIP,
													username, password, false);
										} catch (Exception e) {
											loginNotPossibleExceptionHandler();
										}
									}
								});
							} else {
								Platform.runLater(new Runnable() {
									public void run() {
										Alert alert = new Alert(AlertType.ERROR);
										alert.setTitle("Wrong username/password");
										String s = "Username/password combination is incorrect";
										alert.setContentText(s);
										alert.showAndWait();
									}
								});
							}
						}
					} else {
						// FutureGet was successful, but user does not yet exist
						shutdownNode(node);
						futureGet.removeListener(this);
						Platform.runLater(new Runnable() {
							public void run() {
								try {
									MainWindow mainWindow = new MainWindow();
									mainWindow.start(loginWindow.getStage(), nodeId, nodeIP,
											username, password, false);
								} catch (Exception e) {
									loginNotPossibleExceptionHandler();
								}
							}
						});
					}
				}
			}

			@Override
			public void exceptionCaught(Throwable t) throws Exception {}
		};
		try {
			LoginHelper.retrieveUserInfo(username, node, userExistsListener);
		} catch (Exception e) {
			loginNotPossibleExceptionHandler();
		}
	}

	protected void tryAgain(String name, Node node,
			BaseFutureListener<FutureGet> baseFutureListener)
			throws LineUnavailableException, InterruptedException {
		Thread.sleep(500);
		log.debug("LoginWindowController had unsuccessfull Request, Try again...");
		log.debug("Retrieve User " + name);
		LoginHelper.retrieveUserInfo(name, node, baseFutureListener);
	}

	public void setRequestPaneController(RequestPaneController requestPaneController) {
		this.requestPaneController = requestPaneController;
	}

	public void setRequestPane(AnchorPane requestPane) {
		this.requestPane = requestPane;
	}

	private void loginNotPossibleExceptionHandler() {
		Platform.runLater(new Runnable() {
			public void run() {
				if (!modalOverlayPane.isVisible()) {
					requestPaneController.makeDialog("Login not possible. Try it again!",
							new EventHandler<ActionEvent>() {

								public void handle(ActionEvent event) {}
							}, new EventHandler<ActionEvent>() {

								public void handle(ActionEvent event) {
									System.exit(0);
								}
							});
				}
			}
		});
	}

	protected void shutdownNode(Node node) {
		if (node != null) {
			node.shutdown();
		}
	}

	@Override
	public void showInformOverlay() {}

	@Override
	public void hideInformOverlay() {}

	@Override
	public void hideRequestOverlay() {
		modalOverlayPane.setVisible(false);
	}

	@Override
	public void showRequestOverlay() {
		modalOverlayPane.setVisible(true);
		modalOverlayPane.getChildren().clear();
		modalOverlayPane.getChildren().add(requestPane);
	}

}
