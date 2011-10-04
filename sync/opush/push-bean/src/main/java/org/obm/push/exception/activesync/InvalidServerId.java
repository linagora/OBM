package org.obm.push.exception.activesync;

public class InvalidServerId extends Exception {

	public InvalidServerId(String message) {
		super(message);
	}

	public InvalidServerId(String message, NumberFormatException e) {
		super(message, e);
	}
	
}
