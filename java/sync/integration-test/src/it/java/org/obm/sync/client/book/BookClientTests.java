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
package org.obm.sync.client.book;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.obm.sync.ObmSyncTestCase;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.Address;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.book.Folder;
import org.obm.sync.book.Phone;
import org.obm.sync.calendar.Event;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.locators.AddressBookLocator;

public class BookClientTests extends ObmSyncTestCase {

	protected BookClient book;
	protected AccessToken token;

	protected Contact getTestContact() {
		Contact c = new Contact();

		c.setFirstname("FooFirst");
		c.setLastname("BarLast");
		c.setAka("nickname");
		c.addEmail("INTERNET;X-OBM-Ref1", new Email("foo@bar.baz"));
		c.addPhone("CELL;VOICE;X-OBM-Ref1", new Phone("06 12 40 39 09"));
		c.addPhone("WORK;VOICE;X-OBM-Ref1", new Phone("06 12 40 39 08"));
		c.addAddress("HOME;X-OBM-Ref1", new Address("homestreet", "zip",
				"express", "town", "US", "Nevada"));
		c.addAddress("WORK;X-OBM-Ref1", new Address("workstreet", "zip",
				"express", "town", "US", "Nevada"));
		c.addAddress("OTHER;X-OBM-Ref1", new Address("otherstreet", "zip",
				"express", "town", "US", "Nevada"));

		Calendar cal = Calendar.getInstance(TimeZone
				.getTimeZone("Europe/Paris"));
		cal.setTimeInMillis(0);
		cal.set(Calendar.DAY_OF_MONTH, 9);
		cal.set(Calendar.MONTH, Calendar.AUGUST);
		cal.set(Calendar.YEAR, 1978);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		c.setBirthday(cal.getTime());
		c.setAnniversary(cal.getTime());
		System.out.println("Birthday date: " + cal.getTime());

		return c;
	}

	public void testListBooksIsReadOnly() {
		try {
			BookType[] ret = book.listBooks(token);
			assertNotNull(ret);
			assertEquals(2, ret.length);
			for (BookType b : ret) {
				boolean ro = book.isReadOnly(token, b);
				System.out.println("found book: " + b + " readOnly: " + ro);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}
	
	public void testListAllBooks() {
		try {
			List<AddressBook> ret = book.listAllBooks(token);
			assertNotNull(ret);
			assertEquals(4, ret.size());
			for (AddressBook b : ret) {
				System.out.println("found book: " + b);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}

	public BookType type() {
		return BookType.valueOf(p("book"));
	}

	public void testGetContactTwinKeys() {
		if (p("book").equals("users")) {
			return;
		}
		Contact c = null;

		c = getTestContact();
		c.setFirstname("twin");
		doTwinTest(c);

		c = getTestContact();
		c.getEmails().clear();
		c.setFirstname("twinnny2");
		c.setLastname("TwinLast" + System.currentTimeMillis());
		doTwinTest(c);

		c = new Contact();
		c.setFirstname("Titi");
		c.addPhone("CELL;VOICE;X-Obm-Ref1", new Phone("06 123 234 76"));
		doTwinTest(c);

		Contact c2 = null;

		c = new Contact();
		c.setLastname("Tutu");
		c.setFirstname("Jean");
		c.addPhone("CELL;VOICE;X-Obm-Ref1", new Phone("06 123 234 76"));
		c2 = new Contact();
		c2.setLastname("Tutu");
		c2.setFirstname("Karine");
		c2.addPhone("CELL;VOICE;X-Obm-Ref1", new Phone("06 333 333 33"));
		try {
			Contact created = book.createContactWithoutDuplicate(token, type(),
					c2);
			System.out.println("Karine created with id " + created.getUid());
			doTwinTest(c);
			book.removeContact(token, type(), created.getUid().toString());
		} catch (Exception e) {
			e.printStackTrace();
			fail("exception");
		}
	}

	private void doTwinTest(Contact c) {
		try {
			Contact cUid = book.createContactWithoutDuplicate(token, type(), c);
			int id2 = cUid.getUid();
			KeyList ret = book.getContactTwinKeys(token, type(), c);
			assertNotNull(ret);
			System.out.println("created " + c.getFirstname() + " "
					+ c.getLastname() + " with id " + cUid.getUid());
			for (String s : ret.getKeys()) {
				System.out.println("  - Key: " + s);
			}

			book.removeContact(token, type(), "" + cUid.getUid());
			assertTrue(ret.getKeys().contains("" + id2));
			assertTrue(ret.getKeys().size() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}

	public void testCrud() {
		if (p("book").equals("users")) {
			return;
		}

		try {
			Contact c = getTestContact();
			Contact ret = book.createContact(token, type(), c);
			assertNotNull(ret);
			assertNotNull(ret.getUid());
			assertNotNull(ret.getAnniversary());
			System.out.println("Created contact with uid: " + ret.getUid());

			ret = book.getContactFromId(token, type(), "" + ret.getUid());
			assertNotNull(ret);
			assertEquals(c.getFirstname(), ret.getFirstname());
			assertTrue(c.getPhones().size() > 0);
			assertNotNull(ret.getAnniversary());
			System.out.println("getContactFromId succeeded");

			ret.setFirstname("fooModified");
			ret = book.modifyContact(token, type(), ret);
			assertNotNull(ret);
			assertEquals("fooModified", ret.getFirstname());

			String uid = ret.getUid().toString();
			ret = book.removeContact(token, type(), uid);
			assertNotNull(ret);
			System.out.println("rm succeeded");

			ret = book.getContactFromId(token, type(), uid);
			assertNull(ret);

		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}

	public void testCreate() {
		if (p("book").equals("users")) {
			return;
		}

		try {
			Contact c = getTestContact();
			Contact ret = book.createContact(token, type(), c);
			assertNotNull(ret);
			assertNotNull(ret.getUid());
			System.out.println("Created contact with uid: " + ret.getUid());
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}

	public void testDoSync() {
		try {
			ContactChanges changes = book.getSync(token, type(), null);
			assertNotNull(changes);
			assertNotNull(changes.getLastSync());
			assertNotNull(changes.getRemoved());
			assertNotNull(changes.getUpdated());

			for (Contact c : changes.getUpdated()) {
				System.out.println("id: " + c.getUid() + " first: "
						+ c.getFirstname() + " last: " + c.getLastname()
						+ " mails: " + c.getEmails().size());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception on getSync");
		}

		System.out.println("Start getSync() speed test...");
		int count = 100;
		try {
			long time = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				ContactChanges changes = book.getSync(token, type(), null);
				assertNotNull(changes);
			}
			time = System.currentTimeMillis() - time;
			System.out.println(count + " getSync() calls took " + time
					+ "ms. Performing at " + (count * 1000) / time + "/sec");
		} catch (Exception e) {
			e.printStackTrace();
			fail("speed test failed");
		}

	}

	public void DISABLEDtestFillDb() throws Exception {
		for (int i = 0; i < 10000; i++) {
			Contact c = new Contact();
			c.setFirstname("ContactFirst" + i);
			c.setLastname("LastName" + i);
			c.addEmail("INTERNET;X-OBM-Ref1", new Email("contact" + i
					+ "@pouic" + i + ".com"));
			book.createContact(token, BookType.contacts, c);
			if ((i % 100) == 0) {
				System.out.println("created " + i + " / 10000 contacts.");
			}
		}
	}

	public void testSearchContact() {
		if (p("book").equals("users")) {
			return;
		}

		try {
			Contact c = getTestContact();
			c.setFirstname("ZZZZZsearch");
			c.setLastname("XXXXXsearch");

			Contact cUid = book.createContactWithoutDuplicate(token, type(), c);

			List<Contact> retZ = book.searchContact(token, "ZZZZ",
					Integer.MAX_VALUE);

			assertNotNull(retZ);
			assertTrue(retZ.size() > 0);

//			boolean findZ = false;
			for (Contact contact : retZ) {
				if (cUid.getUid().equals(contact.getUid())) {
//					findZ = true;
				}
				assertTrue(contact.getFirstname().startsWith("ZZZZ")
						|| contact.getLastname().startsWith("ZZZZ"));
			}
//			obm-sync doesn't index the contacts in real time
//			assertTrue(findZ);

			List<Contact> retX = book.searchContact(token, "ZZZZ",
					Integer.MAX_VALUE);

			assertNotNull(retX);
			assertTrue(retX.size() > 0);

//			boolean findX = false;
			for (Contact contact : retX) {
				if (cUid.getUid().equals(contact.getUid())) {
//					findX = true;
				}
				assertTrue(contact.getFirstname().startsWith("XXXX")
						|| contact.getLastname().startsWith("XXXX"));
			}
//			obm-sync doesn't index the contacts in real time
//			assertTrue(findX);

			List<Contact> retEmpty = book.searchContact(token, "ZZZZ", 0);
			assertNotNull(retEmpty);
			assertTrue(retEmpty.size() == 0);

			book.removeContact(token, type(), "" + cUid.getUid());
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}
	
	public void testEmptySearchContact() {
		try {
			List<Contact> retZ = book.searchContact(token, "",
					Integer.MAX_VALUE);
			assertNotNull(retZ);
			assertTrue(retZ.size() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}

	public void testSearchContactByEmail() {
		if (p("book").equals("users")) {
			return;
		}

		doSearch("please@minig.org");

		doSearch("thomas@zz.com");

		doSearch("dtc@zz.com");
	}

	public void testSearchLoop() {
		if (p("book").equals("users")) {
			return;
		}
		int COUNT = 1000;
		long time = System.currentTimeMillis();
		for (int i = 0; i < COUNT; i++) {
			doSearch("pl");
		}
		time = System.currentTimeMillis() - time;
		System.out.println(COUNT + " searches done in " + time + "ms. (avg: "
				+ (time / COUNT) + "ms)");
	}

	private void doSearch(String query) {
		try {
			List<Contact> ret = book.searchContact(token, query,
					Integer.MAX_VALUE);
			System.out.println("search.size: " + ret.size());
			for (Contact c : ret) {
				StringBuilder sb = new StringBuilder("id: " + c.getUid()
						+ " last: " + c.getLastname() + " first: "
						+ c.getFirstname() + " emails:");
				Map<String, Email> mails = c.getEmails();
				for (String l : mails.keySet()) {
					sb.append(' ');
					sb.append(mails.get(l).getEmail());
				}
				System.out.println(sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}

	public void testDoFolderSync() {
		try {
			FolderChanges changes = book.getFolderSync(token, null);
			assertNotNull(changes);
			assertNotNull(changes.getLastSync());
			assertNotNull(changes.getRemoved());
			assertNotNull(changes.getUpdated());

			for (Folder f : changes.getUpdated()) {
				System.out.println("id: " + f.getUid() + " name: "
						+ f.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception on getFolderSync");
		}

		System.out.println("Start getFolderSync() speed test...");
		int count = 100;
		try {
			long time = System.currentTimeMillis();
			for (int i = 0; i < count; i++) {
				FolderChanges changes = book.getFolderSync(token, null);
				assertNotNull(changes);
			}
			time = System.currentTimeMillis() - time;
			System.out.println(count + " getFolderSync() calls took " + time
					+ "ms. Performing at " + (count * 1000) / time + "/sec");
		} catch (Exception e) {
			e.printStackTrace();
			fail("speed test failed");
		}
	}
	
	public void testMarkUpdated() {
		if (p("book").equals("users")) {
			return;
		}
		try {
			Contact c = getTestContact();
			c.setFirstname("twinFirstContact");
			c.setLastname("twinLastContact");
			c.getEmails().clear();
			c.getPhones().clear();
			
			Contact ret = book.createContactWithoutDuplicate(token, type(), c);
			assertNotNull(ret);
			assertNotNull(ret.getUid());
			System.out.println("Created contact with uid: " + ret.getUid());
			
			ContactChanges changes = book.getSync(token, type(), null);
			Date lastUp = changes.getLastSync();
			System.out.println(lastUp);
			assertTrue(changes.getUpdated().size()>0);
			boolean find = false;
			for (Contact con : changes.getUpdated()) {
				if(con.getUid().equals(ret.getUid())){
					find = true;
				}
			}
			assertTrue(find);
			
			ret = book.createContactWithoutDuplicate(token, type(), c);
			assertNotNull(ret);
			assertNotNull(ret.getUid());
			System.out.println("Created contact with uid: " + ret.getUid());
			changes = book.getSync(token, type(), lastUp);
			assertTrue(changes.getUpdated().size()>0);
			find = false;
			for (Contact con : changes.getUpdated()) {
				if(con.getUid().equals(ret.getUid())){
					find = true;
				}
			}
			assertTrue(find);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("test thrown an exception");
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		book = new AddressBookLocator().locate(p("obm.sync.url"));
		assertNotNull(book);
		token = book.login(p("login"), p("password"), "junit");
		assertNotNull(token);
	}

	@Override
	protected void tearDown() throws Exception {
		book.logout(token);
		super.tearDown();
	}

	@Override
	protected Event getTestEvent() {
		// TODO Auto-generated method stub
		return null;
	}

}
