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

import org.apache.mina.transport.socket.nio.SocketConnector;
import org.obm.annotations.technicallogging.KindToBeLogged;
import org.obm.annotations.technicallogging.ResourceType;
import org.obm.annotations.technicallogging.TechnicalLogging;
import org.obm.configuration.EmailConfiguration;
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
import org.obm.push.minig.imap.impl.ClientHandler;
import org.obm.push.minig.imap.impl.ClientSupport;
import org.obm.push.minig.imap.impl.IResponseCallback;
import org.obm.push.minig.imap.impl.MailThread;
import org.obm.push.minig.imap.impl.StoreClientCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreClientImpl implements StoreClient {

	private static final Logger logger = LoggerFactory
			.getLogger(StoreClientImpl.class);
	
	private String password;
	private String login;
	private int port;
	private String hostname;

	private ClientHandler handler;
	private ClientSupport cs;
	private SocketConnector connector;

	public StoreClientImpl(String hostname, EmailConfiguration emailConfiguration, String login, String password) {
		this.hostname = hostname;
		this.port = emailConfiguration.imapPort();
		this.login = login;
		this.password = password;

		IResponseCallback cb = new StoreClientCallback();
		handler = new ClientHandler(cb);
		cs = new ClientSupport(handler, emailConfiguration.imapTimeoutInMilliseconds());
		cb.setClient(cs);
		connector = new SocketConnector();
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onStartOfMethod=true, resourceType=ResourceType.IMAP_CONNECTION)
	public void login(Boolean activateTLS) throws IMAPException {
		logger.debug("login attempt to {}:{} for {}", new Object[]{hostname, port, login});
		SocketAddress sa = new InetSocketAddress(hostname, port);
		cs.login(login, password, connector, sa, activateTLS);
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onEndOfMethod=true, resourceType=ResourceType.IMAP_CONNECTION)
	public void logout() {
		if (logger.isDebugEnabled()) {
			logger.debug("logout attempt for " + login);
		}
		cs.logout();
	}

	@Override
	public boolean select(String mailbox) {
		return cs.select(mailbox);
	}

	@Override
	public boolean create(String mailbox) {
		return cs.create(mailbox);
	}

	@Override
	public boolean subscribe(String mailbox) {
		return cs.subscribe(mailbox);
	}

	@Override
	public boolean unsubscribe(String mailbox) {
		return cs.unsubscribe(mailbox);
	}

	@Override
	public boolean delete(String mailbox) {
		return cs.delete(mailbox);
	}

	@Override
	public boolean rename(String mailbox, String newMailbox) {
		return cs.rename(mailbox, newMailbox);
	}

	@Override
	public Set<String> capabilities() {
		return cs.capabilities();
	}

	@Override
	public boolean noop() {
		return cs.noop();
	}
	
	@Override
	public  ListResult listSubscribed() {
		return cs.listSubscribed();
	}
	
	@Override
	public  ListResult listAll() {
		return cs.listAll();
	}

	@Override
	public boolean append(String mailbox, InputStream in, FlagsList fl) {
		return cs.append(mailbox, in, fl);
	}

	@Override
	public void expunge() {
		cs.expunge();
	}

	@Override
	public QuotaInfo quota(String mailbox) {
		return cs.quota(mailbox);
	}

	@Override
	public InputStream uidFetchMessage(long uid) {
		return cs.uidFetchMessage(uid);
	}

	@Override
	public MessageSet uidSearch(SearchQuery sq) {
		return cs.uidSearch(sq);
	}

	@Override
	public Collection<MimeMessage> uidFetchBodyStructure(MessageSet messages) {
		return cs.uidFetchBodyStructure(messages);
	}

	@Override
	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids, String[] headers) {
		return cs.uidFetchHeaders(uids, headers);
	}

	@Override
	public Collection<UIDEnvelope> uidFetchEnvelope(MessageSet messages) {
		return cs.uidFetchEnvelope(messages);
	}

	@Override
	public Map<Long, FlagsList> uidFetchFlags(MessageSet messages) {
		return cs.uidFetchFlags(messages);
	}
	
	@Override
	public Collection<InternalDate> uidFetchInternalDate(Collection<Long> uids) {
		return cs.uidFetchInternalDate(uids);
	}
	
	@Override
	public Collection<FastFetch> uidFetchFast(MessageSet messages) {
		return cs.uidFetchFast(messages);
	}

	@Override
	public MessageSet uidCopy(MessageSet messages, String destMailbox) {
		return cs.uidCopy(messages, destMailbox);
	}

	@Override
	public boolean uidStore(MessageSet messages, FlagsList fl, boolean set) {
		return cs.uidStore(messages, fl, set);
	}

	@Override
	public InputStream uidFetchPart(long uid, String address, long truncation) {
		return cs.uidFetchPart(uid, address, truncation);
	}
	
	@Override
	public InputStream uidFetchPart(long uid, String address) {
		return cs.uidFetchPart(uid, address);
	}

	@Override
	public List<MailThread> uidThreads() {
		return cs.uidThreads();
	}

	@Override
	public NameSpaceInfo namespace() {
		return cs.namespace();
	}

	@Override
	public boolean isConnected() {
		return cs.isConnected();
	}
	
	@Override
	public long uidNext(String mailbox) {
		return cs.uidNext(mailbox);
	}
	
	@Override
	public long uidValidity(String mailbox) {
		return cs.uidValidity(mailbox);
	}
}
