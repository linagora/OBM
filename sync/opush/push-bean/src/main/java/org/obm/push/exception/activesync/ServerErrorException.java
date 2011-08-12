package org.obm.push.exception.activesync;


public class ServerErrorException extends ActiveSyncException {
	
	public ServerErrorException() {
		super();
	}

	public ServerErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerErrorException(String message) {
		super(message);
	}

	public ServerErrorException(Throwable cause) {
		super(cause);
	}
	
}
