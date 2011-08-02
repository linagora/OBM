package org.obm.push.exception;

/**
 * 
 * @author adrienp
 *
 */
public class ActiveSyncException extends Exception {

	private static final long serialVersionUID = 8811943439586363944L;

	public ActiveSyncException() {
		super();
	}

	public ActiveSyncException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActiveSyncException(String message) {
		super(message);
	}

	public ActiveSyncException(Throwable cause) {
		super(cause);
	}
	
	
}
