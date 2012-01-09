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

import static org.obm.push.mail.MailTestsUtils.loadEmail;
import static org.obm.push.mail.MailTestsUtils.mockOpushConfigurationService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.utils.Mime4jUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.ICalendar;

import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;


public class MailBackendTest {
	
	@Test
	public void testSendEmailWithBigMail()
			throws ProcessingEmailException, ServerFault, StoreEmailException, SendEmailException, SmtpInvalidRcptException, IOException {
		final User user = Factory.create().createUser("test@test", "test@domain");
		final String password = "pass";
		final AccessToken at = new AccessToken(1, "o-push");
		
		MailboxService emailManager = EasyMock.createMock(MailboxService.class);
		ICalendar calendarClient = EasyMock.createMock(ICalendar.class);
		BackendSession backendSession = EasyMock.createMock(BackendSession.class);
		LoginService login = EasyMock.createMock(LoginService.class);
		
		EasyMock.expect(backendSession.getUser()).andReturn(user).once();
		EasyMock.expect(backendSession.getPassword()).andReturn(password).once();

		EasyMock.expect(login.login(user.getLoginAtDomain(), password))
				.andReturn(at).once();
		EasyMock.expect(calendarClient.getUserEmail(at)).andReturn(user.getLoginAtDomain()).once();
		login.logout(at);
		EasyMock.expectLastCall().once();
		Set<Address> addrs = Sets.newHashSet();
		emailManager.sendEmail(EasyMock.anyObject(BackendSession.class), EasyMock.anyObject(Address.class), EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(addrs.getClass()), EasyMock.anyObject(InputStream.class), EasyMock.anyBoolean());
		EasyMock.expectLastCall().once();
				
		MailBackend mailBackend = new MailBackendImpl(emailManager, null, calendarClient, login, new Mime4jUtils(), mockOpushConfigurationService(), null);

		EasyMock.replay(emailManager, calendarClient, backendSession, login);

		InputStream emailStream = loadEmail(getClass(), "bigEml.eml");
		mailBackend.sendEmail(backendSession, ByteStreams.toByteArray(emailStream), true);
		
		EasyMock.verify(emailManager, calendarClient, backendSession, login);
	}
	
}
