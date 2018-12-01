package com.may.ple.phone.ui;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class EventImpl {

	public static void process(ActionEvent e) {
		if(e.getSource() instanceof Button) {
			Button button = ((Button)e.getSource());
			
			if(button.getId() == null) return;
			
			if(button.getId().equals("call")) {
				TextField input = (TextField)Ui1.scene.lookup("#input");
				System.out.println("call..." + input.getText());
//				wobj.API_Call(-1, input.getText().trim());
			} else if(button.getId().equals("end")) {
				TextField input = (TextField)Ui1.scene.lookup("#input");
				System.out.println("end..." + input.getText());
//				wobj.API_Hangup(-1);				
			} else if(button.getId().equals("reg")) {
//				wobj.API_Register("192.168.2.253", "100", "abc123");				
			} else if(button.getId().equals("unReg")) {
//				wobj.API_Unregister();
			}
		}
	}
	
	public static void process(KeyEvent e) {
		if (e.getCode().equals(KeyCode.ENTER)) {
			System.out.println("Triger call");
			((Button)Ui1.scene.lookup("#call")).fire();
        }
	}

}
