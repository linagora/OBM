package org.obm.push.exception;


public class SendEmailException extends Exception {

	int smtpErrorCode = -1;

	protected SendEmailException(String message, int smtpErrorCode, Throwable cause) {
		super(message, cause);
		this.smtpErrorCode = smtpErrorCode;
	}
	
	public SendEmailException(int smtpErrorCode, Throwable cause) {
		super(cause);
		this.smtpErrorCode = smtpErrorCode;
	}

	public int getSmtpErrorCode() {
		return smtpErrorCode;
	}
	
}
