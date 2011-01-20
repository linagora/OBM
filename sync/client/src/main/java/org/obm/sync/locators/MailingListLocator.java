package org.obm.sync.locators;

import org.obm.sync.client.mailingList.MailingListClient;
import org.obm.sync.services.ICalendar;

/**
 * Creates a client for the {@link ICalendar} sync service
 * 
 * @author tom
 * 
 */
public class MailingListLocator {

	/**
	 * @param obmSyncServicesUrl
	 *            https://obm.buffy.kvm/obm-sync/services
	 * @return a calendar client
	 */
	public MailingListClient locate(String obmSyncServicesUrl) {
		MailingListClient ret = new MailingListClient(obmSyncServicesUrl);
		return ret;
	}

}
