/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.mail;

import java.io.InputStream;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.smtp.SmtpSender;

import com.google.common.collect.Sets;

public class EmailManagerTest {

	@Test
	public void testSendEmailWithBigInputStream() throws ProcessingEmailException, StoreEmailException, SendEmailException, SmtpInvalidRcptException {
		
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfiguration.class);
		SmtpSender smtpSender = EasyMock.createMock(SmtpSender.class);
		BackendSession backendSession = EasyMock.createMock(BackendSession.class);
		
		EasyMock.expect(emailConfiguration.loginWithDomain()).andReturn(true).once();
		EasyMock.expect(emailConfiguration.activateTls()).andReturn(false).once();
		Set<Address> addrs = Sets.newHashSet();
		smtpSender.sendEmail(EasyMock.anyObject(BackendSession.class), EasyMock.anyObject(Address.class),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()),
				EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(InputStream.class));
		EasyMock.expectLastCall().once();
		
		EasyMock.replay(emailConfiguration, smtpSender, backendSession);
		
		ImapMailboxService emailManager = 
				new ImapMailboxService(null, emailConfiguration, smtpSender, 
						null, null, null, null);

		emailManager.sendEmail(backendSession,
				new Address("test@test.fr"),
				addrs,
				addrs,
				addrs,
				loadDataFile("bigEml.eml"), false);
		
		EasyMock.verify(emailConfiguration, smtpSender, backendSession);
	}

	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"eml/" + name);
	}
}
