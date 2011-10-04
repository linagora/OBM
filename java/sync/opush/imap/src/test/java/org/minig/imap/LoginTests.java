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

import org.junit.Ignore;

@Ignore("It's necessary to do again all tests")
public class LoginTests extends IMAPTestCase {

	public void testConstructor() {
		StoreClient storeClient = new StoreClient(confValue("imap"), 143, confValue("login"),
				confValue("password"));
		assertNotNull(storeClient);
	}

	public void testLoginLogout() {
		StoreClient sc = new StoreClient(confValue("imap"), 143,
				confValue("login"), confValue("password"));
		try {
			boolean ok = sc.login();
			assertTrue(ok);
		} finally {
			sc.logout();
		}
	}

	public void testLoginLogoutSpeed() {
		StoreClient sc = new StoreClient(confValue("imap"), 143,
				confValue("login"), confValue("password"));
		int COUNT = 1000;
		for (int i = 0; i < COUNT; i++) {
			boolean ok = sc.login();
			assertTrue(ok);
			sc.logout();
		}
	}

}
