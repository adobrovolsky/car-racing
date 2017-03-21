package com.carracing.shared.model;

public class RaceReport extends Model<Long> {

	private static final long serialVersionUID = 3L;
	
	private int totalBets;
	private int amountBets;
	private Car winner;
	private double systemProfit;
	private Race race;
	
	public RaceReport(RaceSummary summary) {
		totalBets = summary.getTotalBets();
		amountBets = summary.getAmountBets();
		winner = summary.getWinner();
		systemProfit = summary.getSystemProfit();
		race = summary.getRace();
	}

	public RaceReport() {}

	public Race getRace() {
		return race;
	}

	public void setRace(Race race) {
		this.race = race;
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
	
	public Car getWinner() {
		return winner;
	}
	
	public void setWinner(Car winner) {
		this.winner = winner;
	}
	
	public double getSystemProfit() {
		return systemProfit;
	}
	public void setSystemProfit(double systemProfit) {
		this.systemProfit = systemProfit;
	}
}
