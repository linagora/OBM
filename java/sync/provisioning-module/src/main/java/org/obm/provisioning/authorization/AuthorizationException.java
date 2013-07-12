package org.obm.provisioning.authorization;

import fr.aliacom.obm.common.domain.ObmDomain;

public class AuthorizationException extends Exception {

		private String login;
		private ObmDomain domain;
		private Boolean technicalError;

		public AuthorizationException(String login, ObmDomain domain, Boolean technicalError, Throwable cause) {
			super(String.format("Unable to get authorizations for user %s at domain %s", login, domain.getName()), cause);
			this.login = login;
			this.domain = domain;
			this.technicalError = technicalError;
		}

		public Boolean isTechnicalError() {
			return technicalError;
		}
		
		public String getLogin() {
			return login;
		}
		
		public ObmDomain getObmDomain() {
			return domain;
		}
}
