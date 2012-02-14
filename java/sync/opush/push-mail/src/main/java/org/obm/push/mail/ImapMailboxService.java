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
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
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
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

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
		IMAPStore store = imapClientProvider.getJavaxMailImapClient(bs);
		try {
			Folder[] folders = store.getDefaultFolder().list("*");
			
			List<MailboxFolder> mailboxFolders = Lists.newArrayList();
			for (Folder folder: folders) {
				mailboxFolders.add(
						new MailboxFolder(folder.getFullName(), folder.getSeparator()));
			}
			return new MailboxFolders(mailboxFolders);
		} catch (MessagingException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	@Override
	public boolean createFolder(BackendSession bs, MailboxFolder folder) throws MailException {
		IMAPStore store = imapClientProvider.getJavaxMailImapClient(bs);
		try {
			Folder newFolder = store.getFolder(folder.getName());
			return newFolder.create(Folder.HOLDS_MESSAGES|Folder.HOLDS_FOLDERS);
		} catch (MessagingException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}

	private void closeQuietly(IMAPStore store) throws MailException {
		try {
			store.close();
		} catch (MessagingException e) {
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
		
		IMAPStore store = imapClientProvider.getJavaxMailImapClient(bs);
		try {
			Message message = getMessage(store, bs, collectionName, uid);
			message.setFlag(flag, status);
			logger.info("Change flag for mail with UID {} in {} ( {}:{} )",
					new Object[] { uid, collectionName, imapMailBoxUtils.flagToString(flag), status });
		} catch (MessagingException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	private Message getMessage(IMAPStore store, BackendSession bs, String collectionName, long uid) 
			throws MailException, ImapMessageNotFoundException {
		
		String mailBoxName = parseMailBoxName(bs, collectionName);
		try {
			IMAPFolder imapFolder = (IMAPFolder) store.getFolder(mailBoxName);
			imapFolder.open(Folder.READ_WRITE);
			Message message = imapFolder.getMessageByUID(Ints.checkedCast(uid));
			if (message != null) {
				return message;
			} else {
				throw new ImapMessageNotFoundException("Mail with UID {" + uid + "} not found in {" + imapFolder.getFullName() + "}");
			}
		} catch (MessagingException e) {
			throw new MailException(e);
		}
	}
	
	/* package */ Message getMessage(BackendSession bs, String collectionName, long uid) throws MailException, ImapMessageNotFoundException {
		IMAPStore store = imapClientProvider.getJavaxMailImapClient(bs);
		try {
			return getMessage(store, bs, collectionName, uid);
		} finally {
			closeQuietly(store);
		}
	}
	
	/* package */ void expunge(BackendSession bs, String collectionName) throws MailException {
		IMAPStore store = imapClientProvider.getJavaxMailImapClient(bs);
		String mailBoxName = parseMailBoxName(bs, collectionName);
		try {
			Folder folder = store.getFolder(mailBoxName);
			folder.open(Folder.READ_WRITE);
			folder.expunge();
		} catch (MessagingException e) {
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
	public void delete(BackendSession bs, Integer devId, String collectionPath, Integer collectionId, Long uid) 
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
	public Long moveItem(BackendSession bs, Integer devId, String srcFolder, Integer srcFolderId, String dstFolder, Integer dstFolderId, 
			Long uid) throws DaoException, MailException {
		
		StoreClient store = imapClientProvider.getImapClient(bs);
		Collection<Long> newUid = null;
		try {
			login(store);
			String srcMailBox = parseMailBoxName(bs, srcFolder);
			String dstMailBox = parseMailBoxName(bs, dstFolder);

			store.select(srcMailBox);
			List<Long> uids = Arrays.asList(uid);
			newUid = store.uidCopy(uids, dstMailBox);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id = ", uid);
			store.uidStore(uids, fl, true);
			store.expunge();
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
		if (newUid == null || newUid.isEmpty()) {
			return null;
		}
		return newUid.iterator().next();
	}

	@Override
	public List<InputStream> fetchMIMEMails(BackendSession bs, String collectionName, 
			Set<Long> uids) throws MailException {
		
		List<InputStream> mails = new LinkedList<InputStream>();
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(bs, collectionName));
			for (Long uid : uids) {
				mails.add(store.uidFetchMessage(uid));
			}
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
		return mails;
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
				final Long uid = storeInSent(bs, streamMail);
				if (uid != null) {
					logger.info("This mail {} is stored in 'sent' folder.", uid);
				} else {
					logger.error("The mail can't to be store in 'sent' folder.");
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
	public Long storeInInbox(BackendSession bs, InputStream mailContent, boolean isRead) 
			throws StoreEmailException {
		
		logger.info("Store mail in folder[Inbox]");
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			return storeMail(store, EmailConfiguration.IMAP_INBOX_NAME, isRead, mailContent, false);
		} catch (IMAPException e) {
			throw new StoreEmailException("Error during store mail in Inbox folder", e);
		} finally {
			store.logout();
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
	 */
	private Long storeInSent(BackendSession bs, InputStream mail) throws MailException {
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
	 * @return the imap uid of the mail
	 */
	private Long storeMail(StoreClient store, String folderName,
			boolean isRead, InputStream mailContent, boolean reset) {
		Long ret = null;
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
