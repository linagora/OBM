package org.obm.sync.client.calendar;

import org.apache.commons.httpclient.HttpClient;

/**
 * OBM sync client implementation for calendar synchronisations
 * 
 * @author tom
 * 
 */
public class CalendarClient extends AbstractEventSyncClient {
	public CalendarClient(String obmSyncServicesUrl) {
		super("/calendar", obmSyncServicesUrl);
	}

	public CalendarClient(String obmSyncServicesUrl, HttpClient cli) {
		super("/calendar", obmSyncServicesUrl, cli);
	}
}
