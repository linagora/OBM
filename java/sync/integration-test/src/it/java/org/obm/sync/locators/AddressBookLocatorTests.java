package org.obm.sync.locators;

import org.obm.sync.ObmSyncTestCase;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.obm.sync.services.IAddressBook;

public class AddressBookLocatorTests extends ObmSyncTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLocate() {
		AddressBookLocator locator = new AddressBookLocator();

		IAddressBook cal = locator.locate(p("obm.sync.url"));
		assertNotNull(cal);
	}

	@Override
	protected Contact getTestContact() {
		return null;
	}

	@Override
	protected Event getTestEvent() {
		return null;
	}

}
