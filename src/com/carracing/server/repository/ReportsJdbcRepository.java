package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.carracing.server.repository.RaceSummaryJdbcRepository.SelectAllRaceSammaryQuery;
import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.RaceReport;
import com.carracing.shared.model.RaceSummary;
import com.carracing.shared.model.TotalReport;

public class ReportsJdbcRepository implements Repository<TotalReport> {
	
	private static final Logger LOGGER = Logger.getLogger(ReportsJdbcRepository.class.getSimpleName());
	private static final RowMapper<ResultSet, TotalReport> DEFAULT_MAPPER = new ReportsMapper();
	private RowMapper<ResultSet, TotalReport> mapper = DEFAULT_MAPPER;

	@Override
	public boolean add(TotalReport entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(TotalReport entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean update(TotalReport entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<TotalReport> query(Specification spec) {
	final SqlSpecification sqlSpec = (SqlSpecification) spec;
		
		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlSpec.toSqlQuery());
			return mapper.map(resultSet);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}
	
	public static class ReportsMapper implements RowMapper<ResultSet, TotalReport>{
		
		private final Repository<RaceSummary> summaryRepo = new RaceSummaryJdbcRepository();

		@Override
		public List<TotalReport> map(ResultSet from) throws SQLException {
			TotalReport report = null;
			if (from.next()) {
				 report = new TotalReport();
				
				List<RaceSummary> list = summaryRepo.query(new SelectAllRaceSammaryQuery());
				List<RaceReport> reports = list.stream()
					.map(summary -> new RaceReport(summary))
					.collect(Collectors.toList());
				
				report.setReports(reports);
			}
			if (report == null) {
				return Collections.emptyList();
			}
			return Collections.singletonList(report);
		}
	}
}
