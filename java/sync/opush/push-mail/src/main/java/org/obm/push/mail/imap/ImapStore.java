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
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.minig.imap.FlagsList;
import org.minig.imap.SearchQuery;
import org.obm.mail.MailboxConnection;
import org.obm.push.bean.Resource;
import org.obm.push.exception.FolderCreationException;
import org.obm.push.exception.ImapCommandException;
import org.obm.push.exception.ImapLoginException;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MailboxFolder;

import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

public interface ImapStore extends MailboxConnection, Resource {

	interface Factory {
		ImapStore create(Session session, IMAPStore store, 
				MessageInputStreamProvider messageInputStreamProvider, ImapMailBoxUtils imapMailBoxUtils,
				String userId, String password, String host, int port);
	}
	
	String getUserId();
	
	void login() throws ImapLoginException;
	boolean isConnected();

	Message createMessage();

	Message createMessage(InputStream messageContent) throws MessagingException;

	Message createStreamedMessage(InputStream messageContent, int mailSize);

	void appendMessageStream(String folderName, StreamedLiteral streamedLiteral, Flags flags) throws ImapCommandException;
	
	void appendMessage(String folderName, Message message) throws ImapCommandException;
	
	void deleteMessage(String folderSrc, long messageUid) throws MessagingException, ImapMessageNotFoundException;

	OpushImapFolder select(String folderName) throws ImapCommandException;

	OpushImapFolder getDefaultFolder() throws MessagingException;

	OpushImapFolder create(MailboxFolder folder, int type) throws FolderCreationException;

	boolean hasCapability(ImapCapability imapCapability) throws MessagingException;

	long moveMessageUID(final String folderSrc, final String folderDst, final Long messageUid)
			throws ImapCommandException, ImapMessageNotFoundException;

	Message fetchEnvelope(String folderSrc, long messageUid) throws ImapCommandException, ImapMessageNotFoundException;
	
	Map<Long, IMAPMessage> fetchFast(String folderSrc, Collection<Long> uids) throws ImapCommandException, ImapMessageNotFoundException;

	Map<Long, IMAPMessage> fetchBodyStructure(String folderSrc, Collection<Long> uids) 
			throws ImapCommandException, ImapMessageNotFoundException;
	
	Collection<Long> uidSearch(String folderName, SearchQuery sq) throws ImapCommandException;
	
	boolean uidStore(Collection<Long> uids, FlagsList fl, boolean set);
	
	void expunge();
}
