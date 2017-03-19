package com.carracing.server.repository;

import java.util.List;

import com.carracing.shared.model.Model;

/**
 * It corresponds to a repository template that 
 * allows you to retrieve data from the storage, 
 * which can be either a database or a network.
 */
public interface Repository<T extends Model> {
	
	boolean add(T entity);
	
	boolean remove(T entity);
	
	boolean update(T entity);
	
	List<T> query(Specification spec);
}
