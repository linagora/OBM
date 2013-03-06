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

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.exception.FolderCreationException;
import org.obm.push.mail.exception.ImapCommandException;
import org.obm.push.mail.exception.ImapLoginException;
import org.obm.push.mail.exception.ImapLogoutException;
import org.obm.push.mail.imap.ImapCapability;
import org.obm.push.mail.imap.ImapStore;
import org.obm.push.mail.imap.MessageInputStreamProvider;
import org.obm.push.mail.imap.OpushImapFolder;
import org.obm.push.mail.imap.StreamMimeMessage;
import org.obm.push.mail.imap.StreamedLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

public class ImapStoreImpl implements ImapStore {

	@Singleton
	public static class Factory implements ImapStore.Factory {
		
		private final EmailConfiguration emailConfiguration;
		private final ImapMailBoxUtils imapMailBoxUtils;

		@Inject
		private Factory(EmailConfiguration emailConfiguration, ImapMailBoxUtils imapMailBoxUtils) {
			this.emailConfiguration = emailConfiguration;
			this.imapMailBoxUtils = imapMailBoxUtils;
			
		}

		@Override
		public ImapStore create(Session session, IMAPStore store, MessageInputStreamProvider messageInputStreamProvider,
				String userId, String password, String host) {
			return new ImapStoreImpl(session, store, messageInputStreamProvider, imapMailBoxUtils, userId, password, host, emailConfiguration.imapPort());
		}
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ImapStoreImpl.class);
	
	private final Session session;
	private final IMAPStore store;
	private final MessageInputStreamProvider messageInputStreamProvider;
	private final ImapMailBoxUtils imapMailBoxUtils;
	private final String password;
	private final String userId;
	private final String host;
	private final int port;

	protected ImapStoreImpl(Session session, IMAPStore store, MessageInputStreamProvider messageInputStreamProvider, ImapMailBoxUtils imapMailBoxUtils,
			String userId, String password, String host, int port) {
		this.session = session;
		this.store = store;
		this.messageInputStreamProvider = messageInputStreamProvider;
		this.imapMailBoxUtils = imapMailBoxUtils;
		this.userId = userId;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	@Override
	public Message createMessage() {
		return new MimeMessage(session);
	}
	
	@Override
	public Message createMessage(InputStream messageContent) throws MessagingException {
		return new MimeMessage(session, messageContent);
	}

	@Override
	public Message createStreamedMessage(InputStream messageContent, int mailSize) {
		return new StreamMimeMessage(session, messageContent, mailSize, new Date());
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public void login() throws ImapLoginException {
		logger.debug("attempt imap login to {}:{} for user : {}",
				new Object[]{host, port, userId});
		
		try {
			store.connect(host, port, userId, password);
		} catch (MessagingException e) {
			throw new ImapLoginException("attempt imap login failed", e);
		}
	}
	
	@Override
	public void close() {
		try {
			store.close();
		} catch (MessagingException e) {
			throw new ImapLogoutException("Logout failed for user: " + userId, e);
		}
	}

	@Override
	public boolean isConnected(OpushImapFolder opushImapFolder) {
		if (opushImapFolder == null) {
			return false;
		}
		try {
			opushImapFolder.noop();
			return true;
		} catch (MessagingException e) {
			return false;
		}
	}

	@Override
	public void appendMessageStream(OpushImapFolder opushImapFolder, StreamedLiteral streamedLiteral, Flags flags) 
			throws ImapCommandException {
		try {
			opushImapFolder.appendMessageStream(streamedLiteral, flags, null);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command APPEND failed. user=%s, folder=%s", userId, opushImapFolder.getFullName());
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public void appendMessage(OpushImapFolder opushImapFolder, Message message) 
			throws ImapCommandException {
		try {
			opushImapFolder.appendMessage(message);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command APPEND failed. user=%s, folder=%s", userId, opushImapFolder.getFullName());
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public OpushImapFolder select(String folderName) throws ImapCommandException {
		try {
			IMAPFolder folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
			folder.open(Folder.READ_WRITE);
			return newOpushImapFolder(folder);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command getFolder failed. user=%s, folder=%s", userId, folderName);
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public OpushImapFolder getDefaultFolder() throws MessagingException {
		IMAPFolder defaultFolder = (IMAPFolder) store.getDefaultFolder();
		return newOpushImapFolder(defaultFolder);
	}

	@Override
	public OpushImapFolder create(MailboxFolder folder, int type) throws FolderCreationException {
		try {
			IMAPFolder imapFolder = (IMAPFolder) store.getDefaultFolder().getFolder(folder.getName());
			if (imapFolder.create(type)) {
				return newOpushImapFolder(imapFolder);
			}
			throw new FolderCreationException("Folder {" + folder.getName() + "} not created.");
		} catch (MessagingException e) {
			throw new FolderCreationException(e.getMessage(), e);
		}
	}

	@Override
	public boolean hasCapability(ImapCapability imapCapability) throws MessagingException {
		return store.hasCapability(imapCapability.capability());
	}

	@Override
	public MessageSet moveMessageUID(final OpushImapFolder sourceFolder, final String folderDst, MessageSet messages)
			throws ImapCommandException, ImapMessageNotFoundException {
		Long currentMessageUid = null;
		try {
			MessageSet.Builder newUids = MessageSet.builder();
			for (long uid: messages) {
				currentMessageUid = uid;
				Message messageToMove = sourceFolder.getMessageByUID(uid);
				
				newUids.add(sourceFolder.copyMessageThenGetNewUID(folderDst, uid));
				sourceFolder.deleteMessage(messageToMove);
			}
			return newUids.build();
		} catch (MessagingException e) {
			String msg = String.format("IMAP command Move failed. user=%s, folderSource=%s, folderDestination=%s, messageUid=%d",
					userId, sourceFolder.getFullName(), folderDst, currentMessageUid);
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public OpushImapFolder openFolder(String folderName, int mode) throws MessagingException {
		OpushImapFolder opushImapFolder = getFolder(folderName);
		opushImapFolder.open(mode);
		return opushImapFolder;
	}
	
	private OpushImapFolder getFolder(String folderName) throws MessagingException {
		IMAPFolder folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
		return newOpushImapFolder(folder);
	}

	@Override
	public void deleteMessage(OpushImapFolder sourceFolder, MessageSet messages) throws MessagingException, ImapMessageNotFoundException {
		for (long uid: messages) {
			Message messageToDelete = sourceFolder.getMessageByUID(uid);
			sourceFolder.deleteMessage(messageToDelete);
		}
	}

	@Override
	public Map<Long, IMAPMessage> fetchEnvelope(OpushImapFolder opushImapFolder, MessageSet messages) throws ImapCommandException, ImapMessageNotFoundException {
		try {
			return opushImapFolder.fetchEnvelope(messages);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command fetch envelope failed. user=%s, folder=%s", userId, opushImapFolder.getFullName());
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public Map<Long, IMAPMessage> fetchFast(OpushImapFolder opushImapFolder, MessageSet messages) throws ImapCommandException {
		try {
			return opushImapFolder.fetchFast(messages);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command fetch fast failed. user=%s, folder=%s", userId, opushImapFolder.getFullName());
			throw new ImapCommandException(msg, e);
		}
	}
	
	@Override
	public Map<Long, IMAPMessage> fetchBodyStructure(OpushImapFolder opushImapFolder, MessageSet messages) throws ImapCommandException, ImapMessageNotFoundException {
		try {
			return opushImapFolder.fetchBodyStructure(messages);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command fetch fast failed. user=%s, folder=%s", userId, opushImapFolder.getFullName());
			throw new ImapCommandException(msg, e);
		}
	}
	
	private OpushImapFolder newOpushImapFolder(IMAPFolder folder) {
		return new OpushImapFolderImpl(imapMailBoxUtils, messageInputStreamProvider, folder);
	}

	protected MessageInputStreamProvider getMessageInputStreamProvider() {
		return messageInputStreamProvider;
	}

	@Override 
	public MessageSet uidSearch(OpushImapFolder opushImapFolder, SearchQuery sq) throws ImapCommandException {
		try {
			return opushImapFolder.uidSearch(sq);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command uid search failed. user=%s", userId);
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public boolean uidStore(MessageSet messages, FlagsList fl, boolean set) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void expunge() {
		// TODO Auto-generated method stub
		
	}
}
