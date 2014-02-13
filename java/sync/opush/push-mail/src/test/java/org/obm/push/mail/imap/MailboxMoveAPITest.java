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

import java.util.Date;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.MessageSet;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

@RunWith(GuiceRunner.class)
@GuiceModule(MailEnvModule.class)
public class MailboxMoveAPITest {

	private static final String INBOX = EmailConfiguration.IMAP_INBOX_NAME;
	private static final String SENTBOX = EmailConfiguration.IMAP_SENT_NAME;
	private static final String DRAFT = EmailConfiguration.IMAP_DRAFTS_NAME;
	private static final String TRASH = EmailConfiguration.IMAP_TRASH_NAME;
	
	@Inject MailboxService mailboxService;
	@Inject ICollectionPathHelper collectionPathHelper;

	@Inject GreenMail greenMail;
	private ServerSetup smtpServerSetup;
	private String mailbox;
	private String password;
	private UserDataRequest udr;

	private Date beforeTest;
	private MailboxTestUtils testUtils;

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
	public void testGreenmailServerImplementUIDPLUS() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		testUtils.createFolders(DRAFT);

		long testErrorUidValue = -1;
		MessageSet movedEmailUid = null;
		try {
			movedEmailUid = mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(DRAFT), MessageSet.singleton(sentEmail.getUid()));
		} catch (Exception nonExpectedException) {
			Assert.fail("Greenmail should implement UIDPLUS, so no exception is expected");
		}

		Set<Email> movedEmails = testUtils.mailboxEmails(DRAFT);
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.getUid()).isNotEqualTo(testErrorUidValue).isEqualTo(Iterables.getOnlyElement(movedEmailUid));
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}
	
	@Test
	public void testMoveFromInbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String toMailbox = "ANYBOX";
		testUtils.createFolders(toMailbox);
		
		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(toMailbox), MessageSet.singleton(sentEmail.getUid()));

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
		
		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(SENTBOX), MessageSet.singleton(sentEmail.getUid()));

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
		
		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(DRAFT), MessageSet.singleton(sentEmail.getUid()));

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
		
		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(TRASH), MessageSet.singleton(sentEmail.getUid()));

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

		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(INBOX), MessageSet.singleton(sentEmail.getUid()));

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

		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(fromMailbox), MessageSet.singleton(sentEmail.getUid()));
		Email emailInSpecialbox = testUtils.emailInMailbox(fromMailbox);
		
		mailboxService.move(udr, testUtils.mailboxPath(fromMailbox), testUtils.mailboxPath(toMailbox), MessageSet.singleton(emailInSpecialbox.getUid()));

		Set<Email> fromEmails = testUtils.mailboxEmails(fromMailbox);
		Set<Email> movedEmails = testUtils.mailboxEmails(toMailbox);
		Assertions.assertThat(fromEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test(expected=CollectionNotFoundException.class)
	public void testMoveFromNonExistingMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String fromNonExistingMailbox = "NONEXISTING_BOX";
		
		mailboxService.move(udr, testUtils.mailboxPath(fromNonExistingMailbox), testUtils.mailboxPath(INBOX), MessageSet.singleton(sentEmail.getUid()));
	}

	@Test(expected=CollectionNotFoundException.class)
	public void testMoveToNonExistingMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String toNonExistingMailbox = "NONEXISTING_BOX";
		
		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(toNonExistingMailbox), MessageSet.singleton(sentEmail.getUid()));
	}

	@Test
	public void testMoveToSubMailbox() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();

		String fromSubMailbox = "ANYMAILBOX.SUBMAILBOX";
		testUtils.createFolders(fromSubMailbox);
		
		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(fromSubMailbox), MessageSet.singleton(sentEmail.getUid()));

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

		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(fromSubMailbox), MessageSet.singleton(sentEmail.getUid()));
		Email emailInSubMailbox = testUtils.emailInMailbox(fromSubMailbox);
		
		mailboxService.move(udr,
				testUtils.mailboxPath(fromSubMailbox), testUtils.mailboxPath(toOtherSubMailbox), MessageSet.singleton(emailInSubMailbox.getUid()));

		Set<Email> fromEmails = testUtils.mailboxEmails(fromSubMailbox);
		Set<Email> movedEmails = testUtils.mailboxEmails(toOtherSubMailbox);
		Assertions.assertThat(fromEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}
	
	@Ignore("Greenmail replied that the command succeed")
	@Test(expected=ImapMessageNotFoundException.class)
	public void testMovingNonExistingEmailTriggersException() throws Exception {
		Email sentEmail = testUtils.sendEmailToInbox();
		Long nonExistingEmail = sentEmail.getUid() + 1;

		String toMoveEmailMailbox = "ANYBOX";
		testUtils.createFolders(toMoveEmailMailbox);
		
		mailboxService.move(udr, testUtils.mailboxPath(INBOX), testUtils.mailboxPath(toMoveEmailMailbox), MessageSet.singleton(nonExistingEmail));
	}

}
