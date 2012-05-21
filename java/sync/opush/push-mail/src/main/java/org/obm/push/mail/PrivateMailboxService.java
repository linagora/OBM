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

import java.io.InputStream;
import java.util.Collection;

import org.minig.imap.FastFetch;
import org.minig.imap.Flag;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.MailboxFolder;
import org.minig.imap.MailboxFolders;
import org.minig.imap.SearchQuery;
import org.minig.imap.UIDEnvelope;
import org.minig.imap.mime.MimeMessage;
import org.obm.push.bean.EmailHeaders;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.mail.imap.OpushImapFolder;

public interface PrivateMailboxService {

	MailboxFolders listAllFolders(UserDataRequest udr) throws MailException;
	
	OpushImapFolder createFolder(UserDataRequest udr, MailboxFolder folder) throws MailException;
	
	Collection<Long> uidSearch(UserDataRequest udr, String collectionName, SearchQuery sq) throws MailException;

	Collection<FastFetch> fetchFast(UserDataRequest udr, String collectionPath, Collection<Long> uids) throws MailException;

	MimeMessage fetchBodyStructure(UserDataRequest udr, String collectionPath, long uid) throws MailException;
	
	Collection<MimeMessage> fetchBodyStructure(UserDataRequest udr, String collectionPath, Collection<Long> uids) throws MailException;

	Collection<Flag> fetchFlags(UserDataRequest udr, String inbox, long uid) throws MailException;

	IMAPHeaders fetchHeaders(UserDataRequest udr, String collectionName, long uid, EmailHeaders headersToFetch) throws MailException, ImapMessageNotFoundException;

	InputStream fetchMimePartData(UserDataRequest udr, String collectionName, long uid, FetchInstructions fetchInstructions) 
			throws MailException;

	UIDEnvelope fetchEnvelope(UserDataRequest udr, String collectionPath, long uid) throws MailException;
	
	public InputStream findAttachment(UserDataRequest udr, String collectionName, Long mailUid, MimeAddress mimePartAddress) throws MailException;
}
