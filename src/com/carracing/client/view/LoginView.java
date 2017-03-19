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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class LoginView {
	
	public static final String TITLE_LOGIN = "Login";
	public static final String TITLE_SIUNUP = "Sign up";
	
	@FXML private TextField fullname;
	@FXML private TextField login;
	@FXML private PasswordField password;
	@FXML private Label errorMessage;
	@FXML private StackPane errorContainer;
	
	private final RaceService service = RaceService.getInstance();
	private Window mod;
	private Scene loginScene;
	private Scene signupScene;
	private Stage stage;
	
	public LoginView() {
		this(null);
	}
	
	public LoginView(Window parent) {
		this.mod = parent;
		inflateLayout();
		service.addListener(Action.ADD_USER,  (a, d) -> {
			Platform.runLater(() -> {
				if (d == null) {
					errorMessage.setText("Incorrect login or password!");
					errorContainer.setVisible(true);
				} else {
					errorContainer.setVisible(false);
					service.setUser((User)d);
					stage.close();
				}
			});
		});
		
		service.addListener(Action.CHECK_SIGNUP_RESULT, (a, d) -> {
			Platform.runLater(() -> {
				boolean successful = (boolean) d;
				if (successful) {
					errorContainer.setVisible(false);
					showLogin();
				} else {
					errorMessage.setText("Incorrect entered data");
					errorContainer.setVisible(true);
				}
			});
		});
	}
	
	public void showLogin() {
		stage.setTitle(TITLE_LOGIN);
		stage.setScene(loginScene);
		stage.show();
	}
	
	public void showSignup() {
		stage.setTitle(TITLE_SIUNUP);
		stage.setScene(signupScene);
		stage.show();
	}
	

	@FXML private void handleLogin(ActionEvent event) {
		User newUser = new User();
		newUser.setLogin(login.getText());
		newUser.setPassword(password.getText());

		service.send(new Command(Action.LOGIN, newUser));
	}

	@FXML private void handleSignUp(ActionEvent event) {
		User newUser = new User();
		newUser.setFullname(fullname.getText());
		newUser.setLogin(login.getText());
		newUser.setPassword(password.getText());
		
		service.send(new Command(Action.SIGNUP, newUser));
	}
	
	@FXML private void showLoginForm(ActionEvent event) {
		showLogin();
	}

	@FXML private void showSignupForm(ActionEvent event) {
		showSignup();
	}


	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
		
		FXMLLoader loader2 = new FXMLLoader(getClass().getResource("signup.fxml"));
		
		try {
			Parent loginRoot = loader.load();
			loginScene = new Scene(loginRoot);
			
			Parent singupRoot = loader2.load();
			signupScene = new Scene(singupRoot);
			
			stage = new Stage();
			//stage.initModality(Modality.WINDOW_MODAL);
			//stage.initOwner(mod);
				
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
