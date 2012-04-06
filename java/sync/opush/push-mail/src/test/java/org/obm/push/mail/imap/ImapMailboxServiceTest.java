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

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import javax.mail.Flags;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.minig.imap.MailboxFolder;
import org.minig.imap.MailboxFolders;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class) @Slow
public class ImapMailboxServiceTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	@Inject CollectionPathHelper collectionPathHelper;

	@Inject EmailConfiguration emailConfig;
	@Inject GreenMail greenMail;
	@Inject ImapMailBoxUtils mailboxUtils;
	private String mailbox;
	private String password;
	private BackendSession bs;
	private ImapTestUtils testUtils;
	private Date beforeTest;

	@Before
	public void setUp() {
		beforeTest = new Date();
	    greenMail.start();
	    mailbox = "to@localhost.com";
	    password = "password";
	    greenMail.setUser(mailbox, password);
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
	    testUtils = new ImapTestUtils(mailboxService, mailboxService, bs, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test
	public void testFetchFast() throws MailException, InterruptedException {
		Date before = new Date();
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(bs, mailboxPath(IMAP_INBOX_NAME), before);
		Assertions.assertThat(emails).isNotNull().hasSize(1);
	}
	
	@Test
	public void testDefaultListFolders() throws Exception {
		MailboxFolders emails = mailboxService.listAllFolders(bs);
		Assertions.assertThat(emails).isNotNull().containsOnly(inbox());
	}
	
	@Test
	public void testListTwoFolders() throws Exception {
		MailboxFolder newFolder = folder("NEW");
		mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(),
				newFolder);
	}

	@Test
	public void testListInboxSubfolder() throws Exception {
		MailboxFolder newFolder = folder("INBOX.NEW");
		mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(),
				newFolder);
	}
	
	@Test
	public void testListInboxDeepSubfolder() throws Exception {
		MailboxFolder newFolder = folder("INBOX.LEVEL1.LEVEL2.LEVEL3.LEVEL4");
		mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(),
				folder("INBOX.LEVEL1"),
				folder("INBOX.LEVEL1.LEVEL2"),
				folder("INBOX.LEVEL1.LEVEL2.LEVEL3"),
				folder("INBOX.LEVEL1.LEVEL2.LEVEL3.LEVEL4"));
	}

	@Test
	public void testListToplevelFolder() throws Exception {
		MailboxFolder newFolder = folder("TOP");
		mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(),
				folder("TOP"));
	}
	
	@Test
	public void testListNestedToplevelFolder() throws Exception {
		MailboxFolder newFolder = folder("TOP.LEVEL1");
		mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(),
				folder("TOP"),
				folder("TOP.LEVEL1"));
	}
	
	@Test(expected=MailException.class)
	public void testCreateCaseInsensitiveInbox() throws Exception {
		MailboxFolder newFolder = folder("inBox");
		OpushImapFolder result = mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox());
	}
	
	@Test
	public void testCreateSpecialNameMailbox() throws Exception {
		MailboxFolder newFolder = folder("to&to");
		OpushImapFolder result = mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test
	public void testCreateUtf8Mailbox() throws Exception {
		MailboxFolder newFolder = folder("éàêôï");
		OpushImapFolder result = mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test(expected=ImapMessageNotFoundException.class)
	public void testExpunge() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();

		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		
		Set<Email> emails = mailboxService.fetchEmails(bs, mailBoxPath, date);
		long uid = Iterables.getOnlyElement(emails).getUid();

		mailboxService.updateMailFlag(bs, mailBoxPath, uid, Flags.Flag.DELETED, true);
		mailboxService.expunge(bs,  mailBoxPath);
		mailboxService.getMessage(bs, mailBoxPath, uid);
	}
	
	@Test
	public void testUpdateMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();

		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		
		Email email = Iterables.getOnlyElement(mailboxService.fetchEmails(bs, mailBoxPath, date));
		
		mailboxService.updateMailFlag(bs, mailBoxPath, email.getUid(), Flags.Flag.SEEN, true);
		Set<Email> emails = mailboxService.fetchEmails(bs, mailBoxPath, date);
		
		Assertions.assertThat(Iterables.getOnlyElement(emails).isRead()).isTrue();
	}
	
	@Test(expected=ImapMessageNotFoundException.class)
	public void testUpdateMailFlagWithBadUID() throws Exception {
		long mailUIDNotExist = 1l;
		mailboxService.updateMailFlag(bs, mailboxPath(IMAP_INBOX_NAME), mailUIDNotExist, Flags.Flag.SEEN, true);
	}
	
	@Test
	public void testUpdateReadMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();
		
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(bs, mailBoxPath, date);
		
		Email emailNotRead = Iterables.getOnlyElement(emails);
		mailboxService.updateReadFlag(bs, mailBoxPath, emailNotRead.getUid(), true);
		
		Set<Email> emailsAfterToReadMail = mailboxService.fetchEmails(bs, mailBoxPath, date);
		Email emailHasRead = Iterables.getOnlyElement(emailsAfterToReadMail);
		
		Assertions.assertThat(emails).isNotNull().hasSize(1);
		Assertions.assertThat(emailsAfterToReadMail).isNotNull().hasSize(1);
		
		Assertions.assertThat(emailNotRead.isRead()).isFalse();
		Assertions.assertThat(emailHasRead.isRead()).isTrue();
	}
	
	@Test
	public void testSetAnsweredFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = testUtils.mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();
		
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(bs, mailBoxPath, date);
		
		Email email = Iterables.getOnlyElement(emails);
		mailboxService.setAnsweredFlag(bs, mailBoxPath, email.getUid());
		
		Set<Email> emailsAfterToSetAnsweredFlag = mailboxService.fetchEmails(bs, mailBoxPath, date);
		Email answeredEmail = Iterables.getOnlyElement(emailsAfterToSetAnsweredFlag);
		
		Assertions.assertThat(emails).isNotNull().hasSize(1);
		Assertions.assertThat(emailsAfterToSetAnsweredFlag).isNotNull().hasSize(1);
		
		Assertions.assertThat(email.isAnswered()).isFalse();
		Assertions.assertThat(answeredEmail.isAnswered()).isTrue();
	}

	@Test
	public void testStoreInInbox() throws Exception {
		final InputStream tinyInputStream = StreamMailTestsUtils.newInputStreamFromString("test");

		mailboxService.storeInInbox(bs, tinyInputStream, true);

		InputStream fetchMailStream = mailboxService.fetchMailStream(bs, mailboxPath(IMAP_INBOX_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("test\r\n\r\n");
		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test
	public void testStoreInInboxStream() throws Exception {
		final InputStream tinyInputStream = StreamMailTestsUtils.newInputStreamFromString("test");

		mailboxService.storeInInbox(bs, tinyInputStream, 4, true);

		InputStream fetchMailStream = mailboxService.fetchMailStream(bs, mailboxPath(IMAP_INBOX_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("test\r\n\r\n");
		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test
	public void testStoreInSentBox() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(bs, newFolder);

		InputStream inputStream = StreamMailTestsUtils.newInputStreamFromString("mail sent");
		mailboxService.storeInSent(bs, inputStream);

		InputStream fetchMailStream = mailboxService.fetchMailStream(bs, mailboxPath(EmailConfiguration.IMAP_SENT_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("mail sent");

		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test(expected=MailException.class)
	public void testStoreInSentBoxWithNullInputStream() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(bs, newFolder);

		InputStream inputStream = null;
		mailboxService.storeInSent(bs, inputStream);
	}

	@Test
	public void testStoreInSentBoxWithNoDirectlyResetableInputStream() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(bs, newFolder);

		InputStream emailData = getInputStreamFromFile("plainText.eml");
		boolean isResetable = true;
		try {
			emailData.reset();
		} catch (IOException e1) {
			isResetable = false;
		}

		mailboxService.storeInSent(bs, emailData);
		Assertions.assertThat(isResetable).isFalse();
	}

	@Test
	public void testStoreInSentBoxAfterToConsumeIt() throws MailException, IOException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(bs, newFolder);

		InputStream inputStream = StreamMailTestsUtils.newInputStreamFromString("mail sent");
		consumeInputStream(inputStream);

		mailboxService.storeInSent(bs, inputStream);

		InputStream fetchMailStream = mailboxService.fetchMailStream(bs, mailboxPath(EmailConfiguration.IMAP_SENT_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("mail sent");

		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}
	
	private void consumeInputStream(InputStream inputStream) throws IOException {
		while (inputStream.read() != -1) {
			// consume Inputstream
		}		
	}

	private String mailboxPath(String boxName) {
		return testUtils.mailboxPath(boxName);
	}

	private MailboxFolder folder(String name) {
		return new MailboxFolder(name);
	}
	
	private MailboxFolder inbox() {
		return folder("INBOX");
	}

	private InputStream getInputStreamFromFile(String name) {
		return ClassLoader.getSystemResourceAsStream("eml/" + name);
	}

}
