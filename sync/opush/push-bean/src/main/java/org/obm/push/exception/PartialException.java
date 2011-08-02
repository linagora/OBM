package org.obm.push.exception;



/**
 * 
 * @author adrienp
 *
 */
public class PartialException extends ActiveSyncException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611196276103565064L;

	public PartialException() {
		super();
	}

	public PartialException(String message, Throwable cause) {
		super(message, cause);
	}

	public PartialException(String message) {
		super(message);
	}

	public PartialException(Throwable cause) {
		super(cause);
	}

}
