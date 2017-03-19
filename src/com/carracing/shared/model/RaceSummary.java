package com.carracing.shared.model;

import java.util.HashMap;
import java.util.Map;

public class RaceSummary extends Model<Long> {

	private static final long serialVersionUID = 7L;
	
	private Car winner;
	private double systemProfit;
	private Race race;
	private int totalBets;
	private int amountBets;
	private Map<User, Double> userProfit = new HashMap<>();
	
	@Override
	public String toString() {
		return new StringBuilder("Race summary {")
				.append("id: ").append(id)
				.append(", winner: ").append(winner.getId())
				.append(", systemProfit: ").append(systemProfit)
				.append("}")
				.toString();
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		
		RaceSummary sumnary = (RaceSummary) obj;
		
		return id.equals(sumnary.getId());
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

	public void addUser(User user, double profit) {
		userProfit.put(user, profit);
	}
	
	public Map<User, Double> getUsers() {
		return userProfit;
	}
	
	public void setUsers(Map<User, Double> users) {
		this.userProfit = users;
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
	
	public Race getRace() {
		return race;
	}
	
	public void setRace(Race race) {
		this.race = race;
	}
}
