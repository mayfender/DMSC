package com.may.ple.phone.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Ui2 extends Application {
	
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml_example.fxml"));
			
			Scene scene = new Scene(root, 300, 275);
	        scene.setFill(Color.BROWN);
	        
	        //--------
	        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("icon.png").toString()));
	        primaryStage.setTitle("FXML Welcome");
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
