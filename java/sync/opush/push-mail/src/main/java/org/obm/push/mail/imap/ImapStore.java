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
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.obm.push.bean.Resource;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MailboxFolder;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.exception.FolderCreationException;
import org.obm.push.mail.exception.ImapCommandException;
import org.obm.push.mail.exception.ImapLoginException;

import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;

public interface ImapStore extends Resource {

	interface Factory {
		ImapStore create(Session session, IMAPStore store, 
				MessageInputStreamProvider messageInputStreamProvider,
				String userId, String password, String host);
	}
	
	String getUserId();
	
	void login() throws ImapLoginException;
	boolean isConnected(OpushImapFolder opushImapFolder);

	Message createMessage();

	Message createMessage(InputStream messageContent) throws MessagingException;

	Message createStreamedMessage(InputStream messageContent, int mailSize);

	void appendMessageStream(OpushImapFolder opushImapFolder, StreamedLiteral streamedLiteral, Flags flags) throws ImapCommandException;
	
	void appendMessage(OpushImapFolder opushImapFolder, Message message) throws ImapCommandException;
	
	void deleteMessage(OpushImapFolder sourceFolder, MessageSet messages) throws MessagingException, ImapMessageNotFoundException;

	OpushImapFolder select(String folderName) throws ImapCommandException;

	OpushImapFolder getDefaultFolder() throws MessagingException;

	OpushImapFolder create(MailboxFolder folder, int type) throws FolderCreationException;

	boolean hasCapability(ImapCapability imapCapability) throws MessagingException;

	MessageSet moveMessageUID(final OpushImapFolder sourceFolder, final String folderDst, final MessageSet messages)
			throws ImapCommandException, ImapMessageNotFoundException;

	Map<Long, IMAPMessage> fetchEnvelope(OpushImapFolder opushImapFolder, MessageSet messages) throws ImapCommandException, ImapMessageNotFoundException;
	
	Map<Long, IMAPMessage> fetchFast(OpushImapFolder opushImapFolder, MessageSet messages) throws ImapCommandException, ImapMessageNotFoundException;

	Map<Long, IMAPMessage> fetchBodyStructure(OpushImapFolder opushImapFolder, MessageSet messages) 
			throws ImapCommandException, ImapMessageNotFoundException;
	
	MessageSet uidSearch(OpushImapFolder opushImapFolder, SearchQuery sq) throws ImapCommandException;
	
	boolean uidStore(MessageSet messages, FlagsList fl, boolean set);
	
	void expunge();

	OpushImapFolder openFolder(String folderName, int mode) throws MessagingException;
}
