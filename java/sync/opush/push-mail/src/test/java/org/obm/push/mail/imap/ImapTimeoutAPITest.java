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

import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailTestsUtils;
import org.obm.push.mail.TimeoutMailEnvModule;

import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

public class ImapTimeoutAPITest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(TimeoutMailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	@Inject EmailConfiguration emailConfig;
	@Inject GreenMail greenMail;
	@Inject CollectionPathHelper collectionPathHelper;
	
	private String mailbox;
	private String password;
	private BackendSession bs;

	@Before
	public void setUp() {
	    greenMail.start();
	    mailbox = "to@localhost.com";
	    password = "password";
	    greenMail.setUser(mailbox, password);
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test(expected=MailException.class)
	public void testTimeout() throws MailException {
		int emailGivenSize = 20;
		byte[] emailSmallerThanExpectedSize = new String("0123456789").getBytes();
		InputStream emailStream = new ByteArrayInputStream(emailSmallerThanExpectedSize);
		
		Stopwatch stopWatch = new Stopwatch().start();
		try {
			mailboxService.storeInInbox(bs, emailStream, emailGivenSize, true);
		} catch (MailException e) {
			int acceptedTimeoutDeltaInMs = 500;
			assertTimeoutIsInAcceptedDelta(stopWatch, acceptedTimeoutDeltaInMs);
			MailTestsUtils.assertThatIsJavaSocketTimeoutException(e);
			throw e;
		}
	}

	private void assertTimeoutIsInAcceptedDelta(Stopwatch stopWatch, int acceptedDeltaInMs) {
		stopWatch.stop();
		int expectedTimeout = emailConfig.imapTimeout();
		long ourTimeout = stopWatch.elapsedTime(TimeUnit.MILLISECONDS);
		Assertions.assertThat(ourTimeout)
			.isGreaterThan(expectedTimeout)
			.isLessThan(expectedTimeout + acceptedDeltaInMs);
	}

	@Test(expected=MailException.class)
	public void testStoreInInboxThrowExceptionWhenGivenMessageSizeIsLonger() throws MailException {
		int emailGivenSize = 60;
		InputStream emailStream = StreamMailTestsUtils.newInputStreamFromString("This sentence contains 36 characters");
		try {
			mailboxService.storeInInbox(bs, emailStream, emailGivenSize, true);
		} catch (MailException e) {
			MailTestsUtils.assertThatIsJavaSocketTimeoutException(e);
			throw e;
		}
	}

	@Test(expected=MailException.class)
	public void testStoreInInboxRollbackWhenGivenMessageSizeIsLonger() throws Exception {
		int emailGivenSize = 60;
		Date before = DateUtils.date("1970-01-01T12:00:00");
		String inboxPath = collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, IMAP_INBOX_NAME);
		InputStream emailStream = StreamMailTestsUtils.newInputStreamFromString("This sentence contains 36 characters");

		try {
			mailboxService.storeInInbox(bs, emailStream, emailGivenSize, true);
		} catch (MailException e) {
			Set<Email> emails = mailboxService.fetchEmails(bs, inboxPath, before);
			Assertions.assertThat(emails).isNotNull().hasSize(0);
			throw e;
		}
	}
}
