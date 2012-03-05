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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.columba.ristretto.smtp.SMTPException;
import org.minig.imap.FastFetch;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.MailboxFolder;
import org.minig.imap.MailboxFolders;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.configuration.EmailConfiguration;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathUtils;
import org.obm.push.bean.Email;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.ImapCommandException;
import org.obm.push.exception.ImapLoginException;
import org.obm.push.exception.ImapLogoutException;
import org.obm.push.exception.NoImapClientAvailableException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.EmailFactory;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailMessageLoader;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.PrivateMailboxService;
import org.obm.push.mail.smtp.SmtpSender;
import org.obm.push.service.EventService;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.mail.imap.IMAPInputStream;
import com.sun.mail.imap.IMAPMessage;

@Singleton
public class ImapMailboxService implements MailboxService, PrivateMailboxService {

	private static final Logger logger = LoggerFactory.getLogger(ImapMailboxService.class);
	
	private final SmtpSender smtpProvider;
	private final EventService eventService;
	private final boolean activateTLS;
	private final boolean loginWithDomain;
	private final ImapClientProvider imapClientProvider;
	private final Ical4jHelper ical4jHelper;
	private final Ical4jUser.Factory ical4jUserFactory;
	private final ImapMailBoxUtils imapMailBoxUtils;
	
	@Inject
	/* package */ ImapMailboxService(EmailConfiguration emailConfiguration, 
			SmtpSender smtpSender, EventService eventService, ImapClientProvider imapClientProvider, 
			Ical4jHelper ical4jHelper, Ical4jUser.Factory ical4jUserFactory, ImapMailBoxUtils imapMailBoxUtils) {
		
		this.smtpProvider = smtpSender;
		this.eventService = eventService;
		this.imapClientProvider = imapClientProvider;
		this.ical4jHelper = ical4jHelper;
		this.ical4jUserFactory = ical4jUserFactory;
		this.imapMailBoxUtils = imapMailBoxUtils;
		this.activateTLS = emailConfiguration.activateTls();
		this.loginWithDomain = emailConfiguration.loginWithDomain();
	}

	@Override
	public List<MSEmail> fetchMails(BackendSession bs, Integer collectionId, 
			String collectionName, Collection<Long> uids) throws MailException {
		
		final List<MSEmail> mails = new LinkedList<MSEmail>();
		final StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(bs, collectionName));
			
			final MailMessageLoader mailLoader = 
					new MailMessageLoader(store, eventService, ical4jHelper, ical4jUserFactory);
			for (final Long uid: uids) {
				final MSEmail email = mailLoader.fetch(collectionId, uid, bs);
				if (email != null) {
					mails.add(email);
				}
			}
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
		return mails;
	}

	@Override
	public MailboxFolders listAllFolders(BackendSession bs) throws MailException {
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			Folder[] folders = store.getDefaultFolder().list("*");
			
			List<MailboxFolder> mailboxFolders = Lists.newArrayList();
			for (Folder folder: folders) {
				mailboxFolders.add(
						new MailboxFolder(folder.getFullName(), folder.getSeparator()));
			}
			return new MailboxFolders(mailboxFolders);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	@Override
	public OpushImapFolder createFolder(BackendSession bs, MailboxFolder folder) throws MailException {
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			return store.create(folder, Folder.HOLDS_MESSAGES|Folder.HOLDS_FOLDERS);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}

	private void closeQuietly(ImapStore store) throws MailException {
		try {
			if (store != null) {
				store.logout();
			}
		} catch (ImapLogoutException e) {
			throw new MailException(e);
		}
	}

	@Override
	public void updateReadFlag(BackendSession bs, String collectionName, long uid, boolean read) 
			throws MailException, ImapMessageNotFoundException {
		
		updateMailFlag(bs, collectionName, uid, Flags.Flag.SEEN, read);
	}

	/* package */ void updateMailFlag(BackendSession bs, String collectionName, long uid, Flags.Flag flag, 
			boolean status) throws MailException, ImapMessageNotFoundException {
		
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			IMAPMessage message = getMessage(store, bs, collectionName, uid);
			message.setFlag(flag, status);
			logger.info("Change flag for mail with UID {} in {} ( {}:{} )",
					new Object[] { uid, collectionName, imapMailBoxUtils.flagToString(flag), status });
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	private IMAPMessage getMessage(ImapStore store, BackendSession bs, String collectionName, long uid) 
			throws MailException, ImapMessageNotFoundException {
		
		String mailBoxName = parseMailBoxName(bs, collectionName);
		try {
			OpushImapFolder folder = store.select(mailBoxName);
			IMAPMessage message = folder.getMessageByUID(Ints.checkedCast(uid));
			if (message != null) {
				return message;
			} else {
				throw new ImapMessageNotFoundException("Mail with UID {" + uid + "} not found in {" + folder.getFullName() + "}");
			}
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		}
	}
	
	/* package */ IMAPMessage getMessage(BackendSession bs, String collectionName, long uid) 
			throws MailException, ImapMessageNotFoundException {
		
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			return getMessage(store, bs, collectionName, uid);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	/* package */ void expunge(BackendSession bs, String collectionName) throws MailException {
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			String mailBoxName = parseMailBoxName(bs, collectionName);
			OpushImapFolder folder = store.select(mailBoxName);
			folder.expunge();
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	@Override
	public String parseMailBoxName(BackendSession bs, String collectionName) throws MailException {
		// parse obm:\\adrien@test.tlse.lng\email\INBOX\Sent
		if (collectionName.toLowerCase().endsWith(EmailConfiguration.IMAP_INBOX_NAME.toLowerCase())) {
			return EmailConfiguration.IMAP_INBOX_NAME;
		}
		
		int slash = collectionName.lastIndexOf("email\\");
		final String boxName = collectionName.substring(slash + "email\\".length());
		final MailboxFolders lr = listAllFolders(bs);
		for (final MailboxFolder i: lr) {
			if (i.getName().toLowerCase().equals(boxName.toLowerCase())) {
				return i.getName();
			}
		}
		throw new MailException("Cannot find IMAP folder for collection [ " + collectionName + " ]");
	}

	 
	@Override
	public void delete(BackendSession bs, String collectionPath, long uid) 
			throws DaoException, MailException {
		
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs, collectionPath);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id = ", uid);
			store.uidStore(Arrays.asList(uid), fl, true);
			store.expunge();
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public long moveItem(BackendSession bs, String srcFolder, String dstFolder, long uid)
			throws DaoException, MailException, ImapMessageNotFoundException, UnsupportedBackendFunctionException {
		
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			
			assertMoveItemIsSupported(store);
			
			logger.debug("Moving email, USER:{} UID:{} SRC:{} DST:{}",
					new Object[] {bs.getUser().getLoginAtDomain(), uid, srcFolder, dstFolder});
			
			String srcMailBox = parseMailBoxName(bs, srcFolder);
			String dstMailBox = parseMailBoxName(bs, dstFolder);

			return store.moveMessageUID(srcMailBox, dstMailBox, uid);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	private void assertMoveItemIsSupported(ImapStore store) throws UnsupportedBackendFunctionException, MessagingException {
		if (!store.hasCapability(ImapCapability.UIDPLUS)) {
			throw new UnsupportedBackendFunctionException("The IMAP server doesn't support UIDPLUS capability");
		}
	}

	@Override
	public InputStream fetchMailStream(BackendSession bs, String collectionName, long uid) throws MailException {
		return getMessageInputStream(bs, collectionName, uid);
	}

	private InputStream getMessageInputStream(BackendSession bs, String collectionName, long messageUID) 
			throws MailException {
		
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			IMAPMessage imapMessage = getMessage(store, bs, collectionName, messageUID);
			IMAPInputStream imapInputStream = new IMAPInputStream(imapMessage, null, -1, true);
			return imapInputStream;
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} 
	}

	private void login(StoreClient store) throws IMAPException {
		store.login(activateTLS);
	}

	@Override
	public void setAnsweredFlag(BackendSession bs, String collectionName, long uid) throws MailException, ImapMessageNotFoundException {
		updateMailFlag(bs, collectionName, uid, Flags.Flag.ANSWERED, true);
	}

	@Override
	public void sendEmail(BackendSession bs, Address from, Set<Address> setTo, Set<Address> setCc, Set<Address> setCci, InputStream mimeMail,
			Boolean saveInSent) throws ProcessingEmailException, SendEmailException, SmtpInvalidRcptException, StoreEmailException {
		
		InputStream streamMail = null;
		try {
			streamMail = new ByteArrayInputStream(FileUtils.streamBytes(mimeMail, true));
			streamMail.mark(streamMail.available());
			
			smtpProvider.sendEmail(bs, from, setTo, setCc, setCci, streamMail);
			
			if (saveInSent) {
				streamMail.reset();
				boolean isMailStoredInSent = storeInSent(bs, streamMail);
				if (isMailStoredInSent) {
					logger.info("The mail is stored in the 'sent' folder.");
				} else {
					logger.error("The mail can't be stored in the 'sent' folder.");
				}
			} else {
				logger.info("The email mustn't be stored in Sent folder.{saveInSent=false}");
			}
			
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} catch (SMTPException e) {
			throw new ProcessingEmailException(e);
		} catch (MailException e) {
			throw new ProcessingEmailException(e);
		} finally {
			closeStream(streamMail);
		}
		
	}	
	
	private void closeStream(InputStream mimeMail) {
		if (mimeMail != null) {
			try {
				mimeMail.close();
			} catch (IOException t) {
				logger.error(t.getMessage(), t);
			}
		}
	}
	
	@Override
	public InputStream findAttachment(BackendSession bs, String collectionName, Long mailUid, String mimePartAddress) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs, collectionName);
			store.select(mailBoxName);
			return store.uidFetchPart(mailUid, mimePartAddress);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public Collection<Long> purgeFolder(BackendSession bs, Integer devId, String collectionPath, Integer collectionId) 
			throws DaoException, MailException {
		
		long time = System.currentTimeMillis();
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(bs, collectionPath);
			store.select(mailBoxName);
			logger.info("Mailbox folder[ {} ] will be purged...", collectionPath);
			Collection<Long> uids = store.uidSearch(new SearchQuery());
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			store.uidStore(uids, fl, true);
			store.expunge();
			time = System.currentTimeMillis() - time;
			logger.info("Mailbox folder[ {} ] was purged in {} millisec. {} messages have been deleted",
					new Object[]{collectionPath, time, uids.size()});
			return uids;
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public void storeInInbox(BackendSession bs, InputStream mailContent, int mailSize, boolean isRead) throws MailException {
		logger.info("Store mail in folder[Inbox]");
		try {
			ImapStore store = imapClientProvider.getImapClientWithJM(bs);
			try {
				store.login();
				Flags flags = new Flags();
				if (isRead) {
					flags.add(Flags.Flag.SEEN);
				}
				StreamedLiteral streamedLiteral = new StreamedLiteral(mailContent, mailSize);
				store.appendMessageStream(EmailConfiguration.IMAP_INBOX_NAME, streamedLiteral, flags);
			} catch (ImapCommandException e) {
				throw new MailException(e.getMessage(), e);
			} catch (LocatorClientException e) {
				throw new MailException(e.getMessage(), e);
			} finally {
				logout(store);
			}
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e.getMessage(), e);
		}
	}
		
	@Override
	public void storeInInbox(BackendSession bs, InputStream mailContent, boolean isRead) throws MailException {
		logger.info("Store mail in folder[Inbox]");
		try {
			ImapStore store = imapClientProvider.getImapClientWithJM(bs);
			try {
				store.login();
				Message message = store.createMessage(mailContent);
				message.setFlag(Flags.Flag.SEEN, isRead);
				store.appendMessage(EmailConfiguration.IMAP_INBOX_NAME, message);
			} catch (ImapCommandException e) {
				throw new MailException(e.getMessage(), e);
			} catch (LocatorClientException e) {
				throw new MailException(e.getMessage(), e);
			} catch (MessagingException e) {
				throw new MailException(e.getMessage(), e);
			} finally {
				logout(store);
			}
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e.getMessage(), e);
		}

	}

	private void logout(ImapStore store) {
		try {
			if (store != null) {
				store.logout();
			}
		} catch (ImapLogoutException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * Store the mail in the Sent folder storeInSent reset the mimeMail will be
	 * if storeInSent read it
	 * 
	 * @param bs the BackendSession
	 * @param mail the mail that will be stored
	 * @return the imap uid of the mail
	 * @throws StoreEmailException
	 * @throws MailException 
	 */
	private boolean storeInSent(BackendSession bs, InputStream mail) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String sentBoxPath = CollectionPathUtils.buildCollectionPath(
					bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
			String sentFolderName = parseMailBoxName(bs, sentBoxPath);
			return storeMail(store, sentFolderName,true, mail, true);
		} catch (IMAPException e) {
			throw new MailException("Error during store mail in Sent folder", e);
		} finally {
			store.logout();
		}
	}

	/**
	 * 
	 * @param store
	 *            the StoreClient
	 * @param folderName
	 *            the folder name where the mail will be stored
	 * @param isRead
	 *            if true the message will be stored with SEEN Flag
	 * @param reset
	 *            if true mailContent will be reseted
	 * @return storeMail response status
	 */
	private boolean storeMail(StoreClient store, String folderName,
			boolean isRead, InputStream mailContent, boolean reset) {
		boolean ret = false;
		if (folderName != null) {
			if (reset && mailContent.markSupported()) {
				mailContent.mark(0);
			}
			FlagsList fl = new FlagsList();
			if(isRead){
				fl.add(Flag.SEEN);
			}
			ret = store.append(folderName, mailContent, fl);
			store.expunge();
		}
		return ret;
	}

	@Override
	public boolean getLoginWithDomain() {
		return loginWithDomain;
	}

	@Override
	public boolean getActivateTLS() {
		return activateTLS;
	}

	@Override
	public Collection<Email> fetchEmails(BackendSession bs, Collection<Long> uids) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			Collection<FastFetch> fetch = store.uidFetchFast(uids);
			Collection<Email> emails = Collections2.transform(fetch, new Function<FastFetch, Email>() {
						@Override
						public Email apply(FastFetch input) {
							return new Email(input.getUid(), input.isRead(), input.getInternalDate());
						}
					});
			return emails;	
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
		
	}
	
	@Override
	public Set<Email> fetchEmails(BackendSession bs, String collectionName, Date windows) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select( parseMailBoxName(bs, collectionName) );
			Collection<Long> uids = store.uidSearch(new SearchQuery(null, windows));
			Collection<FastFetch> mails = store.uidFetchFast(uids);
			return EmailFactory.listEmailFromFastFetch(mails);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

}
