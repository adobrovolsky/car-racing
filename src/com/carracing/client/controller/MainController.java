package com.carracing.client.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.client.RaceService.ActionListener;
import com.carracing.client.view.CarInfoView;
import com.carracing.client.view.CarRacing;
import com.carracing.client.view.LoginView;
import com.carracing.client.view.ReportsView;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.RaceSummary;
import com.carracing.shared.model.User;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements ActionListener {

	private final RaceService service = RaceService.getInstance();

	@FXML private ListView<Race> racesListView;
	@FXML private VBox carsContainer;
	@FXML private Label greeting;
	
	private Scene loginScene;
	private Scene signupScene;
	private Stage userStage;
	
	private Stage stage;
	
	public void showLogin() {
		userStage.setTitle(UserController.TITLE_LOGIN);
		userStage.setScene(loginScene);
		userStage.show();
	}
	
	public void showSignup() {
		userStage.setTitle(UserController.TITLE_SIUNUP);
		userStage.setScene(signupScene);
		userStage.show();
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}

	@FXML public void handleLoginAction(ActionEvent event) throws IOException {
		showLogin();
	}

	@FXML public void handleSignupAction(ActionEvent event) throws IOException {
		showSignup();
	}

	public void initialize() {
		for (int i = 0; i < Race.NUMBER_CARS; i++) {
			carsContainer.getChildren().add(new CarInfoView());
		}
		
		service.addListener(Action.ADD_RACES, this);
		service.addListener(Action.FINISH_GAME, this);
		service.addListener(Action.ADD_USER, this);
		service.send(new Command(Action.OBTAIN_RASES));

		racesListView.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
			ObservableList<Node> children = carsContainer.getChildren();
			Iterator<Car> cars = newVal.getCars().iterator();

			for (int i = 0; i < children.size() && cars.hasNext(); i++) {
				((CarInfoView) children.get(i)).setCar(cars.next());
			}
		});
		
		showNewWindow(new CarRacing(), CarRacing.TITLE);
		showNewWindow(new ReportsView(), ReportsView.TITLE);
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/login.fxml"));
			Parent loginRoot = loader.load();
			loginScene = new Scene(loginRoot);
			
			FXMLLoader loader2 = new FXMLLoader(getClass().getResource("../view/signup.fxml"));
			Parent singupRoot = loader2.load();
			signupScene = new Scene(singupRoot);
			
			userStage = new Stage();
			userStage.initModality(Modality.WINDOW_MODAL);
			userStage.initOwner(stage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void showNewWindow(Parent parent, String title) {
		Scene scene = new Scene(parent);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(title);
		stage.show();
	}

	@Override
	public void actionPerformed(Action action, Object data) {
		Platform.runLater(() -> {
			switch (action) {
			case ADD_RACES: handleAddRaces((List<Race>) data); break;
			case FINISH_GAME: handleFinishGame((RaceSummary) data); break;
			case ADD_USER: handleAddUser(data); break;
			}
		});
	}
	
	private void handleAddUser(Object data) {
		if(data != null) {
			User user = (User) data;
			greeting.setText("Hello " + user.getFullname());
		}
	}

	private void handleFinishGame(RaceSummary summary) {
		racesListView.getItems().remove(summary.getRace());
	}

	public void handleAddRaces(List<Race> races) {
		ObservableList<Race> items = FXCollections.observableArrayList(races);
		racesListView.setItems(items);
		racesListView.getSelectionModel().selectFirst();
	}
}
