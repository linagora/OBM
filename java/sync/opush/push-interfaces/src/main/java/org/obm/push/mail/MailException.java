package org.obm.push.mail;


public class MailException extends Exception {

	public MailException(Throwable cause) {
		super(cause);
	}

	public MailException(String message) {
		super(message);
	}

	public MailException(String message, Throwable cause) {
		super(message, cause);
	}

}
