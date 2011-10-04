package org.obm.push.exception;

public class SmtpServiceNotAvailableException extends SendEmailException {
	
	private static int ERROR_CODE_DOMAIN_SERVICE_NOT_AVAILABLE_CLOSING_TRANSMISSION_CHANNEL = 451;

	public SmtpServiceNotAvailableException(Throwable cause) {
		super(
				"Domain service not available, closing transmission channel",
				ERROR_CODE_DOMAIN_SERVICE_NOT_AVAILABLE_CLOSING_TRANSMISSION_CHANNEL,
				cause);
	}
	
}
