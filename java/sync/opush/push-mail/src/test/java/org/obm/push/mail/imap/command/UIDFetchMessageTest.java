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
package org.obm.push.mail.imap.command;

import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.InputStream;
import java.util.Date;

import javax.mail.MessagingException;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.DateUtils;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.exception.ImapCommandException;
import org.obm.push.exception.ImapLoginException;
import org.obm.push.exception.NoImapClientAvailableException;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;
import org.obm.push.mail.imap.ImapClientProvider;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.ImapTestUtils;
import org.obm.push.mail.imap.OpushImapFolder;

import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;

import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class) @Slow
public class UIDFetchMessageTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapClientProvider clientProvider;

	@Inject CollectionPathHelper collectionPathHelper;
	@Inject MailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private BackendSession bs;
	private ImapTestUtils testUtils;
	private Date beforeTest;

	@Before
	public void setUp() {
		beforeTest = DateUtils.date("1970-01-01T12:00:00");
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		testUtils = new ImapTestUtils(mailboxService, privateMailboxService, bs, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testUidFetchPartSimple() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("plainText.eml"));

		InputStream fetchPart = uidFetchMessage(sentEmail.getUid());
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("plainText.eml"));
	}
	
	@Test
	public void testUidFetchPartAlternative() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartAlternative.eml"));

		InputStream fetchPart = uidFetchMessage(sentEmail.getUid());
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartAlternative.eml"));
	}
	
	@Test
	public void testUidFetchPartForwarded() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartForwarded.eml"));

		InputStream fetchPart = uidFetchMessage(sentEmail.getUid());
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartForwarded.eml"));
	}
	
	@Test
	public void testUidFetchPartMixed() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartMixed.eml"));

		InputStream fetchPart = uidFetchMessage(sentEmail.getUid());
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartMixed.eml"));
	}
	
	@Test
	public void testUidFetchPartRelated() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("multipartRelated.eml"));

		InputStream fetchPart = uidFetchMessage(sentEmail.getUid());
		
		Assertions.assertThat(fetchPart).hasContentEqualTo(loadEmail("multipartRelated.eml"));
	}

	@Test(expected=ImapMessageNotFoundException.class)
	public void testUidFetchPartWrongUid() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("plainText.eml"));
		long wrongUid = sentEmail.getUid() + 1;
		
		uidFetchMessage(wrongUid);
	}
	
	@Test(expected=ImapMessageNotFoundException.class)
	public void testUidFetchPartWrongSelectedMailbox() throws Exception {
		String mailbox = "wrongmailbox";
		testUtils.createFolders(mailbox);
		Email sentEmail = testUtils.sendEmailToInbox(loadEmail("plainText.eml"));

		ImapStore client = loggedClient();
		OpushImapFolder folder = client.select(mailbox);
		folder.uidFetchMessage(sentEmail.getUid());
	}

	private InputStream uidFetchMessage(long uid) throws ImapMessageNotFoundException, LocatorClientException,
			NoImapClientAvailableException, ImapCommandException, MessagingException {
		
		ImapStore client = loggedClient();
		OpushImapFolder folder = client.select(EmailConfiguration.IMAP_INBOX_NAME);
		return folder.uidFetchMessage(uid);
	}
	
	private ImapStore loggedClient()
			throws LocatorClientException, NoImapClientAvailableException, ImapLoginException  {
		
		ImapStore client = clientProvider.getImapClientWithJM(bs);
		client.login();
		return client;
	}
}
