package org.obm.sync.auth;

import org.obm.sync.calendar.EventObmId;

public class EventNotFoundException extends Exception {

	public EventNotFoundException(String s) {
		super(s);
	}
	
	public EventNotFoundException(EventObmId uid) {
		this("Event " + uid + " not found.");
	}

}
