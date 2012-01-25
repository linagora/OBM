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

import java.util.Date;
import java.util.Set;

import javax.mail.Flags;

import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.minig.imap.MailboxFolder;
import org.minig.imap.MailboxFolders;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

public class ImapMailboxServiceTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;
	
	@Inject GreenMail greenMail;
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
						.createUser(mailbox, mailbox, null), password, null), null, null, null);
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
		Set<Email> emails = mailboxService.fetchEmails(bs, IMAP_INBOX_NAME, before);
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
	
	@Test
	public void testCreateCaseInsensitiveInbox() throws Exception {
		MailboxFolder newFolder = folder("inBox");
		boolean result = mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(result).isFalse();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox());
	}
	
	@Test
	public void testCreateSpecialNameMailbox() throws Exception {
		MailboxFolder newFolder = folder("to&to");
		boolean result = mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(result).isTrue();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test
	public void testCreateUtf8Mailbox() throws Exception {
		MailboxFolder newFolder = folder("éàêôï");
		boolean result = mailboxService.createFolder(bs, newFolder);
		MailboxFolders after = mailboxService.listAllFolders(bs);
		Assertions.assertThat(result).isTrue();
		Assertions.assertThat(after).isNotNull().containsOnly(
				inbox(), newFolder);
	}
	
	@Test(expected=MailNotFoundException.class)
	public void testExpunge() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		Date date = DateUtils.getMidnightCalendar().getTime();

		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		
		Set<Email> emails = mailboxService.fetchEmails(bs, mailBox, date);
		long uid = Iterables.getOnlyElement(emails).getUid();

		mailboxService.updateMailFlag(bs, mailBox, uid, Flags.Flag.DELETED, true);
		mailboxService.expunge(bs,  mailBox);
		mailboxService.getMessage(bs, mailBox, uid);
	}
	
	@Test
	public void testUpdateMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		Date date = DateUtils.getMidnightCalendar().getTime();

		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		
		Email email = Iterables.getOnlyElement(mailboxService.fetchEmails(bs, mailBox, date));
		
		mailboxService.updateMailFlag(bs, mailBox, email.getUid(), Flags.Flag.SEEN, true);
		Set<Email> emails = mailboxService.fetchEmails(bs, mailBox, date);
		
		Assertions.assertThat(Iterables.getOnlyElement(emails).isRead()).isTrue();
	}
	
	@Test(expected=MailNotFoundException.class)
	public void testUpdateMailFlagWithBadUID() throws Exception {
		long mailUIDNotExist = 1l;
		mailboxService.updateMailFlag(bs, EmailConfiguration.IMAP_INBOX_NAME, mailUIDNotExist, Flags.Flag.SEEN, true);
	}
	
	@Test
	public void testUpdateReadMailFlag() throws Exception {
		String mailBox = EmailConfiguration.IMAP_INBOX_NAME;
		Date date = DateUtils.getMidnightCalendar().getTime();
		
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		greenMail.waitForIncomingEmail(1);
		Set<Email> emails = mailboxService.fetchEmails(bs, mailBox, date);
		
		Email emailNotRead = Iterables.getOnlyElement(emails);
		mailboxService.updateReadFlag(bs, mailBox, emailNotRead.getUid(), true);
		
		Set<Email> emailsAfterToReadMail = mailboxService.fetchEmails(bs, mailBox, date);
		Email emailHasRead = Iterables.getOnlyElement(emailsAfterToReadMail);
		
		Assertions.assertThat(emails).isNotNull().hasSize(1);
		Assertions.assertThat(emailsAfterToReadMail).isNotNull().hasSize(1);
		
		Assertions.assertThat(emailNotRead.isRead()).isFalse();
		Assertions.assertThat(emailHasRead.isRead()).isTrue();
	}
	
	private MailboxFolder folder(String name) {
		return new MailboxFolder(name);
	}
	
	private MailboxFolder inbox() {
		return folder("INBOX");
	}
}
