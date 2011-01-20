package org.obm.sync.locators;

import org.obm.sync.client.calendar.TodoClient;
import org.obm.sync.services.ICalendar;

/**
 * Creates a client for the {@link ICalendar} sync service
 * 
 * @author tom
 * 
 */
public class TaskLocator {

	/**
	 * @param obmSyncServicesUrl
	 *            https://obm.buffy.kvm/obm-sync/services
	 * @return a calendar client
	 */
	public TodoClient locate(String obmSyncServicesUrl) {
		TodoClient ret = new TodoClient(obmSyncServicesUrl);

		return ret;
	}

}
