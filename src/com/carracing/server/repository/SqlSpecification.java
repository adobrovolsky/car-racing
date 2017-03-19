package com.carracing.server.repository;

/**
 * Describes the logic of fetching data from the repository,
 * namely the sql query for relational databases.
 *
 */
@FunctionalInterface
public interface SqlSpecification extends Specification {
	
	String toSqlQuery();
}