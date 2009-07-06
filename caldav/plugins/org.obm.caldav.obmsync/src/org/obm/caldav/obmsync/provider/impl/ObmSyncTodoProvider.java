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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.ObmSyncConfIni;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.server.exception.AuthorizationException;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.caldav.utils.FileUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.calendar.TodoClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

@SuppressWarnings("unused")
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
	protected AbstractEventSyncClient getObmSyncClient(String url) {
		return new TodoClient(url);
	}
	
	@Override
	public EventChanges getSync(AccessToken token, String userId, Date lastSync)
	throws AuthFault, ServerFault {
		logger.info("Get sync["+lastSync+"] from obm-sync");
		return client.getSync(token, userId, lastSync);
	}

	@Override
	public List<Event> getAll(AccessToken token, String calendar)
			throws ServerFault, AuthFault {
		logger.info("Get all Event from obm-sync");
		return super.getAll(token, calendar, EventType.VTODO);
	}

	@Override
	public List<EventTimeUpdate> getAllEventTimeUpdate(AccessToken token,
			String calendar) throws ServerFault, AuthFault {
		logger.info("Get all EventTimeUpdate from obm-sync");
		return super.getAllEventTimeUpdate(token, calendar, EventType.VTODO);
	}
}
