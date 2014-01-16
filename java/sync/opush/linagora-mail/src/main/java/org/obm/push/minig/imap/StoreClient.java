/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

package org.obm.push.minig.imap;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.mail.bean.FastFetch;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.NameSpaceInfo;
import org.obm.push.mail.bean.QuotaInfo;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.minig.imap.impl.MailThread;

/**
 * IMAP client entry point
 */
public interface StoreClient {

	interface Factory {
		StoreClient create(String hostname, String login, String password);
	}
	
	/**
	 * Logs into the IMAP store
	 */
	void login(Boolean activateTLS) throws IMAPException;

	/**
	 * Logs out & disconnect from the IMAP server. The underlying network
	 * connection is released.
	 * 
	 * @throws IMAPException
	 */
	void logout();

	/**
	 * Opens the given IMAP folder. Only one folder quand be active at a time.
	 * 
	 * @param mailbox
	 *            utf8 mailbox name.
	 * @throws IMAPException
	 */
	boolean select(String mailbox);

	boolean create(String mailbox);

	boolean subscribe(String mailbox);

	boolean unsubscribe(String mailbox);

	boolean delete(String mailbox);

	boolean rename(String mailbox, String newMailbox);

	/**
	 * Issues the CAPABILITY command to the IMAP server
	 * 
	 * @return
	 */
	Set<String> capabilities();

	boolean noop();

	NameSpaceInfo namespace();
	
	boolean isConnected();
	
	long uidNext(String mailbox);
	
	long uidValidity(String mailbox);

	void expunge();
	
	ListResult listSubscribed();
	
	ListResult listAll();

	boolean append(String mailbox, InputStream in, FlagsList fl);
	
	QuotaInfo quota(String mailbox);

	InputStream uidFetchMessage(long uid);

	MessageSet uidSearch(SearchQuery sq);

	Collection<MimeMessage> uidFetchBodyStructure(MessageSet messages);

	Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids, String[] headers);

	Collection<UIDEnvelope> uidFetchEnvelope(MessageSet messages);

	Map<Long, FlagsList> uidFetchFlags(MessageSet messages);
	
	Collection<InternalDate> uidFetchInternalDate(Collection<Long> uids);
	
	Collection<FastFetch> uidFetchFast(MessageSet messages);

	MessageSet uidCopy(MessageSet messages, String destMailbox);

	boolean uidStore(MessageSet messages, FlagsList fl, boolean set);

	InputStream uidFetchPart(long uid, String address, long truncation);
	
	InputStream uidFetchPart(long uid, String address);

	EmailMetadata uidFetchEmailMetadata(long uid);

	List<MailThread> uidThreads();

	String findMailboxNameWithServerCase(String mailbox);

}
