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

package org.obm.push.minig.imap;

import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Base64;
import org.junit.Ignore;
import org.obm.push.minig.imap.SieveClient;

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
