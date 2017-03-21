package com.carracing.client.view;

import java.io.IOException;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.client.RaceService.ActionListener;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Bet;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.User;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;

public class CarInfoView extends TitledPane implements ActionListener {
	
	@FXML private Label shapeValue;
	@FXML private Label sizeValue;
	@FXML private Label typeValue;
	@FXML private Label totalBetsValue;
	@FXML private Label amountBetsValue;
	@FXML private TableView<Bet> betsTable;
	@FXML private TextField amount;
	@FXML private Button btnMakeBet;
	@FXML private StackPane carPreview;
	@FXML private TableColumn<Bet, Integer> amountBetColumn;
	@FXML private TableColumn<Bet, String> userColumn;
		
	private Car car;
	private final RaceService service = RaceService.getInstance();
	private final ObservableList<Bet> observaleBets = FXCollections.observableArrayList();
	
	public CarInfoView(Car car) {
		inflateLayout();
		setCar(car);
		
		service.addListener(Action.ADD_BETS, this);
		service.addListener(Action.ADD_BET, this);
		
		amountBetColumn.setCellValueFactory(data -> {
			int value = data.getValue().getAmount();
			return new ReadOnlyObjectWrapper<Integer>(value);
		});
		
		userColumn.setCellValueFactory(data -> {
			String value = data.getValue().getUser().getFullname();
			return new ReadOnlyStringWrapper(value);
		});
		
		amount.textProperty().addListener((observable, oldVal, newVal) -> {
			boolean isNotDigit = newVal.chars().allMatch(v -> !Character.isDigit(v));
			btnMakeBet.setDisable(isNotDigit);
		});
	}
	
	public CarInfoView() {
		this(null);
	}
	
	private void clear() {
		amount.clear();
		ObservableList<Bet> items = betsTable.getItems();
		items.remove(0, items.size());
		amountBetsValue.setText("0");
		totalBetsValue.setText("0");
		carPreview.getChildren().clear();
	}
	
	private void obtainBets() {
		service.send(new Command(Action.OBTAIN_BETS, car));
	}
	
	public void setCar(Car car) {
		if (car == null) return;
		this.car = car;	
		
		clear();
		obtainBets();
		setText(car.toString());
		shapeValue.setText(car.getShape().toString());
		sizeValue.setText(car.getSize().toString());
		typeValue.setText(car.getType().toString());
		CarView carView = new CarView(car);
		carPreview.getChildren().add(carView);
	}
	
	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("car_info.fxml"));
		loader.setRoot(this);
		loader.setController(this); 

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@FXML
	public void handleMakeBetAction(ActionEvent event) {
		if (!service.isLogin()) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText(null);
			alert.setContentText("You need to login to make bets");
			alert.showAndWait();
			return;
		}
		Integer amountValue = Integer.valueOf(amount.getText());
		Bet bet = new Bet(amountValue, car, service.getUser());
		service.send(new Command(Action.MAKE_BET, bet));
		amount.setText("");
	}

	@Override
	public void actionPerformed(Action action, Object data) {
		switch (action) {
			case ADD_BETS: handleAddBets((List<Bet>) data); break;
			case ADD_BET: handleAddBet((Bet) data); break;
			case FINISH_GAME: handleFinisheGame(); break;
		}
	}

	private void handleFinisheGame() {
		
	}

	private void handleAddBets(List<Bet> bets) {
		Platform.runLater(() -> {
			if (!bets.isEmpty() && bets.get(0).getCar().equals(car)) {
				observaleBets.addAll((List<Bet>) bets);
				betsTable.setItems(observaleBets);
				totalBetsValue.setText(bets.size() + "");
				
				int amountBets = bets.stream()
					.map(bet -> bet.getAmount())
					.reduce(0, Integer::sum);
				
				amountBetsValue.setText(String.valueOf(amountBets));
			}
		});
	}

	private void handleAddBet(Bet bet) {
		Platform.runLater(() -> {
			if (car != null && car.equals(bet.getCar())) {
				betsTable.getItems().add(bet);
				
				int oldVal = Integer.valueOf(amountBetsValue.getText());
				amountBetsValue.setText((oldVal + bet.getAmount()) + "");
				oldVal = Integer.valueOf(totalBetsValue.getText());
				totalBetsValue.setText((oldVal + 1) + "");
			}
		});
	}
}
