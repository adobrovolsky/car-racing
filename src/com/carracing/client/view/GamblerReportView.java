package com.carracing.client.view;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.reports.GamblerReport;

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

public class GamblerReportView extends AnchorPane {
	
	enum Query {
		SELECT_ALL
	}
	
	@FXML private TableView<GamblerReport> gamblersTable;
	@FXML private TableColumn<GamblerReport, String> nameColumn;
	@FXML private TableColumn<GamblerReport, Double> profitColumn;
	@FXML private TableColumn<GamblerReport, Integer> numberRacesColumn;
	@FXML private ComboBox<Query> query;
	@FXML private TextField parameter;
	@FXML private Button submit;
	
	private final RaceService service = RaceService.getInstance();
	
	public GamblerReportView() {
		inflateLayout();
		configureTableColumns();
		
		List<Query> queries = Arrays.asList(Query.values());
		query.setItems(FXCollections.observableArrayList(queries));
		query.getSelectionModel().selectFirst();
		
		service.addListener(Action.ADD_GAMBLER_REPORTS, (a, d) -> {
			List<GamblerReport> reports = (List<GamblerReport>) d;
			gamblersTable.setItems(FXCollections.observableArrayList(reports));
		});
	}
	
	private void configureTableColumns() {
		nameColumn.setCellValueFactory(d -> 
			new ReadOnlyStringWrapper(d.getValue().getName()));
		
		profitColumn.setCellValueFactory(d -> 
			new ReadOnlyObjectWrapper<Double>(d.getValue().getProfit()));
		
		numberRacesColumn.setCellValueFactory(d ->
			new ReadOnlyObjectWrapper<Integer>(d.getValue().getNumberRaces()));
	}

	@FXML public void submitQuery(ActionEvent event) {
		Query selected = query.getSelectionModel().getSelectedItem();
	
		switch (selected) {
		case SELECT_ALL:
			service.send(new Command(Action.OBTAIN_GAMBLER_REPORTS));
			break;
		}
	}
	
	/**
	 * Creates a view based on the fxml file.
	 */
	private void inflateLayout() {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("gamblers_report.fxml"));
		loader.setRoot(this);
		loader.setController(this); 

		try {
			loader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}