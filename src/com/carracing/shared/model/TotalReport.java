package com.carracing.shared.model;

import java.util.List;

public class TotalReport extends Model<Integer> {

	private static final long serialVersionUID = 9L;
	
	private int systenProfit;
	private int completedRaces;
	private int racesReadyToStart;
	private List<RaceReport> reports;
	
	public int getSystenProfit() {
		return systenProfit;
	}
	
	public void setSystenProfit(int systenProfit) {
		this.systenProfit = systenProfit;
	}
	
	public int getCompletedRaces() {
		return completedRaces;
	}
	
	public void setCompletedRaces(int completedRaces) {
		this.completedRaces = completedRaces;
	}
	
	public int getRacesReadyToStart() {
		return racesReadyToStart;
	}
	
	public void setRacesReadyToStart(int racesReadyToStart) {
		this.racesReadyToStart = racesReadyToStart;
	}
	
	public List<RaceReport> getReports() {
		return reports;
	}
	
	public void setReports(List<RaceReport> reports) {
		this.reports = reports;
	}
}
