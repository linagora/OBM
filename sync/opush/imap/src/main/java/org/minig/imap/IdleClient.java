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

	public boolean login(Boolean activateTLS) {
		if (logger.isDebugEnabled()) {
			logger.debug("login called");
		}
		SocketAddress sa = new InetSocketAddress(hostname, port);
		SocketConnector connector = new SocketConnector();

		boolean ret = false;
		if (cs.login(login, password, connector, sa, activateTLS)) {
			ret = true;
		}
		return ret;
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
