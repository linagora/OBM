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
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.mail.bean.AnnotationEntry;
import org.obm.push.mail.bean.AttributeValue;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.FastFetch;
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
public interface StoreClient extends AutoCloseable {

	interface Factory {
		StoreClient create(String hostname, String login, char[] password);
	}
	
	/**
	 * Logs into the IMAP store
	 */
	void login(Boolean activateTLS) throws IMAPException, ImapTimeoutException;

	/**
	 * Logs out & disconnect from the IMAP server. The underlying network
	 * connection is released.
	 * @throws ImapTimeoutException 
	 * @throws IMAPException
	 */
	void logout() throws ImapTimeoutException;

	/**
	 * Opens the given IMAP folder. Only one folder quand be active at a time.
	 * 
	 * @param mailbox
	 *            utf8 mailbox name.
	 * @throws ImapTimeoutException 
	 * @throws IMAPException
	 */
	boolean select(String mailbox) throws MailboxNotFoundException, ImapTimeoutException;

	boolean create(String mailbox) throws ImapTimeoutException;
	
	boolean create(String mailbox, String partition) throws ImapTimeoutException;

	boolean subscribe(String mailbox) throws ImapTimeoutException;

	boolean unsubscribe(String mailbox) throws ImapTimeoutException;

	boolean delete(String mailbox) throws ImapTimeoutException;

	boolean rename(String mailbox, String newMailbox) throws ImapTimeoutException;
	
	boolean setAcl(String mailbox, String identifier, String accessRights) throws ImapTimeoutException;

	Set<Acl> getAcl(String mailbox) throws ImapTimeoutException;
	
	/**
	 * Issues the CAPABILITY command to the IMAP server
	 * 
	 * @return
	 */
	Set<String> capabilities() throws ImapTimeoutException;

	boolean noop() throws ImapTimeoutException;

	NameSpaceInfo namespace() throws ImapTimeoutException;
	
	boolean isConnected() throws ImapTimeoutException;
	
	long uidNext(String mailbox) throws MailboxNotFoundException, ImapTimeoutException;
	
	long uidValidity(String mailbox) throws MailboxNotFoundException, ImapTimeoutException;

	void expunge() throws ImapTimeoutException;
	
	ListResult listSubscribed() throws ImapTimeoutException;
	ListResult listSubscribed(String referenceName, String mailboxName) throws ImapTimeoutException;
	
	ListResult listAll() throws ImapTimeoutException;
	ListResult listAll(String referenceName, String mailboxName) throws ImapTimeoutException;

	boolean append(String mailbox, Reader message, FlagsList fl) throws MailboxNotFoundException, ImapTimeoutException;
	
	QuotaInfo quota(String mailbox) throws MailboxNotFoundException, ImapTimeoutException;

	boolean removeQuota(String mailbox) throws MailboxNotFoundException, ImapTimeoutException;
	
	boolean setQuota(String mailbox, long quotaInKo) throws MailboxNotFoundException, ImapTimeoutException;

	InputStream uidFetchMessage(long uid) throws ImapTimeoutException;
	
	InputStream uidFetchMessage(long uid, long truncation) throws ImapTimeoutException;

	MessageSet uidSearch(SearchQuery sq) throws ImapTimeoutException;

	Collection<MimeMessage> uidFetchBodyStructure(MessageSet messages) throws ImapTimeoutException;

	Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids, String[] headers) throws ImapTimeoutException;

	Collection<UIDEnvelope> uidFetchEnvelope(MessageSet messages) throws ImapTimeoutException;

	Map<Long, FlagsList> uidFetchFlags(MessageSet messages) throws ImapTimeoutException;
	
	List<InternalDate> uidFetchInternalDate(MessageSet messages) throws ImapTimeoutException;
	
	Collection<FastFetch> uidFetchFast(MessageSet messages) throws ImapTimeoutException;

	MessageSet uidCopy(MessageSet messages, String destMailbox) throws MailboxNotFoundException, ImapTimeoutException;

	boolean uidStore(MessageSet messages, FlagsList fl, boolean set) throws ImapTimeoutException;

	InputStream uidFetchPart(long uid, String address, long truncation) throws ImapTimeoutException;
	
	InputStream uidFetchPart(long uid, String address) throws ImapTimeoutException;

	EmailMetadata uidFetchEmailMetadata(long uid) throws ImapTimeoutException;

	List<MailThread> uidThreads() throws ImapTimeoutException;

	String findMailboxNameWithServerCase(String mailbox) throws MailboxNotFoundException, ImapTimeoutException;
	
	boolean setAnnotation(String mailbox, AnnotationEntry annotationEntry, AttributeValue attributeValue) throws MailboxNotFoundException, ImapTimeoutException;

}
