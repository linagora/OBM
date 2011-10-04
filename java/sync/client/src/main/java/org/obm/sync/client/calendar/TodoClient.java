package org.obm.sync.client.calendar;

import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.locators.Locator;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TodoClient extends AbstractEventSyncClient {
	
	@Inject
	private TodoClient(SyncClientException syncClientException, Locator locator) {
		super("/todo", syncClientException, locator);
	}
	
}
