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

import java.io.InputStream;
import java.util.Date;
import java.util.Set;

import javax.mail.internet.MimeMessage;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.exception.MailException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.EmailReader;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MessageSet;

import com.google.common.collect.Iterables;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.user.UserException;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

public class MailboxTestUtils {

	private final MailboxService mailboxService;
	private final UserDataRequest udr;
	private final String mailbox;
	private final Date beforeTest;
	private final ICollectionPathHelper collectionPathHelper;
	private final ServerSetup smtpServerSetup;

	public MailboxTestUtils(MailboxService mailboxService,
			UserDataRequest udr, String mailbox, Date beforeTest,
			ICollectionPathHelper collectionPathHelper, ServerSetup smtpServerSetup) {
		
		this.mailboxService = mailboxService;
		this.udr = udr;
		this.mailbox = mailbox;
		this.beforeTest = beforeTest;
		this.collectionPathHelper = collectionPathHelper;
		this.smtpServerSetup = smtpServerSetup;
	}
	
	public Email sendEmailToInbox() throws MailException {
		GreenMailUtil.sendTextEmail(mailbox, "from@localhost.com", "subject", "body", smtpServerSetup);
		return emailInInbox();
	}

	public Email sendEmailToInbox(InputStream email) throws MailException {
		storeInInbox(udr, mailboxService, email);
		return emailInInbox();
	}

	public static void storeInInbox(UserDataRequest udr, MailboxService mailboxService, InputStream email, boolean read) {
		mailboxService.storeInInbox(udr, emailReader(email), read);
	}

	public static void storeInInbox(UserDataRequest udr, MailboxService mailboxService, InputStream email) {
		storeInInbox(udr, mailboxService, email, false);
	}

	public static void storeInSent(UserDataRequest udr, MailboxService mailboxService, InputStream email) {
		mailboxService.storeInSent(udr, emailReader(email));
	}

	public static EmailReader emailReader(InputStream email) {
		return new EmailReader(email);
	}
	
	public void deliverToUserInbox(GreenMailUser user, MimeMessage message, Date internalDate) throws UserException {
		user.deliver(message, internalDate);
	}
	
	public Email sendEmailToMailbox(String mailbox)
			throws DaoException, MailException, ImapMessageNotFoundException, UnsupportedBackendFunctionException {
		
		Email sentEmail = sendEmailToInbox();
		String inboxPath = mailboxPath(EmailConfiguration.IMAP_INBOX_NAME);
		mailboxService.move(udr, inboxPath, mailboxPath(mailbox), MessageSet.singleton(sentEmail.getUid()));
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
		return mailboxService.fetchEmails(udr, mailboxPath(mailboxName), beforeTest);
	}

	public void createFolders(String...folderNames) throws MailException {
		for (String folderName : folderNames) {
			mailboxService.createFolder(udr, folder(folderName));
		}
	}

	public String mailboxPath(String mailboxName) {
		return collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, mailboxName);
	}
	
	public MailboxFolder folder(String name) {
		return new MailboxFolder(name);
	}

	public MailboxFolder inbox() {
		return folder(EmailConfiguration.IMAP_INBOX_NAME);
	}

	public InputStream getInputStreamFromFile(String name) {
		return ClassLoader.getSystemResourceAsStream("eml/" + name);
	}
}
