package org.obm.push.exception;


/**
 * 
 * @author adrienp
 * 
 */
public class SendEmailException extends ActiveSyncException {

	private static final long serialVersionUID = 6611196276103565064L;

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
