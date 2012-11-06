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

import org.fest.assertions.api.Assertions;
import org.fest.assertions.data.MapEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.TestEmailConfiguration;

@RunWith(SlowFilterRunner.class)
public class LinagoraImapClientProviderTest {

	@Test
	public void testImapTimeout() {
		TestEmailConfiguration emailConfiguration = new TestEmailConfiguration(143) {
			@Override
			public int imapTimeout() {
				return 123456;
			}
		};
		LinagoraImapClientProvider imapClientProvider = new LinagoraImapClientProvider(null, emailConfiguration, null);
		
		Assertions.assertThat(imapClientProvider.defaultSession.getProperties()).contains(
				MapEntry.entry("mail.imap.timeout", 123456),
				MapEntry.entry("mail.imaps.timeout", 123456));
	}
	
	@Test
	public void testImapStartTLSTrue() {
		TestEmailConfiguration emailConfiguration = new TestEmailConfiguration(143) {
			@Override
			public boolean activateTls() {
				return true;
			}
		};
		LinagoraImapClientProvider imapClientProvider = new LinagoraImapClientProvider(null, emailConfiguration, null);
		
		Assertions.assertThat(imapClientProvider.defaultSession.getProperties()).contains(
				MapEntry.entry("mail.imap.starttls.enable", true));
	}
	
	@Test
	public void testImapStartTLSFalse() {
		TestEmailConfiguration emailConfiguration = new TestEmailConfiguration(143) {
			@Override
			public boolean activateTls() {
				return false;
			}
		};
		LinagoraImapClientProvider imapClientProvider = new LinagoraImapClientProvider(null, emailConfiguration, null);
		
		Assertions.assertThat(imapClientProvider.defaultSession.getProperties()).contains(
				MapEntry.entry("mail.imap.starttls.enable", false));
	}
	
	@Test
	public void testImapFetchBlockSize() {
		TestEmailConfiguration emailConfiguration = new TestEmailConfiguration(143) {
			@Override
			public int getImapFetchBlockSize() {
				return 987;
			}
		};
		LinagoraImapClientProvider imapClientProvider = new LinagoraImapClientProvider(null, emailConfiguration, null);
		
		Assertions.assertThat(imapClientProvider.defaultSession.getProperties()).contains(
				MapEntry.entry("mail.imap.fetchsize", 987),
				MapEntry.entry("mail.imaps.fetchsize", 987));
	}
}
