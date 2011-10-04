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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Ignore;

@Ignore("It's necessary to do again all tests")
public abstract class LoggedTestCase extends IMAPTestCase {

	protected StoreClient sc;

	public void setUp() {
		String port = confValue("port");
		if (port == null) {
			port = "143";
		}
		sc = new StoreClient(confValue("imap"), Integer.parseInt(port), confValue("login"),
				confValue("password"));
		boolean login = sc.login();
		if (!login) {
			fail("login failed for "+confValue("login")+" / "+confValue("password"));
		}
	}

	public void tearDown() {
		sc.logout();
	}

	public InputStream getRfc822Message() {
		String m = "From: Thomas Cataldo <thomas@zz.com>\r\n"
				+ "Subject: test message "
				+ System.currentTimeMillis()
				+ "\r\n"
				+ "MIME-Version: 1.0\r\n"
				+ "Content-Type: text/plain; CHARSET=UTF-8\r\n\r\n"
				+ "Hi, this is message about my 300euros from the casino.\r\n\r\n";
		return new ByteArrayInputStream(m.getBytes());
	}

	public InputStream getUtf8Rfc822Message() {
		String m = "From: Thomas Cataldo <thomas@zz.com>\r\n"
				+ "Subject: test message " + System.currentTimeMillis()
				+ "\r\n" + "MIME-Version: 1.0\r\n"
				+ "Content-Type: text/plain; CHARSET=UTF-8\r\n\r\n"
				+ "Hi, this is message about my 300€ from the casino.\r\n\r\n";
		return new ByteArrayInputStream(m.getBytes());
	}
}
