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
	public static Scene scene;
	
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
	        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("icon.png").toString()));
	        primaryStage.setTitle("DMS Phone : ");
	        primaryStage.setScene(scene);
	        primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createNodes(GridPane gridPane) {
		Button button1 = new Button("1"); 
		button1.setStyle(Style.FONT_SIZE);
		button1.setOnAction(e -> EventImpl.process(e));
		
		Button button2 = new Button("2");  
		button2.setStyle(Style.FONT_SIZE);
		button2.setOnAction(e -> EventImpl.process(e));
		
		Button button3 = new Button("3");  
		button3.setStyle(Style.FONT_SIZE);
		button3.setOnAction(e -> EventImpl.process(e));
		
		Button button4 = new Button("4"); 
		button4.setStyle(Style.FONT_SIZE);
		button4.setOnAction(e -> EventImpl.process(e));
		
		Button button5 = new Button("5");  
		button5.setStyle(Style.FONT_SIZE);
		button5.setOnAction(e -> EventImpl.process(e));
		
		Button button6 = new Button("6");  
		button6.setStyle(Style.FONT_SIZE);
		button6.setOnAction(e -> EventImpl.process(e));
		
		Button button7 = new Button("7"); 
		button7.setStyle(Style.FONT_SIZE);
		button7.setOnAction(e -> EventImpl.process(e));
		
		Button button8 = new Button("8");  
		button8.setStyle(Style.FONT_SIZE);
		button8.setOnAction(e -> EventImpl.process(e));
		
		Button button9 = new Button("9"); 
		button9.setStyle(Style.FONT_SIZE);
		button9.setOnAction(e -> EventImpl.process(e));
		
		Button asterisk = new Button("*"); 
		asterisk.setStyle(Style.FONT_SIZE);
		asterisk.setOnAction(e -> EventImpl.process(e));
		
		Button button0 = new Button("0");  
		button0.setStyle(Style.FONT_SIZE);
		button0.setOnAction(e -> EventImpl.process(e));
		
		Button pound = new Button("#");  
		pound.setStyle(Style.FONT_SIZE);
		pound.setOnAction(e -> EventImpl.process(e));
		
		Button end = new Button("End"); 
		end.setId("end");
		end.setStyle(Style.FONT_SIZE);
		end.setOnAction(e -> EventImpl.process(e));
		
		Button call = new Button("Call");
		call.setId("call");
		call.setStyle(Style.FONT_SIZE);
		call.setOnAction(e -> EventImpl.process(e));
		
		Button reg = new Button("Regist"); 
		reg.setId("reg");
		reg.setStyle(Style.FONT_SIZE);
		reg.setOnAction(e -> EventImpl.process(e));
		
		Button unReg = new Button("Unregist"); 
		unReg.setId("unReg");
		unReg.setStyle(Style.FONT_SIZE);
		unReg.setOnAction(e -> EventImpl.process(e));
		
		TextField input = new TextField();
		input.setId("input");
		input.setPromptText("Enter name or number");
		input.setStyle(Style.FONT_SIZE);
		input.setOnKeyPressed(e -> EventImpl.process(e));
		
		gridPane.add(input, 0, 0, 3, 1);
		gridPane.add(call, 4, 0); 
		
		gridPane.add(button1, 0, 1); 
		gridPane.add(button2, 1, 1); 
		gridPane.add(button3, 2, 1); 
//		gridPane.add(end, 5, 0); 
		
		gridPane.add(button4, 0, 2); 
		gridPane.add(button5, 1, 2); 
		gridPane.add(button6, 2, 2); 
//		gridPane.add(reg, 3, 1); 
//		gridPane.add(unReg, 4, 1); 
		
		gridPane.add(button7, 0, 3); 
		gridPane.add(button8, 1, 3); 
		gridPane.add(button9, 2, 3); 
		
		gridPane.add(asterisk, 0, 4); 
		gridPane.add(button0, 1, 4); 
		gridPane.add(pound, 2, 4);
	}
	
	private GridPane layout() {
		GridPane gridPane = new GridPane();
		gridPane.setMinSize(500, 200);
		gridPane.setPadding(new Insets(10, 10, 10, 10)); 
		gridPane.setVgap(5); 
		gridPane.setHgap(5);
		
		//1. createNotes
		createNodes(gridPane);
		
		return gridPane;
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
