package com.may.ple.phone.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Ui1 extends Application {
	private TextField textField1;
	private Button call;
	
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			String fontSize = "-fx-font-size: 12pt;";
			
			Button button1 = new Button("1"); 
			button1.setStyle(fontSize);
			Button button2 = new Button("2");  
			button2.setStyle(fontSize);
			Button button3 = new Button("3");  
			button3.setStyle(fontSize);
			
			Button button4 = new Button("4"); 
			button4.setStyle(fontSize);
			Button button5 = new Button("5");  
			button5.setStyle(fontSize);
			Button button6 = new Button("6");  
			button6.setStyle(fontSize);
			
			Button button7 = new Button("7"); 
			button7.setStyle(fontSize);
			Button button8 = new Button("8");  
			button8.setStyle(fontSize);
			Button button9 = new Button("9"); 
			button9.setStyle(fontSize);
			
			Button asterisk = new Button("*"); 
			asterisk.setStyle(fontSize);
			Button button0 = new Button("0");  
			button0.setStyle(fontSize);
			Button pound = new Button("#");  
			pound.setStyle(fontSize);
			
			this.textField1 = new TextField();
			this.textField1.setStyle(fontSize);
			this.textField1.setOnKeyPressed(ke -> {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					System.out.println("Triger call");
	            	call.fire();
	            }
			});
			
			call = new Button("Call"); 
			call.setStyle(fontSize);
			call.setOnAction(event -> {
				try {
					System.out.println("call..." + textField1.getText());
//					wobj.API_Call(-1, textField1.getText().trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Button end = new Button("End"); 
			end.setStyle(fontSize);
			end.setOnAction(event -> {
				try {
					System.out.println("end..." + textField1.getText());
//					wobj.API_Hangup(-1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Button reg = new Button("Regist"); 
			reg.setStyle(fontSize);
			reg.setOnAction(event -> {
				try {
//					wobj.API_Register("192.168.2.253", "100", "abc123");
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Button unReg = new Button("Unregist"); 
			unReg.setStyle(fontSize);
			unReg.setOnAction(event -> {
				try {
//					wobj.API_Unregister();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			
			GridPane gridPane = new GridPane();
			gridPane.setMinSize(500, 200);
			gridPane.setPadding(new Insets(10, 10, 10, 10)); 
			gridPane.setVgap(5); 
			gridPane.setHgap(5); 
			
			gridPane.add(button1, 0, 0); 
			gridPane.add(button2, 1, 0); 
			gridPane.add(button3, 2, 0); 
			gridPane.add(this.textField1, 3, 0); 
			gridPane.add(call, 4, 0); 
			gridPane.add(end, 5, 0); 
			
			gridPane.add(button4, 0, 1); 
			gridPane.add(button5, 1, 1); 
			gridPane.add(button6, 2, 1); 
			gridPane.add(reg, 3, 1); 
			gridPane.add(unReg, 4, 1); 
			
			gridPane.add(button7, 0, 2); 
			gridPane.add(button8, 1, 2); 
			gridPane.add(button9, 2, 2); 
			
			gridPane.add(asterisk, 0, 3); 
			gridPane.add(button0, 1, 3); 
			gridPane.add(pound, 2, 3); 
			
	        Scene scene = new Scene(gridPane);
	        scene.setFill(Color.BROWN);
	        
	        //--------
	        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("icon.png").toString()));
	        primaryStage.setTitle("DMS Phone : ");
	        primaryStage.setScene(scene);
	        primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
