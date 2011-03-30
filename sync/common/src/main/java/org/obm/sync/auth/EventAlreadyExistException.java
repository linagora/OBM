package org.obm.sync.auth;

public class EventAlreadyExistException extends ServerFault {

	public EventAlreadyExistException(String s) {
		super(s);
	}

}
