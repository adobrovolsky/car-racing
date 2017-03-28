package com.carracing.shared.model.reports;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CarReport implements Serializable {
	
	public static final class Query implements Serializable {
		public static final Query SELECT_ALL = new Query(0, "SELECT_ALL");
		public static final Query SELECT_BY_RACE_ID = new Query(1, "SELECT_BY_RACE_ID");
		
		private Object param;
		private int origin;
		private String name;
		
		private Query(int origin, String name) {
			this.origin = origin;
			this.name = name;
		}
		
		public <T> void setParam(T param) {
			this.param = param;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getParam() {
			return (T) param;
		}
		
		private void writeObject(ObjectOutputStream stream) throws IOException {
			stream.writeObject(param);
			stream.writeInt(origin);
			stream.writeUTF(name);
		}

		private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
			param = stream.readObject();
			origin = stream.readInt();
			name = stream.readUTF();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			
			Query query = (Query) obj;
			
			return this.origin == query.origin;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private String name;
	private String size;
	private String shape;
	private double distance;
	private String color;
	private String raceName;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSize() {
		return size;
	}
	
	public void setSize(String size) {
		this.size = size;
	}
	
	public String getShape() {
		return shape;
	}
	
	public void setShape(String shape) {
		this.shape = shape;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	
	public String getRaceName() {
		return raceName;
	}
	
	public void setRaceName(String raceName) {
		this.raceName = raceName;
	}
}
