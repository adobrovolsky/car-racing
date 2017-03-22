package com.carracing.shared.model;

public class RaceReport extends Model<Long> {

	private static final long serialVersionUID = 3L;
	
	private int totalBets;
	private int amountBets;
	private String carName;
	private double systemProfit;
	private String raceName;
	

	public RaceReport() {}


	public int getTotalBets() {
		return totalBets;
	}


	public void setTotalBets(int totalBets) {
		this.totalBets = totalBets;
	}


	public int getAmountBets() {
		return amountBets;
	}


	public void setAmountBets(int amountBets) {
		this.amountBets = amountBets;
	}


	public String getCarName() {
		return carName;
	}


	public void setCarName(String carName) {
		this.carName = carName;
	}


	public double getSystemProfit() {
		return systemProfit;
	}


	public void setSystemProfit(double systemProfit) {
		this.systemProfit = systemProfit;
	}


	public String getRaceName() {
		return raceName;
	}


	public void setRaceName(String raceName) {
		this.raceName = raceName;
	}
}
