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
package org.obm.sync;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.obm.locator.LocatorClientException;
import org.obm.sync.auth.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class JavamailObmSmtpService implements ObmSmtpService {

	private static final Logger logger = LoggerFactory.getLogger(JavamailObmSmtpService.class);
	
	private final ObmSmtpConf conf;
	
	@Inject
	private JavamailObmSmtpService(ObmSmtpConf obmSmtpConf) {
		conf = obmSmtpConf;
	}
	
	@Override
	public void sendEmail(MimeMessage message, AccessToken token) throws MessagingException {
		Transport transport = null;
		
		try {
			Session session = buildSession(token.getDomain());
			
			transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(message, message.getAllRecipients());
		} catch (LocatorClientException e) {
			logger.error("Couldn't send the message", e);
		} finally {
			if (transport != null) {
				transport.close();
			}
		} 
    }
	
	private Session buildSession(ObmDomain domain) throws LocatorClientException {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", conf.getServerAddr(domain.getName()));	
		properties.put("mail.smtp.port", conf.getServerPort(domain.getName()));
		Session session = Session.getDefaultInstance(properties);
		return session;
	}
}
