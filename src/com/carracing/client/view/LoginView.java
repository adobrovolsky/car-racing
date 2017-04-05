package com.carracing.client.view;

import java.io.IOException;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.User;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class LoginView extends StackPane {
	
	public static final String TITLE_LOGIN = "Login";
	public static final String TITLE_SIUNUP = "Sign up";
	
	@FXML private TextField fullname;
	@FXML private TextField login1;
	@FXML private PasswordField password1;
	@FXML private TextField login2;
	@FXML private PasswordField password2;
	@FXML private Label errorMessage;
	@FXML private StackPane errorContainer;
	@FXML private Label errorMessage1;
	@FXML private StackPane errorContainer1;
	@FXML private AnchorPane loginPane;
	@FXML private AnchorPane signupPane;
	
	private final RaceService service = RaceService.getInstance();
	
	public LoginView() {
		inflateLayout();
		showLoginPane();
		
		service.addListener(Action.ADD_USER,  (a, d) -> {
			Platform.runLater(() -> {
				if (d == null) {
					errorMessage1.setText("Incorrect login or password!");
					errorContainer1.setVisible(true);
				} else {
					errorContainer1.setVisible(false);
					service.setUser((User)d );
				}
			});
		});
		
		service.addListener(Action.CHECK_SIGNUP_RESULT, (a, d) -> {
			Platform.runLater(() -> {
				boolean successful = (boolean) d;
				if (successful) {
					errorContainer.setVisible(false);
					showLoginPane();
				} else {
					errorMessage.setText("Incorrect entered data");
					errorContainer.setVisible(true);
				}
			});
		});
	}
	
	private void showLoginPane() {
		signupPane.setVisible(false);
		loginPane.setVisible(true);
	}
	
	private void showSignupPane() {
		loginPane.setVisible(false);
		signupPane.setVisible(true);
	}
	
	@FXML private void handleLogin(ActionEvent event) {
		User newUser = new User();
		newUser.setLogin(login1.getText());
		newUser.setPassword(password1.getText());

		service.send(new Command(Action.LOGIN, newUser));
	}

	@FXML private void handleSignUp(ActionEvent event) {
		User newUser = new User();
		newUser.setFullname(fullname.getText());
		newUser.setLogin(login2.getText());
		newUser.setPassword(password2.getText());
		
		service.send(new Command(Action.SIGNUP, newUser));
	}
	
	@FXML private void showLoginForm(ActionEvent event) {
		showLoginPane();
	}

	@FXML private void showSignupForm(ActionEvent event) {
		showSignupPane();
	}

	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
