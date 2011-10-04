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
import java.util.List;

import org.apache.mina.transport.socket.nio.SocketConnector;
import org.minig.imap.sieve.SieveClientHandler;
import org.minig.imap.sieve.SieveClientSupport;
import org.minig.imap.sieve.SieveScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client API to cyrus sieve server
 * 
 * <code>http://www.ietf.org/proceedings/06mar/slides/ilemonade-1.pdf</code>
 * 
 * @author tom
 * 
 */
public class SieveClient {

	private static final Logger logger = LoggerFactory
			.getLogger(SieveClient.class);
	
	private String login;
	private SieveClientSupport cs;
	private String host;
	private int port;
	
	public SieveClient(String hostname, int port, String loginAtDomain,
			String password) {
		this.login = loginAtDomain;
		this.host = hostname;
		this.port = port;

		cs = new SieveClientSupport(login, password);
	}

	public boolean login() {
		if (logger.isDebugEnabled()) {
			logger.debug("login called");
		}
		SieveClientHandler handler = new SieveClientHandler(cs);
		SocketAddress sa = new InetSocketAddress(host, port);
		SocketConnector connector = new SocketConnector();
		boolean ret = false;
		if (cs.login(connector, sa, handler)) {
			ret = true;
		}
		return ret;
	}

	public List<SieveScript> listscripts() {
		return cs.listscripts();
	}

	public boolean putscript(String name, InputStream scriptContent) {
		return cs.putscript(name, scriptContent);
	}

	public void unauthenticate() {
		cs.unauthenticate();
	}
	
	public void logout() {
		cs.logout();
	}

	public boolean deletescript(String name) {
		return cs.deletescript(name);
	}

	public String getScript() {
		return "require [ \"fileinto\", \"imapflags\", \"vacation\" ];\n";
	}

	public void activate(String newName) {
		cs.activate(newName);
	}

}
