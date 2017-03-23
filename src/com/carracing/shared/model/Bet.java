package com.carracing.shared.model;

public class Bet extends Model<Long> {
	
	private static final long serialVersionUID = 6L;
	
	private int amount;
	private User user;
	private Car car;
	private boolean raceStateChanged;
	
	public Bet() {}
	
	public Bet(int amount, Car car, User user) {
		this.amount = amount;
		this.user = user;
		this.car = car;
	}
	
	public boolean isRaceStateChanged() {
		return raceStateChanged;
	}
	
	public void setRaceStateChanged(boolean f) {
		this.raceStateChanged = f;
	}
	
	@Override
	public String toString() {
		return String.format("Bet {amount: %d, car: %s, user: %s}", amount, car, user);
	}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		
		Bet bet = (Bet) obj;
		
		return bet.id.equals(id);
	}

	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public Car getCar() {
		return car;
	}
	
	public void setCar(Car car) {
		this.car = car;
	}
}
