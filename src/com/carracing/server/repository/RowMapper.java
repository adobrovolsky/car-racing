package com.carracing.server.repository;

import java.sql.SQLException;
import java.util.List;
/**
 * Allows you to convert data from one type to another.
 *
 * @param <FROM> source type
 * @param <TO> desired type
 */
@FunctionalInterface
public interface RowMapper<FROM, TO> {
	
	List<TO> map(FROM from) throws SQLException;
}