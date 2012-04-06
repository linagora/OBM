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

import java.util.Date;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.User;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class) @Slow
public class ImapDeleteAPITest {

	private static final String INBOX = EmailConfiguration.IMAP_INBOX_NAME;
	private static final String SENTBOX = EmailConfiguration.IMAP_SENT_NAME;
	private static final String DRAFT = EmailConfiguration.IMAP_DRAFTS_NAME;
	private static final String TRASH = EmailConfiguration.IMAP_TRASH_NAME;
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject MailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	@Inject CollectionPathHelper collectionPathHelper;
	
	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private BackendSession bs;
	
	private Date beforeTest;
	private ImapTestUtils testUtils;
	
	@Before
	public void setUp() throws MailException {
		beforeTest = new Date();
	    greenMail.start();
	    mailbox = "to@localhost.com";
	    password = "password";
	    greenMail.setUser(mailbox, password);
	    bs = new BackendSession(
				new Credentials(User.Factory.create()
						.createUser(mailbox, mailbox, null), password), null, null, null);

	    testUtils = new ImapTestUtils(mailboxService, privateMailboxService, bs, mailbox, beforeTest, collectionPathHelper);
	    testUtils.createFolders(TRASH);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testDeleteFromInbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();
		Set<Email> mailboxEmailsBefore = testUtils.mailboxEmails(INBOX);
		
		mailboxService.delete(bs, testUtils.mailboxPath(INBOX), sentEmail.getUid());
		
		Set<Email> mailboxEmailsAfter = testUtils.mailboxEmails(INBOX);
		Assertions.assertThat(mailboxEmailsBefore).hasSize(1);
		Assertions.assertThat(mailboxEmailsAfter).isEmpty();
	}

	@Test
	public void testDeleteFromDraft() throws Exception {
		testUtils.createFolders(DRAFT);
		Email sentEmail = testUtils.sendEmailToMailbox(DRAFT);
		Set<Email> mailboxEmailsBefore = testUtils.mailboxEmails(DRAFT);
		
		mailboxService.delete(bs, testUtils.mailboxPath(DRAFT), sentEmail.getUid());
		
		Set<Email> mailboxEmailsAfter = testUtils.mailboxEmails(DRAFT);
		Assertions.assertThat(mailboxEmailsBefore).hasSize(1);
		Assertions.assertThat(mailboxEmailsAfter).isEmpty();
	}
	
	@Test
	public void testDeleteFromSent() throws Exception {
		testUtils.createFolders(SENTBOX);
		Email sentEmail = testUtils.sendEmailToMailbox(SENTBOX);
		Set<Email> mailboxEmailsBefore = testUtils.mailboxEmails(SENTBOX);
		
		mailboxService.delete(bs, testUtils.mailboxPath(SENTBOX), sentEmail.getUid());
		
		Set<Email> mailboxEmailsAfter = testUtils.mailboxEmails(SENTBOX);
		Assertions.assertThat(mailboxEmailsBefore).hasSize(1);
		Assertions.assertThat(mailboxEmailsAfter).isEmpty();
	}
	
	@Test
	public void testDeleteFromTrash() throws Exception {
		Email sentEmail = testUtils.sendEmailToMailbox(TRASH);
		Set<Email> mailboxEmailsBefore = testUtils.mailboxEmails(TRASH);
		
		mailboxService.delete(bs, testUtils.mailboxPath(TRASH), sentEmail.getUid());
		
		Set<Email> mailboxEmailsAfter = testUtils.mailboxEmails(TRASH);
		Assertions.assertThat(mailboxEmailsBefore).hasSize(1);
		Assertions.assertThat(mailboxEmailsAfter).isEmpty();
	}

	@Test
	public void testDeleteFromOtherFolder() throws Exception {
		String otherFolder = "ANYFOLDER";
		testUtils.createFolders(otherFolder);
		Email sentEmail = testUtils.sendEmailToMailbox(otherFolder);
		Set<Email> mailboxEmailsBefore = testUtils.mailboxEmails(otherFolder);
		
		mailboxService.delete(bs, testUtils.mailboxPath(otherFolder), sentEmail.getUid());
		
		Set<Email> mailboxEmailsAfter = testUtils.mailboxEmails(otherFolder);
		Assertions.assertThat(mailboxEmailsBefore).hasSize(1);
		Assertions.assertThat(mailboxEmailsAfter).isEmpty();
	}
	
	@Test
	public void testDeleteOneEmailAmongManyEmails() throws Exception {
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		
		Set<Email> mailboxEmailsBefore = testUtils.mailboxEmails(INBOX);
		Email anEmailFromMailbox = Iterables.get(mailboxEmailsBefore, 2);
		
		mailboxService.delete(bs, testUtils.mailboxPath(INBOX), anEmailFromMailbox.getUid());
		
		Set<Email> mailboxEmailsAfter = testUtils.mailboxEmails(INBOX);
		Assertions.assertThat(mailboxEmailsBefore).hasSize(3);
		Assertions.assertThat(mailboxEmailsAfter).hasSize(2);
		Assertions.assertThat(mailboxEmailsAfter).doesNotContain(anEmailFromMailbox);
	}

	@Test(expected=ImapMessageNotFoundException.class)
	public void testDeleteNonExistingEmailTriggersAnException() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();
		long nonExistingEmailUid = sentEmail.getUid() + 1;

		mailboxService.delete(bs, testUtils.mailboxPath(INBOX), nonExistingEmailUid);
	}

	@Test(expected=ImapMessageNotFoundException.class)
	public void testDeleteExistingEmailInNonExistingFolderTriggersAnException() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();
		String otherFolder = "ANYFOLDER";
		testUtils.createFolders(otherFolder);
		
		mailboxService.delete(bs, testUtils.mailboxPath(otherFolder), sentEmail.getUid());
	}

	@Test(expected=ImapMessageNotFoundException.class)
	public void testDeleteAttemptedTwiceOnSameEmailTriggersAnException() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();
		String inboxPath = testUtils.mailboxPath(INBOX);
		
		mailboxService.delete(bs, inboxPath, sentEmail.getUid());
		mailboxService.delete(bs, inboxPath, sentEmail.getUid());
	}
}
