package org.obm.push.exception;

public class UnknownObmSyncServerException extends Exception {

	public UnknownObmSyncServerException() {
		super();
	}
	
	public UnknownObmSyncServerException(String message) {
		super(message);
	}
	
	public UnknownObmSyncServerException(Throwable cause) {
		super(cause);
	}
	
	public UnknownObmSyncServerException(String message, Throwable cause) {
		super(message, cause);
	}

}
