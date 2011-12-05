package org.obm.push.bean;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Iterator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.base.Objects;

public class LoginAtDomain implements Serializable {

	private final String loginAtDomain;

	public LoginAtDomain(String userId) {
		this.loginAtDomain = getLoginAtDomain(userId);
	}
	
	private String getLoginAtDomain(String userID) {
		Iterable<String> parts = splitOnSlashes(userID);
		User user = buildUserFromLoginParts(parts);
		if (user == null) {
			parts = splitOnAtSign(userID);
			user = buildUserFromLoginParts(parts);
		}
		if (user == null) {
			throw new InvalidParameterException();
		}
		return user.getLoginAtDomain();
	}
	
	private Iterable<String> splitOnSlashes(String userID) {
		Iterable<String> parts = Splitter.on("\\").split(userID);
		return parts;
	}

	private Iterable<String> splitOnAtSign(String userID) {
		Iterable<String> parts = Splitter.on("@").split(userID);
		return ImmutableList.copyOf(parts).reverse();
	}

	private User buildUserFromLoginParts(Iterable<String> parts) {
		int nbParts = Iterables.size(parts);
		if (nbParts > 2) {
			throw new InvalidParameterException();
		} else if (nbParts == 2) {
			Iterator<String> iterator = parts.iterator();
			String domainName = iterator.next();
			String userName = iterator.next();
			return new User(userName, domainName);
		}
		return null;
	}
	
	public String getLoginAtDomain() {
		return loginAtDomain;
	}

	public String getLogin() {
		return loginAtDomain.split("@")[0].toLowerCase();
	}
	
	public String getDomain() {
		return loginAtDomain.split("@")[1].toLowerCase();
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(loginAtDomain);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof LoginAtDomain) {
			LoginAtDomain that = (LoginAtDomain) object;
			return Objects.equal(this.loginAtDomain, that.loginAtDomain);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("loginAtDomain", loginAtDomain)
			.toString();
	}
	
}
