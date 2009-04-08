package org.obm.caldav.server.impl;

public class Token {

	private String loginAtDomain;
	private String password;

	public Token(String loginAtDomain, String password) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
	}

	public String getLoginAtDomain() {
		return loginAtDomain;
	}

	public void setLoginAtDomain(String loginAtDomain) {
		this.loginAtDomain = loginAtDomain;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
