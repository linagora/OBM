package org.obm.push.exception;



/**
 * 
 * @author adrienp
 *
 */
public class CollectionNotFoundException extends ActiveSyncException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611196276103565064L;

	public CollectionNotFoundException() {
		super();
	}

	public CollectionNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public CollectionNotFoundException(String message) {
		super(message);
	}

	public CollectionNotFoundException(Throwable cause) {
		super(cause);
	}

}
