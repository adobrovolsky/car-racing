package com.carracing.server.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.carracing.server.util.DBUtil;
import com.carracing.shared.model.User;

public class UserJdbcRepository implements Repository<User> {

	private static final Logger LOGGER = Logger.getLogger(UserJdbcRepository.class.getSimpleName());

	public static final SqlSpecification SELECT_ALL = () -> "SELECT * FROM user";
	private static final RowMapper<ResultSet, User> DEFAULT_MAPPER = new UserMapper();
	private RowMapper<ResultSet, User> mapper = DEFAULT_MAPPER;

	public UserJdbcRepository(RowMapper<ResultSet, User> mapper) {
		this.mapper = mapper;
	}

	public UserJdbcRepository() {}

	@Override
	public boolean add(User entity) {
		final String sqlQuery = String.format(
				"INSERT INTO user(fullname, login, password) VALUES ('%s', '%s', '%s')",
				entity.getFullname(), entity.getLogin(), entity.getPassword());

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
	public boolean remove(User entity) {
		final String sqlQuery = String.format("DELETE FROM user WHERE id = %d)", entity.getId());

		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to remove an entry from the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean update(User entity) {
		final String sqlQuery = String.format(
				"UPDATE user SET fullname='%s', login='%s', password='%s' WHERE id = %d",
				entity.getFullname(), entity.getLogin(), entity.getPassword());

		try {
			DBUtil.executeUpdate(sqlQuery);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to update an entry in the repository.", e);
			return false;
		}
		return true;
	}

	@Override
	public List<User> query(Specification spec) {
		final SqlSpecification sqlSpec = (SqlSpecification) spec;

		try {
			ResultSet resultSet = DBUtil.exequteQuery(sqlSpec.toSqlQuery());
			List<User> users = mapper.map(resultSet);
			resultSet.close();
			return users;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to load entries from the repository.", e);
			return Collections.emptyList();
		}
	}

	public static class UserMapper implements RowMapper<ResultSet, User> {
		
		@Override
		public List<User> map(ResultSet resultSet) throws SQLException {
			final List<User> users = new ArrayList<>();
			
			while (resultSet.next()) {
				User user = new User();
				user.setId(resultSet.getLong("id"));
				user.setFullname(resultSet.getString("fullname"));
				user.setLogin(resultSet.getString("login"));
				user.setPassword(resultSet.getString("password"));

				users.add(user);
			}
			return users;
		}
	}
	
	public static class SelectUserByLoginAndPasswordQuery implements SqlSpecification {
		
		private final String login;
		private final String password;

		public SelectUserByLoginAndPasswordQuery(String login, String password) {
			this.login = login;
			this.password = password;
		}

		@Override
		public String toSqlQuery() {
			return String.format("SELECT * FROM user WHERE login LIKE '%s' AND password LIKE '%s'",
					login, password);
		}
	}
}
