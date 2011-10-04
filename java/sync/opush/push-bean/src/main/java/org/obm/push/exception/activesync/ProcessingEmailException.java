package org.obm.push.exception.activesync;


public class ProcessingEmailException extends ActiveSyncException {

	public ProcessingEmailException() {
		super();
	}

	public ProcessingEmailException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessingEmailException(String message) {
		super(message);
	}

	public ProcessingEmailException(Throwable cause) {
		super(cause);
	}

}
