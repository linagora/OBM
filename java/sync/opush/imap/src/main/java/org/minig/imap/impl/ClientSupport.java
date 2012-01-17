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

package org.minig.imap.impl;

import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLException;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.minig.imap.Envelope;
import org.minig.imap.FastFetch;
import org.minig.imap.FlagsList;
import org.minig.imap.IMAPHeaders;
import org.minig.imap.InternalDate;
import org.minig.imap.NameSpaceInfo;
import org.minig.imap.QuotaInfo;
import org.minig.imap.SearchQuery;
import org.minig.imap.command.AppendCommand;
import org.minig.imap.command.CapabilityCommand;
import org.minig.imap.command.CreateCommand;
import org.minig.imap.command.DeleteCommand;
import org.minig.imap.command.ExpungeCommand;
import org.minig.imap.command.ICommand;
import org.minig.imap.command.LoginCommand;
import org.minig.imap.command.NamespaceCommand;
import org.minig.imap.command.NoopCommand;
import org.minig.imap.command.QuotaRootCommand;
import org.minig.imap.command.RenameCommand;
import org.minig.imap.command.SelectCommand;
import org.minig.imap.command.StartIdleCommand;
import org.minig.imap.command.StopIdleCommand;
import org.minig.imap.command.SubscribeCommand;
import org.minig.imap.command.UIDCopyCommand;
import org.minig.imap.command.UIDFetchBodyStructureCommand;
import org.minig.imap.command.UIDFetchEnvelopeCommand;
import org.minig.imap.command.UIDFetchFastCommand;
import org.minig.imap.command.UIDFetchFlagsCommand;
import org.minig.imap.command.UIDFetchHeadersCommand;
import org.minig.imap.command.UIDFetchInternalDateCommand;
import org.minig.imap.command.UIDFetchMessageCommand;
import org.minig.imap.command.UIDFetchPartCommand;
import org.minig.imap.command.UIDSearchCommand;
import org.minig.imap.command.UIDStoreCommand;
import org.minig.imap.command.UIDThreadCommand;
import org.minig.imap.command.UnSubscribeCommand;
import org.minig.imap.command.parser.BodyStructureParser;
import org.minig.imap.mime.MimeMessage;
import org.minig.imap.tls.MinigTLSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSupport {

	private final static Logger logger = LoggerFactory.getLogger(ClientSupport.class);
	
	private final IoHandler handler;
	private IoSession session;
	private Semaphore lock;
	private List<IMAPResponse> lastResponses;
	private TagProducer tagsProducer;
	private MinigTLSFilter sslFilter;

	public ClientSupport(IoHandler handler) {
		this.lock = new Semaphore(1);
		this.handler = handler;
		this.tagsProducer = new TagProducer();
		this.lastResponses = Collections
				.synchronizedList(new LinkedList<IMAPResponse>());
	}

	private void lock() {
		try {
			lock.acquire();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("InterruptedException !!");
		}
	}

	public boolean login(String login, String password,
			SocketConnector connector, SocketAddress address) {
		return this.login(login, password, connector, address, true);
	}

	public boolean login(String login, String password,
			SocketConnector connector, SocketAddress address,
			Boolean activateTLS) {
		if (session != null && session.isConnected()) {
			throw new IllegalStateException(
					"Already connected. Disconnect first.");
		}

		try {
			lock(); // waits for "* OK IMAP4rev1 server...
			ConnectFuture cf = connector.connect(address, handler);
			cf.join();
			if (!cf.isConnected()) {
				lock.release();
				return false;
			}
			session = cf.getSession();
			logger.debug("Connection established");
			if (activateTLS) {
				boolean tlsActivated = run(new StartTLSCommand());
				if (tlsActivated) {
					activateSSL();
				} else {
					logger.debug("TLS not supported by IMAP server.");
				}
			}
			logger.debug("Sending " + login + " login informations.");
			return run(new LoginCommand(login, password));
		} catch (Exception e) {
			logger.error("login error", e);
			return false;
		}
	}

	private void activateSSL() {
		try {
			sslFilter = new MinigTLSFilter();
			sslFilter.setUseClientMode(true);
			session.getFilterChain().addBefore(
					"org.apache.mina.common.ExecutorThreadModel", "tls",
					sslFilter);
			logger.debug("Network traffic with IMAP server will be encrypted. ");
		} catch (Throwable t) {
			logger.error("Error starting ssl", t);
		}
	}

	public void logout() {
		if (session != null) {
			if (sslFilter != null) {
				try {
					sslFilter.stopSSL(session);
				} catch (SSLException e) {
					logger.error("error stopping ssl", e);
				} catch (IllegalStateException ei) {
					logger.error("imap connection is already stop");
				}
			}
			session.close().join();
			session = null;
		}
	}

	private <T> T run(ICommand<T> cmd) {
		if (logger.isDebugEnabled()) {
			logger.debug(Integer.toHexString(hashCode()) + " CMD: "
					+ cmd.getClass().getName() + " Permits: "
					+ lock.availablePermits());
		}
		// grab lock, this one should be ok, except on first call
		// where we might wait for cyrus welcome text.
		lock();
		cmd.execute(session, tagsProducer, lock, lastResponses);
		lock(); // this one should wait until this.setResponses is called
		try {
			cmd.responseReceived(lastResponses);
		} catch (Throwable t) {
			logger.error("receiving/parsing imap response to cmd "
					+ cmd.getClass().getSimpleName(), t);
		} finally {
			lock.release();
		}

		return cmd.getReceivedData();
	}

	/**
	 * Called by MINA on message received
	 * 
	 * @param rs
	 */
	public void setResponses(List<IMAPResponse> rs) {
		if (logger.isDebugEnabled()) {
			for (IMAPResponse ir : rs) {
				logger.debug("S: " + ir.getPayload());
			}
		}

		synchronized (lastResponses) {
			this.lastResponses.clear();
			this.lastResponses.addAll(rs);
		}
		lock.release();
	}

	public boolean select(String mailbox) {
		return run(new SelectCommand(mailbox));
	}

	public Set<String> capabilities() {
		return run(new CapabilityCommand());
	}

	public boolean noop() {
		return run(new NoopCommand());
	}

	public boolean create(String mailbox) {
		return run(new CreateCommand(mailbox));
	}

	public boolean delete(String mailbox) {
		return run(new DeleteCommand(mailbox));
	}

	public boolean rename(String mailbox, String newMailbox) {
		return run(new RenameCommand(mailbox, newMailbox));
	}

	public boolean subscribe(String mailbox) {
		return run(new SubscribeCommand(mailbox));
	}

	public boolean unsubscribe(String mailbox) {
		return run(new UnSubscribeCommand(mailbox));
	}

	public long append(String mailbox, InputStream in, FlagsList fl) {
		return run(new AppendCommand(mailbox, in, fl));
	}

	public void expunge() {
		run(new ExpungeCommand());
	}

	public QuotaInfo quota(String mailbox) {
		return run(new QuotaRootCommand(mailbox));
	}

	public InputStream uidFetchMessage(long uid) {
		return run(new UIDFetchMessageCommand(uid));
	}

	public Collection<Long> uidSearch(SearchQuery sq) {
		return run(new UIDSearchCommand(sq));
	}

	public Collection<MimeMessage> uidFetchBodyStructure(Collection<Long> uid) {
		return run(new UIDFetchBodyStructureCommand(new BodyStructureParser(), uid));
	}

	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids,
			String[] headers) {
		return run(new UIDFetchHeadersCommand(uids, headers));
	}

	public Collection<Envelope> uidFetchEnvelope(Collection<Long> uids) {
		return run(new UIDFetchEnvelopeCommand(uids));
	}

	public Collection<FlagsList> uidFetchFlags(Collection<Long> uids) {
		return run(new UIDFetchFlagsCommand(uids));
	}

	public InternalDate[] uidFetchInternalDate(Collection<Long> uids) {
		return run(new UIDFetchInternalDateCommand(uids));
	}
	
	public Collection<FastFetch> uidFetchFast(Collection<Long> uids) {
		return run(new UIDFetchFastCommand(uids));
	}

	public Collection<Long> uidCopy(Collection<Long> uids, String destMailbox) {
		return run(new UIDCopyCommand(uids, destMailbox));
	}

	public boolean uidStore(Collection<Long> uids, FlagsList fl, boolean set) {
		return run(new UIDStoreCommand(uids, fl, set));
	}

	public InputStream uidFetchPart(long uid, String address) {
		return run(new UIDFetchPartCommand(uid, address));
	}

	public List<MailThread> uidThreads() {
		// UID THREAD REFERENCES UTF-8 NOT DELETED
		return run(new UIDThreadCommand());
	}

	public NameSpaceInfo namespace() {
		return run(new NamespaceCommand());
	}

	public void startIdle() {
		run(new StartIdleCommand());
	}

	public void stopIdle() {
		run(new StopIdleCommand());
	}
}
