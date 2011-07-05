package org.obm.push.exception;

/**
 * 
 * @author adrienp
 * 
 */
public class SmtpServiceNotAvailableException extends SendEmailException {
	
	private static final long serialVersionUID = 5060249645307973053L;
	
	private static int ERROR_CODE_DOMAIN_SERVICE_NOT_AVAILABLE_CLOSING_TRANSMISSION_CHANNEL = 451;

	public SmtpServiceNotAvailableException(Throwable cause) {
		super(
				"Domain service not available, closing transmission channel",
				ERROR_CODE_DOMAIN_SERVICE_NOT_AVAILABLE_CLOSING_TRANSMISSION_CHANNEL,
				cause);
	}
}
