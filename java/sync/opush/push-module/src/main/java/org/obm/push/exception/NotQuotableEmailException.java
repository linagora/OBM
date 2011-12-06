package org.obm.push.exception;

public class NotQuotableEmailException extends Exception {

	public NotQuotableEmailException(String message) {
		super(message);
	}
	
	public NotQuotableEmailException(String message, Exception ex) {
		super(message, ex);
	}
}

