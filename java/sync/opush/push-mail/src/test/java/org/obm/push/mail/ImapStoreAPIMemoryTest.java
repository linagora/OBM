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

import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.mail.greenmail.ClosableProcess;
import org.obm.push.mail.greenmail.ExternalProcessException;
import org.obm.push.mail.greenmail.GreenMailExternalProcess;

import com.google.inject.Inject;

public class ImapStoreAPIMemoryTest {
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	
	private String mailbox;
	private String password;
	private BackendSession bs;

	private ClosableProcess greenMailProcess;
	
	@Before
	public void setUp() throws ExternalProcessException {
	    mailbox = "to@localhost.com";
	    password = "password";
	    int maxHeapSize = getTwiceThisHeapSize();
	    greenMailProcess = new GreenMailExternalProcess(mailbox, password, false, maxHeapSize).execute();
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password, null), null, null, null);
	}
	
	private int getTwiceThisHeapSize() {
		int byteForOneMegaByte = 1048576;
		long thisHeapSizeInByte = Runtime.getRuntime().maxMemory();
		int thisHeapSizeInMo = (int)(thisHeapSizeInByte / byteForOneMegaByte);
		return thisHeapSizeInMo * 2;
	}

	@After
	public void tearDown() {
		greenMailProcess.closeProcess();
	}
	
	@Test
	public void testStoreInInboxSmallerThanMemorySize() throws Exception {
		Date before = new Date();
		InputStream tinyInputStream = StreamMailTestsUtils.getTinyEmailInputStream();

		mailboxService.storeInInbox(bs, tinyInputStream, true);
		
		Set<Email> emails = mailboxService.fetchEmails(bs, IMAP_INBOX_NAME, before);
		Assertions.assertThat(emails).isNotNull().hasSize(1);
	}
	
	@Test(expected=OutOfMemoryError.class)
	public void testStoreInInboxMoreThanMemorySize() throws Exception {
		InputStream heavyInputStream = new RandomGeneratedInputStream();

		mailboxService.storeInInbox(bs, heavyInputStream, true);
	}

	@Test
	public void testStoreInInboxSmallerThanMemorySizeWithJM() throws Exception {
		final InputStream tinyInputStream = StreamMailTestsUtils.getTinyEmailInputStream();

		mailboxService.storeInInboxWithJM(bs, tinyInputStream, tinyInputStream.available(), true);

		InputStream fetchMailStream = mailboxService.fetchMailStream(bs, IMAP_INBOX_NAME, 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.getTinyEmailAsShouldBeFetched();
		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Ignore("This test fails du of a difficulty to set the adapted heap size to greenmail")
	@Test
	public void testStoreInInboxMoreThanMemorySizeWithJM() throws Exception {
		Date before = new Date();
		final InputStream heavyInputStream = new RandomGeneratedInputStream();

		mailboxService.storeInInboxWithJM(bs, heavyInputStream, Integer.MAX_VALUE, true);
		Set<Email> emails = mailboxService.fetchEmails(bs, EmailConfiguration.IMAP_INBOX_NAME, before);
		Assertions.assertThat(emails).hasSize(1);
	}
}
