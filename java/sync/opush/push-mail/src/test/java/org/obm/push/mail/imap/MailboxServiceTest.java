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
package org.obm.push.mail.imap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;
import static org.obm.push.mail.MailTestsUtils.loadEmail;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.exception.MailException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.EmailReader;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MailboxFolders;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(GuiceRunner.class)
@GuiceModule(MailEnvModule.class)
public class MailboxServiceTest {

	@Inject MailboxService mailboxService;
	@Inject ICollectionPathHelper collectionPathHelper;

	@Inject GreenMail greenMail;
	ServerSetup smtpServerSetup;
	private String mailbox;
	private String password;
	private MailboxTestUtils testUtils;
	private Date beforeTest;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		beforeTest = new Date();
		greenMail.start();
		smtpServerSetup = greenMail.getSmtp().getServerSetup();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null);
		testUtils = new MailboxTestUtils(mailboxService, udr, mailbox, beforeTest, collectionPathHelper, smtpServerSetup);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}

	@Test
	public void testFetchFast() throws MailException, InterruptedException {
		Date before = new Date();
		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailboxPath(IMAP_INBOX_NAME), before);
		assertThat(emails).isNotNull().hasSize(1);
	}
	
	@Test(expected=MailException.class)
	public void testCreateCaseInsensitiveInbox() throws Exception {
		MailboxFolder newFolder = folder("inBox");
		mailboxService.createFolder(udr, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(udr);
		assertThat(after).isNotNull().containsOnly(
				inbox());
	}
	
	@Test
	public void testCreateSpecialNameMailbox() throws Exception {
		MailboxFolder newFolder = folder("to&to");
		mailboxService.createFolder(udr, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(udr);
		assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test
	public void testCreateUtf8Mailbox() throws Exception {
		MailboxFolder newFolder = folder("éàêôï");
		mailboxService.createFolder(udr, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(udr);
		assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test
	public void testUpdateMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();

		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(1);
		
		Email email = Iterables.getOnlyElement(mailboxService.fetchEmails(udr, mailBoxPath, date));
		
		mailboxService.updateReadFlag(udr, mailBoxPath, MessageSet.singleton(email.getUid()), true);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		assertThat(Iterables.getOnlyElement(emails).isRead()).isTrue();
	}
	
	@Test
	public void testUpdateSeveralMailsFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();

		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(2);
		
		mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		mailboxService.updateReadFlag(udr, mailBoxPath, MessageSet.builder().add(1l).add(2l).build(), true);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		assertThat(emails).hasSize(2);
		assertThat(Iterables.get(emails, 0).isRead()).isTrue();
		assertThat(Iterables.get(emails, 1).isRead()).isTrue();
	}
	
	@Ignore("Greenmail replied that the command succeed")
	@Test(expected=ImapMessageNotFoundException.class)
	public void testUpdateMailFlagWithBadUID() throws Exception {
		long mailUIDNotExist = 1l;
		mailboxService.updateReadFlag(udr, mailboxPath(IMAP_INBOX_NAME), MessageSet.singleton(mailUIDNotExist), true);
	}
	
	@Test
	public void testUpdateReadMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();
		
		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		Email emailNotRead = Iterables.getOnlyElement(emails);
		mailboxService.updateReadFlag(udr, mailBoxPath, MessageSet.singleton(emailNotRead.getUid()), true);
		
		Set<Email> emailsAfterToReadMail = mailboxService.fetchEmails(udr, mailBoxPath, date);
		Email emailHasRead = Iterables.getOnlyElement(emailsAfterToReadMail);
		
		assertThat(emails).isNotNull().hasSize(1);
		assertThat(emailsAfterToReadMail).isNotNull().hasSize(1);
		
		assertThat(emailNotRead.isRead()).isFalse();
		assertThat(emailHasRead.isRead()).isTrue();
	}
	
	@Test
	public void testSetAnsweredFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = testUtils.mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();
		
		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		Email email = Iterables.getOnlyElement(emails);
		mailboxService.setAnsweredFlag(udr, mailBoxPath, MessageSet.singleton(email.getUid()));
		
		Set<Email> emailsAfterToSetAnsweredFlag = mailboxService.fetchEmails(udr, mailBoxPath, date);
		Email answeredEmail = Iterables.getOnlyElement(emailsAfterToSetAnsweredFlag);
		
		assertThat(emails).isNotNull().hasSize(1);
		assertThat(emailsAfterToSetAnsweredFlag).isNotNull().hasSize(1);
		
		assertThat(email.isAnswered()).isFalse();
		assertThat(answeredEmail.isAnswered()).isTrue();
	}

	@Test
	public void testSetDeletedFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = testUtils.mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();
		
		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		Email email = Iterables.getOnlyElement(emails);
		mailboxService.setDeletedFlag(udr, mailBoxPath, MessageSet.singleton(email.getUid()));
		
		Collection<Email> emailsAfterSetDeletedFlag = mailboxService.fetchEmails(udr, mailBoxPath, MessageSet.singleton(email.getUid()));
		
		assertThat(emails).isNotNull().hasSize(1);
		assertThat(emailsAfterSetDeletedFlag).isNotNull().isEmpty();
	}
	
	@Test
	public void testStoreInInbox() throws Exception {
		final InputStream tinyInputStream = StreamMailTestsUtils.newInputStreamFromString("test");

		MailboxTestUtils.storeInInbox(udr, mailboxService, tinyInputStream);

		InputStream fetchMailStream = mailboxService.fetchMailStream(udr, mailboxPath(IMAP_INBOX_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("test\r\n\r\n");
		assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test
	public void testStoreInSentBox() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		InputStream inputStream = StreamMailTestsUtils.newInputStreamFromString("mail sent");
		MailboxTestUtils.storeInSent(udr, mailboxService, inputStream);

		InputStream fetchMailStream = mailboxService.fetchMailStream(udr, mailboxPath(EmailConfiguration.IMAP_SENT_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("mail sent\r\n\r\n");

		assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test(expected=MailException.class)
	public void testStoreInSentBoxWithNullInputStream() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		EmailReader reader = null;
		mailboxService.storeInSent(udr, reader);
	}

	@Test(expected=MailException.class)
	public void testStoreInSentBoxWithNoDirectlyResetableInputStream() throws Exception {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		EmailReader emailData = new EmailReader(loadEmail("plainText.eml")) {
			@Override
			public void reset() throws IOException {
				throw new IOException("reset not supported");
			}
		};
		boolean isResetable = true;
		try {
			emailData.reset();
		} catch (IOException e1) {
			isResetable = false;
		}

		try {
			mailboxService.storeInSent(udr, emailData);
		} catch (Exception e) {
			assertThat(isResetable).isFalse();
			throw e;
		}
	}

	@Test
	public void testStoreInSentBoxAfterToConsumeIt() throws MailException, IOException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		String emailData = "mail sent";
		EmailReader reader = new EmailReader(StreamMailTestsUtils.newInputStreamFromString(emailData));
		reader.mark(emailData.length() + 1);
		consumeInputStream(reader);

		mailboxService.storeInSent(udr, reader);

		InputStream fetchMailStream = mailboxService.fetchMailStream(udr, mailboxPath(EmailConfiguration.IMAP_SENT_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("mail sent\r\n\r\n");

		assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}
	
	@Test
	public void testMoveItem() throws Exception {
		String trash = EmailConfiguration.IMAP_TRASH_NAME;
		MailboxFolder trashFolder = folder(trash);
		mailboxService.createFolder(udr, trashFolder);

		Reader tinyReader = StreamMailTestsUtils.newReaderFromString("test");
		mailboxService.storeInInbox(udr, new EmailReader(tinyReader), true);
		
		String inboxCollectionName = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		String trashCollectionName = testUtils.mailboxPath(trash);
		
		MessageSet newUid = mailboxService.move(udr, inboxCollectionName, trashCollectionName, MessageSet.singleton(1l));
		assertThat(mailboxService.fetchEmails(udr, inboxCollectionName, newUid)).isEmpty();
		Collection<Email> trashEmails = mailboxService.fetchEmails(udr, trashCollectionName, newUid);
		assertThat(trashEmails).hasSize(1);
		assertThat(Iterables.getFirst(trashEmails, null).getUid()).isEqualTo(Iterables.getOnlyElement(newUid));
	}
	
	@Ignore("Greenmail replied that the command succeed")
	@Test(expected=ImapMessageNotFoundException.class)
	public void testMoveItemEmptyMailbox() throws Exception {
		String trash = EmailConfiguration.IMAP_TRASH_NAME;
		MailboxFolder trashFolder = folder(trash);
		mailboxService.createFolder(udr, trashFolder);
		
		mailboxService.move(udr, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), testUtils.mailboxPath(trash), MessageSet.singleton(1));
	}

	@Test
	public void testAcceptsNotASCIIUserPassword() throws Exception {
		Date fromDate = DateUtils.getMidnightCalendar().getTime();
		String user = "weird@localhost.com";
		String weirdPassword = "password°£";
		greenMail.setUser(user, weirdPassword);
		UserDataRequest udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(user, user, null), weirdPassword), null, null);
		
		GreenMailUtil.sendTextEmail(user, "from@localhost.com", "subject", "body", smtpServerSetup);
		greenMail.waitForIncomingEmail(1);
		String mailBoxPath = new MailboxTestUtils(mailboxService, udr, mailbox, beforeTest, collectionPathHelper, smtpServerSetup)
			.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, fromDate);
		
		assertThat(emails).hasSize(1);
	}
	
	@Test
	public void testUidFetchPartFindAttachment() throws Exception {
		InputStream emailStream = loadEmail("multipartAlternative.eml");
		mailboxService.storeInInbox(udr, new EmailReader(emailStream), false);
		
		String inbox = collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		
		InputStream attachment = mailboxService.findAttachment(udr, inbox, 1l, new MimeAddress("3"));
		
		assertThat(attachment).hasContentEqualTo(loadEmail("multipartAlternative-part3.txt"));
	}

	private void consumeInputStream(Reader inputStream) throws IOException {
		while (inputStream.read() != -1) {
			// consume Inputstream
		}		
	}

	private String mailboxPath(String boxName) {
		return testUtils.mailboxPath(boxName);
	}

	private MailboxFolder folder(String name) {
		return testUtils.folder(name);
	}
	
	private MailboxFolder inbox() {
		return testUtils.inbox();
	}

}
