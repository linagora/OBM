/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.SocketConnector;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.mail.imap.IMAPException;

import com.google.inject.Provider;



public class SessionFactoryImplTest {

	private static final int DEFAULT_IMAP_TIMEOUT = 2000;
	private static final SocketAddress ADDRESS = new InetSocketAddress("localhost", 666);
	private SessionFactoryImpl testee;
	private IMocksControl control;
	private SocketConnector socketConnector;

	@Before
	public void setup() {
		control = createControl();
		Provider<SocketConnector> connectorFactory = control.createMock(Provider.class);
		socketConnector = control.createMock(SocketConnector.class);
		expect(connectorFactory.get()).andReturn(socketConnector);
		ClientHandler handler = control.createMock(ClientHandler.class);
		socketConnector.setHandler(handler);
		expectLastCall().once();
		testee = new SessionFactoryImpl(connectorFactory, handler, DEFAULT_IMAP_TIMEOUT);
	}
	
	@Test
	public void connect() throws Exception {
		IoSession mockSession = createMock(IoSession.class);
		ConnectFuture connectFuture = control.createMock(ConnectFuture.class);
		expect(socketConnector.connect(ADDRESS)).andReturn(connectFuture);
		expect(connectFuture.awaitUninterruptibly(DEFAULT_IMAP_TIMEOUT)).andReturn(true);
		expect(connectFuture.isConnected()).andReturn(true);
		expect(connectFuture.getSession()).andReturn(mockSession);
		control.replay();
		IoSession session = testee.connect(ADDRESS);
		assertThat(session).isSameAs(mockSession);
		control.verify();
	}
	
	@Test(expected=RuntimeException.class)
	public void isConnectedFail() throws Exception {
		ConnectFuture connectFuture = control.createMock(ConnectFuture.class);
		expect(socketConnector.connect(ADDRESS)).andReturn(connectFuture);
		expect(connectFuture.awaitUninterruptibly(DEFAULT_IMAP_TIMEOUT)).andReturn(true);
		expect(connectFuture.isConnected()).andReturn(false);
		socketConnector.dispose();
		expectLastCall();
		RuntimeException expectedException = new RuntimeException();
		expect(connectFuture.getSession()).andThrow(expectedException);
		control.replay();
		try {
			testee.connect(ADDRESS);
		} catch (RuntimeException e) {
			assertThat(e).isSameAs(expectedException);
			control.verify();
			throw e;
		}
	}
	
	@Test(expected=IMAPException.class)
	public void isConnectedFailButNoException() throws Exception {
		ConnectFuture connectFuture = control.createMock(ConnectFuture.class);
		expect(socketConnector.connect(ADDRESS)).andReturn(connectFuture);
		expect(connectFuture.awaitUninterruptibly(DEFAULT_IMAP_TIMEOUT)).andReturn(true);
		expect(connectFuture.isConnected()).andReturn(false);
		socketConnector.dispose();
		expectLastCall();
		expect(connectFuture.getSession()).andReturn(null);
		control.replay();
		try {
			testee.connect(ADDRESS);
		} catch (IMAPException e) {
			control.verify();
			throw e;
		}
	}
	
	@Test(expected=ImapTimeoutException.class)
	public void connectTimeout() throws Exception {
		ConnectFuture connectFuture = control.createMock(ConnectFuture.class);
		expect(socketConnector.connect(ADDRESS)).andReturn(connectFuture);
		expect(connectFuture.awaitUninterruptibly(DEFAULT_IMAP_TIMEOUT)).andReturn(false);
		connectFuture.cancel();
		expectLastCall();
		socketConnector.dispose();
		expectLastCall();
		control.replay();
		try {
			testee.connect(ADDRESS);
		} catch (ImapTimeoutException e) {
			control.verify();
			throw e;
		}
	}
}
