/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.imap.sieve;

import java.io.InputStream;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.minig.imap.sieve.commands.SieveActivate;
import org.minig.imap.sieve.commands.SieveAuthenticate;
import org.minig.imap.sieve.commands.SieveDeleteScript;
import org.minig.imap.sieve.commands.SieveListscripts;
import org.minig.imap.sieve.commands.SievePutscript;
import org.minig.imap.sieve.commands.SieveUnauthenticate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SieveClientSupport {

	private static final Logger logger = LoggerFactory
			.getLogger(SieveClientSupport.class);

	private Semaphore lock;
	private IoSession session;
	private List<SieveResponse> lastResponses;
	private SieveAuthenticate authenticate;

	public SieveClientSupport(String login, String password) {
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

	public boolean login(SocketConnector connector, SocketAddress sa,
			IoHandler handler) {
		if (session != null && session.isConnected()) {
			throw new IllegalStateException(
					"Already connected. Disconnect first.");
		}

		try {

			// wait for
			// "IMPLEMENTATION" "Cyrus timsieved v2.2.13-Debian-2.2.13-10"
			// "SASL" "PLAIN"
			// "SIEVE"
			// "fileinto reject envelope vacation imapflags notify subaddress relational comparator-i;ascii-numeric regex"
			// "STARTTLS"
			// OK
			lock();

			ConnectFuture cf = connector.connect(sa, handler);
			cf.join();

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

	public void logout() {
		if (session != null) {
			session.close().join();
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

	public void setResponses(ArrayList<SieveResponse> copy) {
		if (logger.isDebugEnabled()) {
			logger.debug("in setResponses on "
					+ Integer.toHexString(hashCode()));
		}
		lastResponses.clear();
		lastResponses.addAll(copy);
		lock.release();
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
