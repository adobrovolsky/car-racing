package com.carracing.shared;

import java.io.Serializable;

import com.carracing.shared.Command.Action;
import com.carracing.shared.model.User;

/**
 * This is a command that the server and the client use 
 * to communicate with each other. For example, when a 
 * server needs to send a command to a client, it creates
 * an instance of this class, and specifies the action 
 * and necessary data.
 * 
 *	@see Action
 */
public class Command implements Serializable {
	
	private static final long serialVersionUID = 5487628799123048251L;
	
	/**
	 * This is an action that determines what needs to be done.
	 */
	private final Action action;
	
	/**
	 * This is the data necessary to perform the action.
	 * It can be any type that implements the {@link Serializable} interface.
	 */
	private Object data;
	
	private Object metadata;
	
	public Command(Action action, Object data) {
		this.action = action;
		this.data = data;
	}
	
	public Command(Action action) {
		this.action = action;
	}

	public Command(Action action, Object data, Object metadata) {
		this.action = action;
		this.data = data;
		this.metadata = metadata;
	}

	public Action getAction() {
		return action;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getData() {
		return (T) data;
	}
	
	@Override public String toString() {
		return "action: " + action.toString() + " data: " + data;
	}
	
	public void setMeta(Object metadata) {
		this.metadata = metadata;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getMetadata() {
		return (T) metadata;
	}
	
	/**
	 * This enumeration contains all the commands for communication
	 * between the server and the client. On the left are the commands
	 * sent to the server, on the right to the client.
	 */
	public enum Action {
		LOGIN,						ADD_USER,
		SIGNUP,						CHECK_SIGNUP_RESULT,
		OBTAIN_RASES,      			ADD_RACES,
		OBTAIN_BETS,				ADD_BETS,
		MAKE_BET,					ADD_BET,
		OBTAIN_ACTIVE_RACE,       	ADD_ACTIVE_RACE, FINISH_GAME, CHANGE_SPEED,
		OBTAIN_RACE_REPORTS,		ADD_RACE_REPORT, ADD_RACE_REPORTS,
		OBTAIN_CAR_REPORTS,			ADD_CAR_REPORTS, 
		OBTAIN_GAMBLER_REPORTS,		ADD_GAMBLER_REPORTS, 
		
		CLOSE_BLOCKING_QUEUE, 
	}
}
