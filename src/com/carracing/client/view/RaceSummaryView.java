package com.carracing.client.view;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.carracing.client.RaceService;
import com.carracing.shared.model.RaceSummary;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *	This view displays the result of one race. Shows the car 
 *	that won, and the users who made bets on this car.
 */
public class RaceSummaryView extends VBox {
	
	@FXML private Label message;
	@FXML private TableView<User> usersTable;
	@FXML private StackPane carPreview;
	@FXML private TableColumn<User, String> userColumn;
	@FXML private TableColumn<User, Double> profitColumn;
	
	private final RaceService service = RaceService.getInstance();
	
	public RaceSummaryView(RaceSummary summary) {
		inflateLayout();
		configureTableColumns();

		if (service.isLogin()) {
			com.carracing.shared.model.User currUser = service.getUser();
			Double profit = summary.getUserProfits().get(currUser);
			if (profit != null && profit > 0) {
				message.setText("You Win!");
			} else {
				message.setText("You Lose!");
			}
		}
		
		List<User> users = convertRaceSummaryToUser(summary);
		usersTable.setItems(FXCollections.observableArrayList(users));
		carPreview.getChildren().add(CarView.asSubScene(summary.getWinner()));
	}
	
	private List<User> convertRaceSummaryToUser(RaceSummary summary) {
		return summary.getUserProfits().entrySet().stream()
				.map(entry -> new User(entry.getKey().getFullname(), entry.getValue()))
				.collect(Collectors.toList());
	}

	private void configureTableColumns() {
		userColumn.setCellValueFactory(data ->
			new ReadOnlyStringWrapper(data.getValue().fullname));
	
		profitColumn.setCellValueFactory(data ->
			new ReadOnlyObjectWrapper<Double>(data.getValue().profit));
	}

	/**
	 * Creates a view based on the fxml file.
	 */
	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("race_summary.fxml"));
		loader.setRoot(this);
		loader.setController(this); 

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static class User {
		String fullname;
		double profit;
		
		public User(String fullname, double profit) {
			this.fullname = fullname;
			this.profit = profit;
		}
	}
}
