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
package org.obm.push.java.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.columba.ristretto.smtp.SMTPException;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.Address;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.push.mail.EmailFactory;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.FastFetch;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MailboxFolders;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.exception.ImapCommandException;
import org.obm.push.mail.exception.ImapLoginException;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.mail.imap.ImapCapability;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.OpushImapFolder;
import org.obm.push.mail.mime.IMimePart;
import org.obm.push.mail.mime.MimeAddress;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.mail.smtp.SmtpSender;
import org.obm.push.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.mail.imap.IMAPInputStream;
import com.sun.mail.imap.IMAPMessage;

@Singleton
public class ImapMailboxService implements MailboxService {

	private static final Logger logger = LoggerFactory.getLogger(ImapMailboxService.class);

	private static final String WHOLE_HIERARCHY_PATTERN = "*";
	
	private final SmtpSender smtpProvider;
	private final boolean activateTLS;
	private final boolean loginWithDomain;
	private final ImapClientProviderImpl imapClientProvider;
	private final ImapMailBoxUtils imapMailBoxUtils;
	private final CollectionPathHelper collectionPathHelper;
	
	private OpushImapFolderConnection opushImapFolderConnection;
	
	@Inject
	/* package */ ImapMailboxService(EmailConfiguration emailConfiguration, 
			SmtpSender smtpSender, 
			ImapClientProviderImpl imapClientProvider, 
			ImapMailBoxUtils imapMailBoxUtils, 
			CollectionPathHelper collectionPathHelper) {
		
		this.smtpProvider = smtpSender;
		this.imapClientProvider = imapClientProvider;
		this.imapMailBoxUtils = imapMailBoxUtils;
		this.collectionPathHelper = collectionPathHelper;
		this.activateTLS = emailConfiguration.activateTls();
		this.loginWithDomain = emailConfiguration.loginWithDomain();
		this.opushImapFolderConnection = new OpushImapFolderConnection();
	}

	@Override
	public EmailMetadata fetchEmailMetadata(UserDataRequest udr, String collectionPath, long uid)
			throws MailException, ItemNotFoundException {
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			Map<Long, IMAPMessage> results = currentOpushImapFolder().fetchEmailView(MessageSet.singleton(uid));
			if (results.containsKey(uid)) {
				IMAPMessage message = results.get(uid);
				Set<Flag> flags = imapMailBoxUtils.buildFlagToIMAPMessageFlags(message.getFlags());
				return EmailMetadata.builder()
						.uid(uid)
						.size(message.getSize())
						.flags(new FlagsList(flags))
						.mimeMessage(imapMailBoxUtils.buildMimeMessageFromIMAPMessage(uid, message))
						.envelope(imapMailBoxUtils.buildEnvelopeFromMessage(message))
						.build();
			}
			throw new ItemNotFoundException(String.format(
					"Cannot find expected response:{%d} in imap results:{%s}", uid, results));
		} catch (ImapMessageNotFoundException e) {
			throw new ItemNotFoundException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}

	@Override
	public Map<Long, FlagsList> fetchFlags(UserDataRequest udr, String collectionPath, MessageSet messages) throws MailException {
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			return currentOpushImapFolder().uidFetchFlags(messages);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}
	
	@Override
	public MailboxFolders listAllFolders(UserDataRequest udr) throws MailException {
		try {
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, null);
			Folder[] folders = store.getDefaultFolder().list(WHOLE_HIERARCHY_PATTERN);
			return mailboxFolders(folders);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}
	
	@Override
	public MailboxFolders listSubscribedFolders(UserDataRequest udr) throws MailException {
		try {
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, null);
			Folder[] folders = store.getDefaultFolder().listSubscribed(WHOLE_HIERARCHY_PATTERN);
			return mailboxFolders(folders);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}

	private MailboxFolders mailboxFolders(Folder[] folders) throws MessagingException {
		List<MailboxFolder> mailboxFolders = Lists.newArrayList();
		for (Folder folder: folders) {
			mailboxFolders.add(
					new MailboxFolder(folder.getFullName(), folder.getSeparator()));
		}
		return new MailboxFolders(mailboxFolders);
	}

	@Override
	public void createFolder(UserDataRequest udr, MailboxFolder folder) throws MailException {
		try {
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, null);
			store.create(folder, Folder.HOLDS_MESSAGES|Folder.HOLDS_FOLDERS);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		}
	}

	@Override
	public void updateReadFlag(UserDataRequest udr, String collectionPath, MessageSet messages, boolean read) 
			throws MailException, ImapMessageNotFoundException {
		
		updateMailFlag(udr, collectionPath, messages, Flags.Flag.SEEN, read);
	}

	private void updateMailFlag(UserDataRequest udr, String collectionPath, MessageSet messages, Flags.Flag flag, 
			boolean status) throws MailException, ImapMessageNotFoundException {
		
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			for (long uid: messages) {
				IMAPMessage message = getMessage(uid);
				message.setFlag(flag, status);
				logger.info("Change flag for mail with UID {} in {} ( {}:{} )",
						new Object[] { uid, collectionPath, imapMailBoxUtils.flagToString(flag), status });
			}
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}
	
	private IMAPMessage getMessage(long uid) 
			throws MailException, ImapMessageNotFoundException {
		
		try {
			return currentOpushImapFolder().getMessageByUID(uid);
		} catch (MessagingException e) {
			throw new MailException(e);
		}
	}
	
	/* package */ IMAPMessage getMessage(UserDataRequest udr, String collectionPath, long uid) 
			throws MailException, ImapMessageNotFoundException {
		
		try {
			openImapFolderAndGetCorrespondingImapStore(udr, collectionPath);
			return getMessage(uid);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}
	
	/* package */ void expunge(UserDataRequest udr, String collectionPath) throws MailException {
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			currentOpushImapFolder().expunge();
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}
	
	@Override
	public String parseMailBoxName(UserDataRequest udr, String collectionPath) throws MailException {
		String boxName = collectionPathHelper.extractFolder(udr, collectionPath, PIMDataType.EMAIL);
		
		if (isINBOXSpecificCase(boxName)) {
			return EmailConfiguration.IMAP_INBOX_NAME;
		}
		
		final MailboxFolders lr = listAllFolders(udr);
		for (final MailboxFolder i: lr) {
			if (i.getName().toLowerCase().equals(boxName.toLowerCase())) {
				return i.getName();
			}
		}
		throw new CollectionNotFoundException("Cannot find IMAP folder for collection [ " + collectionPath + " ]");
	}

	private boolean isINBOXSpecificCase(String boxName) {
		return boxName.toLowerCase().equals(EmailConfiguration.IMAP_INBOX_NAME.toLowerCase());
	}

	 
	@Override
	public void delete(UserDataRequest udr, String collectionPath, MessageSet messages) 
			throws MailException, ImapMessageNotFoundException {

		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			
			store.deleteMessage(currentOpushImapFolder(), messages);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		}
	}

	@Override
	public MessageSet move(UserDataRequest udr, String srcFolder, String dstFolder, MessageSet messages)
			throws DaoException, MailException, ImapMessageNotFoundException, UnsupportedBackendFunctionException {
		
		try {
			String srcMailBox = parseMailBoxName(udr, srcFolder);
			String dstMailBox = parseMailBoxName(udr, dstFolder);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, srcMailBox);
			
			assertMoveItemIsSupported(store);
			
			logger.debug("Moving email, USER:{} UIDs:{} SRC:{} DST:{}",
					new Object[] {udr.getUser().getLoginAtDomain(), messages, srcFolder, dstFolder});

			return store.moveMessageUID(currentOpushImapFolder(), dstMailBox, messages);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		}
	}
	
	private void assertMoveItemIsSupported(ImapStore store) throws UnsupportedBackendFunctionException, MessagingException {
		if (!store.hasCapability(ImapCapability.UIDPLUS)) {
			throw new UnsupportedBackendFunctionException("The IMAP server doesn't support UIDPLUS capability");
		}
	}

	@Override
	public InputStream fetchMailStream(UserDataRequest udr, String collectionPath, long uid) throws MailException {
		return getMessageInputStream(udr, collectionPath, uid);
	}

	private InputStream getMessageInputStream(UserDataRequest udr, String collectionPath, long messageUID) 
			throws MailException {
		
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			IMAPMessage imapMessage = getMessage(messageUID);
			IMAPInputStream imapInputStream = new IMAPInputStream(imapMessage, null, -1, true);
			return imapInputStream;
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} 
	}

	@Override
	public void setAnsweredFlag(UserDataRequest udr, String collectionPath, MessageSet messages) throws MailException, ImapMessageNotFoundException {
		updateMailFlag(udr, collectionPath, messages, Flags.Flag.ANSWERED, true);
	}
	
	@Override
	public void setDeletedFlag(UserDataRequest udr, String collectionPath, MessageSet messages) throws MailException, ImapMessageNotFoundException {
		updateMailFlag(udr, collectionPath, messages, Flags.Flag.DELETED, true);
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
	
	@Override
	public void storeInSent(UserDataRequest udr, InputStream mailContent) throws MailException {
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
	public MessageSet purgeFolder(UserDataRequest udr, Integer devId, String collectionPath, Integer collectionId) 
			throws DaoException, MailException {
		
		long time = System.currentTimeMillis();
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			
			logger.info("Mailbox folder[ {} ] will be purged...", mailboxName);
			MessageSet messages = store.uidSearch(currentOpushImapFolder(), SearchQuery.MATCH_ALL);
			FlagsList fl = new FlagsList();
			fl.add(Flag.DELETED);
			store.uidStore(messages, fl, true);
			store.expunge();
			time = System.currentTimeMillis() - time;
			logger.info("Mailbox folder[ {} ] was purged in {} millisec. {} messages have been deleted",
					new Object[]{mailboxName, time, Iterables.size(messages)});
			return messages;
		} catch (ImapCommandException e) {
			throw new MailException(e);
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
		
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			resetInputStream(mailContent);
			Message message = store.createMessage(mailContent);
			message.setFlag(Flags.Flag.SEEN, isRead);
			store.appendMessage(currentOpushImapFolder(), message);
		} catch (ImapCommandException e) {
			throw new MailException(e.getMessage(), e);
		} catch (LocatorClientException e) {
			throw new MailException(e.getMessage(), e);
		} catch (MessagingException e) {
			throw new MailException(e.getMessage(), e);
		} catch (IOException e) {
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
	public Collection<Email> fetchEmails(UserDataRequest udr, String collectionPath, MessageSet messages) throws MailException {
		return EmailFactory.listEmailFromFastFetch(fetchFast(udr, collectionPath, messages));
	}
	
	@Override
	public Set<Email> fetchEmails(UserDataRequest udr, String collectionPath, Date windows) throws MailException {
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			
			SearchQuery query = SearchQuery.builder().after(windows).build();
			MessageSet messages = store.uidSearch(currentOpushImapFolder(), query);
			Collection<FastFetch> mails = fetchFast(udr, collectionPath, messages);
			return EmailFactory.listEmailFromFastFetch(mails);
		} catch (ImapCommandException e) {
			throw new MailException(e.getMessage(), e);
		}
	}
	
	@Override
	public Collection<UIDEnvelope> fetchEnvelope(UserDataRequest udr, String collectionPath, MessageSet messages) throws MailException {
		Map<Long, IMAPMessage> imapMessages = null;
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			
			imapMessages = store.fetchEnvelope(currentOpushImapFolder(), messages);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		}
		
		return Maps.transformEntries(imapMessages, new Maps.EntryTransformer<Long, IMAPMessage, UIDEnvelope>() {
			@Override
			public UIDEnvelope transformEntry(Long uid, IMAPMessage message) {
				return new UIDEnvelope(uid, imapMailBoxUtils.buildEnvelopeFromMessage(message));
			}
		}).values();
	}
	
	@Override
	public Collection<FastFetch> fetchFast(UserDataRequest udr, String collectionPath, MessageSet messages) throws MailException {
		Map<Long, IMAPMessage> imapMessages = null;
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			
			imapMessages = store.fetchFast(currentOpushImapFolder(), messages);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		}
		
		return imapMailBoxUtils.buildFastFetchFromIMAPMessage(imapMessages);
	}
	
	@Override
	public Collection<MimeMessage> fetchBodyStructure(UserDataRequest udr, String collectionPath, MessageSet messages) throws MailException {
		Map<Long, IMAPMessage> imapMessages = null;
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			
			imapMessages = store.fetchBodyStructure(currentOpushImapFolder(), messages);
			return imapMailBoxUtils.buildMimeMessageCollectionFromIMAPMessage(imapMessages);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		}
	}
	
	@Override
	public InputStream fetchPartialMimePartStream(UserDataRequest udr, String collectionPath, long uid, MimeAddress partAddress, int limit) 
			throws MailException {
		Preconditions.checkNotNull(partAddress);
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			OpushImapFolder imapFolder = store.select(mailboxName);
			return imapFolder.uidFetchPart(uid, partAddress, limit);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		}
	}
	
	@Override
	public InputStream fetchMimePartStream(UserDataRequest udr, String collectionPath, long uid, MimeAddress partAddress)
			throws MailException {
		Preconditions.checkNotNull(partAddress);
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			OpushImapFolder imapFolder = store.select(mailboxName);
			return imapFolder.uidFetchPart(uid, partAddress);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapLoginException e) {
			throw new MailException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		}
	}
	
	@Override
	public InputStream findAttachment(UserDataRequest udr, String collectionPath, Long mailUid, MimeAddress mimePartAddress)
			throws MailException {
		
		try {
			String mailboxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailboxName);
			
			OpushImapFolder imapFolder = store.select(mailboxName);
			return imapFolder.uidFetchPart(mailUid, mimePartAddress);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		}
	}

	private OpushImapFolder currentOpushImapFolder() {
		return opushImapFolderConnection.getOpushImapFolder();
	}

	private ImapStore openImapFolderAndGetCorrespondingImapStore(UserDataRequest udr, String folderName) {
		try {
			opushImapFolderConnection.closeImapFolderWhenChanged(folderName);
			ImapStore store = imapClientProvider.getImapClient(udr, currentOpushImapFolder());
			opushImapFolderConnection.setImapFolderToFolderNameAndOpenIt(folderName, store);
			return store;
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (IMAPException e) {
			throw new MailException(e);
		}
	}

	@Override
	public Map<Long, IMAPHeaders> fetchPartHeaders(UserDataRequest udr, String collectionPath, MessageSet messages, IMimePart mimePart) throws IOException {
		ImmutableMap.Builder<Long, IMAPHeaders> headers = ImmutableMap.builder();
		for (long uid: messages) {
			headers.put(uid, fetchPartHeaders(udr, collectionPath, uid, mimePart));
		}
		return headers.build();
	}

	private IMAPHeaders fetchPartHeaders(UserDataRequest udr, String collectionPath, long uid, IMimePart mimePart)
			throws IOException {
		MimeAddress address = mimePart.getAddress();
		String part = null;
		if (address == null) {
			part = "HEADER";
		} else {
			part = address.getAddress() + ".HEADER";
		}
		InputStream is = fetchMimePartStream(udr, collectionPath, uid, new MimeAddress(part));
		return mimePart.decodeHeaders(is);
	}

	@Override
	public long fetchUIDNext(UserDataRequest udr, String collectionPath) throws MailException {
		
		try {
			String mailBoxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailBoxName);
			OpushImapFolder imapFolder = store.select(mailBoxName);
			return imapFolder.uidNext(mailBoxName);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		}
	}

	@Override
	public long fetchUIDValidity(UserDataRequest udr, String collectionPath) throws MailException {
		
		try {
			String mailBoxName = parseMailBoxName(udr, collectionPath);
			ImapStore store = openImapFolderAndGetCorrespondingImapStore(udr, mailBoxName);
			OpushImapFolder imapFolder = store.select(mailBoxName);
			return imapFolder.uidValidity(mailBoxName);
		} catch (LocatorClientException e) {
			throw new MailException(e);
		} catch (ImapMessageNotFoundException e) {
			throw new MailException(e);
		} catch (MessagingException e) {
			throw new MailException(e);
		} catch (ImapCommandException e) {
			throw new MailException(e);
		}
	}
}
