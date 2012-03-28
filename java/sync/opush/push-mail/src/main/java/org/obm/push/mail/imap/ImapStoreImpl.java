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
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.minig.imap.MailboxFolder;
import org.obm.push.exception.FolderCreationException;
import org.obm.push.exception.ImapCommandException;
import org.obm.push.exception.ImapLoginException;
import org.obm.push.exception.ImapLogoutException;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

public class ImapStoreImpl implements ImapStore {

	private static final Logger logger = LoggerFactory.getLogger(ImapStoreImpl.class);

	
	private final Session session;
	private final IMAPStore store;
	private final MessageInputStreamProvider messageInputStreamProvider;
	private final String password;
	private final String userId;
	private final String host;
	private final int port;

	protected ImapStoreImpl(Session session, IMAPStore store, MessageInputStreamProvider messageInputStreamProvider,
			String userId, String password, String host, int port) {
		this.session = session;
		this.store = store;
		this.messageInputStreamProvider = messageInputStreamProvider;
		this.userId = userId;
		this.password = password;
		this.host = host;
		this.port = port;
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
	public void logout() {
		try {
			store.close();
		} catch (MessagingException e) {
			throw new ImapLogoutException("Logout failed for user: " + userId, e);
		}
	}

	@Override
	public boolean isConnected() {
		return store.isConnected();
	}

	@Override
	public void appendMessageStream(String folderName, StreamedLiteral streamedLiteral, Flags flags) 
			throws ImapCommandException {
		try {
			OpushImapFolder folder = select(folderName);
			folder.appendMessageStream(streamedLiteral, flags, null);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command APPEND failed. user=%s, folder=%s", userId, folderName);
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public void appendMessage(String folderName, Message message) 
			throws ImapCommandException {
		try {
			OpushImapFolder folder = select(folderName);
			folder.appendMessage(message);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command APPEND failed. user=%s, folder=%s", userId, folderName);
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
	public long moveMessageUID(final String folderSrc, final String folderDst, final Long messageUid)
			throws ImapCommandException, ImapMessageNotFoundException {
		
		try {
			OpushImapFolder sourceFolder = openFolder(folderSrc, Folder.READ_WRITE);
			Message messageToMove = sourceFolder.getMessageByUID(messageUid);
			
			long newUid = sourceFolder.copyMessageThenGetNewUID(folderDst, messageUid);
			sourceFolder.deleteMessage(messageToMove);
			return newUid;
		} catch (MessagingException e) {
			String msg = String.format("IMAP command Move failed. user=%s, folderSource=%s, folderDestination=%s, messageUid=%d",
					userId, folderSrc, folderDst, messageUid);
			throw new ImapCommandException(msg, e);
		}
	}

	private OpushImapFolder openFolder(String folderName, int mode) throws MessagingException {
		OpushImapFolder opushImapFolder = getFolder(folderName);
		opushImapFolder.open(mode);
		return opushImapFolder;
	}
	
	private OpushImapFolder getFolder(String folderName) throws MessagingException {
		IMAPFolder folder = (IMAPFolder) store.getDefaultFolder().getFolder(folderName);
		return newOpushImapFolder(folder);
	}

	@Override
	public void deleteMessage(String folderSrc, long messageUid) throws MessagingException, ImapMessageNotFoundException {
		OpushImapFolder sourceFolder = openFolder(folderSrc, Folder.READ_WRITE);
		Message messageToDelete = sourceFolder.getMessageByUID(messageUid);
		sourceFolder.deleteMessage(messageToDelete);
	}

	@Override
	public Message fetchEnvelope(String folderSrc, long messageUid) throws ImapCommandException, ImapMessageNotFoundException {
		try {
			OpushImapFolder opushImapFolder = select(folderSrc);
			return opushImapFolder.fetchEnvelope(messageUid);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command fetch envelope failed. user=%s, folder=%s", userId, folderSrc);
			throw new ImapCommandException(msg, e);
		}
	}

	@Override
	public Map<Long, IMAPMessage> fetchFast(String folderSrc, Collection<Long> uids) throws ImapCommandException, ImapMessageNotFoundException {
		try {
			OpushImapFolder opushImapFolder = select(folderSrc);
			return opushImapFolder.fetchFast(uids);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command fetch fast failed. user=%s, folder=%s", userId, folderSrc);
			throw new ImapCommandException(msg, e);
		}
	}
	
	@Override
	public Map<Long, IMAPMessage> fetchBodyStructure(String folderSrc, Collection<Long> uids) throws ImapCommandException, ImapMessageNotFoundException {
		try {
			OpushImapFolder opushImapFolder = select(folderSrc);
			return opushImapFolder.fetchBodyStructure(uids);
		} catch (MessagingException e) {
			String msg = String.format(
					"IMAP command fetch fast failed. user=%s, folder=%s", userId, folderSrc);
			throw new ImapCommandException(msg, e);
		}
	}
	
	private OpushImapFolder newOpushImapFolder(IMAPFolder folder) {
		return new OpushImapFolder(messageInputStreamProvider, folder);
	}

	protected MessageInputStreamProvider getMessageInputStreamProvider() {
		return messageInputStreamProvider;
	}
}
