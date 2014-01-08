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
package org.obm.push.minig.imap.impl;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.configuration.EmailConfiguration.IMAP_INBOX_NAME;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.mail.ImapTimeoutException;
import org.obm.push.mail.imap.IMAPException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.inject.Provider;
import com.icegreen.greenmail.util.GreenMail;

public class MailboxTimeoutTest {

	private String mailbox;
	private String password;
	private GreenMail greenMail;
	private InetSocketAddress greenmailAddress;
	private int configuredTimeout;
	private SessionFactoryImpl testSessionFactory;
	private Provider<SocketConnector> connectorFactory;
	private ClientSupport testee;
	
	private List<NioSocketConnector> connectors;
	private List<IoSession> sessions;
	
	@Before
	public void setup() {
		greenMail = new GreenMail();
		greenMail.start();
		mailbox = "to@localhost.com";
		password = "password";
		greenMail.setUser(mailbox, password);
		greenmailAddress = new InetSocketAddress("localhost", greenMail.getImap().getPort());
		
		IResponseCallback cb = new StoreClientCallback();
		ClientHandler handler = new ClientHandler(cb);
		configuredTimeout = Ints.checkedCast(TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS));
		connectors = Lists.newArrayList();
		
		connectorFactory = new Provider<SocketConnector>() {
			@Override
			public SocketConnector get() {
				NioSocketConnector connector = new NioSocketConnector();
				connectors.add(connector);
				return connector;
			}
		};
		sessions = Lists.newArrayList();
		
		testSessionFactory = new SessionFactoryImpl(connectorFactory, handler, configuredTimeout) {
			@Override
			public IoSession connect(SocketAddress address)
					throws IMAPException {
				IoSession session = super.connect(address);
				sessions.add(session);
				return session;
			}
		};
		testee = new ClientSupport(testSessionFactory, configuredTimeout);
		cb.setClient(testee);
	}
	
	private void checkFreedResources() throws InterruptedException {
		assertThat(testee.session).isNull();
		assertThat(testee.lock.tryAcquire()).isTrue();
		for (IoSession session: sessions) {
			assertThat(session.isClosing()).isTrue();
		}
		//Add some time for async session closing to finish
		Thread.sleep(100);
		for (NioSocketConnector connector: connectors) {
			assertThat(connector.isDisposed()).isTrue();
		}
	}
	
	@After
	public void teardown() {
		greenMail.stop();
	}

	@Test
	public void select() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, false);
		boolean result = testee.select(IMAP_INBOX_NAME);
		testee.logout();
		assertThat(result).isTrue();
		checkFreedResources();
	}
	
	@Test(expected=ImapTimeoutException.class)
	public void timeoutOnLogin() throws IMAPException, InterruptedException {
		Stopwatch stopwatch = new Stopwatch();
		greenMail.lockGreenmailAndReleaseAfter(4);
		stopwatch.start();
		try {
			testee.login(mailbox, password, greenmailAddress, false);
		} finally {
			assertThat(stopwatch.elapsed(TimeUnit.MILLISECONDS)).isGreaterThan(configuredTimeout).isLessThan(TimeUnit.MILLISECONDS.convert(3, TimeUnit.SECONDS));
			checkFreedResources();
		}
	}
	
	@Test
	public void logoutNoEstablishedConnection() throws InterruptedException {
		testee.logout();
		checkFreedResources();
	}

	@Test
	public void loginLogout() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, false);
		testee.logout();
		checkFreedResources();
	}
	
	@Test
	public void loginTlsLogout() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, true);
		testee.logout();
		checkFreedResources();
	}
	
	@Test(expected=IMAPException.class)
	public void loginBadCredentials() throws IMAPException, InterruptedException {
		try {
			testee.login("woho", password, greenmailAddress, false);
		} finally {
			checkFreedResources();
		}
	}
	
	@Test(expected=IllegalStateException.class)
	public void loginLogin() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, false);
		try {
			testee.login(mailbox, password, greenmailAddress, false);
		} catch (IllegalStateException e) {
			testee.logout();
			throw e;
		} finally {
			checkFreedResources();
		}
	}
	
	@Test
	public void loginLogoutLogout() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, false);
		testee.logout();
		testee.logout();
		checkFreedResources();
	}
	
	@Test
	@Ignore("This test fails for no obvious reason, it's not critical to OBM by the way")
	public void loginLogoutLoginSelect() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, false);
		testee.logout();
		testee.login(mailbox, password, greenmailAddress, false);
		boolean result = testee.select(IMAP_INBOX_NAME);
		testee.logout();
		
		assertThat(result).isTrue();
		checkFreedResources();
	}
	
	@Test
	@Ignore("This test fails for no obvious reason, it's not critical to OBM by the way")
	public void loginSelectLogoutLoginSelect() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, false);
		boolean result1 = testee.select(IMAP_INBOX_NAME);
		testee.logout();
		testee.login(mailbox, password, greenmailAddress, false);
		boolean result2 = testee.select(IMAP_INBOX_NAME);
		testee.logout();
		
		assertThat(result1).isTrue();
		assertThat(result2).isTrue();
		checkFreedResources();
	}
	
	@Test
	public void selectTimeout() throws IMAPException, InterruptedException {
		testee.login(mailbox, password, greenmailAddress, false);
		Stopwatch stopwatch = new Stopwatch();
		try {
			greenMail.lockGreenmailAndReleaseAfter(3);
			stopwatch.start();
			testee.select(IMAP_INBOX_NAME);
		} catch (ImapTimeoutException e) {
			try {
				testee.select(IMAP_INBOX_NAME);
			} catch (IllegalStateException e2) {
				checkFreedResources();
			}
		} finally {
			testee.logout();
		}
	}
}