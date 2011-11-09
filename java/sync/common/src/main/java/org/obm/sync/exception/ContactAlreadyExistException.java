package org.obm.sync.exception;

import org.obm.sync.book.Contact;

public class ContactAlreadyExistException extends Exception {

	public ContactAlreadyExistException(String message) {
		super(message);
	}
	
	public ContactAlreadyExistException(String message, Contact contact) {
		super(message + " : {" + contact.getLastname() + ", " + contact.getFirstname() +"}");
	}
	
}
