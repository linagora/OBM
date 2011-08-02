package org.obm.push.exception;


/**
 * 
 * @author adrienp
 *
 */
public class ProcessingEmailException extends ActiveSyncException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611196276103565064L;

	public ProcessingEmailException() {
		super();
	}

	public ProcessingEmailException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessingEmailException(String message) {
		super(message);
	}

	public ProcessingEmailException(Throwable cause) {
		super(cause);
	}

}
