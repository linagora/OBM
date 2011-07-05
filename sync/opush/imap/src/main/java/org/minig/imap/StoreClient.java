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
 * 
 * @author tom
 * 
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
	 * 
	 * @return true if login is successful
	 * @throws IMAPException
	 */
	public boolean login() {
		return login(true);
	}
	
	/**
	 * Logs into the IMAP store
	 * 
	 * @return true if login is successful
	 * @throws IMAPException
	 */
	public boolean login(Boolean activateTLS) {
		if (logger.isDebugEnabled()) {
			logger.debug("login attempt to " + hostname + ":" + port + " for "
					+ login + " / " + password);
		}

		SocketAddress sa = new InetSocketAddress(hostname, port);
		boolean ret = false;
		if (cs.login(login, password, connector, sa, activateTLS)) {
			ret = true;
		}
		return ret;
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

	public ListResult listSubscribed() {
		return cs.listSubscribed();
	}

	public ListResult listAll() {
		return cs.listAll();
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
