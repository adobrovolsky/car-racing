package com.carracing.shared.model;

public class User extends Model<Long> {
	
	private static final long serialVersionUID = 1L;
	
	private String fullname;
	private String login;
	private String password;
	
	public User(String fullname, String login, String password) {
		this.fullname = fullname;
		this.login = login;
		this.password = password;
	}
	
	public User() {}
	
	@Override
	public int hashCode() {
		return id.intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		
		User user = (User) obj;
		
		return id.equals(user.id);
	}
	@Override
	public String toString() {
		return String.format("User {fullname: %s, login: %s, password: %s}", 
				fullname, login, password);
	}

	public String getFullname() {
		return fullname;
	}
	
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}
	
	public String getLogin() {
		return login;
	}
	
	public void setLogin(String login) {
		this.login = login;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
