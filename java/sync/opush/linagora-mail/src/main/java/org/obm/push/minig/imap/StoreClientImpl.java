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

package org.obm.push.minig.imap;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.IMAPException;
import org.obm.push.mail.bean.EmailMetadata;
import org.obm.push.mail.bean.FastFetch;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.NameSpaceInfo;
import org.obm.push.mail.bean.QuotaInfo;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.minig.imap.impl.ClientHandler;
import org.obm.push.minig.imap.impl.ClientSupport;
import org.obm.push.minig.imap.impl.IResponseCallback;
import org.obm.push.minig.imap.impl.MailThread;
import org.obm.push.minig.imap.impl.StoreClientCallback;
import org.obm.push.technicallog.bean.KindToBeLogged;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.TechnicalLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class StoreClientImpl implements StoreClient {

	@Singleton
	public static class Factory implements StoreClient.Factory {
		
		protected final EmailConfiguration emailConfiguration;

		@Inject
		protected Factory(EmailConfiguration emailConfiguration) {
			this.emailConfiguration = emailConfiguration;
		}
		
		public StoreClientImpl create(String hostname, String login, String password) {
			return new StoreClientImpl(hostname, emailConfiguration.imapPort(), login, password,
							createClientSupport());
		}

		protected ClientSupport createClientSupport() {
			IResponseCallback cb = new StoreClientCallback();
			ClientHandler handler = new ClientHandler(cb);
			ClientSupport clientSupport = new ClientSupport(handler, emailConfiguration.imapTimeoutInMilliseconds());
			cb.setClient(clientSupport);
			return clientSupport;
 		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(StoreClientImpl.class);
	
	private final String password;
	private final String login;
	private final int port;
	private final String hostname;
	@VisibleForTesting String activeMailbox;

	private final ClientSupport clientSupport;

	protected StoreClientImpl(String hostname, int port, String login, String password,
			ClientSupport clientSupport) {
		this.hostname = hostname;
		this.port = port;
		this.login = login;
		this.password = password;
		this.clientSupport = clientSupport;
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onStartOfMethod=true, resourceType=ResourceType.IMAP_CONNECTION)
	public void login(Boolean activateTLS) throws IMAPException {
		logger.debug("login attempt to {}:{} for {}", hostname, port, login);
		SocketAddress sa = new InetSocketAddress(hostname, port);
		clientSupport.login(login, password, sa, activateTLS);
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onEndOfMethod=true, resourceType=ResourceType.IMAP_CONNECTION)
	public void logout() {
		if (logger.isDebugEnabled()) {
			logger.debug("logout attempt for " + login);
		}
		clientSupport.logout();
	}

	@Override
	public boolean select(String mailbox) {
		if (hasToSelectMailbox(mailbox) && selectMailboxImpl(mailbox)) {
			activeMailbox = mailbox;
			return true;
		}
		return false;
	}

	@VisibleForTesting boolean hasToSelectMailbox(String mailbox) {
		return givenMailboxIsSelectable(mailbox) && givenMailboxIsDifferentOfActive(mailbox);
	}

	private boolean givenMailboxIsSelectable(String mailbox) {
		return !Strings.isNullOrEmpty(mailbox);
	}

	private boolean givenMailboxIsDifferentOfActive(String mailbox) {
		return hasNoActiveMailbox() || !activeMailbox.equalsIgnoreCase(mailbox);
	}

	private boolean hasNoActiveMailbox() {
		return Strings.isNullOrEmpty(activeMailbox);
	}

	protected boolean selectMailboxImpl(String mailbox) {
		return clientSupport.select(findMailboxNameWithServerCase(mailbox));
	}

	@Override
	public boolean create(String mailbox) {
		return clientSupport.create(mailbox);
	}

	@Override
	public boolean subscribe(String mailbox) {
		return clientSupport.subscribe(mailbox);
	}

	@Override
	public boolean unsubscribe(String mailbox) {
		return clientSupport.unsubscribe(mailbox);
	}

	@Override
	public boolean delete(String mailbox) {
		return clientSupport.delete(mailbox);
	}

	@Override
	public boolean rename(String mailbox, String newMailbox) {
		return clientSupport.rename(mailbox, newMailbox);
	}

	@Override
	public Set<String> capabilities() {
		return clientSupport.capabilities();
	}

	@Override
	public boolean noop() {
		return clientSupport.noop();
	}
	
	@Override
	public  ListResult listSubscribed() {
		return clientSupport.listSubscribed();
	}
	
	@Override
	public  ListResult listAll() {
		return clientSupport.listAll();
	}

	@Override
	public boolean append(String mailbox, InputStream in, FlagsList fl) {
		return clientSupport.append(findMailboxNameWithServerCase(mailbox), in, fl);
	}

	@Override
	public void expunge() {
		clientSupport.expunge();
	}

	@Override
	public QuotaInfo quota(String mailbox) {
		return clientSupport.quota(findMailboxNameWithServerCase(mailbox));
	}

	@Override
	public InputStream uidFetchMessage(long uid) {
		return clientSupport.uidFetchMessage(uid);
	}

	@Override
	public MessageSet uidSearch(SearchQuery sq) {
		return clientSupport.uidSearch(sq);
	}

	@Override
	public Collection<MimeMessage> uidFetchBodyStructure(MessageSet messages) {
		return clientSupport.uidFetchBodyStructure(messages);
	}

	@Override
	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids, String[] headers) {
		return clientSupport.uidFetchHeaders(uids, headers);
	}

	@Override
	public Collection<UIDEnvelope> uidFetchEnvelope(MessageSet messages) {
		return clientSupport.uidFetchEnvelope(messages);
	}

	@Override
	public Map<Long, FlagsList> uidFetchFlags(MessageSet messages) {
		return clientSupport.uidFetchFlags(messages);
	}
	
	@Override
	public Collection<InternalDate> uidFetchInternalDate(Collection<Long> uids) {
		return clientSupport.uidFetchInternalDate(uids);
	}
	
	@Override
	public Collection<FastFetch> uidFetchFast(MessageSet messages) {
		return clientSupport.uidFetchFast(messages);
	}

	@Override
	public MessageSet uidCopy(MessageSet messages, String destMailbox) {
		return clientSupport.uidCopy(messages, findMailboxNameWithServerCase(destMailbox));
	}

	@Override
	public boolean uidStore(MessageSet messages, FlagsList fl, boolean set) {
		return clientSupport.uidStore(messages, fl, set);
	}

	@Override
	public InputStream uidFetchPart(long uid, String address, long truncation) {
		return clientSupport.uidFetchPart(uid, address, truncation);
	}
	
	@Override
	public InputStream uidFetchPart(long uid, String address) {
		return clientSupport.uidFetchPart(uid, address);
	}
	
	@Override
	public EmailMetadata uidFetchEmailMetadata(long uid) {
		return clientSupport.uidFetchEmailMetadata(uid);
	}

	@Override
	public List<MailThread> uidThreads() {
		return clientSupport.uidThreads();
	}

	@Override
	public NameSpaceInfo namespace() {
		return clientSupport.namespace();
	}

	@Override
	public boolean isConnected() {
		return clientSupport.isConnected();
	}
	
	@Override
	public long uidNext(String mailbox) {
		return clientSupport.uidNext(findMailboxNameWithServerCase(mailbox));
	}
	
	@Override
	public long uidValidity(String mailbox) {
		return clientSupport.uidValidity(findMailboxNameWithServerCase(mailbox));
	}
	
	@Override
	public String findMailboxNameWithServerCase(String mailboxName) {
		if (isINBOXSpecificCase(mailboxName)) {
			return EmailConfiguration.IMAP_INBOX_NAME;
		}
		
		ListResult listResult = listAll();
		for (ListInfo result: listResult) {
			if (result.getName().toLowerCase().equals(mailboxName.toLowerCase())) {
				return result.getName();
			}
		}
		throw new CollectionNotFoundException("Cannot find IMAP folder for collection [ " + mailboxName + " ]");
	}

	private boolean isINBOXSpecificCase(String boxName) {
		return boxName.toLowerCase().equals(EmailConfiguration.IMAP_INBOX_NAME.toLowerCase());
	}
}
