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
import java.util.NoSuchElementException;
import java.util.Set;

import org.columba.ristretto.smtp.SMTPException;
import org.minig.imap.CommandIOException;
import org.minig.imap.FastFetch;
import org.minig.imap.Flag;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPException;
import org.minig.imap.ListInfo;
import org.minig.imap.ListResult;
import org.minig.imap.SearchQuery;
import org.minig.imap.StoreClient;
import org.minig.imap.UIDEnvelope;
import org.minig.imap.mime.MimeMessage;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.mail.conversation.EmailView;
import org.obm.push.bean.Address;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Email;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.EmailViewPartsFetcherException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.EmailFactory;
import org.obm.push.mail.EmailViewPartsFetcherImpl;
import org.obm.push.mail.FetchInstructions;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailMessageLoader;
import org.obm.push.mail.MailViewToMSEmailConverter;
import org.obm.push.mail.MailboxFolder;
import org.obm.push.mail.MailboxFolders;
import org.obm.push.mail.MimeAddress;
import org.obm.push.mail.PrivateMailboxService;
import org.obm.push.mail.smtp.SmtpSender;
import org.obm.push.service.EventService;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ImapMailboxService implements PrivateMailboxService {

	private static final Logger logger = LoggerFactory.getLogger(ImapMailboxService.class);

	private final SmtpSender smtpProvider;
	private final EventService eventService;
	private final boolean activateTLS;
	private final boolean loginWithDomain;
	private final ImapClientProvider imapClientProvider;
	private final CollectionPathHelper collectionPathHelper;

	private final MailViewToMSEmailConverter msEmailConverter;
	
	@Inject
	/* package */ ImapMailboxService(EmailConfiguration emailConfiguration, 
			SmtpSender smtpSender, EventService eventService, ImapClientProvider imapClientProvider, 
			CollectionPathHelper collectionPathHelper, 
			MailViewToMSEmailConverter msEmailConverter) {
		
		this.smtpProvider = smtpSender;
		this.eventService = eventService;
		this.imapClientProvider = imapClientProvider;
		this.collectionPathHelper = collectionPathHelper;
		this.msEmailConverter = msEmailConverter;
		this.activateTLS = emailConfiguration.activateTls();
		this.loginWithDomain = emailConfiguration.loginWithDomain();
	}

	@Override
	public List<MSEmail> fetchMails(UserDataRequest udr, Integer collectionId, 
			String collectionName, Collection<Long> uids) throws MailException {
		
		final List<MSEmail> mails = new LinkedList<MSEmail>();
		final StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			store.select(parseMailBoxName(udr, collectionName));
			
			final MailMessageLoader mailLoader = new MailMessageLoader(store, eventService);
			for (final Long uid: uids) {
				final MSEmail email = mailLoader.fetch(collectionId, uid, udr);
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
	public List<org.obm.push.bean.ms.MSEmail> fetch(UserDataRequest udr, Integer collectionId, String collectionName,
			Collection<Long> uids, List<BodyPreference> bodyPreferences) throws EmailViewPartsFetcherException, DaoException {
		
		List<org.obm.push.bean.ms.MSEmail> msEmails  = Lists.newLinkedList();
		EmailViewPartsFetcherImpl emailViewPartsFetcherImpl = 
				new EmailViewPartsFetcherImpl(this, bodyPreferences, udr, collectionName, collectionId);
		
		for (Long uid: uids) {
			EmailView emailView = emailViewPartsFetcherImpl.fetch(uid);
			if (emailView != null) {
				msEmails.add(msEmailConverter.convert(emailView, udr));
			}
		}
		return msEmails;
	}
	
	@Override
	public Collection<Flag> fetchFlags(UserDataRequest udr, String collectionName, long uid) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			store.select(parseMailBoxName(udr, collectionName));
			Collection<FlagsList> fetchFlags = store.uidFetchFlags(ImmutableList.of(uid));
			return Iterables.getOnlyElement(fetchFlags);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@Override
	public MailboxFolders listAllFolders(UserDataRequest udr) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			return mailboxFolders(store.listAll());
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@Override
	public MailboxFolders listSubscribedFolders(UserDataRequest udr) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			return mailboxFolders(store.listSubscribed());
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	private MailboxFolders mailboxFolders(ListResult listResult) {
		List<MailboxFolder> mailboxFolders = Lists.newArrayList();
		for (ListInfo folder: listResult) {
			mailboxFolders.add(
					new MailboxFolder(folder.getName(), listResult.getImapSeparator()));
		}
		return new MailboxFolders(mailboxFolders);
	}

	@Override
	public OpushImapFolder createFolder(UserDataRequest udr, MailboxFolder folder) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			if (!store.create(folder.getName())) {
				throw new MailException("Folder creation failed for : " + folder.getName());
			}
			return new MinigOpushImapFolderImpl(false);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public void updateReadFlag(UserDataRequest udr, String collectionName, long uid, boolean read) 
			throws MailException, ImapMessageNotFoundException {
		
		updateMailFlag(udr, collectionName, uid, Flag.SEEN, read);
	}

	/* package */ void updateMailFlag(UserDataRequest udr, String collectionName, long uid, Flag flag, 
			boolean status) throws MailException {
		
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(udr, collectionName);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(flag);
			store.uidStore(ImmutableList.of(uid), fl, status);
			logger.info("Change flag for mail with UID {} in {} ( {}:{} )",
					new Object[] { uid, collectionName, flag.asCommandValue(), status });
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@Override
	public String parseMailBoxName(UserDataRequest udr, String collectionName) throws MailException {
		String boxName = collectionPathHelper.extractFolder(udr, collectionName, PIMDataType.EMAIL);
		
		if (isINBOXSpecificCase(boxName)) {
			return EmailConfiguration.IMAP_INBOX_NAME;
		}
		
		final MailboxFolders lr = listAllFolders(udr);
		for (final MailboxFolder i: lr) {
			if (i.getName().toLowerCase().equals(boxName.toLowerCase())) {
				return i.getName();
			}
		}
		throw new CollectionNotFoundException("Cannot find IMAP folder for collection [ " + collectionName + " ]");
	}

	private boolean isINBOXSpecificCase(String boxName) {
		return boxName.toLowerCase().equals(EmailConfiguration.IMAP_INBOX_NAME.toLowerCase());
	}

	 
	@Override
	public void delete(UserDataRequest udr, String collectionPath, long uid) 
			throws MailException, ImapMessageNotFoundException {

		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(udr, collectionPath);
			store.select(mailBoxName);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			logger.info("delete conv id = ", uid);
			store.uidStore(ImmutableList.of(uid), fl, true);
			store.expunge();
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public long moveItem(UserDataRequest udr, String srcFolder, String dstFolder, long uid)
			throws DaoException, MailException, ImapMessageNotFoundException, UnsupportedBackendFunctionException {
		
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			
			assertMoveItemIsSupported(store);
			
			logger.debug("Moving email, USER:{} UID:{} SRC:{} DST:{}",
					new Object[] {udr.getUser().getLoginAtDomain(), uid, srcFolder, dstFolder});
			
			String srcMailBox = parseMailBoxName(udr, srcFolder);
			String dstMailBox = parseMailBoxName(udr, dstFolder);

			store.select(srcMailBox);
			List<Long> uids = ImmutableList.of(uid);
			Collection<Long> newUid = copyMessage(store, dstMailBox, uids);
			deleteMessage(store, uids);
			
			return Iterables.getOnlyElement(newUid);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	private Collection<Long> copyMessage(StoreClient store, String dstMailBox, List<Long> uids) {
		Collection<Long> newUids = store.uidCopy(uids, dstMailBox);
		if (newUids == null || newUids.size() != 1) {
			throw new ImapMessageNotFoundException("Message with uid " + Iterables.getOnlyElement(uids) + " not found");
		}
		return newUids;
	}

	private void deleteMessage(StoreClient store, List<Long> uids) {
		FlagsList fl = new FlagsList();
		fl.add(Flag.DELETED);
		logger.info("delete conv id = ", Iterables.getOnlyElement(uids));
		store.uidStore(uids, fl, true);
		store.expunge();
	}
	
	private void assertMoveItemIsSupported(StoreClient store) throws UnsupportedBackendFunctionException {
		if (!store.capabilities().contains(ImapCapability.UIDPLUS.capability())) {
			throw new UnsupportedBackendFunctionException("The IMAP server doesn't support UIDPLUS capability");
		}
	}

	@Override
	public InputStream fetchMailStream(UserDataRequest udr, String collectionName, long uid) throws MailException {
		return getMessageInputStream(udr, collectionName, uid);
	}

	private InputStream getMessageInputStream(UserDataRequest udr, String collectionName, long messageUID) 
			throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(udr, collectionName);
			store.select(mailBoxName);
			return store.uidFetchMessage(messageUID);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	private void login(StoreClient store) throws IMAPException {
		store.login(activateTLS);
	}

	@Override
	public void setAnsweredFlag(UserDataRequest udr, String collectionName, long uid) throws MailException, ImapMessageNotFoundException {
		updateMailFlag(udr, collectionName, uid, Flag.ANSWERED, true);
	}

	@Override
	public void sendEmail(UserDataRequest udr, Address from, Set<Address> setTo, Set<Address> setCc, Set<Address> setCci, InputStream mimeMail,
			boolean saveInSent) throws ProcessingEmailException, SendEmailException, SmtpInvalidRcptException, StoreEmailException {
		
		InputStream streamMail = null;
		try {
			streamMail = new ByteArrayInputStream(FileUtils.streamBytes(mimeMail, true));
			streamMail.mark(streamMail.available());
			
			smtpProvider.sendEmail(udr, from, setTo, setCc, setCci, streamMail);
			if (saveInSent) {	
				storeInSent(udr, streamMail);
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
	
	@VisibleForTesting void storeInSent(UserDataRequest udr, InputStream mailContent) throws MailException {
		logger.info("Store mail in folder[SentBox]");
		if (mailContent != null) {
			String sentboxPath = 
					collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_SENT_NAME);
			storeInFolder(udr, mailContent, true, sentboxPath);
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
	public InputStream findAttachment(UserDataRequest udr, String collectionName, Long mailUid, MimeAddress mimePartAddress)
			throws MailException {
		
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(udr, collectionName);
			store.select(mailBoxName);
			return store.uidFetchPart(mailUid, Objects.firstNonNull(mimePartAddress.getAddress(), ""));
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}

	@Override
	public Collection<Long> purgeFolder(UserDataRequest udr, Integer devId, String collectionPath, Integer collectionId) 
			throws DaoException, MailException {
		
		long time = System.currentTimeMillis();
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			String mailBoxName = parseMailBoxName(udr, collectionPath);
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
	public void storeInInbox(UserDataRequest udr, InputStream mailContent, boolean isRead) throws MailException {
		logger.info("Store mail in folder[Inbox]");
		String inboxPath = 
				collectionPathHelper.buildCollectionPath(udr, PIMDataType.EMAIL, EmailConfiguration.IMAP_INBOX_NAME);
		storeInFolder(udr, mailContent, isRead, inboxPath);
	}
	
	private void storeInFolder(UserDataRequest udr, InputStream mailContent, boolean isRead, String collectionPath) 
			throws MailException {

		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			String folderName = parseMailBoxName(udr, collectionPath);
			FlagsList fl = new FlagsList();
			if(isRead){
				fl.add(Flag.SEEN);
			}
			resetInputStream(mailContent);
			store.append(folderName, mailContent, fl);
			store.expunge();
		} catch (IMAPException e) {
			throw new MailException(e);
		} catch (CommandIOException e) {
			throw new MailException(e);
		} catch (IOException e) {
			throw new MailException(e);
		} finally {
			store.logout();
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
	public Collection<Email> fetchEmails(UserDataRequest udr, String collectionName, Collection<Long> uids) throws MailException {
		Collection<FastFetch> fetch = fetchFast(udr, collectionName, uids);
		Collection<Email> emails = Collections2.transform(fetch, new Function<FastFetch, Email>() {
					@Override
					public Email apply(FastFetch input) {
						return new Email(input.getUid(), input.isRead(), input.getInternalDate());
					}
				});
		return emails;	
	}
	
	@Override
	public Set<Email> fetchEmails(UserDataRequest udr, String collectionName, Date windows) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			store.select( parseMailBoxName(udr, collectionName) );
			SearchQuery query = SearchQuery.builder().after(windows).build();
			Collection<Long> uids = store.uidSearch(query);
			Collection<FastFetch> mails = fetchFast(udr, collectionName, uids);
			return EmailFactory.listEmailFromFastFetch(mails);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@Override
	public UIDEnvelope fetchEnvelope(UserDataRequest udr, String collectionPath, long uid) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			String mailboxName = parseMailBoxName(udr, collectionPath);
			store.select(mailboxName);
			Collection<UIDEnvelope> uidFetchEnvelopes = store.uidFetchEnvelope(Arrays.asList(uid));
			return Iterables.getOnlyElement(uidFetchEnvelopes);
		} catch (NoSuchElementException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} catch (CollectionPathException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@Override
	public Collection<FastFetch> fetchFast(UserDataRequest udr, String collectionPath, Collection<Long> uids) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			store.select(parseMailBoxName(udr, collectionPath));
			return store.uidFetchFast(uids);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@Override
	public MimeMessage fetchBodyStructure(UserDataRequest udr, String collectionPath, long uid) throws MailException {
		return Iterables.getOnlyElement(fetchBodyStructure(udr, collectionPath, ImmutableSet.<Long>of(uid)));
	}
	
	@Override
	public Collection<MimeMessage> fetchBodyStructure(UserDataRequest udr, String collectionPath, Collection<Long> uids) throws MailException {
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			store.select(parseMailBoxName(udr, collectionPath));
			return store.uidFetchBodyStructure(uids);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
	
	@Override
	public InputStream fetchMimePartData(UserDataRequest udr, String collectionName, long uid, 
			FetchInstructions fetchInstructions) throws MailException {

		Preconditions.checkNotNull(fetchInstructions);
		StoreClient store = imapClientProvider.getImapClient(udr);
		try {
			login(store);
			store.select(parseMailBoxName(udr, collectionName));
			
			MimeAddress address = fetchInstructions.getMimePart().getAddress();
			String addressAsString = Objects.firstNonNull(address.getAddress(), "");
			Integer truncation = fetchInstructions.getTruncation();
			if (truncation != null) {
				return store.uidFetchPart(uid, addressAsString, truncation);
			} else {
				return store.uidFetchPart(uid, addressAsString);
			}
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		} finally {
			store.logout();
		}
	}
}
