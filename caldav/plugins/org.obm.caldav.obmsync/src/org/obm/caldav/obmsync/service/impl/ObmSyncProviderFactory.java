/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync.service.impl;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.share.DavComponentType;
import org.obm.locator.client.LocatorClient;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.calendar.TodoClient;

/**
 * 
 * @author adrienp
 * 
 */
public class ObmSyncProviderFactory {

	protected static final Log logger = LogFactory
			.getLog(ObmSyncProviderFactory.class);

	protected String urlSync;

	private HttpClient hc;

	private static ObmSyncProviderFactory instance;

	public static ObmSyncProviderFactory getInstance() {
		if (instance == null) {
			instance = new ObmSyncProviderFactory();
		}
		return instance;
	}

	protected ObmSyncProviderFactory() {
		MultiThreadedHttpConnectionManager mtConMan = new MultiThreadedHttpConnectionManager();
		HttpClient ret = new HttpClient(mtConMan);
		HttpConnectionManagerParams mp = ret.getHttpConnectionManager()
				.getParams();
		
		mp.setDefaultMaxConnectionsPerHost(10);
		mp.setMaxTotalConnections(20);

		this.hc = ret;
	}

	public AbstractEventSyncClient getClient(DavComponentType type,
			String loginAtDomain) throws Exception {
		switch (type) {
		case VTODO:
			return new TodoClient(getObmSyncUrl(type, loginAtDomain), hc);
		case VEVENT:
		case VCALENDAR:
			return new CalendarClient(getObmSyncUrl(type, loginAtDomain), hc);
		}
		return null;
	}

	protected String getObmSyncUrl(DavComponentType type, String loginAtDomain)
			throws Exception {
		if (urlSync == null) {
			LocatorClient lc = new LocatorClient();
			String serverName = lc.locateHost("sync/obm_sync", loginAtDomain);
			if (serverName == null || "".equals(serverName)) {
				logger
						.error("Problem with obm locator: Unable to retrieve the address obm-sync");
				throw new Exception(
						"Problem with obm locator: Unable to retrieve the address obm-sync");
			}
			urlSync = "http://" + serverName + ":8080/obm-sync/services";

		}
		return urlSync;
	}
}
