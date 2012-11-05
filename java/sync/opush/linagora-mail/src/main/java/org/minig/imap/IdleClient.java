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

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.mina.transport.socket.nio.SocketConnector;
import org.minig.imap.idle.IIdleCallback;
import org.minig.imap.idle.IdleClientCallback;
import org.minig.imap.impl.ClientHandler;
import org.minig.imap.impl.ClientSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleClient {

	private static final Logger logger = LoggerFactory
			.getLogger(IdleClient.class);
	
	private String login;
	private String password;
	private String hostname;
	private int port;
	private ClientSupport cs;
	private IdleClientCallback icb;

	public IdleClient(String hostname, int port, String loginAtDomain,
			String password) {
		this.login = loginAtDomain;
		this.password = password;
		this.hostname = hostname;
		this.port = port;
		icb = new IdleClientCallback();
		ClientHandler handler = new ClientHandler(icb);
		cs = new ClientSupport(handler);
		icb.setClient(cs);
	}

	public void login(Boolean activateTLS) throws IMAPException {
		logger.debug("login called");
		SocketAddress sa = new InetSocketAddress(hostname, port);
		SocketConnector connector = new SocketConnector();
		cs.login(login, password, connector, sa, activateTLS);
	}

	public void logout() {
		cs.logout();
	}

	public void startIdle(IIdleCallback observer) {
		if (!icb.isStart()) {
			cs.startIdle();
		}
		icb.attachIdleCallback(observer);
	}

	public void stopIdle() {
		if (icb.isStart()) {
			icb.detachIdleCallback();
			cs.stopIdle();
			icb.stopIdle();
		}
	}

	/**
	 * Opens the given IMAP folder. Only one folder quand be active at a time.
	 * 
	 * @param mailbox
	 *            utf8 mailbox name.
	 */
	public boolean select(String mailbox) {
		return cs.select(mailbox);
	}
}
