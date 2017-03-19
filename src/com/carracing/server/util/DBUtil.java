package com.carracing.server.util;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;

/**
 * Allows you to send raw requests to the database.
 * Manages the opening and closing of a connection to the database.
 */
public class DBUtil {

	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String JDBC_URL = "jdbc:mysql://localhost:3306/carracing";
	private static final String JDBC_USERNAME = "scott";
	private static final String JDBC_PASSWORD = "tiger";
	
	private static final Logger LOGGER = Logger.getLogger(DBUtil.class.getSimpleName());
	
	private static Connection connection;
	
	static {
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
	public static void connect() throws SQLException {
		try {
			connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw e;
		}
	}

	public static void disconnect() throws SQLException {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw e;
		}
	}
	
	/**
	 * Allows you to perform a select query.
	 * 
	 * @param sqlStatement the raw sql query
	 */
	public static ResultSet exequteQuery(final String sqlStatement) throws SQLException {
		LOGGER.log(Level.INFO, sqlStatement);
		
		Statement statement = null;
		ResultSet resultSet = null;
		CachedRowSet cachedResultSet = null;
		
		try {
			connect();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlStatement);
			cachedResultSet = new CachedRowSetImpl();
			cachedResultSet.populate(resultSet);
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw e;
		} finally {
			if (statement != null) {
				statement.close();
			}
			disconnect();
		}
		return cachedResultSet;
	}
	
	/**
	 * Allows you to perform an update and insert requests.
	 * 
	 * @param sqlStatement the raw sql query.
	 * @return auto-generated identifier when inserting a record into a table.
	 */
	public static long executeUpdate(final String sqlStatement) throws SQLException {
		LOGGER.log(Level.INFO, sqlStatement);
		
		Statement statement = null;
		String [] generatedColumns = {"id"};
		
		try {
			connect();
			statement = connection.createStatement();
			statement.executeUpdate(sqlStatement, generatedColumns);
			
			try (ResultSet keys = statement.getGeneratedKeys()) {
				if (keys.next()) {
					return keys.getLong(1);
				}
			}
			return -1;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw e;
		} finally {
			if (statement != null) {
				statement.close();
			}
			disconnect();
		}
	}
}
