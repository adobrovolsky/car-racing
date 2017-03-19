package com.carracing.client.view;

import java.io.IOException;

import com.carracing.client.RaceService;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.RaceReport;
import com.carracing.shared.model.TotalReport;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class ReportsView extends VBox {
	
	public static final String TITLE = "Reports";
	
	@FXML private Label totalSystemProfitView;
	@FXML private Label completedRacesView;
	@FXML private Label racesReadyToStartView;
	@FXML private TableView<RaceReport> reportsTable;
	@FXML private TableColumn<RaceReport, String> raceColumn;
	@FXML private TableColumn<RaceReport, Integer> totalBetsColumn;
	@FXML private TableColumn<RaceReport, Integer> amountBetsColumn;
	@FXML private TableColumn<RaceReport, String> winnerColumn;
	@FXML private TableColumn<RaceReport, Double> systemProfitColumn;
	
	private final RaceService service = RaceService.getInstance();
	private double totalSystemProfit;
	private int completedRaces;
	
	public ReportsView() {
		infliteLayout();
		
		raceColumn.setCellValueFactory(d -> {
			return new ReadOnlyStringWrapper(d.getValue().getRace().getName());
		});
		totalBetsColumn.setCellValueFactory(d -> {
			return new ReadOnlyObjectWrapper<Integer>(d.getValue().getTotalBets());
		});
		amountBetsColumn.setCellValueFactory(d -> {
			return new ReadOnlyObjectWrapper<Integer>(d.getValue().getAmountBets());
		});
		winnerColumn.setCellValueFactory(d -> {
			return new ReadOnlyStringWrapper(d.getValue().getWinner().getName());
		});
		systemProfitColumn.setCellValueFactory(d -> {
			return new ReadOnlyObjectWrapper<Double>(d.getValue().getSystemProfit());
		});
		
		service.addListener(Action.ADD_REPORTS, (a, d) -> {
			Platform.runLater(() -> {
				TotalReport report = (TotalReport) d;
				totalSystemProfit = report.getSystenProfit();
				completedRaces = report.getCompletedRaces();
				
				totalSystemProfitView.setText(totalSystemProfit + "");
				completedRacesView.setText(completedRaces + "");
				reportsTable.setItems(FXCollections.observableArrayList(report.getReports()));
			});
		});
		
		service.addListener(Action.ADD_REPORT, (a, d) -> {
			Platform.runLater(() -> {
				RaceReport report = (RaceReport) d;
				totalSystemProfit += report.getSystemProfit();
				completedRaces++;
				
				totalSystemProfitView.setText(totalSystemProfit + "");
				completedRacesView.setText(completedRaces + "");
				reportsTable.getItems().add(report);
			});
		});
		
		//service.send(new Command(Action.OBTAIN_REPORTS));
	}

	private void infliteLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("reports.fxml"));
		loader.setRoot(this);
		loader.setController(this); 

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
