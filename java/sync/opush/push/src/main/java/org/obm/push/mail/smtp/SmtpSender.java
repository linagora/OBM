package org.obm.push.mail.smtp;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPException;
import org.columba.ristretto.smtp.SMTPProtocol;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.SmtpServiceNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

public class SmtpSender {

	private static final Logger logger = LoggerFactory
			.getLogger(SmtpSender.class);

	private SmtpLocator locator;

	@Inject
	private SmtpSender(SmtpLocator locator) {
		this.locator = locator;
	}

	public void sendEmail(BackendSession bs, Address from, Set<Address> setTo,
			Set<Address> setCc, Set<Address> setCci, InputStream mimeMail)
			throws SendEmailException, SmtpInvalidRcptException {
		Address[] recipients = getAllRistrettoRecipients(setTo, setCc, setCci);
		smtpSendMail(bs, from, recipients, mimeMail);
	}

	private void smtpSendMail(BackendSession bs, Address from, Address[] rcpts,
			InputStream data) throws SendEmailException,
			SmtpInvalidRcptException {
		Map<String, Throwable> undeliveredRcpt = new HashMap<String, Throwable>();

		SMTPProtocol smtp = null;
		try {
			smtp = locator.getSmtpClient(bs);
			smtp.openPort();
			smtp.ehlo(InetAddress.getLocalHost());
			smtp.mail(from);
			for (Address rcpt : rcpts) {
				try {
					smtp.rcpt(rcpt);
				} catch (Throwable e) {
					undeliveredRcpt.put(rcpt.getMailAddress(), e);
				}
			}
			smtp.data(data);
		} catch (SMTPException se) {
			throw new SendEmailException(se.getCode(), se);
		} catch (Throwable e) {
			throw new SmtpServiceNotAvailableException(e);
		} finally {
			try {
				if (smtp != null) {
					smtp.quit();
				}
			} catch (Throwable e) {
				logger.error("Error while closing the smtp connection");
			}
		}
		if (!undeliveredRcpt.isEmpty()) {
			throw new SmtpInvalidRcptException(undeliveredRcpt);
		}
	}

	private Address[] getAllRistrettoRecipients(Set<Address> to,
			Set<Address> cc, Set<Address> bcc) {
		if (to == null) {
			to = ImmutableSet.of();
		}
		if (cc == null) {
			cc = ImmutableSet.of();
		}
		if (bcc == null) {
			bcc = ImmutableSet.of();
		}
		org.columba.ristretto.message.Address addrs[] = new org.columba.ristretto.message.Address[to
				.size() + cc.size() + bcc.size()];
		int i = 0;
		for (Address addr : to) {
			addrs[i++] = getCleanedAddress(addr);
		}
		for (Address addr : cc) {
			addrs[i++] = getCleanedAddress(addr);
		}
		for (Address addr : bcc) {
			addrs[i++] = getCleanedAddress(addr);
		}
		return addrs;
	}

	private Address getCleanedAddress(Address addr) {
		return new Address(addr.getMailAddress());
	}
}
