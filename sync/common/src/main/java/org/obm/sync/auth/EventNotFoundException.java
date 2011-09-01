package org.obm.sync.auth;

public class EventNotFoundException extends Exception {

	public EventNotFoundException(String s) {
		super(s);
	}
	
	public EventNotFoundException(int uid) {
		this("Event from uid " + uid + " not found.");
	}

}
