package org.obm.push.exception.activesync;


public class PartialException extends ActiveSyncException {

	public PartialException() {
		super();
	}

	public PartialException(String message, Throwable cause) {
		super(message, cause);
	}

	public PartialException(String message) {
		super(message);
	}

	public PartialException(Throwable cause) {
		super(cause);
	}

}
