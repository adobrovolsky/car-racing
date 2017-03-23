package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.Car.CarShape;
import com.carracing.shared.model.Car.CarSize;
import com.carracing.shared.model.Car.CarType;

import javafx.scene.paint.Color;

public class CarJdbcRepository implements Repository<Car> {
	
	private static final Logger LOGGER = Logger.getLogger(CarJdbcRepository.class.getSimpleName());

	public static final SqlSpecification SELECT_ALL = () -> {
		return "SELECT * FROM car";
	};
	private static final RowMapper<ResultSet, Car> DEFAULT_MAPPER = new CarMapper();
	private RowMapper<ResultSet, Car> mapper = DEFAULT_MAPPER;

	public CarJdbcRepository(RowMapper<ResultSet, Car> mapper) {
		this.mapper = mapper;
	}

	public CarJdbcRepository() {}

	@Override
	public boolean add(Car entity) {
		final String sqlQuery = String.format(
				"INSERT INTO car(color, name, shape, size, type, race_id, speed, distance) "
				+ "VALUES ('%s', '%s', '%s', '%s', '%s', %d, %d, %f)",
				entity.getColor(), entity.getName(), entity.getShape().toString(), 
				entity.getSize().toString(), entity.getType().toString(), 
				entity.getRace().getId(), entity.getSpeed(), entity.getDistance());

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
	public boolean remove(Car entity) {
		final String sqlQuery = String.format("DELETE FROM car WHERE id = %d)", entity.getId());

		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to remove an entry from the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean update(Car entity) {
		final String sqlQuery = String.format(
				"UPDATE car SET color='%s', name='%s', shape='%s', size='%s', type='%s',"
				+ " race_id=%d, distance=%f WHERE id=%d",
				entity.getColor(), entity.getName(), entity.getShape().toString(), 
				entity.getSize().toString(), entity.getType().toString(),
				entity.getRace().getId(), entity.getDistance(), entity.getId());

		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to update an entry in the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public List<Car> query(Specification spec) {
		final SqlSpecification sqlSpec = (SqlSpecification) spec;

		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlSpec.toSqlQuery());
			List<Car> cars = mapper.map(resultSet);
			resultSet.close();
			return cars;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}

	public static class CarMapper implements RowMapper<ResultSet, Car> {
		
		@Override
		public List<Car> map(ResultSet resultSet) throws SQLException {
			final List<Car> cars = new ArrayList<>();
			
			while (resultSet.next()) {
				Car car = new Car();
				car.setColor(Color.valueOf(resultSet.getString("color")));
				car.setId(resultSet.getLong("id"));
				car.setName(resultSet.getString("name"));
				car.setType(CarType.valueOf(resultSet.getString("type")));
				car.setSize(CarSize.valueOf(resultSet.getString("size")));
				car.setShape(CarShape.valueOf(resultSet.getString("shape")));
				car.setSpeed(resultSet.getInt("speed"));
				car.setDistance(resultSet.getDouble("distance"));
				
				cars.add(car);
			}
			return cars;
		}
	}
	
	public static class SelectCarsByRaceQuery implements SqlSpecification {
		
		private final Race race;

		public SelectCarsByRaceQuery(Race race) {
			this.race = race;
		}

		@Override public String toSqlQuery() {
			return String.format("SELECT * FROM car WHERE race_id=%d", race.getId());
		}
	}
	
	public static class SelectCarById implements SqlSpecification {
		
		private final long id;

		public SelectCarById(long id) {
			this.id = id;
		}

		@Override public String toSqlQuery() {
			return "SELECT * FROM car WHERE id = " + id;
		}
	}
}
