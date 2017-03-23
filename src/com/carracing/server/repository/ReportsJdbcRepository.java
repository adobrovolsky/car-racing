package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.reports.CarReport;
import com.carracing.shared.model.reports.GamblerReport;
import com.carracing.shared.model.reports.RaceReport;

public class ReportsJdbcRepository {
	
	private static final Logger LOGGER = Logger.getLogger(ReportsJdbcRepository.class.getSimpleName());	
	
	public List<RaceReport> selectAllRaceReports() {
		final String sqlQuery = 
				"SELECT CONCAT(r.name, ' ', r.id) AS 'race_name', " + 
						"rs.total_bets, rs.amount_bets, rs.system_profit, " +
						"CONCAT(c.type, '-', c.id, '-', r.id) AS 'car_name', " + 
						"r.started " +
				"FROM race r, race_summary rs, car c " +
				"WHERE r.id = rs.race_id && rs.winner = c.id";
		
		return query(sqlQuery, new RaceReportMapper());
	}
	
	public List<CarReport> selectAllCarReports() {
		final String sqlQuery = 
				"SELECT CONCAT(c.type, '-', c.id, '-', r.id) AS 'name', " +
						"c.color, c.size, c.shape, c.distance, " +
						"CONCAT(r.name, ' ', r.id) AS 'race_name' " + 
				"FROM car c, race r " +
				"WHERE c.race_id = r.id";
		
		return query(sqlQuery, new CarReportMapper());
	}
	
	public List<GamblerReport> selectAllGamblerReports() {
		final String sqlQuery = 
				"SELECT u.fullname, SUM(rsu.profit) AS profit, COUNT(rsu.race_summary_id) AS 'number_races' " +
				"FROM race_summary_user rsu, user u " + 
				"WHERE u.id = rsu.user_id " +
				"GROUP BY rsu.user_id";
		
		return query(sqlQuery, new GamblerReportMapper());
	}

	private <T> List<T> query(String sqlQuery, RowMapper<ResultSet, T> mapper) {
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
				report.setName(from.getString("fullname"));
				report.setProfit(from.getDouble("profit"));
				report.setNumberRaces(from.getInt("number_races"));
				
				reports.add(report);
			}
			return reports;
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
				report.setDate(from.getString("started"));
				
				reports.add(report);
			}
			return reports;
		}
	}
}
