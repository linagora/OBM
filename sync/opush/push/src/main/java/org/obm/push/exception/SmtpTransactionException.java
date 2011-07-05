package org.obm.push.exception;

/**
 * 
 * @author adrienp
 * 
 */
public class SmtpTransactionException extends SendEmailException {

	private static final long serialVersionUID = -4831962951237539474L;
	
	private static int ERROR_CODE_TRANSACTION_FAILED = 554;

	public SmtpTransactionException(Throwable cause) {
		super("Transaction failed", ERROR_CODE_TRANSACTION_FAILED, cause);
	}
}
