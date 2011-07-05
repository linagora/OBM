package org.obm.push.exception;

import org.obm.push.store.ActiveSyncException;

/**
 * 
 * @author adrienp
 *
 */
public class NotAllowedException extends ActiveSyncException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5550850138721348282L;

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
