package org.obm.push.exception.activesync;


public class SendEmailException extends ActiveSyncException {

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
