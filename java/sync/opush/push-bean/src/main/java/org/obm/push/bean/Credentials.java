package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public class Credentials implements Serializable {

	private final String password;
	private final String loginAtDomain;
	private final String email;

	public Credentials(String loginAtDomain, String password, String email) {
		super();
		this.loginAtDomain = loginAtDomain;
		this.password = password;
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public String getLoginAtDomain() {
		return loginAtDomain;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(password, loginAtDomain, email);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Credentials) {
			Credentials that = (Credentials) object;
			return Objects.equal(this.password, that.password)
				&& Objects.equal(this.loginAtDomain, that.loginAtDomain)
				&& Objects.equal(this.email, that.email);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("password", password)
			.add("loginAtDomain", loginAtDomain)
			.add("email", email)
			.toString();
	}
	
}
