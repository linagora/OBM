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

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import org.minig.imap.MailboxFolder;
import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Email;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;

import com.google.common.collect.Iterables;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMailUtil;

public class ImapTestUtils {

	private final MailboxService mailboxService;
	private final BackendSession bs;
	private final String mailbox;
	private final Date beforeTest;
	private final PrivateMailboxService privateMailboxService;
	private final CollectionPathHelper collectionPathHelper;

	public ImapTestUtils(MailboxService mailboxService, PrivateMailboxService privateMailboxService,
			BackendSession bs, String mailbox, Date beforeTest, CollectionPathHelper collectionPathHelper) {
		
		this.mailboxService = mailboxService;
		this.privateMailboxService = privateMailboxService;
		this.bs = bs;
		this.mailbox = mailbox;
		this.beforeTest = beforeTest;
		this.collectionPathHelper = collectionPathHelper;
	}
	
	public Email sendEmailToInbox() throws MailException {
		GreenMailUtil.sendTextEmailTest(mailbox, "from@localhost.com", "subject", "body");
		return emailInInbox();
	}

	public Email sendEmailToInbox(InputStream email) throws MailException {
		mailboxService.storeInInbox(bs, email, false);
		return emailInInbox();
	}
	
	public void deliverToUserInbox(GreenMailUser user, MimeMessage message, Date internalDate) throws UserException {
		user.deliver(message, internalDate);
	}

	public Email sendEmailToMailbox(String mailbox)
			throws DaoException, MailException, ImapMessageNotFoundException, UnsupportedBackendFunctionException {
		
		Email sentEmail = sendEmailToInbox();
		String inboxPath = mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		mailboxService.moveItem(bs, inboxPath, mailboxPath(mailbox), sentEmail.getUid());
		return emailInMailbox(mailbox);
	}

	public Email emailInInbox() throws MailException {
		return emailInMailbox(EmailConfiguration.IMAP_INBOX_NAME);
	}
	
	public Email emailInMailbox(String mailboxName) throws MailException {
		Set<Email> emailsFromInbox = mailboxEmails(mailboxName);
		return Iterables.getLast(emailsFromInbox);
	}
	
	public Set<Email> mailboxEmails(String mailboxName) throws MailException {
		return mailboxService.fetchEmails(bs, mailboxPath(mailboxName), beforeTest);
	}

	public void createFolders(String...folderNames) throws MailException {
		for (String folderName : folderNames) {
			privateMailboxService.createFolder(bs, folder(folderName));
		}
	}

	public String mailboxPath(String mailboxName) {
		return collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, mailboxName);
	}
	
	public MailboxFolder folder(String name) {
		return new MailboxFolder(name);
	}
}
