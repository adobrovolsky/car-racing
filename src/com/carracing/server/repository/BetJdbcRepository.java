package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.Bet;
import com.carracing.shared.model.Car;
import com.carracing.shared.model.User;

public class BetJdbcRepository implements Repository<Bet> {
	
	private static final Logger LOGGER = Logger.getLogger(BetJdbcRepository.class.getSimpleName());

	public static final SqlSpecification SELECT_ALL = () -> "SELECT * FROM bet";
	private static final RowMapper<ResultSet, Bet> DEFAULT_MAPPER = new BetMapper();
	private RowMapper<ResultSet, Bet> mapper = DEFAULT_MAPPER;

	public BetJdbcRepository(RowMapper<ResultSet, Bet> mapper) {
		this.mapper = mapper;
	}

	public BetJdbcRepository() {}

	@Override
	public boolean add(Bet entity) {
		final String sqlQuery = String.format(
				"INSERT INTO bet(amount, user_id, car_id) VALUES (%d, %d, %d)",
				entity.getAmount(), entity.getUser().getId(), entity.getCar().getId());

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
	public boolean remove(Bet entity) {
		final String sqlQuery = String.format("DELETE FROM bet WHERE id = %d)", entity.getId());

		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to remove an entry from the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean update(Bet entity) {
		final String sqlQuery = String.format(
				"UPDATE bet SET amount=%d, user_id=%d, car_id=%d WHERE id=%d",
				entity.getAmount(), entity.getUser().getId(), entity.getCar().getId(), entity.getId());

		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to update an entry in the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public List<Bet> query(Specification spec) {
		final SqlSpecification sqlSpec = (SqlSpecification) spec;

		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlSpec.toSqlQuery());
			List<Bet> bets = mapper.map(resultSet);
			resultSet.close();
			return bets;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}
	
	public static class SelectBetsByCar implements SqlSpecification {
		
		private final Car car;

		public SelectBetsByCar(Car car) {
			this.car = car;
		}

		@Override
		public String toSqlQuery() {
			return String.format("SELECT * FROM bet WHERE car_id = %d", car.getId());
		}
		
	}

	public static class BetMapper implements RowMapper<ResultSet, Bet> {
		
		private final Repository<User> userRepository = new UserJdbcRepository();
		private final Repository<Car> carRepository = new CarJdbcRepository();
		
		@Override
		public List<Bet> map(ResultSet resultSet) throws SQLException {
			final List<Bet> bets = new ArrayList<>();
			
			while (resultSet.next()) {
				Bet bet = new Bet();
				bet.setId(resultSet.getLong("id"));
				bet.setAmount(resultSet.getInt("amount"));
				
				long userID = resultSet.getLong("user_id");
				bet.setUser(loadUser(userID));
				
				long carID = resultSet.getLong("car_id");
				bet.setCar(loadCar(carID));
				
				bets.add(bet);
			}
			return bets;
		}
		
		private User loadUser(long id) {
			List<User> users = userRepository.query(new SqlSpecification() {
				@Override public String toSqlQuery() {
					return "SELECT * FROM user WHERE id = " + id;
				}
			});
			return users.get(0);
		}
		
		private Car loadCar(long id) {
			List<Car> cars = carRepository.query(new SqlSpecification() {
				@Override public String toSqlQuery() {
					return "SELECT * FROM car WHERE id = " + id;
				}
			});
			return cars.get(0);
		}
	}
}
