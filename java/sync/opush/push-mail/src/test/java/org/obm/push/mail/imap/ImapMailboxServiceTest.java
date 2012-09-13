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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.minig.imap.Flag;
import org.obm.configuration.EmailConfiguration;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.opush.mail.StreamMailTestsUtils;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxFolder;
import org.obm.push.mail.MailboxFolders;
import org.obm.push.utils.DateUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

@RunWith(SlowFilterRunner.class) @Slow
public class ImapMailboxServiceTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	@Inject CollectionPathHelper collectionPathHelper;

	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private ImapTestUtils testUtils;
	private Date beforeTest;
	private UserDataRequest udr;

	@Before
	public void setUp() {
		beforeTest = new Date();
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		udr = new UserDataRequest(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);
		testUtils = new ImapTestUtils(mailboxService, mailboxService, udr, mailbox, beforeTest, collectionPathHelper);
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
		Set<Email> emails = mailboxService.fetchEmails(udr, mailboxPath(IMAP_INBOX_NAME), before);
		Assertions.assertThat(emails).isNotNull().hasSize(1);
	}
	
	@Test(expected=MailException.class)
	public void testCreateCaseInsensitiveInbox() throws Exception {
		MailboxFolder newFolder = folder("inBox");
		OpushImapFolder result = mailboxService.createFolder(udr, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(udr);
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox());
	}
	
	@Test
	public void testCreateSpecialNameMailbox() throws Exception {
		MailboxFolder newFolder = folder("to&to");
		OpushImapFolder result = mailboxService.createFolder(udr, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(udr);
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test
	public void testCreateUtf8Mailbox() throws Exception {
		MailboxFolder newFolder = folder("éàêôï");
		OpushImapFolder result = mailboxService.createFolder(udr, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(udr);
		Assertions.assertThat(result).isNotNull();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test
	public void testUpdateMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();

		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		
		Email email = Iterables.getOnlyElement(mailboxService.fetchEmails(udr, mailBoxPath, date));
		
		mailboxService.updateMailFlag(udr, mailBoxPath, email.getUid(), Flag.SEEN, true);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		Assertions.assertThat(Iterables.getOnlyElement(emails).isRead()).isTrue();
	}
	
	@Ignore("Greenmail replied that the command succeed")
	@Test(expected=ImapMessageNotFoundException.class)
	public void testUpdateMailFlagWithBadUID() throws Exception {
		long mailUIDNotExist = 1l;
		mailboxService.updateMailFlag(udr, mailboxPath(IMAP_INBOX_NAME), mailUIDNotExist, Flag.SEEN, true);
	}
	
	@Test
	public void testUpdateReadMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		String mailBoxPath = mailboxPath(mailBox);
		Date date = DateUtils.getMidnightCalendar().getTime();
		
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		Email emailNotRead = Iterables.getOnlyElement(emails);
		mailboxService.updateReadFlag(udr, mailBoxPath, emailNotRead.getUid(), true);
		
		Set<Email> emailsAfterToReadMail = mailboxService.fetchEmails(udr, mailBoxPath, date);
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
		Set<Email> emails = mailboxService.fetchEmails(udr, mailBoxPath, date);
		
		Email email = Iterables.getOnlyElement(emails);
		mailboxService.setAnsweredFlag(udr, mailBoxPath, email.getUid());
		
		Set<Email> emailsAfterToSetAnsweredFlag = mailboxService.fetchEmails(udr, mailBoxPath, date);
		Email answeredEmail = Iterables.getOnlyElement(emailsAfterToSetAnsweredFlag);
		
		Assertions.assertThat(emails).isNotNull().hasSize(1);
		Assertions.assertThat(emailsAfterToSetAnsweredFlag).isNotNull().hasSize(1);
		
		Assertions.assertThat(email.isAnswered()).isFalse();
		Assertions.assertThat(answeredEmail.isAnswered()).isTrue();
	}

	@Test
	public void testStoreInInbox() throws Exception {
		final InputStream tinyInputStream = StreamMailTestsUtils.newInputStreamFromString("test");

		mailboxService.storeInInbox(udr, tinyInputStream, true);

		InputStream fetchMailStream = mailboxService.fetchMailStream(udr, mailboxPath(IMAP_INBOX_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("test\r\n\r\n");
		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test
	public void testStoreInSentBox() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		InputStream inputStream = StreamMailTestsUtils.newInputStreamFromString("mail sent");
		mailboxService.storeInSent(udr, inputStream);

		InputStream fetchMailStream = mailboxService.fetchMailStream(udr, mailboxPath(EmailConfiguration.IMAP_SENT_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("mail sent");

		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}

	@Test(expected=MailException.class)
	public void testStoreInSentBoxWithNullInputStream() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		InputStream inputStream = null;
		mailboxService.storeInSent(udr, inputStream);
	}

	@Test
	public void testStoreInSentBoxWithNoDirectlyResetableInputStream() throws MailException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		InputStream emailData = testUtils.getInputStreamFromFile("plainText.eml");
		boolean isResetable = true;
		try {
			emailData.reset();
		} catch (IOException e1) {
			isResetable = false;
		}

		mailboxService.storeInSent(udr, emailData);
		Assertions.assertThat(isResetable).isFalse();
	}

	@Test
	public void testStoreInSentBoxAfterToConsumeIt() throws MailException, IOException {
		MailboxFolder newFolder = folder(EmailConfiguration.IMAP_SENT_NAME);
		mailboxService.createFolder(udr, newFolder);

		InputStream inputStream = StreamMailTestsUtils.newInputStreamFromString("mail sent");
		consumeInputStream(inputStream);

		mailboxService.storeInSent(udr, inputStream);

		InputStream fetchMailStream = mailboxService.fetchMailStream(udr, mailboxPath(EmailConfiguration.IMAP_SENT_NAME), 1l);
		InputStream expectedEmailData = StreamMailTestsUtils.newInputStreamFromString("mail sent");

		Assertions.assertThat(fetchMailStream).hasContentEqualTo(expectedEmailData);
	}
	
	@Test
	public void testMoveItem() throws Exception {
		String trash = EmailConfiguration.IMAP_TRASH_NAME;
		MailboxFolder trashFolder = folder(trash);
		mailboxService.createFolder(udr, trashFolder);
		
		final InputStream tinyInputStream = StreamMailTestsUtils.newInputStreamFromString("test");
		mailboxService.storeInInbox(udr, tinyInputStream, true);
		
		String inboxCollectionName = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		String trashCollectionName = testUtils.mailboxPath(trash);
		
		long newUid = mailboxService.moveItem(udr, inboxCollectionName, trashCollectionName, 1);
		assertThat(mailboxService.fetchEmails(udr, inboxCollectionName, ImmutableList.<Long> of(newUid))).isEmpty();
		Collection<Email> trashEmails = mailboxService.fetchEmails(udr, trashCollectionName, ImmutableList.<Long> of(newUid));
		assertThat(trashEmails).hasSize(1);
		assertThat(Iterables.getFirst(trashEmails, null).getUid()).isEqualTo(newUid);
	}
	
	@Ignore("Greenmail replied that the command succeed")
	@Test(expected=ImapMessageNotFoundException.class)
	public void testMoveItemEmptyMailbox() throws Exception {
		String trash = EmailConfiguration.IMAP_TRASH_NAME;
		MailboxFolder trashFolder = folder(trash);
		mailboxService.createFolder(udr, trashFolder);
		
		mailboxService.moveItem(udr, testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME), testUtils.mailboxPath(trash), 1);
	}
	
	@Test
	public void testFetchMimeSinglePartBase64Email() throws Exception {
		InputStream mailStream = testUtils.getInputStreamFromFile("SinglePartBase64.eml");
		mailboxService.storeInInbox(udr, mailStream, false);
		
		String inboxCollectionName = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		List<MSEmail> emails = mailboxService.fetch(udr, 1, inboxCollectionName, 
				Arrays.asList(1l), 
				Arrays.asList(BodyPreference.builder().bodyType(MSEmailBodyType.MIME).build()));
		MSEmail actual = Iterables.getOnlyElement(emails);
		assertThat(actual.getBody().getMimeData()).hasContentEqualTo(testUtils.getInputStreamFromFile("SinglePartBase64.eml"));
	}

	@Ignore("greenmail seems to unexpectedly decode base64 part on-the-fly")
	@Test
	public void testFetchTextPlainSinglePartBase64Email() throws Exception {
		InputStream mailStream = testUtils.getInputStreamFromFile("SinglePartBase64.eml");
		mailboxService.storeInInbox(udr, mailStream, false);
		
		String inboxCollectionName = testUtils.mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		List<MSEmail> emails = mailboxService.fetch(udr, 1, inboxCollectionName, 
				Arrays.asList(1l), 
				Arrays.asList(BodyPreference.builder().bodyType(MSEmailBodyType.PlainText).build()));
		MSEmail actual = Iterables.getOnlyElement(emails);
		String bodyText = new String(ByteStreams.toByteArray(actual.getBody().getMimeData()), Charsets.UTF_8);
		assertThat(bodyText).contains("Envoyé de mon iPhone");
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
		return testUtils.folder(name);
	}
	
	private MailboxFolder inbox() {
		return testUtils.inbox();
	}

}
