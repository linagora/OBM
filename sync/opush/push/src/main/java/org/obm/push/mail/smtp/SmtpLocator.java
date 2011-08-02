package org.obm.push.mail.smtp;

import javax.naming.ConfigurationException;

import org.columba.ristretto.smtp.SMTPProtocol;
import org.obm.configuration.ConfigurationService;
import org.obm.locator.LocatorClient;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.SmtpLocatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SmtpLocator {

	private static final Logger logger = LoggerFactory.getLogger(SmtpLocator.class);
	private final LocatorClient locatorClient;

	@Inject
	private SmtpLocator(ConfigurationService configurationService) throws ConfigurationException {
		super();
		locatorClient = new LocatorClient(configurationService.getLocatorUrl());
	}

	public SMTPProtocol getSmtpClient(BackendSession bs)
			throws SmtpLocatorException {
		String smtpHost = locatorClient.getServiceLocation("mail/smtp_out",
				bs.getLoginAtDomain());
		if (smtpHost == null) {
			throw new SmtpLocatorException("Smtp server cannot be discovered");
		}
		logger.info("Using " + smtpHost + " as smtp host.");
		SMTPProtocol proto = new SMTPProtocol(smtpHost);
		return proto;
	}

}
