/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.mail.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;

import org.columba.ristretto.smtp.SMTPException;
import org.columba.ristretto.smtp.SMTPProtocol;
import org.obm.push.bean.Address;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.SendEmailException;
import org.obm.push.mail.exception.SmtpLocatorException;
import org.obm.push.service.SmtpSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

public class SmtpSenderImpl implements SmtpSender {

	private static final Logger logger = LoggerFactory.getLogger(SmtpSenderImpl.class);

	private final SmtpProvider locator;

	@Inject
	private SmtpSenderImpl(SmtpProvider locator) {
		this.locator = locator;
	}

	@Override
	public void sendEmail(UserDataRequest udr, Address from, Set<Address> setTo, Set<Address> setCc, Set<Address> setCci, 
			InputStream mimeMail) throws SendEmailException {
		
		try {
			smtpSendMail(udr, 
					getCleanedAddress(from), 
					getAllRistrettoRecipients(setTo, setCc, setCci), 
					mimeMail);
		} catch (SMTPException e) {
			throw new SendEmailException(e);
		}
	}
	
	private void smtpSendMail(UserDataRequest udr, org.columba.ristretto.message.Address from, 
			org.columba.ristretto.message.Address[] rcpts, InputStream data) throws SendEmailException, SMTPException {
		
		SMTPProtocol smtp = null;
		try {
			smtp = locator.getSmtpClient(udr);
			smtp.openPort();
			smtp.ehlo(InetAddress.getLocalHost());
			setSmtpFrom(smtp, from);
			setSmtpRcpts(smtp, rcpts);
			smtp.data(data);
		} catch (SmtpLocatorException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			quit(smtp);
		}
	}

	private void setSmtpFrom(SMTPProtocol smtp, org.columba.ristretto.message.Address from) throws SendEmailException, IOException {
		try {
			smtp.mail(from);
		} catch (SMTPException e) {
			throw new SendEmailException("Bad sender address syntax {from:" + from.getMailAddress() + "}", e);
		}
	}
	
	private void setSmtpRcpts(SMTPProtocol smtp, org.columba.ristretto.message.Address[] rcpts) {
		for (org.columba.ristretto.message.Address rcpt : rcpts) {
			try {
				smtp.rcpt(rcpt);
			} catch (SMTPException e) {
				logger.error("Bad sender address syntax {from:" + rcpt.getMailAddress() + "}", e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	private void quit(SMTPProtocol smtp) throws SMTPException {
		try {
			if (smtp != null) {
				smtp.quit();
			}
		} catch (IOException e) {
			logger.error("Error while closing the smtp connection");
		}
	}

	private org.columba.ristretto.message.Address[] getAllRistrettoRecipients(Collection<Address> to,
			Collection<Address> cc, Collection<Address> bcc) {
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

	private org.columba.ristretto.message.Address getCleanedAddress(Address addr) {
		return new org.columba.ristretto.message.Address(addr.getMailAddress());
	}
}
