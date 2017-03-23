package com.carracing.client.view;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.reports.RaceReport;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class RaceReportView extends AnchorPane {
	
	enum Query {
		SELECT_ALL
	}
	
	@FXML private TableView<RaceReport> racesTable;
	@FXML private TableColumn<RaceReport, String> raceColumn;
	@FXML private TableColumn<RaceReport, Integer> totalBetsColumn;
	@FXML private TableColumn<RaceReport, Integer> amountBetsColumn;
	@FXML private TableColumn<RaceReport, String> winnerColumn;
	@FXML private TableColumn<RaceReport, Double> profitColumn;
	@FXML private TableColumn<RaceReport, String> dateColumn;
	@FXML private ComboBox<Query> query;
	@FXML private TextField parameter;
	@FXML private Button submit;
	
	private final RaceService service = RaceService.getInstance();
	
	
	public RaceReportView() {
		inflateLayout();
		configureTableColumns();
		
		service.addListener(Action.ADD_RACE_REPORTS, (a, d) -> {
			List<RaceReport> reports = (List<RaceReport>) d;
			racesTable.setItems(FXCollections.observableArrayList(reports));
		});
		
		service.addListener(Action.ADD_RACE_REPORT, (a, d) -> {
			racesTable.getItems().add((RaceReport) d);
		});
		
		List<Query> queries = Arrays.asList(Query.values());
		query.setItems(FXCollections.observableArrayList(queries));
		query.getSelectionModel().selectFirst();
		
	}
	
	@FXML public void submitQuery(ActionEvent event) {
		Query selected = query.getSelectionModel().getSelectedItem();
	
		switch (selected) {
		case SELECT_ALL:
			service.send(new Command(Action.OBTAIN_RACE_REPORTS));
			break;
		}
	}
	
	private void configureTableColumns() {
		raceColumn.setCellValueFactory(d -> 
			new ReadOnlyStringWrapper(d.getValue().getRaceName()));

		totalBetsColumn.setCellValueFactory(d -> 
			new ReadOnlyObjectWrapper<Integer>(d.getValue().getTotalBets()));

		amountBetsColumn.setCellValueFactory(d ->
			new ReadOnlyObjectWrapper<Integer>(d.getValue().getAmountBets()));

		winnerColumn.setCellValueFactory(d ->
			new ReadOnlyStringWrapper(d.getValue().getCarName()));

		profitColumn.setCellValueFactory(d -> 
			new ReadOnlyObjectWrapper<Double>(d.getValue().getSystemProfit()));
		
		dateColumn.setCellValueFactory(d -> 
			new ReadOnlyStringWrapper(d.getValue().getDate()));
	}
	
	/**
	 * Creates a view based on the fxml file.
	 */
	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("races_report.fxml"));
		loader.setRoot(this);
		loader.setController(this); 

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
