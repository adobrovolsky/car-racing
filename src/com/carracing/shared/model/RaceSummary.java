package com.carracing.shared.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RaceSummary extends Model<Long> {

	private static final long serialVersionUID = 7L;
	
	private Car winner;
	private double systemProfit;
	private Race race;
	private int totalBets;
	private int amountBets;
	private Map<User, Double> userProfits = new HashMap<>();
	private Map<User, List<Bet>> userBets = new HashMap<>();
	
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
		userProfits.put(user, profit);
	}
	
	public Map<User, Double> getUserProfits() {
		return userProfits;
	}
	
	public void setUserProfits(Map<User, Double> users) {
		this.userProfits = users;
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

	public void setUserBets(Map<User, List<Bet>> userBets) {
		this.userBets = userBets;
	}
	
	public Map<User, List<Bet>> getUserBets() {
		return userBets;
	}
}
