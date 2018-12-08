/*package com.may.ple.phone.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
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
	        scene.getStylesheets().add(getClass().getClassLoader().getResource("style.css").toExternalForm());
	        scene.setFill(Color.BROWN);
	        	        
	        //2.
	        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("logo.png").toString()));
	        primaryStage.setTitle("DMS Phone : ");
	        primaryStage.setScene(scene);
	        primaryStage.setResizable(false);
	        primaryStage.sizeToScene();
	        primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createNodes(GridPane gridPane) {
		GridPane gridPane1 = new GridPane();
		gridPane1.getStyleClass().add("gridPane1");
		
		GridPane gridPane2 = new GridPane();
		gridPane2.getStyleClass().add("gridPane2");
		
		GridPane gridPane3 = new GridPane();
		gridPane3.getStyleClass().add("gridPane3");
		
		GridPane gridPane4 = new GridPane();
		gridPane4.getStyleClass().add("gridPane4");
		
		Button button1 = new Button("1"); 
		button1.getStyleClass().add("keypad");
		button1.setId("1");
		button1.setOnAction(e -> EventImpl.process(e));
		
		Button button2 = new Button("2");
		button2.getStyleClass().add("keypad");
		button2.setId("2");
		button2.setOnAction(e -> EventImpl.process(e));
		
		Button button3 = new Button("3");
		button3.getStyleClass().add("keypad");
		button3.setId("3");
		button3.setOnAction(e -> EventImpl.process(e));
		
		Button button4 = new Button("4");
		button4.getStyleClass().add("keypad");
		button4.setId("4");
		button4.setOnAction(e -> EventImpl.process(e));
		
		Button button5 = new Button("5");
		button5.getStyleClass().add("keypad");
		button5.setId("5");
		button5.setOnAction(e -> EventImpl.process(e));
		
		Button button6 = new Button("6");
		button6.getStyleClass().add("keypad");
		button6.setId("6");
		button6.setOnAction(e -> EventImpl.process(e));
		
		Button button7 = new Button("7");
		button7.getStyleClass().add("keypad");
		button7.setId("7");
		button7.setOnAction(e -> EventImpl.process(e));
		
		Button button8 = new Button("8");
		button8.getStyleClass().add("keypad");
		button8.setId("8");
		button8.setOnAction(e -> EventImpl.process(e));
		
		Button button9 = new Button("9");
		button9.getStyleClass().add("keypad");
		button9.setId("9");
		button9.setOnAction(e -> EventImpl.process(e));
		
		Button asterisk = new Button("*");
		asterisk.getStyleClass().add("keypad");
		asterisk.setId("asterisk");
		asterisk.setOnAction(e -> EventImpl.process(e));
		
		Button button0 = new Button("0");
		button0.getStyleClass().add("keypad");
		button0.setId("0");
		button0.setOnAction(e -> EventImpl.process(e));
		
		Button pound = new Button("#");
		pound.getStyleClass().add("keypad");
		pound.setId("pound");
		pound.setOnAction(e -> EventImpl.process(e));
		
		ImageView iv = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("phone_1.png")));
		iv.setFitWidth(27);
		iv.setFitHeight(32);
		Button call = new Button("", iv);
		call.getStyleClass().add("call");
		call.setId("call");
		call.setOnAction(e -> EventImpl.process(e));
		call.setPadding(new Insets(3, 3, 3, 3));
		
		iv = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("hangup.png")));
		iv.setFitWidth(27);
		iv.setFitHeight(32);
		Button end = new Button("", iv); 
		end.getStyleClass().add("hangup");
		end.setId("end");
		end.setOnAction(e -> EventImpl.process(e));
		end.setPadding(new Insets(3, 3, 3, 3));
		
		Button reg = new Button("Regist"); 
		reg.setId("reg");
		reg.setStyle(Style.FONT_SIZE);
		reg.setOnAction(e -> EventImpl.process(e));
		
		Button unReg = new Button("Unregist"); 
		unReg.setId("unReg");
		unReg.setStyle(Style.FONT_SIZE);
		unReg.setOnAction(e -> EventImpl.process(e));
		
		TextField input = new TextField();
		input.getStyleClass().add("input");
		input.setId("input");
		input.setPromptText("Enter name or number");
		input.setOnKeyPressed(e -> EventImpl.process(e));
		input.setOnKeyReleased(e -> EventImpl.process(e));
		
		gridPane1.add(input, 0, 0);
		gridPane1.add(call, 1, 0); 
		gridPane1.add(end, 2, 0); 
		gridPane.add(gridPane1, 0, 0, 2, 1); // Row 0
		
		Button test = new Button("<>");
		test.setOnAction(e -> EventImpl.process(e));
		gridPane2.add(test, 0, 0);
		gridPane.add(gridPane2, 0, 1); // Row 1, Cell 0
		
		gridPane3.add(button1, 0, 0); 
		gridPane3.add(button2, 1, 0); 
		gridPane3.add(button3, 2, 0); 
		
		gridPane3.add(button4, 0, 1); 
		gridPane3.add(button5, 1, 1); 
		gridPane3.add(button6, 2, 1); 
		
		gridPane3.add(button7, 0, 2); 
		gridPane3.add(button8, 1, 2); 
		gridPane3.add(button9, 2, 2); 
		
		gridPane3.add(asterisk, 0, 3); 
		gridPane3.add(button0, 1, 3); 
		gridPane3.add(pound, 2, 3);
		gridPane.add(gridPane3, 1, 1); // Row 1, Cell 1
		
		Slider slider = new Slider();
		gridPane4.add(slider, 0, 0);
		gridPane.add(gridPane4, 0, 2, 2, 1); // Row 2
	}
	
	private GridPane layout() {
		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().add("grid-container");
//		gridPane.setGridLinesVisible(true);
		
		ColumnConstraints col1 = new ColumnConstraints();
	    col1.setHgrow( Priority.ALWAYS );

	    ColumnConstraints col2 = new ColumnConstraints();
	    col2.setHgrow( Priority.NEVER );
		
		gridPane.getColumnConstraints().addAll(col1, col2);
		
		//1. createNotes
		createNodes(gridPane);
		
		return gridPane;
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
*/