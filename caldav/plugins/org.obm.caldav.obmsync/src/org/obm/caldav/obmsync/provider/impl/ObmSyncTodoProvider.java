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

package org.obm.caldav.obmsync.provider.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.service.impl.CalDavInfo;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.TodoClient;
import org.obm.sync.items.EventChanges;

public class ObmSyncTodoProvider extends AbstractObmSyncProvider  {

	protected static final Log logger = LogFactory.getLog(ObmSyncTodoProvider.class);
	
	private static ICalendarProvider instance;
	
	public static ICalendarProvider getInstance(){
		if(instance == null){
			instance = new ObmSyncTodoProvider();
		}
		return instance;
	}
	
	protected ObmSyncTodoProvider() {
		super(); 
	}
	
	
	@Override
	protected AbstractEventSyncClient getClient(String loginAtDomain) {
		try {
			return new TodoClient(getObmSyncUrl(loginAtDomain));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}
	
	@Override
	public EventChanges getSync(CalDavInfo caldavInfo, Date lastSync)
	throws AuthFault, ServerFault {
		logger.info("Get sync["+lastSync+"] from obm-sync");
		return getClient(caldavInfo).getSync(caldavInfo.getToken(), caldavInfo.getCalendar(), lastSync);
	}

	@Override
	public List<Event> getAll(CalDavInfo caldavInfo)
			throws ServerFault, AuthFault {
		logger.info("Get all Event from obm-sync");
		return super.getAll(caldavInfo, EventType.VTODO);
	}

	@Override
	public List<EventTimeUpdate> getAllEventTimeUpdate(CalDavInfo caldavInfo) throws ServerFault, AuthFault {
		logger.info("Get all EventTimeUpdate from obm-sync");
		return super.getAllEventTimeUpdate(caldavInfo, EventType.VTODO);
	}
}
