package com.carracing.client.view;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.client.util.ColumnFormatter;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.reports.CarReport;
import com.carracing.shared.model.reports.CarReport.Query;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class CarReportView extends AnchorPane {
	
	@FXML private TableView<CarReport> carsTable;
	@FXML private TableColumn<CarReport, String> nameColumn;
	@FXML private TableColumn<CarReport, String> sizeColumn;
	@FXML private TableColumn<CarReport, String> shapeColumn;
	@FXML private TableColumn<CarReport, String> colorColumn;
	@FXML private TableColumn<CarReport, Double> distanceColumn;
	@FXML private TableColumn<CarReport, String> raceColumn;
	@FXML private ComboBox<Query> query;
	@FXML private TextField parameter;
	@FXML private Button submit;
	
	private final RaceService service = RaceService.getInstance();
	
	public CarReportView() {
		inflateLayout();
		configureTableColumns();
		
		List<Query> queries = Arrays.asList(Query.SELECT_ALL, Query.SELECT_BY_RACE_ID);
		query.setItems(FXCollections.observableArrayList(queries));
		query.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.equals(CarReport.Query.SELECT_BY_RACE_ID)) {
				parameter.setDisable(false);
				parameter.requestFocus();
			} else if (newVal.equals(CarReport.Query.SELECT_ALL)) {
				parameter.setDisable(true);
			}
		});
		query.getSelectionModel().selectFirst();
		
		service.addListener(Action.ADD_CAR_REPORTS, this::addCarReports);
	}
	
	@SuppressWarnings("unchecked")
	private void addCarReports(Action a, Object data) {
		Platform.runLater(() -> {
			List<CarReport> cars = (List<CarReport>) data;
			carsTable.setItems(FXCollections.observableArrayList(cars));
		});
	}
	
	private void configureTableColumns() {
		nameColumn.setCellValueFactory(d ->
			new ReadOnlyStringWrapper(d.getValue().getName()));
		
		sizeColumn.setCellValueFactory(d -> 
			new ReadOnlyStringWrapper(d.getValue().getSize()));
		
		shapeColumn.setCellValueFactory(d -> 
			new ReadOnlyStringWrapper(d.getValue().getShape()));
		
		colorColumn.setCellValueFactory(d ->
			new ReadOnlyStringWrapper(d.getValue().getColor())); 
		
		colorColumn.setCellFactory(column -> {
			return new TableCell<CarReport, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setStyle("-fx-background-color: #" + item.substring(2));
					}
				}
			};
		});
		
		distanceColumn.setCellValueFactory(d ->
			new ReadOnlyObjectWrapper<Double>(d.getValue().getDistance()));
		distanceColumn.setCellFactory(new ColumnFormatter<>(new DecimalFormat("0.##")));
		
		raceColumn.setCellValueFactory(d -> 
			new ReadOnlyStringWrapper(d.getValue().getRaceName()));
	}

	@FXML public void submitQuery(ActionEvent event) {
		Query selected = query.getSelectionModel().getSelectedItem();
		
		if (selected.equals(Query.SELECT_ALL)) {
			service.send(new Command(Action.OBTAIN_CAR_REPORTS, selected));
			
		} else if (selected.equals(Query.SELECT_BY_RACE_ID)) {
			String value = parameter.getText().trim();
			boolean isValidValue = value.matches("^[0-9]+$");
			
			if (isValidValue) {
				long raceID = Long.valueOf(value);
				selected.<Long>setParam(raceID);
				service.send(new Command(Action.OBTAIN_CAR_REPORTS, selected));
			} else {
				parameter.requestFocus();
			}
		}
	}
	
	/**
	 * Creates a view based on the fxml file.
	 */
	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("cars_report.fxml"));
		loader.setRoot(this);
		loader.setController(this); 

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
