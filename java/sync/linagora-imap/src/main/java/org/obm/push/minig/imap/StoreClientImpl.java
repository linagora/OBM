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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.mina.transport.socket.SocketConnector;
import org.obm.breakdownduration.bean.Watch;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.EmailConfiguration.MailboxNameCheckPolicy;
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
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.NameSpaceInfo;
import org.obm.push.mail.bean.QuotaInfo;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.bean.UIDEnvelope;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.minig.imap.impl.ClientHandler;
import org.obm.push.minig.imap.impl.ClientSupport;
import org.obm.push.minig.imap.impl.IResponseCallback;
import org.obm.push.minig.imap.impl.MailThread;
import org.obm.push.minig.imap.impl.SessionFactoryImpl;
import org.obm.push.minig.imap.impl.StoreClientCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Watch("IMAP")
public class StoreClientImpl implements StoreClient {

	@Singleton
	public static class Factory implements StoreClient.Factory {
		
		protected final EmailConfiguration emailConfiguration;
		private final Provider<SocketConnector> socketConnectorProvider;

		@Inject
		protected Factory(EmailConfiguration emailConfiguration, Provider<SocketConnector> socketConnectorProvider) {
			this.emailConfiguration = emailConfiguration;
			this.socketConnectorProvider = socketConnectorProvider;
		}
		
		public StoreClientImpl create(String hostname, String login, char[] password) {
			return new StoreClientImpl(hostname, emailConfiguration.imapPort(), login, password, emailConfiguration.mailboxNameCheckPolicy(),
							createClientSupport());
		}

		protected ClientSupport createClientSupport() {
			IResponseCallback cb = new StoreClientCallback();
			ClientHandler handler = new ClientHandler(cb);
			SessionFactoryImpl sessionFactory = new SessionFactoryImpl(socketConnectorProvider, handler, emailConfiguration.imapTimeoutInMilliseconds());
			ClientSupport clientSupport = new ClientSupport(sessionFactory, emailConfiguration.imapTimeoutInMilliseconds());
			cb.setClient(clientSupport);
			return clientSupport;
 		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(StoreClientImpl.class);
	
	private final char[] password;
	private final String login;
	private final int port;
	private final String hostname;
	private final MailboxNameCheckPolicy mailboxNameCheckPolicy;
	@VisibleForTesting String activeMailbox;

	private final ClientSupport clientSupport;

	protected StoreClientImpl(String hostname, int port, String login, char[] password, MailboxNameCheckPolicy mailboxNameCheckPolicy,
			ClientSupport clientSupport) {
		this.hostname = hostname;
		this.port = port;
		this.login = login;
		this.password = password;
		this.clientSupport = clientSupport;
		this.mailboxNameCheckPolicy = mailboxNameCheckPolicy;
	}

	@Override
	public void close() throws Exception {
		logout();
	}

	@Override
	public void login(Boolean activateTLS) throws IMAPException, ImapTimeoutException {
		logger.debug("login attempt to {}:{} for {}", hostname, port, login);
		SocketAddress sa = new InetSocketAddress(hostname, port);
		clientSupport.login(login, password, sa, activateTLS);
	}

	@Override
	public void logout() throws ImapTimeoutException {
		if (logger.isDebugEnabled()) {
			logger.debug("logout attempt for " + login);
		}
		clientSupport.logout();
	}

	@Override
	public boolean select(String mailbox) throws MailboxNotFoundException, ImapTimeoutException {
		if (Strings.isNullOrEmpty(mailbox)) {
			return false;
		}
		
		if (!givenMailboxIsDifferentOfActive(mailbox)) {
			return true;
		} else {
			if (selectMailboxImpl(mailbox)) {
				activeMailbox = mailbox;
				return true;
			}
		}
		return false;
	}

	private boolean givenMailboxIsDifferentOfActive(String mailbox) {
		return hasNoActiveMailbox() || !activeMailbox.equalsIgnoreCase(mailbox);
	}

	private boolean hasNoActiveMailbox() {
		return Strings.isNullOrEmpty(activeMailbox);
	}

	protected boolean selectMailboxImpl(String mailbox) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.select(findMailboxNameWithServerCase(mailbox));
	}

	@Override
	public boolean create(String mailbox) throws ImapTimeoutException {
		return clientSupport.create(mailbox);
	}
	
	@Override
	public boolean create(String mailbox, String partition) throws ImapTimeoutException {
		return clientSupport.create(mailbox, partition);
	}

	@Override
	public boolean subscribe(String mailbox) throws ImapTimeoutException {
		return clientSupport.subscribe(mailbox);
	}

	@Override
	public boolean unsubscribe(String mailbox) throws ImapTimeoutException {
		return clientSupport.unsubscribe(mailbox);
	}

	@Override
	public boolean delete(String mailbox) throws ImapTimeoutException {
		return clientSupport.delete(mailbox);
	}

	@Override
	public boolean rename(String mailbox, String newMailbox) throws ImapTimeoutException {
		return clientSupport.rename(mailbox, newMailbox);
	}

	@Override
	public Set<String> capabilities() throws ImapTimeoutException {
		return clientSupport.capabilities();
	}

	@Override
	public boolean noop() throws ImapTimeoutException {
		return clientSupport.noop();
	}
	
	@Override
	public  ListResult listSubscribed() throws ImapTimeoutException {
		return clientSupport.listSubscribed();
	}
	
	@Override
	public  ListResult listSubscribed(String referenceName, String mailboxName) throws ImapTimeoutException {
		return clientSupport.listSubscribed(referenceName, mailboxName);
	}
	
	@Override
	public  ListResult listAll() throws ImapTimeoutException {
		return clientSupport.listAll();
	}
	
	@Override
	public  ListResult listAll(String referenceName, String mailboxName) throws ImapTimeoutException {
		return clientSupport.listAll(referenceName, mailboxName);
	}

	@Override
	public boolean append(String mailbox, Reader message, FlagsList fl) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.append(findMailboxNameWithServerCase(mailbox), message, fl);
	}

	@Override
	public void expunge() throws ImapTimeoutException {
		clientSupport.expunge();
	}

	@Override
	public QuotaInfo quota(String mailbox) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.quota(findMailboxNameWithServerCase(mailbox));
	}
	
	@Override
	public boolean removeQuota(String mailbox) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.removeQuota(findMailboxNameWithServerCase(mailbox));
	}

	@Override
	public boolean setQuota(String mailbox, long quotaInKb) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.setQuota(findMailboxNameWithServerCase(mailbox), quotaInKb);
	}

	@Override
	public InputStream uidFetchMessage(long uid) throws ImapTimeoutException {
		return clientSupport.uidFetchMessage(uid);
	}
	
	@Override
	public InputStream uidFetchMessage(long uid, long truncation) throws ImapTimeoutException {
		return clientSupport.uidFetchMessage(uid, truncation);
	}

	@Override
	public MessageSet uidSearch(SearchQuery sq) throws ImapTimeoutException {
		return clientSupport.uidSearch(sq);
	}

	@Override
	public Collection<MimeMessage> uidFetchBodyStructure(MessageSet messages) throws ImapTimeoutException {
		return clientSupport.uidFetchBodyStructure(messages);
	}

	@Override
	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids, String[] headers) throws ImapTimeoutException {
		return clientSupport.uidFetchHeaders(uids, headers);
	}

	@Override
	public Collection<UIDEnvelope> uidFetchEnvelope(MessageSet messages) throws ImapTimeoutException {
		return clientSupport.uidFetchEnvelope(messages);
	}

	@Override
	public Map<Long, FlagsList> uidFetchFlags(MessageSet messages) throws ImapTimeoutException {
		return clientSupport.uidFetchFlags(messages);
	}
	
	@Override
	public List<InternalDate> uidFetchInternalDate(MessageSet messages) throws ImapTimeoutException {
		return clientSupport.uidFetchInternalDate(messages);
	}
	
	@Override
	public Collection<FastFetch> uidFetchFast(MessageSet messages) throws ImapTimeoutException {
		return clientSupport.uidFetchFast(messages);
	}

	@Override
	public MessageSet uidCopy(MessageSet messages, String destMailbox) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.uidCopy(messages, findMailboxNameWithServerCase(destMailbox));
	}

	@Override
	public boolean uidStore(MessageSet messages, FlagsList fl, boolean set) throws ImapTimeoutException {
		return clientSupport.uidStore(messages, fl, set);
	}

	@Override
	public InputStream uidFetchPart(long uid, String address, long truncation) throws ImapTimeoutException {
		return clientSupport.uidFetchPart(uid, address, truncation);
	}
	
	@Override
	public InputStream uidFetchPart(long uid, String address) throws ImapTimeoutException {
		return clientSupport.uidFetchPart(uid, address);
	}
	
	@Override
	public EmailMetadata uidFetchEmailMetadata(long uid) throws ImapTimeoutException {
		return clientSupport.uidFetchEmailMetadata(uid);
	}

	@Override
	public List<MailThread> uidThreads() throws ImapTimeoutException {
		return clientSupport.uidThreads();
	}

	@Override
	public NameSpaceInfo namespace() throws ImapTimeoutException {
		return clientSupport.namespace();
	}

	@Override
	public boolean isConnected() {
		return clientSupport.isConnected();
	}
	
	@Override
	public long uidNext(String mailbox) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.uidNext(findMailboxNameWithServerCase(mailbox));
	}
	
	@Override
	public long uidValidity(String mailbox) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.uidValidity(findMailboxNameWithServerCase(mailbox));
	}

	@Override
	public String findMailboxNameWithServerCase(String mailboxName) throws MailboxNotFoundException, ImapTimeoutException {
		if (isINBOXSpecificCase(mailboxName)) {
			return EmailConfiguration.IMAP_INBOX_NAME;
		}

		switch (mailboxNameCheckPolicy) {
			case NEVER:
				return mailboxName;
			case ALWAYS:
				ListResult listResult = listAll();

				for (ListInfo result: listResult) {
					if (result.getName().toLowerCase().equals(mailboxName.toLowerCase())) {
						return result.getName();
					}
				}

				throw new MailboxNotFoundException("Cannot find IMAP folder for collection [ " + mailboxName + " ]");
		}

		return mailboxName;
	}

	private boolean isINBOXSpecificCase(String boxName) {
		return boxName.toLowerCase().equals(EmailConfiguration.IMAP_INBOX_NAME.toLowerCase());
	}

	@Override
	public boolean setAcl(String mailbox, String identifier, String accessRights)  throws ImapTimeoutException {
		return clientSupport.setAcl(mailbox, identifier, accessRights);
	}

	@Override
	public Set<Acl> getAcl(String mailbox)  throws ImapTimeoutException {
		return clientSupport.getAcl(mailbox);
	}

	@Override
	public boolean setAnnotation(String mailbox, AnnotationEntry annotationEntry, AttributeValue attributeValue) throws MailboxNotFoundException, ImapTimeoutException {
		return clientSupport.setAnnotation(mailbox, annotationEntry, attributeValue);
	}
}
