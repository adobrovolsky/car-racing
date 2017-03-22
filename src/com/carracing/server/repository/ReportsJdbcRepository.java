package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.RaceReport;

public class ReportsJdbcRepository implements Repository<RaceReport> {
	
	private static final Logger LOGGER = Logger.getLogger(ReportsJdbcRepository.class.getSimpleName());
	private static final RowMapper<ResultSet, RaceReport> DEFAULT_MAPPER = new ReportsMapper();
	private RowMapper<ResultSet, RaceReport> mapper = DEFAULT_MAPPER;

	@Override
	public boolean add(RaceReport entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(RaceReport entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean update(RaceReport entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<RaceReport> query(Specification spec) {
		final SqlSpecification sqlSpec = (SqlSpecification) spec;
		
		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlSpec.toSqlQuery());
			return mapper.map(resultSet);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}
	
	public static class ReportsMapper implements RowMapper<ResultSet, RaceReport> {

		@Override public List<RaceReport> map(ResultSet from) throws SQLException {
			List<RaceReport> reports = new ArrayList<>();
			
			while (from.next()) {
				RaceReport report = new RaceReport();
				report.setAmountBets(from.getInt("amount_bets"));
				report.setTotalBets(from.getInt("total_bets"));
				report.setCarName(from.getString("car_name"));
				report.setRaceName(from.getString("race_name"));
				report.setSystemProfit(from.getDouble("system_profit"));
				
				reports.add(report);
			}
			
			return reports;
		}
	}
	
	public static class SelectAllRaceReportsQuery implements SqlSpecification {

		@Override public String toSqlQuery() {
			return "SELECT * FROM race_report";
		}
	}
}
