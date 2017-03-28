package com.carracing.shared.model.reports;

import java.io.Serializable;
import java.util.List;

public class GamblerReport implements Serializable {
	
	private String name;
	private double profit;
	private int numberRaces;
	
	private List<Race> races;
	private long id;
	
	public static class Race implements Serializable {
		
		private String name;
		private double profit;
		private List<Car> cars;
		private long id;
		
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
	}
	
	public static class Car implements Serializable {
		
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
	
	public int getNumberRaces() {
		return numberRaces;
	}
	
	public void setNumberRaces(int numberRaces) {
		this.numberRaces = numberRaces;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
}
