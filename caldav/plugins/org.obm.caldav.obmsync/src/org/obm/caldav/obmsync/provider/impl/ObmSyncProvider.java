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

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.ObmSyncConfIni;
import org.obm.caldav.obmsync.exception.AuthorizationException;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.service.IEventService;
import org.obm.caldav.utils.FileUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.CalendarLocator;

@SuppressWarnings("unused")
public class ObmSyncProvider implements ICalendarProvider {

	private static final Log logger = LogFactory.getLog(ObmSyncProvider.class);
	private CalendarClient cal;
	private boolean connected;

	private static ICalendarProvider instance;

	public static ICalendarProvider getInstance() {
		if (instance == null) {
			instance = new ObmSyncProvider();
		}
		return instance;
	}

	private ObmSyncProvider() {
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = obmCalendarParameters
				.getPropertyValue("obmsync.server.url");
		this.cal = new CalendarLocator().locate(url);
	}

	public CalendarInfo getMyCalendar(AccessToken token, String userId)
			throws ServerFault, AuthFault {
		CalendarInfo[] listCalInfo = cal.listCalendars(token);
		for (CalendarInfo calInfo : listCalInfo) {
			if (calInfo.getMail().equals(userId)) {
				return calInfo;
			}
		}
		return null;
	}

	public Event getEventFromExtId(AccessToken token, String userId, String uid)
			throws AuthFault, ServerFault {
		return cal.getEventFromExtId(token, getMyCalendar(token, userId)
				.getUid(), uid);
	}

	public Event createEvent(AccessToken token, String userId, Event event)
			throws AuthFault, ServerFault {
		cal.createEvent(token, getMyCalendar(token, userId).getUid(), event);
		return event;
	}

	@Override
	public Event updateOrCreateEvent(AccessToken token, String login,
			String ics, String extId) throws Exception {
		Event event = cal.getEventFromExtId(token, login, extId);
		if (event != null) {
			String uid = event.getUid();
			InputStream is = new ByteArrayInputStream(ics.getBytes());
			event = cal.getEvent(is);
			event.setExtId(extId);
			event.setUid(uid);
			
			event = cal.modifyEvent(token, login, event, true);
		} else {
			InputStream is = new ByteArrayInputStream(ics.getBytes());
			event = cal.getEvent(is);
			event.setExtId(extId);
			Attendee att = new Attendee();
			att.setEmail(login);
			att.setState(ParticipationState.NEEDSACTION);
			att.setRequired(ParticipationRole.OPT);
			event.addAttendee(att);
			
			String uid = cal.createEvent(token, login, event);
			event.setUid(uid);
		}
		
		return event;
	}

	public void logout(AccessToken token) {
		cal.logout(token);
	}

	@Override
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String userId, Date start, Date end) throws AuthFault, ServerFault {
		return cal.getListEventsFromIntervalDate(token, getMyCalendar(token,
				userId).getUid(), start, end);
	}

	public String getUserEmail(AccessToken token) throws AuthFault, ServerFault {
		return cal.getUserEmail(token);
	}

	@Override
	public AccessToken login(String login, String password) {
		return cal.login(login, password, "obm-caldav");
	}

	@Override
	public Set<CalendarInfo> getListCalendars(AccessToken token)
			throws ServerFault, AuthFault {

		Set<CalendarInfo> list = new HashSet<CalendarInfo>(Arrays.asList(cal
				.listCalendars(token)));
		return list;
	}

	@Override
	public List<Event> getAllEvents(AccessToken token, String userId)
			throws ServerFault, AuthFault {
		return this.cal.getAllEvents(token, getMyCalendar(token, userId)
				.getUid());
	}

	@Override
	public Map<String, String> getICSEventsFromExtId(AccessToken token,
			String userId, Set<String> listUidEvent) throws AuthFault,
			ServerFault {
		Map<String, String> listICS = new HashMap<String, String>();

		for (String id : listUidEvent) {
			Event event = cal.getEventFromExtId(token, userId, id);
			if (event != null) {
				listICS.put(id, cal.getICS(event));
			} else {
				listICS.put(id, "");
			}
		}
		return listICS;
	}

	@Override
	public void remove(AccessToken token, String login, String extId) throws AuthFault, ServerFault, AuthorizationException {
		Event event = cal.getEventFromExtId(token, login, extId);
		cal.removeEvent(token, login, event.getUid());
		
		Event ret = cal.getEventFromId(token, login, event.getUid());
		if(ret != null){
			throw new AuthorizationException("You don't have the right to delete this event");
		}
	}
}
