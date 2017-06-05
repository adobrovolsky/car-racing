package com.carracing.client.view;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
	
	public interface Listener {
		void onLogin(User user);
	}
	
	public static final String TITLE_LOGIN = "Login";
	public static final String TITLE_SIUNUP = "Sign up";
	
	@FXML private TextField fullname;
	@FXML private TextField login1;
	@FXML private TextField login2;
	@FXML private PasswordField password1;
	@FXML private PasswordField password2;
	@FXML private Label errorMessage1;
	@FXML private Label errorMessage2;
	@FXML private StackPane errorContainer1;
	@FXML private StackPane errorContainer2;
	@FXML private AnchorPane loginPane;
	@FXML private AnchorPane signupPane;
	
	private final RaceService service = RaceService.getInstance();
	
	/**
	 * Keeps all users who are logged in successfully.
	 */
	private static Set<User> loggedOnUsers = new HashSet<>();
	
	/**
	 * This listener will be notified of successful login.
	 */
	private Listener listener;
	
	/**
	 * Authorized user.
	 */
	private User user;
	
	public LoginView() {
		this(null);
	}
	
	public LoginView(Listener listener) {
		this.listener = listener;
		inflateLayout();
		showLoginPane();
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	public User getUser() {
		return user;
	}
	
	/**
	 * A simple way to authorize users.
	 * 
	 * @return true if the user is not null, otherwise false. 
	 */
	public boolean isLogin() {
		return user != null;
	}
	
	public void logout() {
		loggedOnUsers.remove(user);
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
		newUser.setLogin(login1.getText().trim());
		newUser.setPassword(password1.getText().trim());

		service.send(new Command(Action.LOGIN, newUser), this::handleLogin);
	}

	@FXML private void handleSignUp(ActionEvent event) {
		String errorMsg = validateEnteredData();
		if (!errorMsg.isEmpty()) {
			showSingupError(errorMsg);
			return;
		}
		
		User newUser = new User();
		newUser.setFullname(fullname.getText().trim());
		newUser.setLogin(login2.getText().trim());
		newUser.setPassword(password2.getText().trim());
		
		service.send(new Command(Action.SIGNUP, newUser), this::handleSignup);
	}
	
	private String validateEnteredData() {
		String fullnameValue = fullname.getText().trim();
		String loginValue = login2.getText().trim();
		String passwordValue = password2.getText().trim();
		StringBuilder errorMsg = new StringBuilder();
		
		if (!fullnameValue.matches("^\\w+$")) {
			errorMsg.append("- The Fullname is incorrect!\n");
		}
		if (!loginValue.matches("^\\w+$")) {
			errorMsg.append("- The Login is incorrect!\n");
		}
		if (!passwordValue.matches("^\\w+$")) {
			errorMsg.append("- The Password is incorrect!\n");
		}
		
		return errorMsg.toString();
	}
	
	@FXML private void showLoginForm(ActionEvent event) {
		showLoginPane();
	}

	@FXML private void showSignupForm(ActionEvent event) {
		showSignupPane();
	}
	
	public void handleSignup(Action a, Object d) {
		Platform.runLater(() -> {
			boolean successful = (boolean) d;
			if (successful) {
				hideSignupError();
				showLoginPane();
			} else {
				showSingupError("User already exists! Please try again");
			}
		});
	}
	
	public void handleLogin(Action a, Object d) {
		Platform.runLater(() -> {
			if (d == null) {
				showLoginError("Incorrect login or password!");
			} else {
				hideLoginError();
				User user = (User) d;
				
				if (loggedOnUsers.contains(user)) {
					showLoginError("Error. Can't Sign in with this User. User already connected");
					return;
				}
				
				loggedOnUsers.add(user);
				this.user = user;
				if (listener != null) listener.onLogin(user);
			}
		});
	}
	
	private void showLoginError(String msg) {
		errorMessage1.setText(msg);
		errorContainer1.setVisible(true);
	}
	
	private void hideLoginError() {
		errorContainer1.setVisible(true);
	}
	
	private void showSingupError(String msg) {
		errorMessage2.setText(msg);
		errorContainer2.setVisible(true);
	}
	
	private void hideSignupError() {
		errorContainer2.setVisible(false);
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
