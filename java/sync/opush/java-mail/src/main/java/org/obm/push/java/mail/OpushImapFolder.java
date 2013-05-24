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

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.mime.MimeAddress;

import com.sun.mail.imap.IMAPMessage;

public interface OpushImapFolder {

	boolean isSubscribed();

	InputStream getMessageInputStream(long anyMessageUID) throws MessagingException, ImapMessageNotFoundException;

	InputStream uidFetchPart(long anyMessageUID, MimeAddress anyMimePartAddress) throws MessagingException, ImapMessageNotFoundException;

	InputStream uidFetchPart(long uid, MimeAddress address, Integer truncation) throws MessagingException, ImapMessageNotFoundException;

	Map<Long, IMAPMessage> fetch(MessageSet messages, FetchProfile fetchProfile) throws MessagingException, ImapMessageNotFoundException;

	Map<Long, IMAPMessage> fetchEnvelope(MessageSet messages) throws MessagingException, ImapMessageNotFoundException;

	Map<Long, FlagsList> uidFetchFlags(MessageSet messages) throws MessagingException, ImapMessageNotFoundException;
	
	IMAPMessage getMessageByUID(long uid) throws MessagingException, ImapMessageNotFoundException;

	Folder[] list(String string) throws MessagingException;

	Folder[] listSubscribed(String pattern) throws MessagingException;
	
	InputStream uidFetchMessage(long uid) throws MessagingException, ImapMessageNotFoundException;

	void appendMessageStream(StreamedLiteral streamedLiteral, Flags flags, Date messageDate) throws MessagingException;

	void appendMessage(Message message) throws MessagingException;

	long copyMessageThenGetNewUID(String folderDst, long messageUid) throws MessagingException;

	void deleteMessage(Message messageToMove) throws MessagingException;

	void expunge() throws MessagingException;
	
	void open(int mode) throws MessagingException;

	Map<Long, IMAPMessage> fetchFast(MessageSet messages) throws MessagingException;

	MessageSet uidSearch(SearchQuery query) throws MessagingException;
	
	Map<Long, IMAPMessage> fetchBodyStructure(MessageSet messages) throws MessagingException, ImapMessageNotFoundException;

	Map<Long, IMAPMessage> fetchEmailView(MessageSet messages) throws MessagingException, ImapMessageNotFoundException;
	
	void noop() throws MessagingException;
	
	void close() throws MessagingException;

	String getFullName();
	
	boolean isOpen() throws MessagingException;
	
	long uidNext(String mailbox) throws MessagingException;
	
	long uidValidity(String mailbox) throws MessagingException;

}
