package com.carracing.shared;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javafx.beans.property.SimpleObjectProperty;

public class SerializableProperty<T> extends SimpleObjectProperty<T> implements Serializable {

	private static final long serialVersionUID = 8546649376033941515L;
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(getValue());
	}

	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		 setValue((T) stream.readObject());
	}
}