package com.may.ple.phone.ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public class ManageEvents {
	private Scene scene;
	
	public ManageEvents(Scene scene) {
		this.scene = scene;
	}
	
	public void process() {
		Button call = (Button)this.scene.lookup("#call");
		call.setStyle(Style.FONT_SIZE);
		
		TextField input = (TextField)this.scene.lookup("#input");
		input.setStyle(Style.FONT_SIZE);
		input.setOnKeyPressed(ke -> {
			if (ke.getCode().equals(KeyCode.ENTER)) {
				System.out.println("Triger call");
				call.fire();
            }
		});
		
		call.setOnAction(event -> {
			try {
				System.out.println("call..." + input.getText());
//				wobj.API_Call(-1, input.getText().trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		Button end = (Button)this.scene.lookup("#end");
		end.setStyle(Style.FONT_SIZE);
		end.setOnAction(event -> {
			try {
				System.out.println("end..." + input.getText());
//				wobj.API_Hangup(-1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		
		/*Button reg = new Button("Regist"); 
		reg.setStyle(FONT_SIZE);
		reg.setOnAction(event -> {
			try {
				wobj.API_Register("192.168.2.253", "100", "abc123");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Button unReg = new Button("Unregist"); 
		unReg.setStyle(FONT_SIZE);
		unReg.setOnAction(event -> {
			try {
				wobj.API_Unregister();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}); */
	}
	
}
