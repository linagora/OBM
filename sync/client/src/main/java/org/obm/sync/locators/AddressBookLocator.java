package org.obm.sync.locators;

import org.obm.sync.client.book.BookClient;
import org.obm.sync.services.IAddressBook;

/**
 * Creates a client for the {@link IAddressBook} sync service
 * 
 * @author tom
 * 
 */
public class AddressBookLocator {

	/**
	 * @param obmSyncServicesUrl
	 *            https://obm.buffy.kvm/obm-sync/services
	 * @return an address book client
	 */
	public BookClient locate(String obmSyncServicesUrl) {
		BookClient ret = new BookClient(obmSyncServicesUrl);

		return ret;
	}

}
