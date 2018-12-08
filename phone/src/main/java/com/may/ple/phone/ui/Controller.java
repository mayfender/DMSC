package com.may.ple.phone.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class Controller {
	@FXML private TextField input;
	@FXML private Button call;
	
	@FXML protected void handleAction(ActionEvent event) {
		EventImpl.process(event, input);
    }
	@FXML protected void handleKeyAction(KeyEvent event) {
		EventImpl.process(event, call);
	}
	
}
