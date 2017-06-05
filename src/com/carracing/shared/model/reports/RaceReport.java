package com.carracing.shared.model.reports;

import java.io.Serializable;
import java.time.LocalDateTime;

public class RaceReport implements Serializable {

	private static final long serialVersionUID = 3L;
	
	private int totalBets;
	private int amountBets;
	private String carName;
	private double systemProfit;
	private String raceName;
	private LocalDateTime date;

	public RaceReport() {}
	
	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

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
