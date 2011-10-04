package org.obm.push.exception.activesync;


public class NotAllowedException extends ActiveSyncException {

	public NotAllowedException() {
		super();
	}

	public NotAllowedException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAllowedException(String message) {
		super(message);
	}

	public NotAllowedException(Throwable cause) {
		super(cause);
	}
	
}
