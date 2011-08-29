package org.obm.push.exception;

public class EmailNotFoundException extends Exception {

	public EmailNotFoundException(long uid) {
		super("Email " + uid + " not found in database.");
	}
	
}
