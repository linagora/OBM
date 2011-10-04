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

	public void testList() {
		ListResult lr = sc.listAll();
		assertNotNull(lr);
	}

	public void testLsub() {
		ListResult lr = sc.listSubscribed();
		assertNotNull(lr);
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
