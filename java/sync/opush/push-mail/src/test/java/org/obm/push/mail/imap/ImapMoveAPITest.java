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
import org.minig.imap.MailboxFolder;
import org.obm.configuration.EmailConfiguration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathUtils;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Email;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.User;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailEnvModule;
import org.obm.push.mail.MailException;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;

public class ImapMoveAPITest {

	private static final String INBOX = EmailConfiguration.IMAP_INBOX_NAME;
	private static final String SENTBOX = EmailConfiguration.IMAP_SENT_NAME;
	private static final String DRAFT = EmailConfiguration.IMAP_DRAFTS_NAME;
	private static final String TRASH = EmailConfiguration.IMAP_TRASH_NAME;
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(MailEnvModule.class);

	@Inject ImapMailboxService mailboxService;

	@Inject ImapMailBoxUtils mailboxUtils;
	@Inject GreenMail greenMail;
	private String mailbox;
	private String password;
	private BackendSession bs;

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
						.createUser(mailbox, mailbox, null), password, null), null, null, null);
	}
	
	@After
	public void tearDown() {
		greenMail.stop();
	}
	
	@Test
	public void testGreenmailServerImplementUIDPLUS() throws Exception {
		Email sentEmail = sendEmailToInbox();

		createFolders(DRAFT);

		long testErrorUidValue = -1;
		long movedEmailUid = testErrorUidValue;
		try {
			movedEmailUid = mailboxService.moveItem(bs, INBOX, mailboxPath(DRAFT), sentEmail.getUid());
		} catch (Exception nonExpectedException) {
			Assert.fail("Greenmail should implement UIDPLUS, so no exception is expected");
		}

		Set<Email> movedEmails = mailboxEmails(DRAFT);
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.getUid()).isNotEqualTo(testErrorUidValue).isEqualTo(movedEmailUid);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}
	
	@Test
	public void testMoveFromInbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		String toMailbox = "ANYBOX";
		createFolders(toMailbox);
		
		mailboxService.moveItem(bs, INBOX, mailboxPath(toMailbox), sentEmail.getUid());

		Set<Email> inboxEmails = mailboxEmails(INBOX);
		Set<Email> movedEmails = mailboxEmails(toMailbox);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToSentbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		createFolders(SENTBOX);
		
		mailboxService.moveItem(bs, INBOX, mailboxPath(SENTBOX), sentEmail.getUid());

		Set<Email> inboxEmails = mailboxEmails(INBOX);
		Set<Email> movedEmails = mailboxEmails(SENTBOX);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToDraft() throws Exception {
		Email sentEmail = sendEmailToInbox();

		createFolders(DRAFT);
		
		mailboxService.moveItem(bs, INBOX, mailboxPath(DRAFT), sentEmail.getUid());

		Set<Email> inboxEmails = mailboxEmails(INBOX);
		Set<Email> movedEmails = mailboxEmails(DRAFT);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToTrash() throws Exception {
		Email sentEmail = sendEmailToInbox();

		createFolders(TRASH);
		
		mailboxService.moveItem(bs, INBOX, mailboxPath(TRASH), sentEmail.getUid());

		Set<Email> inboxEmails = mailboxEmails(INBOX);
		Set<Email> movedEmails = mailboxEmails(TRASH);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveToInbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		mailboxService.moveItem(bs, INBOX, INBOX, sentEmail.getUid());

		Set<Email> inboxEmails = mailboxEmails(INBOX);
		Assertions.assertThat(inboxEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(inboxEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveFromSpecialMailbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		String fromMailbox = "SPECIALBOX";
		String toMailbox = "ANYBOX";
		createFolders(fromMailbox, toMailbox);

		mailboxService.moveItem(bs, INBOX, mailboxPath(fromMailbox), sentEmail.getUid());
		Email emailInSpecialbox = emailInMailbox(fromMailbox);
		
		mailboxService.moveItem(bs, mailboxPath(fromMailbox), mailboxPath(toMailbox), emailInSpecialbox.getUid());

		Set<Email> fromEmails = mailboxEmails(fromMailbox);
		Set<Email> movedEmails = mailboxEmails(toMailbox);
		Assertions.assertThat(fromEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test(expected=MailException.class)
	public void testMoveFromNonExistingMailbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		String fromNonExistingMailbox = "NONEXISTING_BOX";
		
		mailboxService.moveItem(bs, mailboxPath(fromNonExistingMailbox), INBOX, sentEmail.getUid());
	}

	@Test(expected=MailException.class)
	public void testMoveToNonExistingMailbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		String toNonExistingMailbox = "NONEXISTING_BOX";
		
		mailboxService.moveItem(bs, INBOX, mailboxPath(toNonExistingMailbox), sentEmail.getUid());
	}

	@Test
	public void testMoveToSubMailbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		String fromSubMailbox = "ANYMAILBOX.SUBMAILBOX";
		createFolders(fromSubMailbox);
		
		mailboxService.moveItem(bs, INBOX, mailboxPath(fromSubMailbox), sentEmail.getUid());

		Set<Email> inboxEmails = mailboxEmails(INBOX);
		Set<Email> movedEmails = mailboxEmails(fromSubMailbox);
		Assertions.assertThat(inboxEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test
	public void testMoveFromAndToSubMailbox() throws Exception {
		Email sentEmail = sendEmailToInbox();

		String fromSubMailbox = "ANYMAILBOX.SUBMAILBOX";
		String toOtherSubMailbox = "ANYMAILBOX.SUBMAILBOX.SUBSUBMAILBOX";
		createFolders(fromSubMailbox, toOtherSubMailbox);

		mailboxService.moveItem(bs, INBOX, mailboxPath(fromSubMailbox), sentEmail.getUid());
		Email emailInSubMailbox = emailInMailbox(fromSubMailbox);
		
		mailboxService.moveItem(bs, mailboxPath(fromSubMailbox), mailboxPath(toOtherSubMailbox), emailInSubMailbox.getUid());

		Set<Email> fromEmails = mailboxEmails(fromSubMailbox);
		Set<Email> movedEmails = mailboxEmails(toOtherSubMailbox);
		Assertions.assertThat(fromEmails).isEmpty();
		Assertions.assertThat(movedEmails).hasSize(1);
		Email movedEmail = Iterables.getOnlyElement(movedEmails);
		Assertions.assertThat(movedEmail.isAnswered()).isEqualTo(sentEmail.isAnswered());
		Assertions.assertThat(movedEmail.isRead()).isEqualTo(sentEmail.isRead());
	}

	@Test(expected=ImapMessageNotFoundException.class)
	public void testMovingNonExistingEmailTriggersException() throws Exception {
		Email sentEmail = sendEmailToInbox();
		Long nonExistingEmail = sentEmail.getUid() + 1;

		String toMoveEmailMailbox = "ANYBOX";
		createFolders(toMoveEmailMailbox);
		
		mailboxService.moveItem(bs, INBOX, mailboxPath(toMoveEmailMailbox), nonExistingEmail);
	}
	
	private Email sendEmailToInbox() throws MailException {
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		return emailInInbox();
	}

	private Email emailInInbox() throws MailException {
		return emailInMailbox(INBOX);
	}
	
	private Email emailInMailbox(String mailboxName) throws MailException {
		Set<Email> emailsFromInbox = mailboxEmails(mailboxName);
		return Iterables.getOnlyElement(emailsFromInbox);
	}
	
	private Set<Email> mailboxEmails(String mailboxName) throws MailException {
		return mailboxService.fetchEmails(bs, mailboxPath(mailboxName), beforeTest);
	}

	private void createFolders(String...folderNames) throws MailException {
		for (String folderName : folderNames) {
			mailboxService.createFolder(bs, folder(folderName));
		}
	}

	private String mailboxPath(String mailboxName) {
		return CollectionPathUtils.buildCollectionPath(bs, PIMDataType.EMAIL, mailboxName);
	}
	
	private MailboxFolder folder(String name) {
		return new MailboxFolder(name);
	}

}
