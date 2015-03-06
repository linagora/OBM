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

package org.obm.push.minig.imap.impl;

import java.io.InputStream;
import java.io.Reader;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.obm.push.exception.ImapTimeoutException;
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
import org.obm.push.minig.imap.command.AppendCommand;
import org.obm.push.minig.imap.command.CapabilityCommand;
import org.obm.push.minig.imap.command.CreateCommand;
import org.obm.push.minig.imap.command.DeleteCommand;
import org.obm.push.minig.imap.command.ExpungeCommand;
import org.obm.push.minig.imap.command.GetACLCommand;
import org.obm.push.minig.imap.command.ICommand;
import org.obm.push.minig.imap.command.ListCommand;
import org.obm.push.minig.imap.command.LoginCommand;
import org.obm.push.minig.imap.command.LsubCommand;
import org.obm.push.minig.imap.command.NamespaceCommand;
import org.obm.push.minig.imap.command.NoopCommand;
import org.obm.push.minig.imap.command.QuotaRootCommand;
import org.obm.push.minig.imap.command.RenameCommand;
import org.obm.push.minig.imap.command.SelectCommand;
import org.obm.push.minig.imap.command.SetACLCommand;
import org.obm.push.minig.imap.command.SetAnnotationCommand;
import org.obm.push.minig.imap.command.SetQuotaCommand;
import org.obm.push.minig.imap.command.StartIdleCommand;
import org.obm.push.minig.imap.command.StopIdleCommand;
import org.obm.push.minig.imap.command.SubscribeCommand;
import org.obm.push.minig.imap.command.UIDCopyCommand;
import org.obm.push.minig.imap.command.UIDFetchBodyStructureCommand;
import org.obm.push.minig.imap.command.UIDFetchEmailMetadataCommand;
import org.obm.push.minig.imap.command.UIDFetchEnvelopeCommand;
import org.obm.push.minig.imap.command.UIDFetchFastCommand;
import org.obm.push.minig.imap.command.UIDFetchFlagsCommand;
import org.obm.push.minig.imap.command.UIDFetchHeadersCommand;
import org.obm.push.minig.imap.command.UIDFetchInternalDateCommand;
import org.obm.push.minig.imap.command.UIDFetchMessageCommand;
import org.obm.push.minig.imap.command.UIDFetchPartCommand;
import org.obm.push.minig.imap.command.UIDNextCommand;
import org.obm.push.minig.imap.command.UIDSearchCommand;
import org.obm.push.minig.imap.command.UIDStoreCommand;
import org.obm.push.minig.imap.command.UIDThreadCommand;
import org.obm.push.minig.imap.command.UIDValidityCommand;
import org.obm.push.minig.imap.command.UnSubscribeCommand;
import org.obm.push.minig.imap.command.parser.BodyStructureParser;
import org.obm.push.minig.imap.tls.MinigTLSFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class ClientSupport {

	private final static Logger logger = LoggerFactory.getLogger(ClientSupport.class);
	
	private final int imapTimeoutInMilliseconds;
	private final TagProducer tagsProducer;
	private final List<IMAPResponse> lastResponses;
	private final SessionFactory sessionFactory;

	@VisibleForTesting IoSession session;
	@VisibleForTesting Semaphore lock;
	private MinigTLSFilter sslFilter;

	public ClientSupport(SessionFactory sessionFactory, int imapTimeoutInMilliseconds) {
		this.sessionFactory = sessionFactory;
		this.lock = new Semaphore(1);
		this.tagsProducer = new TagProducer();
		this.imapTimeoutInMilliseconds = imapTimeoutInMilliseconds;
		this.lastResponses = Collections.synchronizedList(new LinkedList<IMAPResponse>());
	}

	private void lock() throws ImapTimeoutException {
		try {
			boolean success = lock.tryAcquire(imapTimeoutInMilliseconds, TimeUnit.MILLISECONDS);
			assertTimeout(success);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException("InterruptedException !!");
		}
	}


	public void login(String login, char[] password, SocketAddress address,
			Boolean activateTLS) throws IMAPException, ImapTimeoutException {
		if (isConnected()) {
			throw new IllegalStateException(
					"Already connected. Disconnect first.");
		}

		lock(); // waits for "* OK IMAP4rev1 server...
		try {
			session = sessionFactory.connect(address);
			logger.debug("Connection established");
			if (activateTLS) {
				logger.debug("try to enable tls for connection");
				configureSessionForTls();
			}
			logger.debug("Sending {} login informations.", login);
			if (!run(new LoginCommand(login, password))) {
				throw new IMAPException("Cannot log into imap server");
			}
		} catch (RuntimeException e) {
			cleanupClientState();
			throw e;
		} catch (IMAPException e) {
			cleanupClientState();
			throw e;
		}
	}

	private void configureSessionForTls() throws ImapTimeoutException {
		boolean tlsActivated = run(new StartTLSCommand());
		if (tlsActivated) {
			activateSSL();
		} else {
			logger.debug("TLS not supported by IMAP server.");
		}
	}

	private void cleanupClientState() {
		try {
			if (session != null) {
				boolean immediatly = true;
				session.close(immediatly);
				
				SocketConnector socketConnector = (SocketConnector) session.getAttribute(SessionFactory.SOCKET_CONNECTOR);
				socketConnector.dispose();
				
				session = null;				
			}
		} finally {
			lock.release();
		}
	}

	private void join(IoFuture future) throws ImapTimeoutException {
		boolean joinSuccess = future.awaitUninterruptibly(imapTimeoutInMilliseconds);
		assertTimeout(joinSuccess);
	}

	private void assertTimeout(boolean joinSuccess) throws ImapTimeoutException {
		if (!joinSuccess) {
			throw new ImapTimeoutException();
		}
	}

	private void activateSSL() {
		try {
			sslFilter = new MinigTLSFilter();
			sslFilter.setUseClientMode(true);
			session.getFilterChain().addFirst("tls", sslFilter);
			logger.debug("Network traffic with IMAP server will be encrypted. ");
		} catch (Throwable t) {
			logger.error("Error starting ssl", t);
		}
	}

	public void logout() throws ImapTimeoutException {
		lock();
		try {
			if (session != null) {
				if (sslFilter != null) {
					try {
						sslFilter.stopSsl(session);
					} catch (SSLException e) {
						logger.error("error stopping ssl", e);
					} catch (IllegalStateException ei) {
						logger.error("imap connection is already stop");
					}
				}
				join(session.close(false));
			}
		} finally {
			cleanupClientState();
		}
	}

	private <T> T run(ICommand<T> cmd) throws ImapTimeoutException {
		logger.debug(Integer.toHexString(hashCode()) + " CMD: "
				+ cmd.getClass().getName() + " Permits: "
				+ lock.availablePermits());
		// grab lock, this one should be ok, except on first call
		// where we might wait for cyrus welcome text.
		lock();
		try {
			Preconditions.checkState(isConnected());
			WriteFuture writeFuture = cmd.execute(session, tagsProducer, lock);
			join(writeFuture);
			if (writeFuture.isWritten()) {
				lock(); // this one should wait until this.setResponses is called
				cmd.responseReceived(lastResponses);
			}
		} catch (ImapTimeoutException e) {
			cleanupClientState();
			throw e;
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
		for (IMAPResponse ir : rs) {
			logger.debug("S: " + ir.getPayload());
		}

		synchronized (lastResponses) {
			this.lastResponses.clear();
			this.lastResponses.addAll(rs);
		}
		lock.release();
	}

	public boolean select(String mailbox) throws ImapTimeoutException {
		return run(new SelectCommand(mailbox));
	}
	
	public ListResult listSubscribed() throws ImapTimeoutException {
	 	return run(new LsubCommand());
	}
	
	public ListResult listSubscribed(String referenceName, String mailboxName) throws ImapTimeoutException {
	 	return run(new LsubCommand(referenceName, mailboxName));
	}
	
	public ListResult listAll() throws ImapTimeoutException {
		return run(new ListCommand());
	}
	
	public ListResult listAll(String referenceName, String mailboxName) throws ImapTimeoutException {
		return run(new ListCommand(referenceName, mailboxName));
	}

	public Set<String> capabilities() throws ImapTimeoutException {
		return run(new CapabilityCommand());
	}

	public boolean noop() throws ImapTimeoutException {
		return run(new NoopCommand());
	}

	public boolean create(String mailbox) throws ImapTimeoutException {
		return run(new CreateCommand(mailbox));
	}

	public boolean create(String mailbox, String partition) throws ImapTimeoutException {
		return run(new CreateCommand(mailbox, partition));
	}

	public boolean delete(String mailbox) throws ImapTimeoutException {
		return run(new DeleteCommand(mailbox));
	}

	public boolean rename(String mailbox, String newMailbox) throws ImapTimeoutException {
		return run(new RenameCommand(mailbox, newMailbox));
	}

	public boolean subscribe(String mailbox) throws ImapTimeoutException {
		return run(new SubscribeCommand(mailbox));
	}

	public boolean unsubscribe(String mailbox) throws ImapTimeoutException {
		return run(new UnSubscribeCommand(mailbox));
	}

	public boolean append(String mailbox, Reader message, FlagsList fl) throws ImapTimeoutException {
		return run(new AppendCommand(mailbox, message, fl));
	}
	
	public Set<Acl> getAcl(String mailbox) throws ImapTimeoutException {
		return run(new GetACLCommand(mailbox));
	}
	
	public boolean setAcl(String mailbox, String identifier, String accessRights) throws ImapTimeoutException {
		return run(new SetACLCommand(mailbox, identifier, accessRights));
	}

	public void expunge() throws ImapTimeoutException {
		run(new ExpungeCommand());
	}

	public QuotaInfo quota(String mailbox) throws ImapTimeoutException {
		return run(new QuotaRootCommand(mailbox));
	}
	
	public boolean removeQuota(String mailbox) throws ImapTimeoutException {
		return run(new SetQuotaCommand(mailbox));
	}

	public boolean setQuota(String mailbox, long quotaInKb) throws ImapTimeoutException {
		return run(new SetQuotaCommand(mailbox, quotaInKb));
	}

	public InputStream uidFetchMessage(long uid) throws ImapTimeoutException {
		return run(new UIDFetchMessageCommand(uid));
	}

	public InputStream uidFetchMessage(long uid, long truncation) {
		return run(new UIDFetchMessageCommand(uid, truncation));
	}

	public MessageSet uidSearch(SearchQuery sq) throws ImapTimeoutException {
		return run(new UIDSearchCommand(sq));
	}

	public Collection<MimeMessage> uidFetchBodyStructure(MessageSet messages) throws ImapTimeoutException {
		return run(new UIDFetchBodyStructureCommand(new BodyStructureParser(), messages));
	}

	public Collection<IMAPHeaders> uidFetchHeaders(Collection<Long> uids,
			String[] headers) throws ImapTimeoutException {
		return run(new UIDFetchHeadersCommand(uids, headers));
	}

	public Collection<UIDEnvelope> uidFetchEnvelope(MessageSet messages) throws ImapTimeoutException {
		return run(new UIDFetchEnvelopeCommand(messages));
	}

	public Map<Long, FlagsList> uidFetchFlags(MessageSet messages) throws ImapTimeoutException {
		return run(new UIDFetchFlagsCommand(messages));
	}

	public List<InternalDate> uidFetchInternalDate(MessageSet messages) throws ImapTimeoutException {
		return run(new UIDFetchInternalDateCommand(messages));
	}
	
	public Collection<FastFetch> uidFetchFast(MessageSet messages) throws ImapTimeoutException {
		return run(new UIDFetchFastCommand(messages));
	}

	public MessageSet uidCopy(MessageSet messages, String destMailbox) throws ImapTimeoutException {
		return run(new UIDCopyCommand(messages, destMailbox));
	}

	public boolean uidStore(MessageSet messages, FlagsList fl, boolean set) throws ImapTimeoutException {
		return run(new UIDStoreCommand(messages, fl, set));
	}

	public InputStream uidFetchPart(long uid, String address) throws ImapTimeoutException {
		return run(new UIDFetchPartCommand(uid, address));
	}
	
	public InputStream uidFetchPart(long uid, String address, long truncation) throws ImapTimeoutException {
		return run(new UIDFetchPartCommand(uid, address, truncation));
	}

	public EmailMetadata uidFetchEmailMetadata(long uid) throws ImapTimeoutException {
		return run(new UIDFetchEmailMetadataCommand(new BodyStructureParser(), uid));
	}

	public List<MailThread> uidThreads() throws ImapTimeoutException {
		// UID THREAD REFERENCES UTF-8 NOT DELETED
		return run(new UIDThreadCommand());
	}

	public NameSpaceInfo namespace() throws ImapTimeoutException {
		return run(new NamespaceCommand());
	}

	public void startIdle() throws ImapTimeoutException {
		run(new StartIdleCommand());
	}

	public void stopIdle() throws ImapTimeoutException {
		run(new StopIdleCommand());
	}
	
	public boolean isConnected() {
		return session != null && session.isConnected();
	}
	
	public long uidNext(String mailbox) throws ImapTimeoutException {
		return run(new UIDNextCommand(mailbox));
	}
	
	public long uidValidity(String mailbox) throws ImapTimeoutException {
		return run(new UIDValidityCommand(mailbox));
	}

	public boolean setAnnotation(String mailbox, AnnotationEntry annotationEntry, AttributeValue attributeValue) {
		return run(new SetAnnotationCommand(mailbox, annotationEntry, attributeValue));
	}
}
