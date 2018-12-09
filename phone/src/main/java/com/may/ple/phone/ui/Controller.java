package com.may.ple.phone.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class Controller {
	@FXML 
	private TextField input;
	@FXML 
	private Button call;
	@FXML 
	private ListView listView;
	
	@FXML
	private void initialize() {
		System.out.println("Controller init.");
		
		String test[] = new String[] {"1. Spring", "2. Summer", "3. Fall", "4. Winter", "5. Rain",
									  "6. Spring", "7. Summer", "8. Fall", "9. Winter", "10. Rain",
									  "11. Spring", "12. Summer", "13. Fall", "14. Winter", "15. Rain",
									  "16. Spring", "17. Summer", "18. Fall", "19. Winter", "20. Rain"}; 
		
		ObservableList<String> seasonList = FXCollections.<String>observableArrayList(test);
		listView.setItems(seasonList);
	}
	
	@FXML protected void handleAction(ActionEvent event) {
		EventImpl.process(event, input);
    }
	@FXML protected void handleKeyAction(KeyEvent event) {
		EventImpl.process(event, call);
	}
	
}
