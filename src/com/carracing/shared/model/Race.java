package com.carracing.shared.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Race extends Model<Long> {
	
	private static final long serialVersionUID = 4L;

	public enum RaceStatus {
		ACTIVE, FINISHED, READY, WAITING
	}
	
	public static final int NUMBER_CARS = 5;
	
	/** The duration of a race in seconds */
	public static final int DURATION = 60;
	
	private final Random random = new Random();
	private int soundID = random.nextInt(10);
	private RaceStatus status = RaceStatus.WAITING;
	private String name = "Race";
	private LocalDateTime started = LocalDateTime.now();
	private LocalDateTime finished = LocalDateTime.now();
	private Set<Car> cars;
	
	public Race() {}

	public void start() {
		setStatus(RaceStatus.ACTIVE);
		started = LocalDateTime.now();
	}
	
	public void finish() {
		setStatus(RaceStatus.FINISHED);
		finished = LocalDateTime.now();
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		
		Race race = (Race) obj;
		
		return this.id.equals(race.id);
	}
	
	@Override
	public String toString() {
		return String.format("Race %d, cars: %d", id, cars.size());
	}
	
	public void addCars(Collection<Car> cars) {
		if (this.cars == null) {
			this.cars = new HashSet<>();
		}
		this.cars.addAll(cars);
	}
	
	public void addCar(Car car) {
		addCars(Collections.singletonList(car));
	}
	
	public Set<Car> getCars() {
		if (cars == null) {
			return Collections.emptySet();
		}
		return cars;
	}
	
	public boolean hasCar(final Car car) {
		if (cars == null) return false;
		return cars.contains(car);
	}
	
	public void randomSpeed() {
		getCars().stream().forEach(car -> {
			car.setSpeed(random.nextInt(50) + 50);
		});
	}
	
	public void calcDistance(int t) {
		cars.stream().forEach(car -> {
			car.setDistance(car.getDistance() + (car.getSpeed() * t));
		});
	}

	public int getSoundID() {
		return soundID;
	}

	public void setSoundID(int soundID) {
		this.soundID = soundID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getStarted() {
		return started;
	}

	public void setStarted(LocalDateTime started) {
		this.started = started;
	}

	public LocalDateTime getFinished() {
		return finished;
	}

	public void setFinished(LocalDateTime finished) {
		this.finished = finished;
	}

	public RaceStatus getStatus() {
		return status;
	}

	public void setStatus(RaceStatus status) {
		this.status = status;
	}
	
	public boolean isActive() {
		return status == RaceStatus.ACTIVE;
	}
	
	public boolean isFinished() {
		return status == RaceStatus.FINISHED;
	}
	
	public boolean isReady() {
		return status == RaceStatus.READY;
	}
}
