package org.obm.sync.auth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Login {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private String login;
		private String domain;

		public Builder() {
		}

		public Builder login(String login) {
			this.login = login;
			return this;
		}
		
		public Builder domain(String domain) {
			this.domain = domain;
			return this;
		}
		
		public Builder from(Login login) {
			Preconditions.checkNotNull(login);
			this.login = login.getLogin();
			this.domain = login.getDomain();
			return this;
		}
		
		public Login build() {
			Preconditions.checkState(login != null);
			
			String[] parts = login.split(FULL_LOGIN_SEPARATOR, 2);
			String loginPart = parts[0].toLowerCase();
			String domainPart = parts.length < 2 ? null : parts[1];
			if (domainPart != null && this.domain != null && !domainPart.equals(domain)) {
					throw new IllegalStateException(
							String.format("login '%s' and domain '%s' don't match", this.login, this.domain));
			}
			return new Login(loginPart, domainPart != null ? domainPart : domain);
		}
		
	}
	
	@VisibleForTesting
	protected static final String FULL_LOGIN_SEPARATOR = "@";

	private final String login;
	private final String domain;
	private final String fullLogin;

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

	public Login withDomain(String alternativeDomain) {
		return new Login(login, alternativeDomain);
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("login", login).add("domain", domain).toString();
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(getLogin(), getDomain());
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj != null && obj instanceof Login) {
			Login other = (Login) obj;
			
			return Objects.equal(login, other.login) && Objects.equal(domain, other.domain);
		}
		
		return false;
	}
}
