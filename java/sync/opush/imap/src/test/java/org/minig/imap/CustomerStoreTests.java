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
package org.minig.imap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.minig.imap.impl.MailThread;
import org.minig.imap.mime.MimeMessage;
import org.obm.push.utils.FileUtils;

@Ignore("It's necessary to do again all tests")
public class CustomerStoreTests extends LoggedTestCase {

	public void testSelect() {
		sc.select("INBOX");
	}

	public void testCapability() {
		Set<String> caps = sc.capabilities();
		assertNotNull(caps);
	}

	public void testNoop() {
		sc.noop();
	}

	public void testUidSearch() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		assertNotNull(uids);
		assertFalse(uids.isEmpty());
	}

	public void testUidFetchHeaders() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		String[] headers = new String[] { "date", "from", "subject",
				"message-id" };

		long nstime = System.nanoTime();
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());
	}

	public void testUidFetchHeadersOneByOne() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		String[] headers = new String[] { "date", "from", "subject",
				"message-id" };

		for (long l : uids) {
			Collection<IMAPHeaders> h = sc.uidFetchHeaders(Arrays.asList(l), headers);
			if (h.size() == 1) {
				assertTrue(true);
			} else {
				fail("could not read headers for uid " + l);
			}
		}

	}

	public void testUidFetches() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		String[] headers = new String[] { "date", "from", "subject",
				"message-id" };

		long nstime = System.nanoTime();
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());

		Iterator<Long> it = uids.iterator(); 
		for (int i = 0; i < uids.size(); i++) {
			Long current = it.next();
			try {
				Collection<MimeMessage> ret = sc.uidFetchBodyStructure(Arrays.asList(current));
				assertNotNull(ret);
			} catch (Throwable t) {
				fail("error for uid " + current);
			}
		}

	}

	public void testUidFetchFlags() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());
		
		long nstime = System.nanoTime();
		Collection<FlagsList> h = sc.uidFetchFlags(firstTwo);
		nstime = System.nanoTime() - nstime;
		assertEquals(firstTwo.size(), h.size());

		nstime = System.nanoTime();
		h = sc.uidFetchFlags(uids);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());
	}

	public void testUidCopy() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());

		long nstime = System.nanoTime();
		Collection<Long> result = sc.uidCopy(firstTwo, "Sent");
		nstime = System.nanoTime() - nstime;
		assertNotNull(result);
		assertEquals(firstTwo.size(), result.size());
	}

	public void testUidThreads() {
		sc.select("INBOX");
		List<MailThread> threads = sc.uidThreads();
		assertNotNull(threads);
		assertTrue(threads.size() > 0);
	}

	public void testUidFetchPart() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		long uid = uids.iterator().next();

		long nstime = System.nanoTime();
		InputStream in = sc.uidFetchPart(uid, "1");
		nstime = System.nanoTime() - nstime;
		assertNotNull(in);
		try {
			FileUtils.dumpStream(in, System.out, true);
		} catch (IOException e) {
			fail("Cannot dump part stream");
		}
	}

	public void testNamespace() {
		NameSpaceInfo nsi = sc.namespace();
		assertNotNull(nsi);
	}

}
