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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.minig.imap.FastFetch;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Email;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.smtp.SmtpSender;
import org.obm.push.service.EventService;
import org.obm.push.store.EmailDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.FileUtils;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.ICalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ImapMailboxService implements MailboxService {

	private static final Logger logger = LoggerFactory.getLogger(ImapMailboxService.class);
	
	private final EmailDao emailDao;
	private final SmtpSender smtpProvider;
	private final EmailSync emailSync;
	private final EventService eventService;
	private final LoginService login;
	private final boolean activateTLS;
	private final boolean loginWithDomain;

	private final ImapClientProvider imapClientProvider;
	
	@Inject
	/* package */ ImapMailboxService(EmailDao emailDao, EmailConfiguration emailConfiguration, 
			SmtpSender smtpSender, EmailSync emailSync,
			EventService eventService, LoginService login,
			ImapClientProvider imapClientProvider) {
		
		this.emailSync = emailSync;
		this.smtpProvider = smtpSender;
		this.emailDao = emailDao;
		this.eventService = eventService;
		this.login = login;
		this.imapClientProvider = imapClientProvider;
		this.activateTLS = emailConfiguration.activateTls();
		this.loginWithDomain = emailConfiguration.loginWithDomain();
	}

	@Override
	public MailChanges getSync(BackendSession bs, SyncState syncState, Integer deviceId, Integer collectionId, String collectionName) 
			throws DaoException, MailException {
		
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select( parseMailBoxName(store, collectionName) );
			return emailSync.getSync(store, deviceId, syncState, collectionId);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public List<MSEmail> fetchMails(BackendSession bs, ICalendar calendarClient, Integer collectionId, 
			String collectionName, Collection<Long> uids) throws MailException {
		
		final List<MSEmail> mails = new LinkedList<MSEmail>();
		final StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(store, collectionName));
			
			final MailMessageLoader mailLoader = 
					new MailMessageLoader(store, calendarClient, eventService, login);
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

	private ListResult listAllFolder(StoreClient store) {
		return store.listAll();
	}

	@Override
	public void updateReadFlag(BackendSession bs, String collectionName, Long uid, boolean read) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.SEEN);
			store.uidStore(Arrays.asList(uid), fl, read);
			logger.info("flag  change: " + (read ? "+" : "-") + " SEEN"
					+ " on mail " + uid + " in " + mailBoxName);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public String parseMailBoxName(BackendSession bs, String collectionName) throws MailException {
		// parse obm:\\adrien@test.tlse.lng\email\INBOX\Sent
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			return parseMailBoxName(store, collectionName);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	private String parseMailBoxName(StoreClient store, String collectionName) throws IMAPException {
		if (collectionName.toLowerCase().endsWith(EmailConfiguration.IMAP_INBOX_NAME.toLowerCase())) {
			return EmailConfiguration.IMAP_INBOX_NAME;
		}
		
		int slash = collectionName.lastIndexOf("email\\");
		final String boxName = collectionName.substring(slash + "email\\".length());
		final ListResult lr = listAllFolder(store);
		for (final ListInfo i: lr) {
			if (i.getName().toLowerCase().contains(boxName.toLowerCase())) {
				return i.getName();
			}
		}
		throw new IMAPException("Cannot find IMAP folder for collection [ " + collectionName + " ]");
	}
	 
	@Override
	public void delete(BackendSession bs, Integer devId, String collectionPath, Integer collectionId, Long uid) 
			throws DaoException, MailException {
		
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionPath);
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
			String srcMailBox = parseMailBoxName(store, srcFolder);
			String dstMailBox = parseMailBoxName(store, dstFolder);
			store.select(srcMailBox);
			List<Long> uids = Arrays.asList(uid);
			newUid = store.uidCopy(uids, dstMailBox);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id = ", uid);
			store.uidStore(uids, fl, true);
			store.expunge();
			deleteEmails(devId, srcFolderId, Arrays.asList(uid));
			addMessageInCache(store, devId, dstFolderId, uid);
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
	public List<InputStream> fetchMIMEMails(BackendSession bs, ICalendar calendarClient, String collectionName, 
			Set<Long> uids) throws MailException {
		
		List<InputStream> mails = new LinkedList<InputStream>();
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(store, collectionName));
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
		if (!store.login(activateTLS)) {
			throw new IMAPException("Cannot log into imap server");
		}
	}

	@Override
	public void setAnsweredFlag(BackendSession bs, String collectionName, Long uid) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.ANSWERED);
			store.uidStore(Arrays.asList(uid), fl, true);
			logger.info("flag  change : ANSWERED on mail {} in {}", new Object[]{uid, mailBoxName});
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public void sendEmail(BackendSession bs, Address from, Set<Address> setTo, Set<Address> setCc, Set<Address> setCci, InputStream mimeMail,
			Boolean saveInSent) throws ProcessingEmailException, SendEmailException, SmtpInvalidRcptException, StoreEmailException {
		
		SmtpInvalidRcptException invalidRctp = null;
		InputStream streamMail = null;
		try {
			streamMail = new ByteArrayInputStream(FileUtils.streamBytes(mimeMail, true));
			streamMail.mark(streamMail.available());
			
			try {
				smtpProvider.sendEmail(bs, from, setTo, setCc, setCci, streamMail);
			} catch (SmtpInvalidRcptException e1) {
				invalidRctp = e1;
			}
			
			if (saveInSent) {
				streamMail.reset();
				final Long uid = storeInSent(bs, streamMail);
				if (uid != null) {
					logger.info("This mail {} is stored in 'sent' folder.", uid);
				} else {
					logger.error("The mail can't to be store in 'sent' folder.");
				}
			}
			
		} catch (IOException e) {
			throw new ProcessingEmailException(e);
		} catch (LocatorClientException e) {
			throw new ProcessingEmailException(e);
		} finally {
			closeStream(streamMail);
		}
		
		if (invalidRctp != null) {
			throw invalidRctp;
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
			String mailBoxName = parseMailBoxName(store, collectionName);
			store.select(mailBoxName);
			return store.uidFetchPart(mailUid, mimePartAddress);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public void purgeFolder(BackendSession bs, Integer devId, String collectionPath, Integer collectionId) 
			throws DaoException, MailException {
		
		long time = System.currentTimeMillis();
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionPath);
			store.select(mailBoxName);
			logger.info("Mailbox folder[ {} ] will be purged...", collectionPath);
			Collection<Long> uids = store.uidSearch(new SearchQuery());
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			store.uidStore(uids, fl, true);
			store.expunge();
			deleteEmails(devId, collectionId, uids);
			time = System.currentTimeMillis() - time;
			logger.info("Mailbox folder[ {} ] was purged in {} millisec. {} messages have been deleted",
					new Object[]{collectionPath, time, uids.size()});
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
	private Long storeInSent(BackendSession bs, InputStream mail) throws StoreEmailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			String sentFolderName = null;
			ListResult lr = listAllFolder(store);
			for (ListInfo i: lr) {
				if (i.getName().toLowerCase().endsWith("sent")) {
					sentFolderName = i.getName();
				}
			}
			return storeMail(store, sentFolderName,true, mail, true);
		} catch (IMAPException e) {
			throw new StoreEmailException("Error during store mail in Sent folder", e);
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

	private void deleteEmails(Integer devId, Integer collectionId, Collection<Long> mailUids) throws DaoException {
		try {
			emailDao.deleteSyncEmails(devId, collectionId, mailUids);
		} catch (DaoException e) {
			throw new DaoException("Error while deleting messages in db", e);
		}
	}

	private void addMessageInCache(StoreClient store, Integer devId, Integer collectionId, Long mailUids) throws DaoException {
		Collection<FastFetch> fetch = store.uidFetchFast(ImmutableSet.of(mailUids));
		Collection<Email> emails = Collections2.transform(fetch, new Function<FastFetch, Email>() {
					@Override
					public Email apply(FastFetch input) {
						return new Email(input.getUid(), input.isRead(), input.getInternalDate());
					}
				});
		try {
			markEmailsAsSynced(devId, collectionId, emails);
		} catch (DaoException e) {
			throw new DaoException("Error while adding messages in db", e);
		}
	}

	public void markEmailsAsSynced(Integer devId, Integer collectionId, Collection<Email> messages) throws DaoException {
		markEmailsAsSynced(devId, collectionId, DateUtils.getCurrentDate(), messages);
	}

	public void markEmailsAsSynced(Integer devId, Integer collectionId, Date lastSync, Collection<Email> emails) throws DaoException {
		Set<Email> allEmailsToMark = Sets.newHashSet(emails);
		Set<Email> alreadySyncedEmails = emailDao.alreadySyncedEmails(collectionId, devId, emails);
		Set<Email> modifiedEmails = findModifiedEmails(allEmailsToMark, alreadySyncedEmails);
		Set<Email> emailsNeverTrackedBefore = filterOutAlreadySyncedEmails(allEmailsToMark, alreadySyncedEmails);
		logger.info("mark {} updated mail(s) as synced", modifiedEmails.size());
		emailDao.updateSyncEntriesStatus(devId, collectionId, modifiedEmails);
		logger.info("mark {} new mail(s) as synced", emailsNeverTrackedBefore.size());
		emailDao.createSyncEntries(devId, collectionId, emailsNeverTrackedBefore, lastSync);
	}

	private Set<Email> findModifiedEmails(Set<Email> allEmailsToMark, Set<Email> alreadySyncedEmails) {
		Map<Long, Email> indexedEmailsToMark = getEmailsAsUidTreeMap(allEmailsToMark);
		HashSet<Email> modifiedEmails = Sets.newHashSet();
		for (Email email: alreadySyncedEmails) {
			Email modifiedEmail = indexedEmailsToMark.get(email.getUid());
			if (modifiedEmail != null && !modifiedEmail.equals(email)) {
				modifiedEmails.add(modifiedEmail);
			}
		}
		return modifiedEmails;
	}

	private Set<Email> filterOutAlreadySyncedEmails(Set<Email> allEmailsToMark, Set<Email> alreadySyncedEmails) {
		return org.obm.push.utils.collection.Sets.difference(allEmailsToMark, alreadySyncedEmails, new Comparator<Email>() {
			@Override
			public int compare(Email o1, Email o2) {
				return Long.valueOf(o1.getUid() - o2.getUid()).intValue();
			}
		});
	}
	
	private Map<Long, Email> getEmailsAsUidTreeMap(Set<Email> emails) {
		return Maps.uniqueIndex(emails, new Function<Email, Long>() {
			@Override
			public Long apply(Email email) {
				return email.getIndex();
			}
		});
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
	public void updateData(Integer devId, Integer collectionId, Date lastSync, Collection<Long> removedEmailUids,
			Collection<Email> newAndUpdatedEmails) throws DaoException {
		
		if (removedEmailUids != null && !removedEmailUids.isEmpty()) {
			emailDao.deleteSyncEmails(devId, collectionId, lastSync, removedEmailUids);
		}
		
		if (newAndUpdatedEmails != null && !newAndUpdatedEmails.isEmpty()) {
			markEmailsAsSynced(devId, collectionId, lastSync, newAndUpdatedEmails);
		}
	}

}
