package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.User;
import com.carracing.shared.model.reports.CarReport;
import com.carracing.shared.model.reports.GamblerReport;
import com.carracing.shared.model.reports.GamblerReport.Car;
import com.carracing.shared.model.reports.GamblerReport.Race;
import com.carracing.shared.model.reports.RaceReport;

public class ReportsJdbcRepository {
	
	private static final Logger LOGGER = Logger.getLogger(ReportsJdbcRepository.class.getSimpleName());	
	
	private final String selectCarsQuery = 
			"SELECT CONCAT(c.type, '-', c.id, '-', r.id) AS 'name', " +
					"c.color, c.size, c.shape, c.distance, " +
					"CONCAT(r.name, ' ', r.id) AS 'race_name' " + 
			"FROM car c, race r ";
	
	public List<RaceReport> selectAllRaceReports() {
		final String sqlQuery = 
				"SELECT CONCAT(r.name, ' ', r.id) AS 'race_name', " + 
						"rs.total_bets, rs.amount_bets, rs.system_profit, " +
						"CONCAT(c.type, '-', c.id, '-', r.id) AS 'car_name', " + 
						"r.started " +
				"FROM race r, race_summary rs, car c " +
				"WHERE r.id = rs.race_id && rs.winner = c.id " + 
				"ORDER BY rs.system_profit DESC";
		
		return query(sqlQuery, new RaceReportMapper());
	}
	
	public List<CarReport> selectAllCarReports() {
		final String sqlQuery = selectCarsQuery + " WHERE c.race_id = r.id";
		return query(sqlQuery, new CarReportMapper());
	}
	
	public List<CarReport> selectCarsByRace(long param) {
		final String sqlQuery = selectCarsQuery + 
				" WHERE c.race_id = r.id AND c.race_id = " + param +
				" ORDER BY c.distance DESC";
		return query(sqlQuery, new CarReportMapper());
	}
	
	public List<GamblerReport> selectAllGamblerReports() {
		final String selectGamblers = 
				"SELECT u.id, u.fullname, u.login, SUM(rsu.profit) AS profit, COUNT(rsu.race_summary_id) AS 'number_races' " +
				"FROM race_summary_user rsu, user u " + 
				"WHERE u.id = rsu.user_id " +
				"GROUP BY rsu.user_id " +
				"ORDER BY profit DESC";
		
		return query(selectGamblers, new GamblerReportMapper());
	}

	private static <T> List<T> query(String sqlQuery, RowMapper<ResultSet, T> mapper) {
		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlQuery);
			return mapper.map(resultSet);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}
	
	public static class GamblerReportMapper implements RowMapper<ResultSet, GamblerReport> {

		@Override public List<GamblerReport> map(ResultSet from) throws SQLException {
			final List<GamblerReport> reports = new ArrayList<>();
			
			while (from.next()) {
				GamblerReport report = new GamblerReport();
				report.setProfit(from.getDouble("profit"));
				report.setNumberRaces(from.getInt("number_races"));
				
				User user = new User();
				user.setId(from.getLong("id"));
				user.setFullname(from.getString("fullname"));
				user.setLogin(from.getString("login"));
				
				report.setUser(user);
				reports.add(report);
			}
			
			reports.stream().forEach(report -> {
				List<Race> races = query(new SelectRacesByUserID(report.getUser().getId()).toSqlQuery(), resultSet -> {
					final List<Race> list = new ArrayList<>();
					
					while (resultSet.next()) {
						Race race = new Race();
						race.setName(resultSet.getString("race_name"));
						race.setProfit(resultSet.getDouble("profit"));
						race.setId(resultSet.getLong("id"));
						String started = resultSet.getString("started");
						race.setDate(started.equals("null") ? null : LocalDateTime.parse(started));
						
						list.add(race);
					}
					return list;
				});
				report.setRaces(races);
			});
			
			reports.stream().forEach(report -> {  
				report.getRaces().stream().forEach(race -> {
					List<Car> cars = query(new SelectCarsByUserID(report.getUser().getId(), race.getId()).toSqlQuery(), resultSet -> {
						List<Car> list = new ArrayList<>();
						
						while(resultSet.next()) {
							Car car = new Car();
							car.setName(resultSet.getString("car_name"));
							car.setAmountBet(resultSet.getInt("amount"));
							list.add(car);
						}
						return list;
					});
					race.setCars(cars);
				});
			});
			return reports;
		}
	}
		
	public static class SelectRacesByUserID implements SqlSpecification {
		private long id;
		
		public SelectRacesByUserID(long id) {
			this.id = id;
		}

		@Override public String toSqlQuery() {
			return  " SELECT r.id, CONCAT(r.name, ' ', r.id) AS 'race_name', r.started, rsu.profit " +
					" FROM race_summary_user rsu, race r, race_summary rs " + 
					" WHERE rsu.race_summary_id = rs.id AND rs.race_id = r.id AND rsu.user_id = " +  id +
					" ORDER BY rsu.profit DESC";
		}
	}
	
	public static class SelectCarsByUserID implements SqlSpecification {
		private long userID;
		private long raceID;
		
		public SelectCarsByUserID(long userID, long raceID) {
			this.userID = userID;
			this.raceID = raceID;
		}

		@Override public String toSqlQuery() {
			return  " SELECT CONCAT(c.type, '-', c.id, '-', r.id) AS 'car_name', SUM(b.amount) AS amount" +
					" FROM bet b, car c,  race r" +
					" WHERE b.car_id = c.id AND c.race_id = r.id AND b.user_id = " + userID + " AND r.id = " + raceID + 
					" GROUP BY b.car_id " +
					" ORDER BY amount DESC";
		}
	}
	
	public static class CarReportMapper implements RowMapper<ResultSet, CarReport> {

		@Override public List<CarReport> map(ResultSet from) throws SQLException {
			final List<CarReport> reports = new ArrayList<>();
			
			while (from.next()) {
				CarReport report = new CarReport();
				report.setName(from.getString("name"));
				report.setColor(from.getString("color"));
				report.setDistance(from.getDouble("distance"));
				report.setShape(from.getString("shape"));
				report.setSize(from.getString("size"));
				report.setRaceName(from.getString("race_name"));
				
				reports.add(report);
			}
			return reports;
		}
	}
	
	public static class RaceReportMapper implements RowMapper<ResultSet, RaceReport> {

		@Override public List<RaceReport> map(ResultSet from) throws SQLException {
			final List<RaceReport> reports = new ArrayList<>();
			
			while (from.next()) {
				RaceReport report = new RaceReport();
				report.setAmountBets(from.getInt("amount_bets"));
				report.setTotalBets(from.getInt("total_bets"));
				report.setCarName(from.getString("car_name"));
				report.setRaceName(from.getString("race_name"));
				report.setSystemProfit(from.getDouble("system_profit"));
				String started = from.getString("started");
				report.setDate(started.equals("null") ? null : LocalDateTime.parse(started));
				
				reports.add(report);
			}
			return reports;
		}
	}
}
