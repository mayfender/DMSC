package com.may.ple.phone.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FXMLUI extends Application {
	
	@Override
	public void init() throws Exception {
		super.init();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml_example.fxml"));
			Scene scene = new Scene(root);
	        
	        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResource("logo.png").toString()));
	        primaryStage.setTitle("FXML DMS Phone : ");
	        primaryStage.setScene(scene);
	        primaryStage.setResizable(false);
	        primaryStage.sizeToScene();
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
