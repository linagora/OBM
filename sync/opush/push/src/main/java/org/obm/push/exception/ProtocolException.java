package org.obm.push.exception;

import org.obm.push.store.ActiveSyncException;

/**
 * 
 * @author adrienp
 *
 */
public class ProtocolException extends ActiveSyncException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611196276103565064L;

	public ProtocolException() {
		super();
	}

	public ProtocolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProtocolException(String message) {
		super(message);
	}

	public ProtocolException(Throwable cause) {
		super(cause);
	}

}
