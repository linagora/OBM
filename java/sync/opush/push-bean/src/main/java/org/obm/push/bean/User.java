package org.obm.push.bean;

import java.security.InvalidParameterException;

import com.google.common.base.Objects;

public class User {

	private final String domainName;
	private final String userName;
	
	public User(String userName, String domainName) {
		this.userName = userName.toLowerCase();
		this.domainName = domainName.toLowerCase();
		checkUserName();
		checkDomainName();
	}
	
	private void checkUserName() {
		if (userName.contains("@") || userName.contains("\\")) {
			throw new InvalidParameterException("username is invalid : " + userName);
		}
	}

	private void checkDomainName() {
		if (domainName.contains("@") || domainName.contains("\\")) {
			throw new InvalidParameterException("domain is invalid : " + domainName);
		}
	}

	public String getLoginAtDomain() {
		return userName + "@" + domainName;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(domainName, userName);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof User) {
			User that = (User) object;
			return Objects.equal(this.domainName, that.domainName)
				&& Objects.equal(this.userName, that.userName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("domainName", domainName)
			.add("userName", userName)
			.toString();
	}
	
}
