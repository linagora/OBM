package org.obm.sync.auth;

public class ServerFault extends Exception {

	private static final long serialVersionUID = -3163328487442877317L;

	public ServerFault(String s) {
		super(s);
	}
	
	public ServerFault(String message, Exception cause) {
		super(message, cause);
	}
	
	public ServerFault(Throwable cause) {
		super(cause);
	}

}
