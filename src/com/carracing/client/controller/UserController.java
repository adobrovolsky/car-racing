package com.carracing.client.controller;

import java.io.IOException;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class UserController {

	public static final String TITLE_LOGIN = "Login";
	public static final String TITLE_SIUNUP = "Sign up";

	private final RaceService service = RaceService.getInstance();

	@FXML private TextField fullname;
	@FXML private TextField login;
	@FXML private PasswordField password;
	@FXML private Label errorMessage;
	@FXML private StackPane errorContainer;

	@FXML public void handleLogin(ActionEvent event) {
		User newUser = new User();
		newUser.setLogin(login.getText());
		newUser.setPassword(password.getText());

		service.send(new Command(Action.LOGIN, newUser));
	}

	@FXML public void handleSignUp(ActionEvent event) {
		User newUser = new User();
		newUser.setFullname(fullname.getText());
		newUser.setLogin(login.getText());
		newUser.setPassword(password.getText());
		
		service.send(new Command(Action.SIGNUP, newUser));
	}

	@FXML public void showLoginForm(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/login.fxml"));
		Parent parent = loader.load();
		Stage stage = (Stage) login.getScene().getWindow();
		stage.setScene(new Scene(parent));
		stage.setTitle(TITLE_LOGIN);
		stage.show();
	}

	@FXML public void showSignupForm(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/signup.fxml"));
		Parent parent = loader.load();
		Stage stage = (Stage) login.getScene().getWindow();
		stage.setScene(new Scene(parent));
		stage.setTitle(TITLE_SIUNUP);
		stage.show();
	}
	
	public void initialize() {
		service.addListener(Action.ADD_USER,  (a, d) -> {
			Platform.runLater(() -> {
				if (d == null) {
					errorMessage.setText("Incorrect login or password!");
					errorContainer.setVisible(true);
					
				} else {
					errorContainer.setVisible(false);
					service.setUser((User)d);
					
				}
			});
		});
		
		service.addListener(Action.CHECK_SIGNUP_RESULT, (a, d) -> {
			Platform.runLater(() -> {
				boolean successful = (boolean) d;
				if (successful) {
					errorContainer.setVisible(false);
					try {
						showLoginForm(null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					errorMessage.setText("Incorrect entered data");
					errorContainer.setVisible(true);
				}
			});
		});
	}
}
