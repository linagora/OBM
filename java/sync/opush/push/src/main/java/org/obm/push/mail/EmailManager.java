/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
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

import org.columba.ristretto.message.Address;
import org.minig.imap.FastFetch;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.store.LocatorService;
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
import org.obm.push.store.EmailDao;
import org.obm.push.utils.FileUtils;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EmailManager implements IEmailManager {

	private static final Logger logger = LoggerFactory.getLogger(EmailManager.class);
	
	private final EmailDao emailDao;
	private final SmtpSender smtpProvider;
	private final LocatorService locatorService;
	private final EmailSync emailSync;
	private final boolean loginWithDomain;
	private final boolean activateTLS;
	
	@Inject
	/*package*/ EmailManager(EmailDao emailDao, EmailConfiguration emailConfiguration, SmtpSender smtpSender, 
			EmailSync emailSync, LocatorService locatorService) {
		
		this.emailSync = emailSync;
		this.smtpProvider = smtpSender;
		this.emailDao = emailDao;
		this.locatorService = locatorService;
		this.loginWithDomain = emailConfiguration.loginWithDomain();
		this.activateTLS = emailConfiguration.activateTls();
	}

	@Override
	public String locateImap(BackendSession bs) {
		String locateImap = locatorService.
				getServiceLocation("mail/imap_frontend", bs.getLoginAtDomain());
		logger.info("Using {} as imap host.", locateImap);
		return locateImap;
	}

	private StoreClient getImapClient(BackendSession bs) {
		final String imapHost = locateImap(bs);
		final String login = getLogin(bs);
		StoreClient storeClient = new StoreClient(imapHost, 143, login, bs.getPassword()); 
		
		logger.debug("Creating storeClient with login {} : " +
				"loginWithDomain = {} | activateTLS = {}", 
				new Object[]{login, loginWithDomain, activateTLS});
		
		return storeClient; 
	}

	private String getLogin(BackendSession bs) {
		String login = bs.getLoginAtDomain();
		if (!loginWithDomain) {
			int at = login.indexOf("@");
			if (at > 0) {
				login = login.substring(0, at);
			}
		}
		return login;
	}	

	@Override
	public MailChanges getSync(BackendSession bs, SyncState syncState, Integer deviceId, Integer collectionId, String collectionName) 
			throws IMAPException, DaoException {
		
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			store.select( parseMailBoxName(store, collectionName) );
			return emailSync.getSync(store, deviceId, syncState, collectionId);
		} finally {
			store.logout();
		}
	}

	@Override
	public List<MSEmail> fetchMails(BackendSession bs, AbstractEventSyncClient calendarClient, Integer collectionId, 
			String collectionName, Collection<Long> uids) throws IMAPException {
		
		final List<MSEmail> mails = new LinkedList<MSEmail>();
		final StoreClient store = getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(store, collectionName));
			
			final MailMessageLoader mailLoader = new MailMessageLoader(store, calendarClient);
			for (final Long uid: uids) {
				final MSEmail email = mailLoader.fetch(collectionId, uid, bs);
				if (email != null) {
					mails.add(email);
				}
			}
		} finally {
			store.logout();
		}
		return mails;
	}

	private ListResult listAllFolder(StoreClient store) {
		return store.listAll();
	}

	@Override
	public void updateReadFlag(BackendSession bs, String collectionName, Long uid, boolean read) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.SEEN);
			store.uidStore(Arrays.asList(uid), fl, read);
			logger.info("flag  change: " + (read ? "+" : "-") + " SEEN"
					+ " on mail " + uid + " in " + mailBoxName);
		} finally {
			store.logout();
		}
	}

	@Override
	public String parseMailBoxName(BackendSession bs, String collectionName) throws IMAPException {
		// parse obm:\\adrien@test.tlse.lng\email\INBOX\Sent
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			return parseMailBoxName(store, collectionName);
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
			throws IMAPException, DaoException {
		
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionPath);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id = ", uid);
			store.uidStore(Arrays.asList(uid), fl, true);
			store.expunge();
			deleteEmails(devId, collectionId, Arrays.asList(uid));
		} finally {
			store.logout();
		}
	}

	@Override
	public Long moveItem(BackendSession bs, Integer devId, String srcFolder, Integer srcFolderId, String dstFolder, Integer dstFolderId, 
			Long uid) throws IMAPException, DaoException {
		
		StoreClient store = getImapClient(bs);
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
		} finally {
			store.logout();
		}
		if (newUid == null || newUid.isEmpty()) {
			return null;
		}
		return newUid.iterator().next();
	}

	@Override
	public List<InputStream> fetchMIMEMails(BackendSession bs, AbstractEventSyncClient calendarClient, String collectionName, 
			Set<Long> uids) throws IMAPException {
		
		List<InputStream> mails = new LinkedList<InputStream>();
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(store, collectionName));
			for (Long uid : uids) {
				mails.add(store.uidFetchMessage(uid));
			}
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
	public void setAnsweredFlag(BackendSession bs, String collectionName, Long uid) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.ANSWERED);
			store.uidStore(Arrays.asList(uid), fl, true);
			logger.info("flag  change : ANSWERED on mail {} in {}", new Object[]{uid, mailBoxName});
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
	public InputStream findAttachment(BackendSession bs, String collectionName, Long mailUid, String mimePartAddress) throws IMAPException {
		StoreClient store = getImapClient(bs);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(store, collectionName);
			store.select(mailBoxName);
			return store.uidFetchPart(mailUid, mimePartAddress);
		} finally {
			store.logout();
		}
	}

	@Override
	public void purgeFolder(BackendSession bs, Integer devId, String collectionPath, Integer collectionId) throws IMAPException, DaoException {
		long time = System.currentTimeMillis();
		StoreClient store = getImapClient(bs);
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
		} finally {
			store.logout();
		}
	}

	@Override
	public Long storeInInbox(BackendSession bs, InputStream mailContent, boolean isRead) throws StoreEmailException {
		logger.info("Store mail in folder[Inbox]");
		StoreClient store = getImapClient(bs);
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
		StoreClient store = getImapClient(bs);
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
			emailDao.markEmailsAsSynced(devId, collectionId, emails);
		} catch (DaoException e) {
			throw new DaoException("Error while adding messages in db", e);
		}
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
			Collection<Email> updatedEmails) throws DaoException {
		
		if (removedEmailUids != null && !removedEmailUids.isEmpty()) {
			emailDao.deleteSyncEmails(devId, collectionId, lastSync, removedEmailUids);
		}
		
		if (updatedEmails != null && !updatedEmails.isEmpty()) {
			emailDao.markEmailsAsSynced(devId, collectionId, lastSync, updatedEmails);
		}
	}

}
