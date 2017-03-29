package com.carracing.client.view;

import com.carracing.shared.model.Car;
import com.carracing.shared.model.Car.CarSize;

import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class CarView extends Group {
	
	private Car car;
	private int duration;
	private TranslateTransition transition;

	public CarView(Car car) {
		this.car = car;
		
		buildCar();
		setSize(car.getSize());
		duration = calcDuration();
	}
	
	public static SubScene asSubScene(Car car) {
		CarView carView = new CarView(car);
		Rotate rotateY = new Rotate(40, Rotate.Y_AXIS);
		carView.setTranslateX(100);
		carView.setTranslateY(50);
		carView.getTransforms().addAll(rotateY);
		
		RotateTransition transition = new RotateTransition(Duration.seconds(5), carView);
		transition.setByAngle(360);
		transition.setAxis(Rotate.Y_AXIS);
		transition.setCycleCount(Timeline.INDEFINITE);
		transition.play();
		
		SubScene scene = new SubScene(carView, 200, 150, true, null);
		scene.setCamera(new PerspectiveCamera());
		return scene;
	}
	
	public Group buildCar() {
		Group carGroup = new Group();
		
		buildFrame(carGroup);
		buildWindshield(carGroup);
		buildWheels(carGroup);
		buildHeadlights(carGroup);
		buildTaillights(carGroup);
		
		return carGroup;
	}
	
	private void buildFrame(Group parent) {
		Box carFrame = new Box(200, 50, 100);
		carFrame.setDrawMode(DrawMode.FILL);
		carFrame.setMaterial(new PhongMaterial(Color.valueOf(car.getColor())));
		
		getChildren().add(carFrame);
	}

	private void buildWindshield(Group parent) {
		Box windshield = new Box(5, 50, 100);
		windshield.setMaterial(new PhongMaterial(Color.GREY));
		windshield.setTranslateY(-46);
		windshield.setTranslateX(50);
		windshield.setRotate(-30);
		
		getChildren().add(windshield);
	}
	
	private void buildWheels(Group parent) {
		PhongMaterial mat = new PhongMaterial(Color.GREY);
		
		Cylinder wheel1 = new Cylinder(20, 10);
		wheel1.setTranslateX(-50);
		wheel1.setTranslateZ(-50);
		wheel1.setTranslateY(25);
		wheel1.setRotate(90);
		wheel1.setRotationAxis(Rotate.X_AXIS);
		wheel1.setMaterial(mat);
		
		Cylinder wheel2 = new Cylinder(20, 20);
		wheel2.setTranslateX(50);
		wheel2.setTranslateZ(-50);
		wheel2.setTranslateY(25);
		wheel2.setRotate(90);
		wheel2.setRotationAxis(Rotate.X_AXIS);
		wheel2.setMaterial(mat);
		
		Cylinder wheel3 = new Cylinder(20, 20);
		wheel3.setTranslateX(-50);
		wheel3.setTranslateZ(50);
		wheel3.setTranslateY(25);
		wheel3.setRotate(90);
		wheel3.setRotationAxis(Rotate.X_AXIS);
		wheel3.setMaterial(mat);
		
		Cylinder wheel4 = new Cylinder(20, 20);
		wheel4.setTranslateX(50);
		wheel4.setTranslateZ(50);
		wheel4.setTranslateY(25);
		wheel4.setRotate(90);
		wheel4.setRotationAxis(Rotate.X_AXIS);
		wheel4.setMaterial(mat);
		
		getChildren().addAll(wheel1, wheel2, wheel3, wheel4);
	}
	
	private void buildHeadlights(Group parent) {
		Sphere headLightLeft = new Sphere(10);
		headLightLeft.setTranslateX(98);
		headLightLeft.setTranslateZ(-30);
		
		Sphere headLightRight = new Sphere(10);
		headLightRight.setTranslateX(98);
		headLightRight.setTranslateZ(30);
		
		getChildren().addAll(headLightLeft, headLightRight);
	}
	
	private void buildTaillights(Group parent) {
		Box tailLightLeft = new Box(10, 10, 25);
		tailLightLeft.setTranslateX(-98);
		tailLightLeft.setTranslateZ(30);
		tailLightLeft.setMaterial(new PhongMaterial(Color.RED));
		
		Box tailLightRight = new Box(10, 10, 25);
		tailLightRight.setTranslateX(-98);
		tailLightRight.setTranslateZ(-30);
		tailLightRight.setMaterial(new PhongMaterial(Color.RED));
		
		getChildren().addAll(tailLightLeft, tailLightRight);
	}	

	private int calcDuration() {
		int cycle = 10;
		int maxDistance = cycle * Car.MAX_SPEED;
		int speed = car.getSpeed();
		int distance = cycle * speed;
		int delta = 100 -(distance * 100 / maxDistance);
		int x = cycle * delta / 100;
		int duration = cycle + x;
		
		return duration;
	}

	private void setSize(CarSize size) {
		double scale = 0.50;
		if (size == CarSize.NORMAL) scale = 0.45;
		if (size == CarSize.MINI) scale = 0.40;
		setScaleX(scale);
		setScaleY(scale);
		setScaleZ(scale);
	}

	public Car getCar() {
		return car;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setTransition(TranslateTransition transition) {
		this.transition = transition;
	}
	
	public TranslateTransition getTransition() {
		return transition;
	}
}
