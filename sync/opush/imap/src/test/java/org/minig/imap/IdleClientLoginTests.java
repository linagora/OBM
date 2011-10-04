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
public class IdleClientLoginTests extends IMAPTestCase {

	public void testConstructor() {
		create();
	}

	private IdleClient create() {
		return new IdleClient(confValue("imap"), 143, confValue("login"),
				confValue("password"));
	}

	public void testLoginLogout() {
		IdleClient sc = create();
		try {
			boolean ok = sc.login(true);
			assertTrue(ok);
			sc.select("INBOX");
			sc.stopIdle();
		} catch (Throwable e) {
			fail(e.getMessage());
		} finally {
			try {
				sc.logout();
			} catch (Throwable e) {
				fail(e.getMessage());
			}
		}
	}

	public void testLoginLogoutSpeed() {
		IdleClient sc = create();
		int COUNT = 1000;
		for (int i = 0; i < COUNT; i++) {
			boolean ok = sc.login(true);
			assertTrue(ok);
			sc.select("INBOX");
			sc.stopIdle();
			sc.logout();
		}
	}

}
