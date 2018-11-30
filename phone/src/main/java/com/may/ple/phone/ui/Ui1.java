package com.may.ple.phone.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Ui1 extends Application {
	private Scene scene;
	
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			//1.
	        scene = new Scene(layout());
	        scene.setFill(Color.BROWN);
	        
	        //2.
	        new ManageEvents(scene).process();
	        
	        //3.
	        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("icon.png").toString()));
	        primaryStage.setTitle("DMS Phone : ");
	        primaryStage.setScene(scene);
	        primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createNotes(GridPane gridPane) {
		Button button1 = new Button("1"); 
		button1.setStyle(Style.FONT_SIZE);
		Button button2 = new Button("2");  
		button2.setStyle(Style.FONT_SIZE);
		Button button3 = new Button("3");  
		button3.setStyle(Style.FONT_SIZE);
		
		Button button4 = new Button("4"); 
		button4.setStyle(Style.FONT_SIZE);
		Button button5 = new Button("5");  
		button5.setStyle(Style.FONT_SIZE);
		Button button6 = new Button("6");  
		button6.setStyle(Style.FONT_SIZE);
		
		Button button7 = new Button("7"); 
		button7.setStyle(Style.FONT_SIZE);
		Button button8 = new Button("8");  
		button8.setStyle(Style.FONT_SIZE);
		Button button9 = new Button("9"); 
		button9.setStyle(Style.FONT_SIZE);
		
		Button asterisk = new Button("*"); 
		asterisk.setStyle(Style.FONT_SIZE);
		Button button0 = new Button("0");  
		button0.setStyle(Style.FONT_SIZE);
		Button pound = new Button("#");  
		pound.setStyle(Style.FONT_SIZE);
		
		Button end = new Button("End"); 
		end.setId("end");
		end.setStyle(Style.FONT_SIZE);
		
		Button call = new Button("Call");
		call.setId("call");
		call.setStyle(Style.FONT_SIZE);
		
		Button reg = new Button("Regist"); 
		reg.setStyle(Style.FONT_SIZE);
		
		Button unReg = new Button("Unregist"); 
		unReg.setStyle(Style.FONT_SIZE);
		
		TextField textField1 = new TextField();
		textField1.setId("input");
		textField1.setStyle(Style.FONT_SIZE);
		
		gridPane.add(button1, 0, 0); 
		gridPane.add(button2, 1, 0); 
		gridPane.add(button3, 2, 0); 
		gridPane.add(textField1, 3, 0); 
		gridPane.add(call, 4, 0); 
		gridPane.add(end, 5, 0); 
		
		gridPane.add(button4, 0, 1); 
		gridPane.add(button5, 1, 1); 
		gridPane.add(button6, 2, 1); 
//		gridPane.add(reg, 3, 1); 
//		gridPane.add(unReg, 4, 1); 
		
		gridPane.add(button7, 0, 2); 
		gridPane.add(button8, 1, 2); 
		gridPane.add(button9, 2, 2); 
		
		gridPane.add(asterisk, 0, 3); 
		gridPane.add(button0, 1, 3); 
		gridPane.add(pound, 2, 3);
	}
	
	private GridPane layout() {
		GridPane gridPane = new GridPane();
		gridPane.setMinSize(500, 200);
		gridPane.setPadding(new Insets(10, 10, 10, 10)); 
		gridPane.setVgap(5); 
		gridPane.setHgap(5);
		
		//1. createNotes
		createNotes(gridPane);
		
		return gridPane;
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
