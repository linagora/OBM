/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Ignore;
import org.obm.push.minig.imap.sieve.SieveScript;

@Ignore("It's necessary to do again all tests")
public class SieveClientTests extends SieveTestCase {

	private SieveClient sc;

	protected void setUp() {
		sc = null;
		sc.login();
	}

	public void testEmpty() {
		// empty test to validate setup & teardown
	}

	public void testPutscript() {
		String name = "test." + System.currentTimeMillis() + ".sieve";
		String content = "" // test script
				+ "require [ \"fileinto\", \"imapflags\", "
				// +"\"body\", " // cyrus 2.3 extensions ?!
				+ "\"vacation\" ];\n" // extensions
				// +"if body :text :contains \"viagra\"{\n   discard;\n}\n"
				+ "if size :over 500K {\n   setflag \"\\\\Flagged\";\n}\n"
				+ "fileinto \"INBOX\";\n";
		InputStream contentStream = new ByteArrayInputStream(content.getBytes());
		boolean ret = sc.putscript(name, contentStream);
		assertTrue(ret);
	}

	public void testListscripts() {
		sc.listscripts();
	}

	public void testListscriptsBenchmark() throws InterruptedException {
		int COUNT = 1000;

		sc.logout();

		sc.login();
		int old = sc.listscripts().size();
		sc.logout();

		
		for (int i = 0; i < COUNT; i++) {
			boolean loginOk = sc.login();
			assertTrue(loginOk);
			int cur = sc.listscripts().size();
			sc.logout();
			assertEquals(old, cur);
			old = cur;
		}
	}

	public void testListAndDeleteAll() {
		List<SieveScript> scripts = sc.listscripts();
		for (SieveScript ss : scripts) {
			sc.deletescript(ss.getName());
		}
		scripts = sc.listscripts();
		assertTrue(scripts.isEmpty());
	}

	protected void tearDown() throws InterruptedException {
		sc.logout();
		sc = null;
	}

}
