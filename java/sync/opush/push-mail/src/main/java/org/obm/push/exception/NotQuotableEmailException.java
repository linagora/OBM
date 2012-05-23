package org.obm.push.exception;

import org.obm.push.mail.MailException;

public class NotQuotableEmailException extends MailException {

	public NotQuotableEmailException(String message) {
		super(message);
	}
	
	public NotQuotableEmailException(String message, Exception ex) {
		super(message, ex);
	}
}

