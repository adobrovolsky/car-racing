package com.carracing.client.view;

import com.carracing.shared.model.Car;
import com.carracing.shared.model.Race;
import com.carracing.shared.model.Car.CarSize;

import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class CarView extends Pane {
	
	private Car car;
	private Path carPath;
	private TranslateTransition transition;

	public CarView(Car car) {
		this.car = car;
		
		createCarFrame();
		createWheels();
		setSize(car.getSize());
		setColor(car.getColor());
		
		int duration = calcDuration(car);
		
		transition = new TranslateTransition();
		transition.setDuration(Duration.seconds(duration));
		transition.setCycleCount(Timeline.INDEFINITE);
		transition.setNode(this);
	}
	
	public TranslateTransition getTransition() {
		return transition;
	}
	

	private int calcDuration(Car car) {
		int cycle = 10;
		int maxDistance = cycle * Car.MAX_SPEED;
		int speed = car.getSpeed();
		int distance = cycle * speed;
		int delta = 100 -(distance * 100 / maxDistance);
		int x = cycle * delta / 100;
		int duration = cycle + x;
		
		return duration;
	}
	
	private void createCarFrame() {
		carPath = new Path();
		
		MoveTo start = new MoveTo();
		start.setX(50);
		start.setY(60);
		
		LineTo line1 = new LineTo();
		line1.setX(34);
		line1.setY(60);
		
		LineTo line2 = new LineTo();
		line2.setX(34);
		line2.setY(60);
		
		CubicCurveTo cubic1 = new CubicCurveTo();
		cubic1.setX(54);
		cubic1.setY(34);
		cubic1.setControlX1(30);
		cubic1.setControlY1(37);
		cubic1.setControlX2(36);
		cubic1.setControlY2(32);
		
		CubicCurveTo cubic2 = new CubicCurveTo();
		cubic2.setX(136);
		cubic2.setY(34);
		cubic2.setControlX1(66);
		cubic2.setControlY1(2);
		cubic2.setControlX2(124);
		cubic2.setControlY2(2);
		
		LineTo line3 = new LineTo();
		line3.setX(148);
		line3.setY(34);
		
		CubicCurveTo cubic3 = new CubicCurveTo();
		cubic3.setX(178);
		cubic3.setY(60);
		cubic3.setControlX1(162);
		cubic3.setControlY1(36);
		cubic3.setControlX2(178);
		cubic3.setControlY2(46);
		
		LineTo line4 = new LineTo();
		line4.setX(50);
		line4.setY(60);
		
		carPath.getElements()
			.addAll(start, line1, line2, cubic1, cubic2, line3, cubic3, line4);
		getChildren().add(carPath);
	}

	private void setColor(String color) {
		carPath.setFill(Color.valueOf(color));
	}

	private void setSize(CarSize size) {
		double scale = 0.9;
		if (size == CarSize.NORMAL) scale = 0.8;
		if (size == CarSize.MINI) scale = 0.7;
		setScaleX(scale);
		setScaleY(scale);
	}

	private void createWheels() {
		Ellipse tire1 = new Ellipse(66, 60, 30/2, 30/2);
		tire1.setFill(Color.BLACK);
		tire1.setStrokeWidth(10);
		
		Ellipse rim1 = new Ellipse(66, 60, 25/2, 25/2);
		rim1.setFill(Color.SILVER);
		
		Ellipse hub1 = new Ellipse(66, 60, 8/2, 8/2);
		hub1.setFill(Color.BLACK);
		
		
		
		Ellipse tire2 = new Ellipse(132, 60, 30/2, 30/2);
		tire2.setFill(Color.BLACK);
		tire2.setStrokeWidth(10);
		
		Ellipse rim2 = new Ellipse(132, 60, 25/2, 25/2);
		rim2.setFill(Color.SILVER);
		
		Ellipse hub2 = new Ellipse(132, 60, 8/2, 8/2);
		hub2.setFill(Color.BLACK);
		
		getChildren().addAll(tire1, rim1, hub1, tire2, rim2, hub2);
	}

	public Car getCar() {
		return car;
	}
}
