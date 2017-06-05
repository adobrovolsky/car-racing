package com.carracing.client.view;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.carracing.client.Client;
import com.carracing.client.RaceService;
import com.carracing.client.WindowCounter;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.RaceSummary;

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
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainView extends StackPane {
	
	public static final String TITLE = "Car racing"; 

	private final RaceService service = RaceService.getInstance();
	private static Queue<Stage> windowQueue = new ConcurrentLinkedQueue<>();

	@FXML private ListView<Race> racesListView;
	@FXML private VBox carsContainer;
	@FXML private Label greeting;
	@FXML private SplitPane splitPane;
	@FXML private StackPane mainPane;
	@FXML private AnchorPane contentPane;
	
	private LoginView loginView;
	private boolean initialized = false;
	private static boolean firstStart = true;
	private static boolean raceStarted = false;
	
	public MainView() {
		infliteLayout();

		loginView = new LoginView((user) -> {
			greeting.setText("Hello " + user.getFullname() + "!");
			showContent();
			initialize();
		});
		mainPane.getChildren().add(loginView);
		showLoginPane();
	}
	
	@FXML private void handleAddRace(ActionEvent event) {
		showRaceWindow(new CarRacingView(), true);
	}
	
	@FXML private void handleAddUser(ActionEvent event) {
		MainView root = new MainView();
		Stage stage = createStage(root, TITLE);
		stage.setOnCloseRequest(e -> {
			WindowCounter.decrement();
			WindowCounter.executeIfZero(() -> Client.finaly());
			root.close();
		});
		stage.show();
		WindowCounter.incement();
	}
	
	private void showLoginPane() {
		contentPane.setVisible(false);
		loginView.setVisible(true);
	}
	
	private void showContent() {
		loginView.setVisible(false);
		contentPane.setVisible(true);
	}

	private void initialize() {
		splitPane.setDividerPosition(0, 0.2);
		for (int i = 0; i < Race.NUMBER_CARS; i++) {
			carsContainer.getChildren().add(new CarInfoView(loginView.getUser()));
		}

		racesListView.getSelectionModel().selectedItemProperty().addListener((o, oldVal, newVal) -> {
			if (newVal == null) {
				carsContainer.getChildren().clear();
				return;
			}
			ObservableList<Node> children = carsContainer.getChildren();
			Iterator<Car> cars = newVal.getCars().iterator();

			for (int i = 0; i < children.size() && cars.hasNext(); i++) {
				((CarInfoView) children.get(i)).setCar(cars.next());
			}
		});
		
		if (firstStart) {
			showRaceWindow(new CarRacingView(), true);
			showRaceWindow(new CarRacingView(), true);
			showRaceWindow(new CarRacingView(), true);
			showReportsWindow();
			firstStart = false;
		}
		
		service.addListener(Action.ADD_ACTIVE_RACE, this::handleAddActiveRace);
		service.addListener(Action.FINISH_GAME, this::handleFinishGame);
		service.send(new Command(Action.OBTAIN_RASES), this::handleAddRaces);
		initialized = true;
	}
	
	private void showReportsWindow() {
		Stage stage = createStage(new ReportsView(), ReportsView.TITLE);
		stage.setOnCloseRequest(e -> {
			stage.close();
			WindowCounter.decrement();
			WindowCounter.executeIfZero(() -> Client.finaly());
		});
		stage.show();
		WindowCounter.incement();
	}
	
	private void showRaceWindow(CarRacingView carRacingView, boolean addToQueue) {
		Stage stage = createStage(carRacingView, CarRacingView.TITLE);
		stage.setResizable(false);
		stage.setOnCloseRequest(e -> {
			windowQueue.remove(stage);
			carRacingView.close();
			WindowCounter.decrement();
			WindowCounter.executeIfZero(() -> Client.finaly());
		});
		if (addToQueue) {
			windowQueue.add(stage);
		}
		stage.show();
		WindowCounter.incement();
	}
		
	private Stage createStage(Parent parent, String title) {
		Scene scene = new Scene(parent);
		Stage stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(title);
		return stage;
	}
	
	public void handleAddActiveRace(Action a, Object d) {
		Platform.runLater(() -> {
			if (raceStarted) {
				return;
			}
			Race race = (Race) d;
			Stage stage = windowQueue.poll();
			CarRacingView carRacingView;
			
			if (stage != null) {
				stage.setTitle(race.toString());
				carRacingView = (CarRacingView) stage.getScene().getRoot();
			} else {
				carRacingView = new CarRacingView();
				showRaceWindow(carRacingView, false);
			}
			carRacingView.startRace(race);
			raceStarted = true;
		});
	}

	public void handleFinishGame(Action a, Object d) {
		Platform.runLater(() -> {
			racesListView.getItems().remove(((RaceSummary) d).getRace());
			raceStarted = false;
		});
	}

	@SuppressWarnings("unchecked")
	public void handleAddRaces(Action a, Object d) {
		Platform.runLater(() -> {
			ObservableList<Race> items = FXCollections.observableArrayList((List<Race>) d);
			racesListView.setItems(items);
			racesListView.getSelectionModel().selectFirst();
		});
	}
	
	/**
	 * Creates a view based on the fxml file.
	 */
	private void infliteLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
		loader.setRoot(this);
		loader.setController(this); 

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		if (initialized) {
			service.removeListener(Action.FINISH_GAME, this::handleFinishGame);
			service.removeListener(Action.ADD_ACTIVE_RACE, this::handleAddActiveRace);
			loginView.logout();
		}
		
		Stage stage = (Stage) getScene().getWindow();
		stage.close();
	}
}
