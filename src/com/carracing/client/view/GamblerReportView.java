package com.carracing.client.view;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.carracing.client.RaceService;
import com.carracing.shared.Command;
import com.carracing.shared.Command.Action;
import com.carracing.shared.model.Bet;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.RaceSummary;
import com.carracing.shared.model.reports.GamblerReport;

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

public class GamblerReportView extends AnchorPane {
	
	enum Query {
		SELECT_ALL
	}
	
	@FXML private TableView<GamblerReport> gamblersTable;
	@FXML private TableColumn<GamblerReport, String> nameColumn;
	@FXML private TableColumn<GamblerReport, Double> totalProfitColumn;
	@FXML private TableColumn<GamblerReport, Integer> numberRacesColumn;
	
	@FXML private TableView<GamblerReport.Race> racesTable;
	@FXML private TableColumn<GamblerReport.Race, String> raceNameColumn;
	@FXML private TableColumn<GamblerReport.Race, Double> profitColumn;
	
	@FXML private TableView<GamblerReport.Car> carsTable;
	@FXML private TableColumn<GamblerReport.Car, String> carNameColumn;
	@FXML private TableColumn<GamblerReport.Car, Integer> amountBetColumn;
	
	@FXML private ComboBox<Query> query;
	@FXML private TextField parameter;
	@FXML private Button submit;
	
	private final RaceService service = RaceService.getInstance();
	private final ObservableList<GamblerReport> reports = FXCollections.observableArrayList();
	private ObservableList<GamblerReport.Race> races;
	private ObservableList<GamblerReport.Car> cars = FXCollections.observableArrayList();
	
	public GamblerReportView() {
		inflateLayout();
		configureTableColumns();
		
		Callback<GamblerReport.Race, Observable[]> cb = race -> new Observable[] {
				new SimpleDoubleProperty(race.getProfit())
		};
		Comparator<GamblerReport.Race> comparator = (x, y) -> Comparator
				.<Double>reverseOrder()
				.compare(x.getProfit(), y.getProfit());

		races = FXCollections.observableArrayList(cb);
		SortedList<GamblerReport.Race> sortedList = new SortedList<>(races, comparator);
		racesTable.setRowFactory(new LightingCallback());
		racesTable.setItems(sortedList);
		gamblersTable.setItems(reports);
		carsTable.setItems(cars);
		
		List<Query> queries = Arrays.asList(Query.values());
		query.setItems(FXCollections.observableArrayList(queries));
		query.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal.equals(Query.SELECT_ALL)) {
				parameter.setDisable(true);
			}
		});
		query.getSelectionModel().selectFirst();
		
		gamblersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				races.clear();
				races.addAll(newSelection.getRaces());
				cars.clear();
			}
		});
		
		racesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				cars.clear();
				cars.addAll(newSelection.getCars());
			}
		});
		
		service.addListener(Action.ADD_GAMBLER_REPORTS, this::addGamblerReports);
		service.addListener(Action.FINISH_GAME, this::finishGame);
	}
	
	@SuppressWarnings("unchecked")
	public void addGamblerReports(Action a, Object d) {
		reports.clear();
		races.clear();
		cars.clear();
		reports.addAll((List<GamblerReport>) d);
	}
	
	public void finishGame(Action a, Object d) {
		RaceSummary summary = (RaceSummary) d;
		
		reports.stream().forEach(report -> {

			Double profit = summary.getUserProfits().get(report.getUser());
			if (profit == null) return;

			report.setProfit(report.getProfit() + profit);
			report.setNumberRaces(report.getNumberRaces() + 1);

			GamblerReport.Race race = new GamblerReport.Race();
			race.setName(summary.getRace().toString());
			race.setProfit(profit);
			race.setDate(summary.getRace().getStarted().toString());

			List<Bet> bets = summary.getUserBets().get(report.getUser());

			Map<Car,List<Bet>> groupedBets = bets.stream()
					.collect(Collectors.groupingBy(bet -> bet.getCar()));

			groupedBets.forEach((car, b) -> {
				GamblerReport.Car carReport = new GamblerReport.Car();
				carReport.setName(car.getType().toString() + "-" + car.getId() + '-' + summary.getRace().getId());
				carReport.setAmountBet(b.stream().map(bet -> bet.getAmount()).reduce(0, Integer::sum));
				race.addCar(carReport);
			});
			
			report.getRaces().add(race);
			GamblerReport selectedGambler = gamblersTable.getSelectionModel().getSelectedItem();
			if (selectedGambler != null && selectedGambler.getUser().equals(report.getUser())) {
				races.add(race);
			}
		});
	}
	
	private void configureTableColumns() {
		nameColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getUser().getFullname()));
		totalProfitColumn.setCellValueFactory(d -> d.getValue().profitProperty());
		numberRacesColumn.setCellValueFactory(d -> d.getValue().numberRacesProperty());
		
		raceNameColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));
		profitColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<Double>(d.getValue().getProfit()));
		
		carNameColumn.setCellValueFactory(d -> new ReadOnlyStringWrapper(d.getValue().getName()));
		amountBetColumn.setCellValueFactory(d -> new ReadOnlyObjectWrapper<Integer>(d.getValue().getAmountBet()));
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
	
	/**
	 * This class allows to highlight the last race. Within 15 seconds 
	 * the last race will be highlighted in yellow.
	 */
	private class LightingCallback implements Callback<TableView<GamblerReport.Race>, TableRow<GamblerReport.Race>> {
		@Override
		public TableRow<GamblerReport.Race> call(TableView<GamblerReport.Race> param) {
			return new TableRow<GamblerReport.Race>() {
				@Override
				public void updateItem(GamblerReport.Race report, boolean empty) {
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
						if (delta < 15_000) {
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
