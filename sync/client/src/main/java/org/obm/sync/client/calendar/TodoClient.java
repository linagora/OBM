package org.obm.sync.client.calendar;

import org.apache.commons.httpclient.HttpClient;

/**
 * OBM sync client implementation for calendar synchronisations
 * 
 * @author tom
 * 
 */
public class TodoClient extends AbstractEventSyncClient {
	public TodoClient(String obmSyncServicesUrl) {
		super("/todo", obmSyncServicesUrl);
	}

	public TodoClient(String obmSyncServicesUrl, HttpClient cli) {
		super("/todo", obmSyncServicesUrl, cli);
	}
}
