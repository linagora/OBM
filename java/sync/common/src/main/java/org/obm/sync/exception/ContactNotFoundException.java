package org.obm.sync.exception;

public class ContactNotFoundException extends Exception {

	public ContactNotFoundException(String message) {
		super(message);
	}
	
	public ContactNotFoundException(String message, Integer contactId) {
		super(message + " : {" + contactId + "}");
	}
	
}
