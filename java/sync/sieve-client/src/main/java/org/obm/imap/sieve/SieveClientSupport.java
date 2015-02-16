/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2015  Linagora
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

package org.obm.imap.sieve;

import java.io.InputStream;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.obm.imap.sieve.commands.SieveActivate;
import org.obm.imap.sieve.commands.SieveAuthenticate;
import org.obm.imap.sieve.commands.SieveDeleteScript;
import org.obm.imap.sieve.commands.SieveGetScript;
import org.obm.imap.sieve.commands.SieveListscripts;
import org.obm.imap.sieve.commands.SievePutscript;
import org.obm.imap.sieve.commands.SieveUnauthenticate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SieveClientSupport {

	private static final Logger logger = LoggerFactory
			.getLogger(SieveClientSupport.class);

	private final Semaphore lock;
	private IoSession session;
	private final List<SieveResponse> lastResponses;
	private final SieveAuthenticate authenticate;
	private final NioSocketConnector socketConnector;

	public SieveClientSupport(String login, String password) {
		this.socketConnector = new NioSocketConnector();
		this.lock = new Semaphore(1);
		this.lastResponses = new LinkedList<SieveResponse>();
		this.authenticate = new SieveAuthenticate(login, password);
	}

	private void lock() {
		try {
			boolean ret = lock.tryAcquire(1000, TimeUnit.MILLISECONDS);
			if (!ret) {
				throw new RuntimeException("cannot acquire lock !!!");
			}
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
		}
	}

	public boolean login(SocketAddress sa, IoHandler handler) {
		
		if (session != null && session.isConnected()) {
			throw new IllegalStateException(
					"Already connected. Disconnect first.");
		}

		try {
			socketConnector.setHandler(handler);
			
			// wait for
			// "IMPLEMENTATION" "Cyrus timsieved v2.2.13-Debian-2.2.13-10"
			// "SASL" "PLAIN"
			// "SIEVE"
			// "fileinto reject envelope vacation imapflags notify subaddress relational comparator-i;ascii-numeric regex"
			// "STARTTLS"
			// OK
			lock();

			ConnectFuture cf = socketConnector.connect(sa);
			cf.await();

			if (!cf.isConnected()) {
				return false;
			}
			session = cf.getSession();
			if (logger.isDebugEnabled()) {
				logger.debug("Connection established, sending login.");
			}
			return run(authenticate);
		} catch (Exception e) {
			logger.error("login error", e);
			return false;
		}
	}

	public void logout() throws InterruptedException {
		if (session != null) {
			session.close(false).await();
			session = null;
		}
	}

	private <T> T run(SieveCommand<T> cmd) {
		if (logger.isDebugEnabled()) {
			logger.debug("running command " + cmd);
		}
		// grab lock, this one should be ok, except on first call
		// where we might wait for sieve welcome text.
		lock();
		cmd.execute(session);
		lock(); // this one should wait until this.setResponses is called

		try {
			cmd.responseReceived(lastResponses);
		} catch (Throwable t) {
			logger.error("receiving/parsing sieve response to cmd "
					+ cmd.getClass().getSimpleName(), t);
		} finally {
			lock.release();
		}
		return cmd.getReceivedData();
	}

	public void setResponses(List<SieveResponse> copy) {
		if (logger.isDebugEnabled()) {
			logger.debug("in setResponses on "
					+ Integer.toHexString(hashCode()));
		}
		lastResponses.clear();
		lastResponses.addAll(copy);
		lock.release();
	}

	public String getScriptContent(String name) {
		return run(new SieveGetScript(name));
	}

	public List<SieveScript> listscripts() {
		return run(new SieveListscripts());
	}

	public boolean putscript(String name, InputStream scriptContent) {
		return run(new SievePutscript(name, scriptContent));
	}

	public void unauthenticate() {
		run(new SieveUnauthenticate());
	}

	public boolean deletescript(String name) {
		return run(new SieveDeleteScript(name));
	}

	public void activate(String newName) {
		run(new SieveActivate(newName));
	}

}
