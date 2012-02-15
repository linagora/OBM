package org.obm.push.exception;

public class IllegalMSEventStateException extends MSObjectException {

	public IllegalMSEventStateException() {
		super();
	}

	public IllegalMSEventStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalMSEventStateException(String message) {
		super(message);
	}

	public IllegalMSEventStateException(Throwable cause) {
		super(cause);
	}
}
