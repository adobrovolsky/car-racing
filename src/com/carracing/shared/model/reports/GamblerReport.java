package com.carracing.shared.model.reports;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.carracing.shared.SerializableProperty;
import com.carracing.shared.model.User;

import javafx.beans.property.ObjectProperty;

public class GamblerReport implements Serializable {
	
	private static final long serialVersionUID = -4440701741354176720L;
	
	private User user;
	private SerializableProperty<Double> profit = new SerializableProperty<>();
	private SerializableProperty<Integer> numberRaces = new SerializableProperty<>();
	private List<Race> races;
	
	public static class Race implements Serializable {
		
		private static final long serialVersionUID = 5771799143702141896L;
		
		private String name;
		private double profit;
		private List<Car> cars = new ArrayList<>();
		private long id;
		private String date;
		
		public Race(String name, double profit, List<Car> cars) {
			this.name = name;
			this.profit = profit;
			this.cars = cars;
		}

		public Race() {}

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public double getProfit() {
			return profit;
		}
		
		public void setProfit(double profit) {
			this.profit = profit;
		}
		
		public List<Car> getCars() {
			return cars;
		}
		
		public void setCars(List<Car> cars) {
			this.cars = cars;
		}

		public void setId(long id) {
			this.id = id;
		}
		
		public long getId() {
			return id;
		}

		public void addCar(Car car) {
			cars.add(car);
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}
	}
	
	public static class Car implements Serializable {
		
		private static final long serialVersionUID = -4220958581948400637L;
		
		private String name;
		private int amountBet;
		
		public Car(String name, int amountBet) {
			this.name = name;
			this.amountBet = amountBet;
		}

		public Car() {}

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public int getAmountBet() {
			return amountBet;
		}
		
		public void setAmountBet(int amountBet) {
			this.amountBet = amountBet;
		}
	}
	
	public List<Race> getRaces() {
		return races;
	}

	public void setRaces(List<Race> races) {
		this.races = races;
	}
	
	public double getProfit() {
		return profit.get();
	}
	
	public void setProfit(double profit) {
		this.profit.set(profit);
	}
	
	public ObjectProperty<Double> profitProperty() {
		return profit;
	}
	
	public int getNumberRaces() {
		return numberRaces.get();
	}
	
	public void setNumberRaces(int numberRaces) {
		this.numberRaces.set(numberRaces);
	}
	
	public ObjectProperty<Integer> numberRacesProperty() {
		return numberRaces;
	}

	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
}
