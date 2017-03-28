package com.carracing.client.view;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.reports.CarReport;
import com.carracing.shared.model.reports.CarReport.Query;
import com.carracing.shared.model.reports.RaceReport;

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
import javafx.scene.paint.Color;
import javafx.util.Callback;

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
		query.getSelectionModel().selectFirst();
		
		service.addListener(Action.ADD_CAR_REPORTS, this::addCarReports);
	}
	
	private void addCarReports(Action a, Object data) {
		List<CarReport> cars = (List<CarReport>) data;
		carsTable.setItems(FXCollections.observableArrayList(cars));
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
		
		raceColumn.setCellValueFactory(d -> 
			new ReadOnlyStringWrapper(d.getValue().getRaceName()));
	}

	@FXML public void submitQuery(ActionEvent event) {
		Query selected = query.getSelectionModel().getSelectedItem();
		
		if (selected.equals(Query.SELECT_ALL)) {
			service.send(new Command(Action.OBTAIN_CAR_REPORTS, selected));
			
		} else if (selected.equals(Query.SELECT_BY_RACE_ID)) {
			long raceID = Long.valueOf(parameter.getText());
			selected.<Long>setParam(raceID);
			service.send(new Command(Action.OBTAIN_CAR_REPORTS, selected));
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
