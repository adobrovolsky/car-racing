package com.carracing.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.client.view.MainView;

import javafx.application.Application;
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
public class Client extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		MainView root = new MainView();
		Scene scene = new Scene(root);
		
		primaryStage.setTitle(MainView.TITLE);
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(event -> {
			WindowCounter.decrement();
			WindowCounter.executeIfZero(() -> finaly());
			root.close();
		});
		primaryStage.show();
		WindowCounter.incement();
	}
	
	public static void finaly() {
		try {
			RaceService.getInstance().close();
		} catch (Exception e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public static void main(String [] args) {
		launch(args);
	}
}
