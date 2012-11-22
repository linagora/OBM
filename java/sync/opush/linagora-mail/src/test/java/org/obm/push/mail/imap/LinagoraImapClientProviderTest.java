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
package org.obm.push.mail.imap;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.fest.assertions.api.Assertions;
import org.fest.assertions.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class LinagoraImapClientProviderTest {

	private EmailConfiguration emailConfiguration;

	@Before
	public void setUp() {
		emailConfiguration = createMock(EmailConfiguration.class);
	}
	
	@Test
	public void testProviderConfiguration() {
		expect(emailConfiguration.getImapFetchBlockSize()).andReturn(987);
		expect(emailConfiguration.activateTls()).andReturn(true).anyTimes();
		expect(emailConfiguration.imapPort()).andReturn(143);
		expect(emailConfiguration.loginWithDomain()).andReturn(true);
		expect(emailConfiguration.imapTimeout()).andReturn(123456);
		
		replay(emailConfiguration);
		LinagoraImapClientProvider imapClientProvider = new LinagoraImapClientProvider(null, emailConfiguration, null, null);
		verify(emailConfiguration);
		
		Assertions.assertThat(imapClientProvider.defaultSession.getProperties()).contains(
				MapEntry.entry("mail.imap.timeout", 123456),
				MapEntry.entry("mail.imaps.timeout", 123456),
				MapEntry.entry("mail.imap.fetchsize", 987),
				MapEntry.entry("mail.imaps.fetchsize", 987),
				MapEntry.entry("mail.imap.starttls.enable", true));
	}
	
	@Test
	public void testImapStartTLSFalse() {
		expect(emailConfiguration.getImapFetchBlockSize()).andReturn(10);
		expect(emailConfiguration.imapPort()).andReturn(143);
		expect(emailConfiguration.loginWithDomain()).andReturn(true);
		expect(emailConfiguration.imapTimeout()).andReturn(123456);
		expect(emailConfiguration.activateTls()).andReturn(false).anyTimes();
		
		replay(emailConfiguration);
		LinagoraImapClientProvider imapClientProvider = new LinagoraImapClientProvider(null, emailConfiguration, null, null);
		verify(emailConfiguration);
		
		Assertions.assertThat(imapClientProvider.defaultSession.getProperties()).contains(
				MapEntry.entry("mail.imap.starttls.enable", false));
	}
	
}
