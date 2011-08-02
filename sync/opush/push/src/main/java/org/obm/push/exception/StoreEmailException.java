package org.obm.push.exception;


/**
 * 
 * @author adrienp
 *
 */
public class StoreEmailException extends ActiveSyncException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611196276103565064L;

	public StoreEmailException() {
		super();
	}

	public StoreEmailException(String message, Throwable cause) {
		super(message, cause);
	}

	public StoreEmailException(String message) {
		super(message);
	}

	public StoreEmailException(Throwable cause) {
		super(cause);
	}

}
