package org.obm.push.impl;

public class Credentials {

	public Credentials(String loginAtDomain, String password) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
	}
	private String loginAtDomain;
	private String password;
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
