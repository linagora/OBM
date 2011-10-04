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

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Base64;
import org.junit.Ignore;

@Ignore("It's necessary to do again all tests")
public class SieveLoginTests extends SieveTestCase {

	public void testConstructor() {
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);
	}

	public void testB64Decode() {
		String value = "dGhvbWFzQHp6LmNvbQB0aG9tYXNAenouY29tAGFsaWFjb20=";
		ByteBuffer decoded = ByteBuffer.wrap(Base64.decodeBase64(value));
	
		value = "dGhvbWFzAHRob21hcwBhbGlhY29t";
		decoded = ByteBuffer.wrap(Base64.decodeBase64(value));
		assertNotNull(decoded);
	}

	public void testLoginLogout() {
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);

		try {
			boolean ret = sc.login();
			assertTrue(ret);
			sc.logout();
		} catch (Throwable t) {
			fail("should not get an exception");
		}
	}

	public void testUnauthenticate() {
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);

		try {
			boolean ret = sc.login();
			assertTrue(ret);
			sc.unauthenticate();
			sc.logout();
		} catch (Throwable t) {
			fail("should not get an exception");
		}
	}

	public void testLoginLogoutPerf() {
		final int IT_COUNT = 10000;
		SieveClient sc = new SieveClient(confValue("imap"), 2000,
				confValue("login"), confValue("password"));
		assertNotNull(sc);

		for (int i = 0; i < 1000; i++) {
			sc.login();
			sc.logout();
		}
		for (int i = 0; i < IT_COUNT; i++) {
			sc.login();
			sc.logout();
		}
	}

}
