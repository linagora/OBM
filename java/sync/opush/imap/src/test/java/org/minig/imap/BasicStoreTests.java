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
public class BasicStoreTests extends LoggedTestCase {

	private static final int COUNT = 50000;

	public void testSelect() {
		sc.select("INBOX");
	}

	public void testSelectSpeed() {
		long time;

		time = System.currentTimeMillis();

		for (int i = 0; i < COUNT; i++) {
			sc.select("INBOX");
		}

		time = System.currentTimeMillis() - time;
		assertTrue(COUNT + " iterations in " + time + "ms. "
				+ (time / COUNT) + "ms avg, " + 1000 / (time / COUNT)
				+ " per sec.", true);
	}

	public void testCapability() {
		Set<String> caps = sc.capabilities();
		assertNotNull(caps);
	}

	public void testCreateSubUnsubRenameDelete() {
		String mbox = "test" + System.currentTimeMillis();
		String newMbox = "rename" + System.currentTimeMillis();
		boolean b = sc.create(mbox);
		assertTrue(b);
		boolean sub = sc.subscribe(mbox);
		assertTrue(sub);
		sub = sc.unsubscribe(mbox);
		assertTrue(sub);

		boolean renamed = sc.rename(mbox, newMbox);
		assertTrue("Rename success : ", renamed);
		boolean del = false;
		if (!renamed) {
			del = sc.delete(mbox);
		} else {
			del = sc.delete(newMbox);
		}
		assertTrue(del);
	}

	public void testNoop() {
		sc.noop();
	}

	public void testAppend() {
		FlagsList fl = new FlagsList();
		fl.add(Flag.SEEN);
		boolean firstEmailAppendOK = sc.append("INBOX", getRfc822Message(), fl);
		boolean secondEmailAppendOK = sc.append("INBOX", getUtf8Rfc822Message(), fl);
		assertTrue(firstEmailAppendOK);
		assertTrue("Added status : " + firstEmailAppendOK + " " + secondEmailAppendOK, secondEmailAppendOK);
	}

	public void testNested() {
		sc.select("INBOX");
		Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(5194l));
		assertTrue(mts.size() == 1);
	}

	/**
	 * Loads specific uid's in my mailbox with complex headers
	 */
	public void testUidFetchHeadersBroken() {
		sc.select("INBOX");
		try {
			Collection<IMAPHeaders> mts = sc.uidFetchHeaders(
					Arrays.asList(4947l, 5256l, 5011l, 4921l, 4837l), new String[] {
							"subject", "from" });
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	/**
	 * Loads specific uid's in my mailbox with complex trees
	 */
	public void testUidFetchBodyStructureBroken() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(47339l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}

	}

	public void testMiniBroken7() {
		sc.select("Dossiers partagés/minigbroken");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(7l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox3() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(3l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox72() {
		sc.select("Dossiers partagés/obm-dev");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(72l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox3709() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(3709l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox414() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(414l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox3916() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(3916l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox3711() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(3711l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox356() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(356l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testInbox4() {
		sc.select("INBOX");
		try {
			Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(4l));
			assertNotNull(mts);
		} catch (Throwable t) {
			fail(t.getMessage());
		}
	}

	public void testUidSearch() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		assertNotNull(uids);
		assertTrue(uids.size() > 0);

		for (int i = 0; i < COUNT; i++) {
			Collection<Long> u = sc.uidSearch(sq);
			assertTrue(u.size() > 0);
		}
	}

	public void testUidFetchHeadersPerf() {
		final String[] HEADS_LOAD = new String[] { "Subject", "From", "Date",
				"To", "Cc", "Bcc", "X-Mailer", "User-Agent", "Message-ID" };

		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(new SearchQuery());
		Iterator<Long> iterator = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(iterator.next(), iterator.next());

		for (int i = 0; i < COUNT; i++) {
			Collection<IMAPHeaders> h = sc.uidFetchHeaders(firstTwo, HEADS_LOAD);
			assertNotNull(h);
		}
	}

	public void testUidFetchHeaders() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);
		String[] headers = new String[] { "date", "from", "subject" };

		long nstime = System.nanoTime();
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		nstime = System.nanoTime() - nstime;
		assertEquals(uids.size(), h.size());
		
		for (IMAPHeaders header : h) {
			assertNotNull(header.getSubject());
			assertNotNull(header.getDate());
			assertNotNull(header.getFrom().getMail());
			assertNotNull(header.getFrom().getDisplayName());
		}

	}

	public void testUidFetchHeadersSpeed() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		String[] headers = new String[] { "x-priority" };

		Collection<Long> uids = sc.uidSearch(sq);
		Collection<Envelope> e = sc.uidFetchEnvelope(uids);
		assertNotNull(e);
		assertEquals(uids.size(), e.size());
		Collection<IMAPHeaders> h = sc.uidFetchHeaders(uids, headers);
		assertEquals(uids.size(), h.size());
	}

	public void testUidFetchEnvelopePerf() {
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(new SearchQuery());
		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());

		for (int i = 0; i < COUNT; i++) {
			Collection<Envelope> h = sc.uidFetchEnvelope(firstTwo);
			assertNotNull(h);
		}
	}

	public void testUidFetchEnvelope() {
		sc.select("INBOX");
		Collection<Long> one = Arrays.asList(280l);

		long nstime = System.nanoTime();
		Collection<Envelope> h = sc.uidFetchEnvelope(one);
		nstime = System.nanoTime() - nstime;
		assertEquals(one.size(), h.size());

		for (Envelope e : h) {
			assertNotNull(e.getSubject());
			assertNotNull(e.getSubject());
			assertNotNull(e.getFrom().getDisplayName());
		}

	}

	public void testUidFetchEnvelopeReliable() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		for (long l : uids) {
			try {
				Collection<Envelope> h = sc.uidFetchEnvelope(Arrays.asList(l));
				assertEquals(1, h.size());
			} catch (Throwable t) {
				fail(t.getMessage());
			}
		}
	}

	public void testUidFetchFlags() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> iterator = uids.iterator();
		List<Long> firstTwo = Arrays.asList(iterator.next(), iterator.next());

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

	public void testUidStore() {
		SearchQuery sq = new SearchQuery();
		sc.select("INBOX");
		Collection<Long> uids = sc.uidSearch(sq);

		Iterator<Long> it = uids.iterator();
		Collection<Long> firstTwo = Arrays.asList(it.next(), it.next());

		FlagsList fl = new FlagsList();
		fl.add(Flag.ANSWERED);
		long nstime = System.nanoTime();
		boolean result = sc.uidStore(firstTwo, fl, true);
		nstime = System.nanoTime() - nstime;
		assertTrue(result);
		result = sc.uidStore(firstTwo, fl, false);
		assertTrue(result);
	}

	public void testUidFetchPartBroken() {
		// allows test to be green bar when not running on my computer
		boolean selection = sc.select("Shared Folders/partage");
		if (!selection) {
			return;
		}

		Collection<MimeMessage> mts = sc.uidFetchBodyStructure(Arrays.asList(1l));
		if (mts.size() == 1) {
			InputStream part = sc.uidFetchPart(1, "1");
			try {
				FileUtils.dumpStream(part, System.err, true);
			} catch (IOException e) {
				fail(e.getMessage());
			}
		}
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
			fail(e.getMessage());
		}
	}

	public void testNamespace() {
		NameSpaceInfo nsi = sc.namespace();
		assertNotNull(nsi);
	}

}
