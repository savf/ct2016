package ch.uzh.csg.p2p.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class RequestPaneController {

	@FXML
	private Label requestPaneLabel;

	@FXML
	private Button requestPaneAcceptBtn;

	@FXML
	private Button requestPaneRejectBtn;

	@FXML
	private Label informPaneLabel;

	@FXML
	private Button informPaneOkBtn;

	private Controller mainWindowController;

	public RequestPaneController(Controller mainWindowController) {
		this.mainWindowController = mainWindowController;
	}

	public void makeDialog(String dialogText, EventHandler<ActionEvent> acceptHandler,
			EventHandler<ActionEvent> rejectHandler) {
		mainWindowController.showRequestOverlay();
		requestPaneLabel.setText(dialogText);
		requestPaneAcceptBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				mainWindowController.hideRequestOverlay();
				acceptHandler.handle(event);
			}
		});
		requestPaneRejectBtn.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				mainWindowController.hideRequestOverlay();
				rejectHandler.handle(event);
			}
		});
	}

	public void inform(String information) {
		mainWindowController.showInformOverlay();
		informPaneLabel.setText(information);
		informPaneOkBtn.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				mainWindowController.hideInformOverlay();
			}
		});
	}

}
