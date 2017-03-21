package com.carracing.shared.model;

import java.io.Serializable;

public abstract class Model<T> implements Serializable {
	
	private static final long serialVersionUID = 0L;
	
	protected T id;

	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}
}
