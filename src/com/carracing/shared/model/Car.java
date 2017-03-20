package com.carracing.shared.model;

import java.util.Random;

import javafx.scene.paint.Color;

public class Car extends Model<Long> {
	
	private static final long serialVersionUID = 5L;
	
	public static final int MAX_SPEED = 100;

	public enum CarType {
		JAGUAR, MERCEDES, PORSCHE
	}
	
	public enum CarShape {
		CABRIOLET, SPORT
	}
	
	public enum CarSize {
		MINI, NORMAL, LARGE
	}
	
	private String name;
	private int speed;
	private CarType type;
	private CarSize size;
	private CarShape shape;
	private String color;
	private Race race;
	private double distance;
	
	public void fillRandom() {
		Random random = new Random();
		
		CarType[] types = CarType.values();
		type = types[random.nextInt(types.length)];
		
		CarShape[] shapes = CarShape.values();
		shape = shapes[random.nextInt(shapes.length)];
		
		CarSize[] sizes = CarSize.values();
		size = sizes[random.nextInt(sizes.length)];
		
		color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)).toString();
		speed = random.nextInt(40) + 60;
		name = new StringBuilder(type.toString())
				.append("-")
				.append(id)
				.toString(); 
	}
	
	@Override
	public String toString() {
		return new StringBuilder("Car {")
				.append("id:").append(id)
				.append(" , speed: ").append(speed)
				.append(", name: ").append(name).append("}")
				.toString();
	}
	
	@Override
	public int hashCode() {
		return this.id.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		
		Car car = (Car) obj;
		
		return this.id.equals(car.id);
	}
	
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public CarType getType() {
		return type;
	}

	public void setType(CarType type) {
		this.type = type;
	}

	public CarSize getSize() {
		return size;
	}

	public void setSize(CarSize size) {
		this.size = size;
	}

	public CarShape getShape() {
		return shape;
	}

	public void setShape(CarShape shape) {
		this.shape = shape;
	}

	public String getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color.toString();
	}

	public Race getRace() {
		return race;
	}

	public void setRace(Race race) {
		this.race = race;
	}
}
