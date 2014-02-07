package org.obm.sync.exception;

import org.obm.sync.auth.ServerFault;

public class ObmUserNotFoundException extends ServerFault {
	
	public ObmUserNotFoundException(String s) {
		super(s);
	}
	
	public ObmUserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ObmUserNotFoundException(Throwable cause) {
		super(cause);
	}
}
