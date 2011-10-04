package org.obm.sync.auth;

public class ServerFault extends Exception {

	public ServerFault(String s) {
		super(s);
	}
	
	public ServerFault(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ServerFault(Throwable cause) {
		super(cause);
	}

}
