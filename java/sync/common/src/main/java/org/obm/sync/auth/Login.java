package org.obm.sync.auth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Login {

	@VisibleForTesting
	protected static final String FULL_LOGIN_SEPARATOR = "@";

	private final String login;
	private final String domain;
	private final String fullLogin;

	public Login(String fullLogin) {
		this.fullLogin = fullLogin;
		String[] parts = fullLogin.split(FULL_LOGIN_SEPARATOR, 2);
		login = parts[0];
		domain = parts.length < 2 ? null : parts[1];
	}

	public Login(String login, String domain) {
		this.login = Preconditions.checkNotNull(login);
		this.domain = domain;
		this.fullLogin = login + (Strings.isNullOrEmpty(domain) ? "" : FULL_LOGIN_SEPARATOR + domain);
	}

	public String getLogin() {
		return login;
	}

	public String getDomain() {
		return domain;
	}

	public String getFullLogin() {
		return fullLogin;
	}

	public boolean hasDomain() {
		return domain != null;
	}

}
