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

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang.NotImplementedException;
import org.minig.imap.FlagsList;
import org.minig.imap.SearchQuery;
import org.obm.push.mail.ImapMessageNotFoundException;
import org.obm.push.mail.MimeAddress;

import com.sun.mail.imap.IMAPMessage;

public class MinigOpushImapFolderImpl implements OpushImapFolder {

	private boolean isSubscribed;

	public MinigOpushImapFolderImpl(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	@Override
	public boolean isSubscribed() {
		return isSubscribed;
	}

	@Override
	public InputStream getMessageInputStream(long anyMessageUID)
			throws MessagingException, ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public InputStream uidFetchPart(long anyMessageUID,
			MimeAddress anyMimePartAddress) throws MessagingException,
			ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public InputStream uidFetchPart(long uid, MimeAddress address,
			Integer truncation) throws MessagingException,
			ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public IMAPMessage fetch(long anyMessageUID, FetchProfile fetchProfile)
			throws MessagingException, ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public Message fetchEnvelope(long anyMessageUID) throws MessagingException,
			ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public IMAPMessage getMessageByUID(long uid) throws MessagingException,
			ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public Folder[] list(String string) throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public InputStream uidFetchMessage(long uid) throws MessagingException,
			ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public void appendMessageStream(StreamedLiteral streamedLiteral,
			Flags flags, Date messageDate) throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public void appendMessage(Message message) throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public long copyMessageThenGetNewUID(String folderDst, long messageUid)
			throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public void deleteMessage(Message messageToMove) throws MessagingException {
		throw new NotImplementedException();
		
	}

	@Override
	public void open(int mode) throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public Map<Long, IMAPMessage> fetchFast(Collection<Long> uids)
			throws MessagingException, ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public Map<Long, IMAPMessage> fetchBodyStructure(Collection<Long> uids)
			throws MessagingException, ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public FlagsList uidFetchFlags(long messageUid) throws MessagingException,
		ImapMessageNotFoundException {
		throw new NotImplementedException();
	}

	@Override
	public Folder[] listSubscribed(String pattern) throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public void expunge() throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public Collection<Long> uidSearch(SearchQuery query)
		throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public void noop() throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public void close() throws MessagingException {
		throw new NotImplementedException();
	}

	@Override
	public String getFullName() {
		throw new NotImplementedException();
	}

	@Override
	public boolean isOpen() throws MessagingException {
		throw new NotImplementedException();
	}
}
