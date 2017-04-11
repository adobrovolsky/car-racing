package com.carracing.client.view;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.reports.RaceReport;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

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
	private ObservableList<RaceReport> reports;
	
	@SuppressWarnings("unchecked")
	public RaceReportView() {
		inflateLayout();
		configureTable(); 
		
		service.addListener(Action.ADD_RACE_REPORTS, (a, d) -> {
			reports.clear();
			reports.addAll((List<RaceReport>) d);
		});

		service.addListener(Action.ADD_RACE_REPORT, (a, d) -> {
			reports.add((RaceReport) d);
		});

		List<Query> queries = Arrays.asList(Query.values());
		query.setItems(FXCollections.observableArrayList(queries));
		query.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.equals(Query.SELECT_ALL)) {
				parameter.setDisable(true);
			}
		});
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
	
	private void configureTable() {
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
		
		Callback<RaceReport, Observable[]> cb = report -> new Observable[] {
				new SimpleDoubleProperty(report.getSystemProfit())
		};
		Comparator<RaceReport> comparator = (x, y) -> Comparator
				.<Double>reverseOrder()
				.compare(x.getSystemProfit(), y.getSystemProfit());

		reports = FXCollections.observableArrayList(cb);
		SortedList<RaceReport> sortedList = new SortedList<>(reports, comparator);
		racesTable.setRowFactory(new LightingCallback());
		racesTable.setItems(sortedList);
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
	
	/**
	 * This class allows to highlight the last race. Within 10 seconds 
	 * the last race will be highlighted in yellow.
	 */
	private class LightingCallback implements Callback<TableView<RaceReport>, TableRow<RaceReport>> {
		@Override
		public TableRow<RaceReport> call(TableView<RaceReport> param) {
			return new TableRow<RaceReport>() {
				@Override
				public void updateItem(RaceReport report, boolean empty) {
					super.updateItem(report, empty);
					if (report == null || empty) {
						return;
					}
					
					String raceDateTime = report.getDate();
					if (raceDateTime != null) {
						long raceTime = LocalDateTime.parse(raceDateTime)
								.atZone(ZoneId.systemDefault())
								.toInstant()
								.toEpochMilli();
						
						raceTime += Race.DURATION * 1_000;
						long currentTime = System.currentTimeMillis();
						long delta = currentTime - raceTime;
						if (delta < 10_000) {
							setStyle("-fx-control-inner-background: yellow");
						} else {
							setStyle(null);
						}
					}
				}
			};
		}
	}
}
