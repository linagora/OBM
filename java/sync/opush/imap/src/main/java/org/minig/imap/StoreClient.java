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

package org.minig.imap;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.mina.transport.socket.nio.SocketConnector;
import org.minig.imap.impl.ClientHandler;
import org.minig.imap.impl.ClientSupport;
import org.minig.imap.impl.IResponseCallback;
import org.minig.imap.impl.MailThread;
import org.minig.imap.impl.StoreClientCallback;
import org.minig.imap.mime.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IMAP client entry point
 */
public class StoreClient {

	private static final Logger logger = LoggerFactory
			.getLogger(StoreClient.class);
	
	private String password;
	private String login;
	private int port;
	private String hostname;

	private ClientHandler handler;
	private ClientSupport cs;
	private SocketConnector connector;

	public StoreClient(String hostname, int port, String login, String password) {
		this.hostname = hostname;
		this.port = port;
		this.login = login;
		this.password = password;

		IResponseCallback cb = new StoreClientCallback();
		handler = new ClientHandler(cb);
		cs = new ClientSupport(handler);
		cb.setClient(cs);
		connector = new SocketConnector();
	}

	/**
	 * Logs into the IMAP store
	 */
	public void login(Boolean activateTLS) throws IMAPException {
		logger.debug("login attempt to {}:{} for {}", new Object[]{hostname, port, login});
		SocketAddress sa = new InetSocketAddress(hostname, port);
		cs.login(login, password, connector, sa, activateTLS);
	}

	/**
	 * Logs out & disconnect from the IMAP server. The underlying network
	 * connection is released.
	 * 
	 * @throws IMAPException
	 */
	public void logout() {
		if (logger.isDebugEnabled()) {
			logger.debug("logout attempt for " + login);
		}
		cs.logout();
	}

	/**
	 * Opens the given IMAP folder. Only one folder quand be active at a time.
	 * 
	 * @param mailbox
	 *            utf8 mailbox name.
	 * @throws IMAPException
	 */
	public boolean select(String mailbox) {
		return cs.select(mailbox);
	}

	public boolean create(String mailbox) {
		return cs.create(mailbox);
	}

	public boolean subscribe(String mailbox) {
		return cs.subscribe(mailbox);
	}

	public boolean unsubscribe(String mailbox) {
		return cs.unsubscribe(mailbox);
	}

	public boolean delete(String mailbox) {
		return cs.delete(mailbox);
	}

	public boolean rename(String mailbox, String newMailbox) {
		return cs.rename(mailbox, newMailbox);
	}

	/**
	 * Issues the CAPABILITY command to the IMAP server
	 * 
	 * @return
	 */
	public Set<String> capabilities() {
		return cs.capabilities();
	}

	public boolean noop() {
		return cs.noop();
	}

	public long append(String mailbox, InputStream in, FlagsList fl) {
		return cs.append(mailbox, in, fl);
	}

	public void expunge() {
		cs.expunge();
	}

	public QuotaInfo quota(String mailbox) {
		return cs.quota(mailbox);
	}

	public InputStream uidFetchMessage(long uid) {
		return cs.uidFetchMessage(uid);
	}

	public Collection<Long> uidSearch(SearchQuery sq) {
		return cs.uidSearch(sq);
	}

	public Collection<MimeMessage> uidFetchBodyStructure(Collection<Long> uids) {
		return cs.uidFetchBodyStructure(uids);
	}

	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids, String[] headers) {
		return cs.uidFetchHeaders(uids, headers);
	}

	public Collection<Envelope> uidFetchEnvelope(Collection<Long> uids) {
		return cs.uidFetchEnvelope(uids);
	}

	public Collection<FlagsList> uidFetchFlags(Collection<Long> uids) {
		return cs.uidFetchFlags(uids);
	}
	
	public InternalDate[] uidFetchInternalDate(Collection<Long> uids) {
		return cs.uidFetchInternalDate(uids);
	}
	
	public Collection<FastFetch> uidFetchFast(Collection<Long> uids) {
		return cs.uidFetchFast(uids);
	}

	public Collection<Long> uidCopy(Collection<Long> uids, String destMailbox) {
		return cs.uidCopy(uids, destMailbox);
	}

	public boolean uidStore(Collection<Long> uids, FlagsList fl, boolean set) {
		return cs.uidStore(uids, fl, set);
	}

	public InputStream uidFetchPart(long uid, String address) {
		return cs.uidFetchPart(uid, address);
	}

	public List<MailThread> uidThreads() {
		return cs.uidThreads();
	}

	public NameSpaceInfo namespace() {
		return cs.namespace();
	}

}
