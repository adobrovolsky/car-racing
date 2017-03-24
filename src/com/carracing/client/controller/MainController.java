package com.carracing.client.controller;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.carracing.client.RaceService;
import com.carracing.client.RaceService.ActionListener;
import com.carracing.client.view.CarInfoView;
import com.carracing.client.view.CarRacing;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements ActionListener {

	private final RaceService service = RaceService.getInstance();
	private final Queue<Stage> windowQueue = new LinkedList<>();

	@FXML private ListView<Race> racesListView;
	@FXML private VBox carsContainer;
	@FXML private Label greeting;
	@FXML private SplitPane splitPane;
	
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
	
	@FXML private void handleAddRace(ActionEvent event) {
		showRaceWindow(new CarRacing());
	}

	@FXML public void handleLoginAction(ActionEvent event) {
		showLogin();
	}

	@FXML public void handleSignupAction(ActionEvent event) {
		showSignup();
	}

	public void initialize() {
		splitPane.setDividerPosition(0, 0.2);
		for (int i = 0; i < Race.NUMBER_CARS; i++) {
			carsContainer.getChildren().add(new CarInfoView());
		}
		
		service.addListener(Action.ADD_RACES, this);
		service.addListener(Action.FINISH_GAME, this);
		service.addListener(Action.ADD_USER, this);
		service.addListener(Action.ADD_ACTIVE_RACE, this);

		racesListView.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
			ObservableList<Node> children = carsContainer.getChildren();
			Iterator<Car> cars = newVal.getCars().iterator();

			for (int i = 0; i < children.size() && cars.hasNext(); i++) {
				((CarInfoView) children.get(i)).setCar(cars.next());
			}
		});
		
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
		
		showRaceWindow(new CarRacing());
		showRaceWindow(new CarRacing());
		showRaceWindow(new CarRacing());
		createStage(new ReportsView(), ReportsView.TITLE).show();
		service.send(new Command(Action.OBTAIN_RASES));
	}
	
	private void showRaceWindow(CarRacing carRacing) {
		Stage stage = createStage(carRacing, CarRacing.TITLE);
		stage.setOnCloseRequest(e -> {
			windowQueue.remove(stage);
			carRacing.close();
		});
		windowQueue.add(stage);
		stage.show();
	}
		
	private Stage createStage(Parent parent, String title) {
		Scene scene = new Scene(parent);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(title);
		return stage;
	}

	@Override
	public void actionPerformed(Action action, Object data) {
		Platform.runLater(() -> {
			switch (action) {
			case ADD_RACES: handleAddRaces((List<Race>) data); break;
			case FINISH_GAME: handleFinishGame((RaceSummary) data); break;
			case ADD_USER: handleAddUser(data); break;
			case ADD_ACTIVE_RACE: handleAddActiveRace((Race) data); break;
			}
		});
	}
	
	private void handleAddActiveRace(Race race) {
		Stage stage = windowQueue.poll();
		if (stage != null) {
			stage.setTitle(race.toString());
			CarRacing carRacing = (CarRacing) stage.getScene().getRoot();
			carRacing.startRace(race);
		} else {
			CarRacing carRacing = new CarRacing();
			stage = createStage(carRacing, race.toString());
			stage.setOnCloseRequest(e -> carRacing.close());
			stage.show();
			carRacing.startRace(race);
		}
	}

	private void handleAddUser(Object data) {
		if(data != null) {
			User user = (User) data;
			greeting.setText("Hello " + user.getFullname() + "!");
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
