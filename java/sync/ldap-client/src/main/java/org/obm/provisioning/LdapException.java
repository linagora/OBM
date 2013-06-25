package org.obm.provisioning;

public class LdapException extends RuntimeException {

	public LdapException(Throwable exception) {
		super(exception);
	}

	public LdapException(String message, Throwable exception) {
		super(message, exception);
	}

}
