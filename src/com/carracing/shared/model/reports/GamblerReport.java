package com.carracing.shared.model.reports;

import java.io.Serializable;

public class GamblerReport implements Serializable {
	
	private String name;
	private double profit;
	private int numberRaces;
	
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
}
