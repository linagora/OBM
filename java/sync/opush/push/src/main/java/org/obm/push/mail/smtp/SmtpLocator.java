package org.obm.push.mail.smtp;

import org.columba.ristretto.smtp.SMTPProtocol;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.SmtpLocatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SmtpLocator {

	private static final Logger logger = LoggerFactory.getLogger(SmtpLocator.class);
	private final LocatorService locatorService;

	@Inject
	private SmtpLocator(LocatorService locatorService) {
		this.locatorService = locatorService;
	}
	
	public SMTPProtocol getSmtpClient(BackendSession bs) throws SmtpLocatorException {
		try {
			String smtpHost = locatorService.getServiceLocation("mail/smtp_out", bs.getUser().getLoginAtDomain());
			logger.info("Using " + smtpHost + " as smtp host.");
			SMTPProtocol proto = new SMTPProtocol(smtpHost);
			return proto;
		} catch (LocatorClientException e) {
			throw new SmtpLocatorException("Smtp server cannot be discovered", e);
		}
	}

}
