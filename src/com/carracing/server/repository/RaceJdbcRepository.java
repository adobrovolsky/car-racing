package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.repository.CarJdbcRepository.SelectCarsByRaceQuery;
import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.Race.RaceStatus;

public class RaceJdbcRepository implements Repository<Race> {
	
	public static final SqlSpecification SELECT_ALL = () -> {
		return "SELECT * FROM race"; 
	};
	
	private static final Logger LOGGER = Logger.getLogger(RaceJdbcRepository.class.getSimpleName());
	private static final RowMapper<ResultSet, Race> DEFAULT_MAPPER = new RaceMapper();
	private RowMapper<ResultSet, Race> mapper = DEFAULT_MAPPER;
	
	public RaceJdbcRepository(RowMapper<ResultSet, Race> mapper) {
		this.mapper = mapper;
	}
	
	public RaceJdbcRepository() {}

	@Override
	public boolean add(Race entity) {
		final String sqlQuery = String.format(
				"INSERT INTO race(name, started, finished, status, sound_id) VALUES ('%s', '%s', '%s', '%s', %d)", 
				entity.getName(), String.valueOf(entity.getStarted()), 
				String.valueOf(entity.getFinished()), entity.getStatus(), entity.getSoundID());

		try {
			long generatedID = DBUtil.executeUpdate(sqlQuery);
			entity.setId(generatedID);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to add an entry to the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean remove(Race entity) {
		final String sqlQuery = String.format("DELETE FROM race WHERE id = %d", entity.getId());

		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to remove an entry from the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean update(Race entity) {
		final String sqlQuery = String.format(
				"UPDATE race SET name='%s', started='%s', finished='%s', status='%s', sound_id=%d WHERE id = %d", 
				entity.getName(),
				entity.getStarted() == null ? null : entity.getStarted().toString(), 
				entity.getFinished() == null ? null : entity.getFinished().toString(), 
				entity.getStatus().toString(), entity.getSoundID(), entity.getId());
		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to update an entry in the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public List<Race> query(Specification spec) {
		final SqlSpecification sqlSpec = (SqlSpecification) spec;
		
		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlSpec.toSqlQuery());
			return mapper.map(resultSet);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}
	
	public static class RaceMapper implements RowMapper<ResultSet, Race> {
		
		private final Repository<Car> carRepository = new CarJdbcRepository();
				
		@Override
		public List<Race> map(ResultSet resultSet) throws SQLException {
			final List<Race> races = new ArrayList<>();
			
			while (resultSet.next()) {
				Race race = new Race();
				race.setId(resultSet.getLong("id"));
				race.setName(resultSet.getString("name"));
				
				String started = resultSet.getString("started");
				if (!started.equals("null")) {
					race.setStarted(LocalDateTime.parse(started));
				}
				
				String finished = resultSet.getString("finished");
				if (!finished.equals("null")) {
					race.setFinished(LocalDateTime.parse(finished));
				}
				
				race.setSoundID(resultSet.getInt("sound_id"));
				race.setStatus(RaceStatus.valueOf(resultSet.getString("status")));
				
				List<Car> cars = carRepository.query(new SelectCarsByRaceQuery(race));
				cars.stream().forEach(car -> car.setRace(race));
				race.addCars(cars);
				
				races.add(race);
			}
			
			LOGGER.info(races.toString());
			
			return races;
		}
	}
	
	public static class SelectUnfinishedRacesQuery implements SqlSpecification {

		@Override public String toSqlQuery() {
			return String.format(
					"SELECT * FROM race WHERE status IN ('%s', '%s', '%s')", 
					RaceStatus.ACTIVE, RaceStatus.READY, RaceStatus.WAITING);
		}
	}
	
	public static class SelectRaceById implements SqlSpecification {
		private final long id;

		public SelectRaceById(long id) {
			this.id = id;
		}

		@Override public String toSqlQuery() {
			return "SELECT * FROM race WHERE id = " + id;
		}
	}
}
