package com.carracing.client.view;

import java.io.IOException;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.reports.RaceReport;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * This view displays the results of races.
 */
public class ReportsView extends VBox {
	
	public static final String TITLE = "Reports";
	
	@FXML private Label totalSystemProfitView;
	@FXML private Label completedRacesView;
	@FXML private Label racesReadyToStartView;
	@FXML private Tab raceResultsTab;
	@FXML private Tab gamblersTab;
	@FXML private Tab carsTab;
	@FXML private Tab racesTab;

	
	private final RaceService service = RaceService.getInstance();
	
	private double totalSystemProfit;
	private int completedRaces;
	
	public ReportsView() {
		infliteLayout();
		raceResultsTab.setContent(new Pane());
		gamblersTab.setContent(new GamblerReportView());
		carsTab.setContent(new CarReportView());
		racesTab.setContent(new RaceReportView());
		
		service.addListener(Action.ADD_RACE_REPORTS, (a, d) -> {
			Platform.runLater(() -> {
				List<RaceReport> reports = (List<RaceReport>) d;
				
				totalSystemProfit = reports.stream()
						.map(report -> report.getSystemProfit())
						.reduce(0., Double::sum);
				
				completedRaces = reports.size();
				
				totalSystemProfitView.setText(totalSystemProfit + "");
				completedRacesView.setText(completedRaces + "");
			});
		});
		
		service.addListener(Action.ADD_RACE_REPORT, (a, d) -> {
			Platform.runLater(() -> {
				RaceReport report = (RaceReport) d;
				totalSystemProfit += report.getSystemProfit();
				completedRaces++;
				
				totalSystemProfitView.setText(totalSystemProfit + "");
				completedRacesView.setText(completedRaces + "");
			});
		});
	}

	/**
	 * Creates a view based on the fxml file.
	 */
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
