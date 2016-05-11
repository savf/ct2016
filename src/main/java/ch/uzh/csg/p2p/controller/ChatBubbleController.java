package ch.uzh.csg.p2p.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ChatBubbleController {

	@FXML
	private Label senderLbl;
	@FXML
	private Label messageLbl;
	@FXML
	private Label dateTimeLbl;
	@FXML
	private AnchorPane bubbleContainerAnchorPane;
	
	public void setMessage(String message, double maxWidth) {
		messageLbl.setText(message);
		if(maxWidth == 0){
			maxWidth = 648.0;
		}
		bubbleContainerAnchorPane.setMinWidth(maxWidth * 0.75);
	}
	
	public void setSender(String sender) {
		if(sender.equals("")) {
			senderLbl.setVisible(false);
		}
		else {
			senderLbl.setText(sender);
		}
	}
	
	public void setDateTime() {
		DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		dateTimeLbl.setText(f.format(new Date()));
	}
	
	
}
