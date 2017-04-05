package com.carracing.client.view;

import java.io.IOException;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.client.RaceService.ActionListener;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Bet;
import com.carracing.shared.model.Car;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.StackPane;

/**
 * This view allows you to make bet on this car. Also displays 
 * all information about the car and list of already made bets.
 */
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
		
		amountBetColumn.setCellValueFactory(data ->
			new ReadOnlyObjectWrapper<Integer>(data.getValue().getAmount()));
		
		userColumn.setCellValueFactory(data ->
			new ReadOnlyStringWrapper(data.getValue().getUser().getFullname()));
		
		amount.textProperty().addListener((observable, oldVal, newVal) -> {
			String value = oldVal + newVal;
			boolean isValidValue = value.matches("^[0-9]+$");
			btnMakeBet.setDisable(!isValidValue);
		});
	}
	
	public CarInfoView() {
		this(null);
	}
	
	/**
	 * Deletes all data so that this view can be reused.
	 */
	private void clear() {
		amount.clear();
		betsTable.getItems().clear();
		amountBetsValue.setText("0");
		totalBetsValue.setText("0");
		carPreview.getChildren().clear();
	}
	
	/**
	 * Sends a request to the server to receive all bets on this car.
	 */
	private void obtainBets() {
		service.send(new Command(Action.OBTAIN_BETS, car));
	}
	
	/**
	 * Initializes the view with new data.
	 */
	public void setCar(Car car) {
		if (car == null) return;
		this.car = car;	
		
		clear();
		obtainBets();
		setText(car.toString());
		shapeValue.setText(car.getShape().toString());
		sizeValue.setText(car.getSize().toString());
		typeValue.setText(car.getType().toString());
		SubScene carPreviewSubScene = CarView.asSubScene(car);
		carPreview.getChildren().add(carPreviewSubScene);
	}
	
	/**
	 * Creates a view based on the fxml file.
	 */
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
	
	@FXML public void handleMakeBetAction(ActionEvent event) {
		String value = amount.getText().trim();
		if (!value.isEmpty()) {
			Integer amountValue = Integer.valueOf(value);
			Bet bet = new Bet(amountValue, car, service.getUser());
			service.send(new Command(Action.MAKE_BET, bet));
			amount.setText("");
		}
	}
	
	public boolean isValidBet(int amount) {
		return amount > 0;
	}

	@Override public void actionPerformed(Action action, Object data) {
		switch (action) {
			case ADD_BETS: handleAddBets((List<Bet>) data); break;
			case ADD_BET: handleAddBet((Bet) data); break;
		}
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
