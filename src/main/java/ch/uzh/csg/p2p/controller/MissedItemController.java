package ch.uzh.csg.p2p.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class MissedItemController {

	@FXML
	AnchorPane missedItemAnchorPane;

	@FXML
	Label messageLabel;

	@FXML
	Label dateTimeLabel;

	public MissedItemController() {}

	public void setClickHandler(EventHandler<MouseEvent> clickHandler) {
		missedItemAnchorPane.setOnMouseClicked(clickHandler);
	}

	public void removeMyself() {
		VBox parent = (VBox) missedItemAnchorPane.getParent();
		parent.getChildren().remove(missedItemAnchorPane);
	}

	public void setMessage(String message) {
		messageLabel.setText(message);
	}

	public void setDateTime(Date date) {
		dateTimeLabel.setText(formatDate(date));
	}

	public String formatDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		Calendar today = Calendar.getInstance();
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);
		DateFormat timeFormatter = new SimpleDateFormat("HH:mm");

		if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
			return "Today, " + timeFormatter.format(date);
		} else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
			return "Yesterday, " + timeFormatter.format(date);
		} else {
			DateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yy HH:mm");
			return dateTimeFormatter.format(date);
		}
	}

}
