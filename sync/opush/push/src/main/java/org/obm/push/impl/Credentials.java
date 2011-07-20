package org.obm.push.impl;

import java.io.Serializable;

public class Credentials implements Serializable {

	private final String loginAtDomain;
	private final String password;

	public Credentials(String loginAtDomain, String password) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
	}

	public String getLoginAtDomain() {
		return loginAtDomain;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((loginAtDomain == null) ? 0 : loginAtDomain.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Credentials other = (Credentials) obj;
		if (loginAtDomain == null) {
			if (other.loginAtDomain != null)
				return false;
		} else if (!loginAtDomain.equals(other.loginAtDomain))
			return false;
		return true;
	}	

}
