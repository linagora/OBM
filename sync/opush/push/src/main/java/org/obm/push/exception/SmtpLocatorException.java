package org.obm.push.exception;

/**
 * 
 * @author adrienp
 *
 */
public class SmtpLocatorException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6611196276103565064L;

	public SmtpLocatorException() {
		super();
	}

	public SmtpLocatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public SmtpLocatorException(String message) {
		super(message);
	}

	public SmtpLocatorException(Throwable cause) {
		super(cause);
	}

}
