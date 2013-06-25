package org.obm.provisioning;

public class ConnectionException extends RuntimeException {

	public ConnectionException(Throwable exception) {
		super(exception);
	}

	public ConnectionException(String message, Throwable exception) {
		super(message, exception);
	}

}
