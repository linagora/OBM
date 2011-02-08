package org.obm.sync.locators;

import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.services.ICalendar;

/**
 * Creates a client for the {@link ICalendar} sync service
 * 
 * @author tom
 * 
 */
public class CalendarLocator {

	/**
	 * @param obmSyncServicesUrl
	 *            https://obm.buffy.kvm/obm-sync/services
	 * @return a calendar client
	 */
	public CalendarClient locate(String obmSyncServicesUrl) {
		CalendarClient ret = new CalendarClient(obmSyncServicesUrl);

		return ret;
	}

}
