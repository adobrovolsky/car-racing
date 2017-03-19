package com.carracing.client.view;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.carracing.client.RaceService;
import com.carracing.shared.model.RaceSummary;

import javafx.beans.property.ReadOnlyDoubleWrapper;
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

public class RaceSummaryView extends VBox {
	
	@FXML private Label message;
	@FXML private TableView<User> usersTable;
	@FXML private StackPane carPreview;
	@FXML private TableColumn<User, String> userColumn;
	@FXML private TableColumn<User, Double> winSumColumn;
	
	private final RaceSummary summary;
	private final RaceService service = RaceService.getInstance();
	
	public RaceSummaryView(RaceSummary summary) {
		this.summary = summary;
		
		inflateLayout();
	
		userColumn.setCellValueFactory(data -> {
			String fullname = data.getValue().fullname;
			return new ReadOnlyStringWrapper(fullname);
		});
		
		winSumColumn.setCellValueFactory(data -> {
			double winSum = data.getValue().winSum;
			return new ReadOnlyObjectWrapper<Double>(winSum);
		});

		List<User> users = summary.getUsers().entrySet().stream()
			.map(entry -> new User(entry.getKey().getFullname(), entry.getValue()))
			.collect(Collectors.toList());
		
		if (service.isLogin()) {
			com.carracing.shared.model.User currUser = service.getUser();
			if (summary.getUsers().containsKey(currUser)) {
				message.setText("You Win!");
			} else {
				message.setText("You Lose!");
			}
		}
		
		usersTable.setItems(FXCollections.observableArrayList(users));
		carPreview.getChildren().add(new CarView(summary.getWinner()));
	}
	
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
		double winSum;
		
		public User(String fullname, double winSum) {
			this.fullname = fullname;
			this.winSum = winSum;
		}
		
		
	}

}
