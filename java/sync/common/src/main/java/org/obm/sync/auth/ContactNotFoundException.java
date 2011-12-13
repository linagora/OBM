package org.obm.sync.auth;

public class ContactNotFoundException extends Exception {

	public ContactNotFoundException(String message) {
		super(message);
	}
	
	public ContactNotFoundException(String message, Integer contactId) {
		super(message + " : {" + contactId + "}");
	}
	
}
