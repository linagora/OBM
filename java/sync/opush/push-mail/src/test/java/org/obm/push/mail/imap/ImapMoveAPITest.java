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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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

public class ImapMoveAPITest {

	private static final String INBOX = EmailConfiguration.IMAP_INBOX_NAME;
	private static final String SENTBOX = EmailConfiguration.IMAP_SENT_NAME;
	private static final String DRAFT = EmailConfiguration.IMAP_DRAFTS_NAME;
	private static final String TRASH = EmailConfiguration.IMAP_TRASH_NAME;
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject MailboxService mailboxService;
	@Inject PrivateMailboxService privateMailboxService;
	@Inject CollectionPathHelper collectionPathHelper;

	@Inject ImapMailBoxUtils mailboxUtils;
	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private BackendSession bs;

	private Date beforeTest;
	private ImapTestUtils testUtils;

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
	    testUtils = new ImapTestUtils(mailboxService, privateMailboxService, bs, mailbox, beforeTest, collectionPathHelper);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testGreenmailServerImplementUIDPLUS() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		testUtils.createFolders(DRAFT);

		long testErrorUidValue = -1;
		long movedEmailUid = testErrorUidValue;
		try {
			movedEmailUid = mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(DRAFT), sentEmail.getUid());
		} catch (Exception nonExpectedException) {
			Assert.fail("Greenmail should implement UIDPLUS, so no exception is expected");
		}

		Set<Email> movedEmails = testUtils.mailboxEmails(DRAFT);
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.getUid()).isNotEqualTo(testErrorUidValue).isEqualTo(movedEmailUid);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}
	
	@Test
	public void testMoveFromInbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String toMailbox = "ANYBOX";
		testUtils.createFolders(toMailbox);
		
		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(toMailbox), sentEmail.getUid());

		Set<Email> inboxEmails = testUtils.mailboxEmails(INBOX);
		Set<Email> movedEmails = testUtils.mailboxEmails(toMailbox);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToSentbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		testUtils.createFolders(SENTBOX);
		
		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(SENTBOX), sentEmail.getUid());

		Set<Email> inboxEmails = testUtils.mailboxEmails(INBOX);
		Set<Email> movedEmails = testUtils.mailboxEmails(SENTBOX);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToDraft() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		testUtils.createFolders(DRAFT);
		
		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(DRAFT), sentEmail.getUid());

		Set<Email> inboxEmails = testUtils.mailboxEmails(INBOX);
		Set<Email> movedEmails = testUtils.mailboxEmails(DRAFT);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToTrash() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		testUtils.createFolders(TRASH);
		
		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(TRASH), sentEmail.getUid());

		Set<Email> inboxEmails = testUtils.mailboxEmails(INBOX);
		Set<Email> movedEmails = testUtils.mailboxEmails(TRASH);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToInbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		mailboxService.moveItem(bs, INBOX, INBOX, sentEmail.getUid());

		Set<Email> inboxEmails = testUtils.mailboxEmails(INBOX);
		Assertions.assertThat(inboxEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(inboxEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveFromSpecialMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String fromMailbox = "SPECIALBOX";
		String toMailbox = "ANYBOX";
		testUtils.createFolders(fromMailbox, toMailbox);

		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(fromMailbox), sentEmail.getUid());
		Email emailInSpecialbox = testUtils.emailInMailbox(fromMailbox);
		
		mailboxService.moveItem(bs, testUtils.mailboxPath(fromMailbox), testUtils.mailboxPath(toMailbox), emailInSpecialbox.getUid());

		Set<Email> fromEmails = testUtils.mailboxEmails(fromMailbox);
		Set<Email> movedEmails = testUtils.mailboxEmails(toMailbox);
		Assertions.assertThat(fromEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test(expected=MailException.class)
	public void testMoveFromNonExistingMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String fromNonExistingMailbox = "NONEXISTING_BOX";
		
		mailboxService.moveItem(bs, testUtils.mailboxPath(fromNonExistingMailbox), INBOX, sentEmail.getUid());
	}

	@Test(expected=MailException.class)
	public void testMoveToNonExistingMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String toNonExistingMailbox = "NONEXISTING_BOX";
		
		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(toNonExistingMailbox), sentEmail.getUid());
	}

	@Test
	public void testMoveToSubMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String fromSubMailbox = "ANYMAILBOX.SUBMAILBOX";
		testUtils.createFolders(fromSubMailbox);
		
		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(fromSubMailbox), sentEmail.getUid());

		Set<Email> inboxEmails = testUtils.mailboxEmails(INBOX);
		Set<Email> movedEmails = testUtils.mailboxEmails(fromSubMailbox);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveFromAndToSubMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String fromSubMailbox = "ANYMAILBOX.SUBMAILBOX";
		String toOtherSubMailbox = "ANYMAILBOX.SUBMAILBOX.SUBSUBMAILBOX";
		testUtils.createFolders(fromSubMailbox, toOtherSubMailbox);

		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(fromSubMailbox), sentEmail.getUid());
		Email emailInSubMailbox = testUtils.emailInMailbox(fromSubMailbox);
		
		mailboxService.moveItem(bs,
				testUtils.mailboxPath(fromSubMailbox), testUtils.mailboxPath(toOtherSubMailbox), emailInSubMailbox.getUid());

		Set<Email> fromEmails = testUtils.mailboxEmails(fromSubMailbox);
		Set<Email> movedEmails = testUtils.mailboxEmails(toOtherSubMailbox);
		Assertions.assertThat(fromEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test(expected=ImapMessageNotFoundException.class)
	public void testMovingNonExistingEmailTriggersException() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();
		Long nonExistingEmail = sentEmail.getUid() + 1;

		String toMoveEmailMailbox = "ANYBOX";
		testUtils.createFolders(toMoveEmailMailbox);
		
		mailboxService.moveItem(bs, INBOX, testUtils.mailboxPath(toMoveEmailMailbox), nonExistingEmail);
	}

}
