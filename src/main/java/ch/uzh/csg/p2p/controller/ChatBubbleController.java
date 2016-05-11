package ch.uzh.csg.p2p.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.uzh.csg.p2p.screens.MainWindow;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;

public class ChatBubbleController {

	@FXML
	private Label senderLbl;
	@FXML
	private Label messageLbl;
	@FXML
	private Label dateTimeLbl;
	@FXML
	private AnchorPane bubbleContainerAnchorPane;
	@FXML
	private Region chatBubbleCorner;

	Image bubbleMeImage =
			new Image(MainWindow.class.getResourceAsStream("/bubble_me.png"), 34, 35, false, true);
	Image bubblePeerImage = new Image(MainWindow.class.getResourceAsStream("/bubble_peer.png"), 34,
			35, false, true);
	BackgroundPosition backgroundPositionBottomLeft =
			new BackgroundPosition(Side.LEFT, 0, true, Side.BOTTOM, 0, true);
	BackgroundPosition backgroundPositionBottomRight =
			new BackgroundPosition(Side.RIGHT, 0, true, Side.BOTTOM, 0, true);
	BackgroundImage bubbleMeBackgroundImage = new BackgroundImage(bubbleMeImage,
			BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, backgroundPositionBottomRight,
			BackgroundSize.DEFAULT);
	BackgroundImage bubblePeerBackgroundImage = new BackgroundImage(bubblePeerImage,
			BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, backgroundPositionBottomLeft,
			BackgroundSize.DEFAULT);

	public void setMessage(String message, double maxWidth) {
		messageLbl.setText(message);
		if (maxWidth == 0) {
			maxWidth = 648.0;
		}
		bubbleContainerAnchorPane.setMinWidth(maxWidth * 0.75);
	}

	public void setSender(String sender) {
		chatBubbleCorner.setBackground(new Background(bubblePeerBackgroundImage));
		senderLbl.setText(sender);
	}
	
	public void setBackground(boolean fromMe) {
		if (fromMe) {
			chatBubbleCorner.setBackground(new Background(bubbleMeBackgroundImage));
		} else {
			chatBubbleCorner.setBackground(new Background(bubblePeerBackgroundImage));
		}
	}

	public void setDateTime() {
		DateFormat f = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
		dateTimeLbl.setText(f.format(new Date()));
	}


}
