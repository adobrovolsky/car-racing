package com.carracing.client;

import com.carracing.client.controller.MainController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This is the entry point to the program. 
 * To start the client, you need to run this class.
 * Launches the application main screen.
 * 
 * @see Application
 * @version 1.0
 */
public class Main extends Application {
	
	private static final String TITLE = "Car racing"; 

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));
		Parent parent = loader.load();
		MainController controller = loader.getController();
		controller.setStage(primaryStage);
		
		Scene scene = new Scene(parent);
		
		primaryStage.setTitle(TITLE);
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> {
			try {
				RaceService.getInstance().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		primaryStage.show();
	}
	
	public static void main(String [] args) {
		launch(args);
	}
}
