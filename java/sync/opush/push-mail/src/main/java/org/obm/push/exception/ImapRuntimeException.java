package org.obm.push.exception;


public class ImapRuntimeException extends RuntimeException {
	
	public ImapRuntimeException(Throwable cause) {
		super(cause);
	}
	
	public ImapRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
}
