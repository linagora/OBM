package org.obm.sync.client.calendar;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.services.ImportICalendarException;

public class AbstractEventSyncClientTest {

	private final static String OBM_SYNC_SERVICES_URL = "https://10.69.0.40/obm-sync/services";
	
	@Ignore
	@Test
	public void importICalendar() throws ImportICalendarException, AuthFault, URISyntaxException {
		final AbstractEventSyncClient eventSyncClient = new CalendarClient(OBM_SYNC_SERVICES_URL);
		final AccessToken token = eventSyncClient.login("admin", "admin", "test");
		
		final URI ics = new URI("file:/tmp/event-obm-test.ics");
		try {
			eventSyncClient.importICalendar(token, "admin", ics);
		} catch (ServerFault e) {
			Assert.fail(e.getMessage());
		}
	}
	
}
