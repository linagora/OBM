package org.obm.sync.client;

import org.obm.sync.ObmSyncTestCase;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.CalendarLocator;

public class LoginTests extends ObmSyncTestCase {

	public void testCalendarLogin() {
		CalendarClient cal = new CalendarLocator().locate(p("obm.sync.url"));

		AccessToken token = cal.login(p("login"), p("password"), "junit");
		assertNotNull(token);
		assertNotNull(token.getSessionId());
		assertNotNull(token.getVersion());

		System.out.println("version: " + token.getVersion());

		cal.logout(token);

		try {
			cal.listCategories(token);
			fail("list categories should not work after logout");
		} catch (AuthFault e) {
			e.printStackTrace();
		} catch (ServerFault e) {
		}
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
