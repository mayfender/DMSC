package com.may.ple.phone;

import com.may.ple.phone.ui.FXMLUI;

import javafx.application.Application;

public class App {

	public static void main(String[] args) {
		System.out.println("Start Phone...");
		Application.launch(FXMLUI.class, args);
		System.out.println("End Start Phone...");
	}
	
}
