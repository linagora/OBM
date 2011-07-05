package org.obm.push.exception;

import org.obm.push.store.ActiveSyncException;

/**
 * 
 * @author adrienp
 *
 */
public class ServerErrorException extends ActiveSyncException{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6353475048296553646L;

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
