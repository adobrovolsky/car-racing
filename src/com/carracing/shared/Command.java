package com.carracing.shared;

import java.io.Serializable;

public class Command implements Serializable {
	
	private final Action action;
	private Object data;
	private int clientID = -1;
	
	public Command(Action action, Object data, int clientID) {
		this.action = action;
		this.data = data;
		this.clientID = clientID;
	}
	
	public Command(Action action, Object data) {
		this.action = action;
		this.data = data;
	}
	
	public Command(Action action) {
		this.action = action;
	}

	public Action getAction() {
		return action;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData() {
		return (T) data;
	}
	
	public int getClientID() {
		return clientID;
	}
	
	@Override
	public String toString() {
		return "action: " + action.toString() + " data: " + data;
	}
	
	public enum Action {
		LOGIN,						ADD_USER,
		SIGNUP,						CHECK_SIGNUP_RESULT,
		OBTAIN_RASES,      			ADD_RACES,
		OBTAIN_BETS,				ADD_BET, ADD_BETS,
		MAKE_BET,
		OBTAIN_ACTIVE_RACE,       	ADD_ACTIVE_RACE, START_GAME, FINISH_GAME, CHANGE_SPEED,
		OBTAIN_REPORTS,				ADD_REPORT, ADD_REPORTS,
		OBTAIN_CLIENT_ID, 			ADD_CLIENT_ID,
		CLOSE_BLOCKING_QUEUE,
	}
}
