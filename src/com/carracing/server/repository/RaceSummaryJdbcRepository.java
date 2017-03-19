package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.repository.CarJdbcRepository.SelectCarById;
import com.carracing.server.repository.RaceJdbcRepository.SelectRaceById;
import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.RaceSummary;
import com.carracing.shared.model.User;

public class RaceSummaryJdbcRepository implements Repository<RaceSummary>{
	
	private static final Logger LOGGER = Logger.getLogger(RaceSummaryJdbcRepository.class.getSimpleName());
	private static final RowMapper<ResultSet, RaceSummary> DEFAULT_MAPPER = new RaceSummaryMapper();
	private RowMapper<ResultSet, RaceSummary> mapper = DEFAULT_MAPPER;

	@Override
	public boolean add(RaceSummary entity) {
		final String sqlQuery = new StringBuilder()
			.append("INSERT INTO race_summary (race_id, winner, system_profit, total_bets, amount_bets) VALUES (")
			.append(entity.getRace().getId()).append(", ")
			.append(entity.getWinner().getId()).append(", ")
			.append(entity.getSystemProfit()).append(", ")
			.append(entity.getTotalBets()).append(", ")
			.append(entity.getAmountBets())
			.append(")")
			.toString();
		
		try {
			long generatedID = DBUtil.executeUpdate(sqlQuery);
			entity.setId(generatedID);
			
			for(Entry<User, Double> entry : entity.getUsers().entrySet()) {
				String query = new StringBuilder()
					.append("INSERT INTO race_summary_user (race_summary_id, user_id, profit) VALUES (")
					.append(entity.getId()).append(", ")
					.append(entry.getKey().getId()).append(", ")
					.append(entry.getValue())
					.append(")")
					.toString();
				DBUtil.executeUpdate(query);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to add an entry to the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean remove(RaceSummary entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean update(RaceSummary entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RaceSummary> query(Specification spec) {
		final SqlSpecification sqlSpec = (SqlSpecification) spec;
		
		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlSpec.toSqlQuery());
			return mapper.map(resultSet);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}
	
	public static class SelectAllRaceSammaryQuery implements SqlSpecification {

		@Override public String toSqlQuery() {
			return "SELECT * FROM race_summary";
		}
		
	}
	
	public static class RaceSummaryMapper implements RowMapper<ResultSet, RaceSummary> {
		
		private final Repository<Car> carRepo = new CarJdbcRepository();
		private final Repository<Race> raceRepo = new RaceJdbcRepository();

		@Override
		public List<RaceSummary> map(ResultSet from) throws SQLException {
			final List<RaceSummary> list = new ArrayList<>();
			
			while (from.next()) {
				RaceSummary summary = new RaceSummary();
				summary.setId(from.getLong("id"));
				summary.setAmountBets(from.getInt("amount_bets"));
				summary.setTotalBets(from.getInt("total_bets"));
				List<Race> races = raceRepo.query(new SelectRaceById(from.getLong("race_id")));
				summary.setRace(races.get(0));
				List<Car> cars = carRepo.query(new SelectCarById(from.getLong("winner")));
				summary.setWinner(cars.get(0));
				summary.setSystemProfit(from.getDouble("system_profit"));
				
				list.add(summary);
			}
			return list;
		}
		
	}

}
