package org.obm.push.exception;

public class IllegalMSEventRecurrenceException extends IllegalMSEventStateException {

	public IllegalMSEventRecurrenceException() {
		super();
	}

	public IllegalMSEventRecurrenceException(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalMSEventRecurrenceException(String message) {
		super(message);
	}

	public IllegalMSEventRecurrenceException(Throwable cause) {
		super(cause);
	}
}
