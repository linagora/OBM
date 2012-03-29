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
import java.util.Map;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.columba.ristretto.smtp.SMTPException;
import org.minig.imap.Envelope;
import org.minig.imap.FastFetch;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.MailboxFolder;
import org.minig.imap.MailboxFolders;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.minig.imap.UIDEnvelope;
import org.minig.imap.mime.MimeMessage;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.Address;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Email;
import org.obm.push.bean.EmailHeaders;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.CollectionPathException;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
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
	private final ImapMailBoxUtils imapMailBoxUtils;
	private final CollectionPathHelper collectionPathHelper;
	
	@Inject
	/* package */ ImapMailboxService(EmailConfiguration emailConfiguration, 
			SmtpSender smtpSender, EventService eventService, ImapClientProvider imapClientProvider, 
			ImapMailBoxUtils imapMailBoxUtils, CollectionPathHelper collectionPathHelper) {
		
		this.smtpProvider = smtpSender;
		this.eventService = eventService;
		this.imapClientProvider = imapClientProvider;
		this.imapMailBoxUtils = imapMailBoxUtils;
		this.collectionPathHelper = collectionPathHelper;
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
			
			final MailMessageLoader mailLoader = new MailMessageLoader(store, eventService);
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
	public IMAPHeaders uidFetchHeaders(BackendSession bs, String collectionName, long uid, EmailHeaders headersToFetch) throws MailException, ImapMessageNotFoundException {
		Preconditions.checkNotNull(headersToFetch);
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select(parseMailBoxName(bs, collectionName));
			Collection<IMAPHeaders> fetchHeaders = store.uidFetchHeaders(Arrays.asList(uid), headersToFetch.toStringArray());
			if (fetchHeaders.size() < 1) {
				throw new ImapMessageNotFoundException("message UID " + uid);
			}
			return Iterables.getOnlyElement(fetchHeaders);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public Collection<Long> uidSearch(BackendSession bs, String collectionName, SearchQuery sq) throws MailException {
		ImapStore store = null; 
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			OpushImapFolder folder = store.select(parseMailBoxName(bs, collectionName));
			return folder.uidSearch(sq);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
	}
	
	@Override
	public Collection<Flag> uidFetchFlags(BackendSession bs, String collectionName, long uid) throws MailException {
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			OpushImapFolder imapFolder = store.select( parseMailBoxName(bs, collectionName) );
			return imapFolder.uidFetchFlags(uid);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
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
			return folder.getMessageByUID(uid);
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
		try {
			String boxName = collectionPathHelper.extractFolder(bs, collectionName, PIMDataType.EMAIL);
			
			if (isINBOXSpecificCase(boxName)) {
				return EmailConfiguration.IMAP_INBOX_NAME;
			}
			
			final MailboxFolders lr = listAllFolders(bs);
			for (final MailboxFolder i: lr) {
				if (i.getName().toLowerCase().equals(boxName.toLowerCase())) {
					return i.getName();
				}
			}
			throw new MailException("Cannot find IMAP folder for collection [ " + collectionName + " ]");
		} catch (CollectionPathException e){
			throw new MailException(e);
		}
	}

	private boolean isINBOXSpecificCase(String boxName) {
		return boxName.toLowerCase().equals(EmailConfiguration.IMAP_INBOX_NAME.toLowerCase());
	}

	 
	@Override
	public void delete(BackendSession bs, String collectionPath, long uid) 
			throws MailException, ImapMessageNotFoundException {

		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			
			String mailboxName = parseMailBoxName(bs, collectionPath);
			store.deleteMessage(mailboxName, uid);
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
			boolean saveInSent) throws ProcessingEmailException, SendEmailException, SmtpInvalidRcptException, StoreEmailException {
		
		InputStream streamMail = null;
		try {
			streamMail = new ByteArrayInputStream(FileUtils.streamBytes(mimeMail, true));
			streamMail.mark(streamMail.available());
			
			smtpProvider.sendEmail(bs, from, setTo, setCc, setCci, streamMail);
			if (saveInSent) {	
				storeInSent(bs, streamMail);
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
	
	@VisibleForTesting void storeInSent(BackendSession bs, InputStream mailContent) throws MailException {
		logger.info("Store mail in folder[SentBox]");
		if (mailContent != null) {
			String sentboxPath = 
					collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
			storeInFolder(bs, mailContent, true, sentboxPath);
		} else {
			throw new MailException("The mail that user try to store in sent box is null.");
		}
	}

	private void resetInputStream(InputStream mailContent) throws IOException {
		try {
			mailContent.reset();
		} catch (IOException e) {
			mailContent.mark(0);
			mailContent.reset();
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
	public InputStream findAttachment(BackendSession bs, String collectionName, Long mailUid, String mimePartAddress)
			throws MailException {
		
		ImapStore store = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			String mailBoxName = parseMailBoxName(bs, collectionName);
			OpushImapFolder imapFolder = store.select(mailBoxName);
			return imapFolder.uidFetchPart(mailUid, mimePartAddress);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
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
			Collection<Long> uids = store.uidSearch(SearchQuery.MATCH_ALL);
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
				closeQuietly(store);
			}
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e.getMessage(), e);
		}
	}
		
	@Override
	public void storeInInbox(BackendSession bs, InputStream mailContent, boolean isRead) throws MailException {
		logger.info("Store mail in folder[Inbox]");
		String inboxPath = 
				collectionPathHelper.buildCollectionPath(bs, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		storeInFolder(bs, mailContent, isRead, inboxPath);
	}
	
	private void storeInFolder(BackendSession bs, InputStream mailContent, boolean isRead, String collectionPath) 
			throws MailException {
		
		try {
			ImapStore store = imapClientProvider.getImapClientWithJM(bs);
			try {
				store.login();
				resetInputStream(mailContent);
				Message message = store.createMessage(mailContent);
				message.setFlag(Flags.Flag.SEEN, isRead);
				String folderName = parseMailBoxName(bs, collectionPath);
				store.appendMessage(folderName, message);
			} catch (ImapCommandException e) {
				throw new MailException(e.getMessage(), e);
			} catch (LocatorClientException e) {
				throw new MailException(e.getMessage(), e);
			} catch (MessagingException e) {
				throw new MailException(e.getMessage(), e);
			} catch (IOException e) {
				throw new MailException(e.getMessage(), e);
			} finally {
				closeQuietly(store);
			}
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e.getMessage(), e);
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
	public Collection<Email> fetchEmails(BackendSession bs, String collectionName, Collection<Long> uids) throws MailException {
		Collection<FastFetch> fetch = fetchFast(bs, collectionName, uids);
		Collection<Email> emails = Collections2.transform(fetch, new Function<FastFetch, Email>() {
					@Override
					public Email apply(FastFetch input) {
						return new Email(input.getUid(), input.isRead(), input.getInternalDate());
					}
				});
		return emails;	
	}
	
	@Override
	public Set<Email> fetchEmails(BackendSession bs, String collectionName, Date windows) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(bs);
		try {
			login(store);
			store.select( parseMailBoxName(bs, collectionName) );
			SearchQuery query = new SearchQuery.Builder().after(windows).build();
			Collection<Long> uids = store.uidSearch(query);
			Collection<FastFetch> mails = fetchFast(bs, collectionName, uids);
			return EmailFactory.listEmailFromFastFetch(mails);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@VisibleForTesting UIDEnvelope fetchEnvelope(BackendSession bs, String collectionPath, long uid) throws MailException {
		ImapStore store = null;
		IMAPMessage message = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			
			String mailboxName = parseMailBoxName(bs, collectionPath);
			message = (IMAPMessage) store.fetchEnvelope(mailboxName, uid);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
		
		Envelope envelope = imapMailBoxUtils.buildEnvelopeFromMessage(message);
		return new UIDEnvelope(uid, envelope);
	}
	
	@Override
	public Collection<FastFetch> fetchFast(BackendSession bs, String collectionPath, Collection<Long> uids) throws MailException {
		ImapStore store = null;
		Map<Long, IMAPMessage> imapMessages = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			
			String mailboxName = parseMailBoxName(bs, collectionPath);
			imapMessages = store.fetchFast(mailboxName, uids);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
		
		return imapMailBoxUtils.buildFastFetchFromIMAPMessage(imapMessages);
	}
	
	@Override
	public Collection<MimeMessage> fetchBodyStructure(BackendSession bs, String collectionPath, Collection<Long> uids) throws MailException {
		ImapStore store = null;
		Map<Long, IMAPMessage> imapMessages = null;
		try {
			store = imapClientProvider.getImapClientWithJM(bs);
			store.login();
			
			String mailboxName = parseMailBoxName(bs, collectionPath);
			imapMessages = store.fetchBodyStructure(mailboxName, uids);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (NoImapClientAvailableException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} finally {
			closeQuietly(store);
		}
		return imapMailBoxUtils.buildMimeMessageCollectionFromIMAPMessage(imapMessages);
	}
}
