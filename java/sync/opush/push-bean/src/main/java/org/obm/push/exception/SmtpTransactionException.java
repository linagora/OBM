package org.obm.push.exception;

public class SmtpTransactionException extends SendEmailException {

	private static int ERROR_CODE_TRANSACTION_FAILED = 554;

	public SmtpTransactionException(Throwable cause) {
		super("Transaction failed", ERROR_CODE_TRANSACTION_FAILED, cause);
	}
	
}
