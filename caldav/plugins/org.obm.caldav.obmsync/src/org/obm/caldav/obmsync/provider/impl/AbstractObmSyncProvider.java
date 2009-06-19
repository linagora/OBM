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
import java.util.UUID;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.ObmSyncConfIni;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.server.AuthorizationException;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.caldav.utils.Constants;
import org.obm.caldav.utils.FileUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.CalendarLocator;

@SuppressWarnings("unused")
public abstract class AbstractObmSyncProvider {

	private static final Log logger = LogFactory.getLog(AbstractObmSyncProvider.class);
	protected AbstractEventSyncClient client;
	
	public static AccessToken login(String username, String password){
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = obmCalendarParameters
				.getPropertyValue("obmsync.server.url");
		
		CalendarClient client = new CalendarClient(url); 
		return client.login(username, password, "obm-caldav");
	}
	
	public static void logout(AccessToken token) {
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = obmCalendarParameters
				.getPropertyValue("obmsync.server.url");
		
		CalendarClient client = new CalendarClient(url); 
		client.logout(token);
	}


	protected AbstractObmSyncProvider() {
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = obmCalendarParameters
		.getPropertyValue("obmsync.server.url");
		this.client = initObmSyncProvider(url);
	}
	
	protected abstract AbstractEventSyncClient initObmSyncProvider(String url);

	protected AbstractObmSyncProvider(AbstractEventSyncClient client) {
		ObmSyncConfIni obmCalendarParameters = new ObmSyncConfIni();
		String url = obmCalendarParameters
				.getPropertyValue("obmsync.server.url");
		this.client = new CalendarLocator().locate(url);
	}

	public CalendarInfo getMyCalendar(AccessToken token, String userId)
			throws ServerFault, AuthFault {
		CalendarInfo[] listCalInfo = client.listCalendars(token);
		for (CalendarInfo calInfo : listCalInfo) {
			if (calInfo.getMail().equals(userId)) {
				return calInfo;
			}
		}
		return null;
	}

	public Event getEventFromExtId(AccessToken token, String userId, String uid)
			throws AuthFault, ServerFault {
		return client.getEventFromExtId(token, getMyCalendar(token, userId)
				.getUid(), uid);
	}

	public Event createEvent(AccessToken token, String userId, Event event)
			throws AuthFault, ServerFault {
		String uid = client.createEvent(token, getMyCalendar(token, userId)
				.getUid(), event);
		event.setUid(uid);
		return event;
	}

	public List<Event> createEventsFromICS(AccessToken token, String login,
			String ics, String extId) throws Exception {
		List<Event> events = client.parseICS(token, ics);
		for (Event event : events) {
			event.setExtId(extId);
			Attendee att = new Attendee();
			att.setEmail(login);
			att.setState(ParticipationState.NEEDSACTION);
			att.setRequired(ParticipationRole.OPT);
			event.addAttendee(att);
			String uid = client.createEvent(token, login, event);
			event.setUid(uid);
		}
		return events;
	}

	public List<Event> updateEventFromICS(AccessToken token, String login,
			String ics, Event eventOrigin) throws Exception {
		List<Event> ret = new LinkedList<Event>();

		String uid = eventOrigin.getUid();
		List<Event> events = client.parseICS(token,ics);
		for(Event event : events){
			event.setExtId(eventOrigin.getExtId());
			event.setUid(uid);
			if( event.getRecurrence() != null ){
				for(Event exception : event.getRecurrence().getEventExceptions()){
					if(exception.getExtId() == null || "".equals(exception.getExtId())){
						exception.setExtId(CalDavUtils.generateExtId());
					}
				}
			}
			event = client.modifyEvent(token, login, event, true);
			ret.add(event);
		}
		return ret;
	}

	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String userId, Date start, Date end) throws AuthFault, ServerFault {
		return client.getListEventsFromIntervalDate(token, getMyCalendar(token,
				userId).getUid(), start, end);
	}

	public String getUserEmail(AccessToken token) throws AuthFault, ServerFault {
		return client.getUserEmail(token);
	}

	public Set<CalendarInfo> getListCalendars(AccessToken token)
			throws ServerFault, AuthFault {

		Set<CalendarInfo> list = new HashSet<CalendarInfo>(Arrays.asList(client
				.listCalendars(token)));
		return list;
	}

	public List<Event> getAll(AccessToken token, String userId, EventType eventType)
			throws ServerFault, AuthFault {
		return this.client.getAllEvents(token, getMyCalendar(token, userId)
				.getUid(),eventType);
	}

	public Map<String, String> getICSEventsFromExtId(AccessToken token,
			String userId, Set<String> listUidEvent) throws AuthFault,
			ServerFault {
		Map<String, String> listICS = new HashMap<String, String>();

		for (String id : listUidEvent) {
			Event event = client.getEventFromExtId(token, userId, id);
			if (event != null) {
				listICS.put(id, client.parseEvent(token,event));
			} else {
				listICS.put(id, "");
			}
		}
		return listICS;
	}

	public void remove(AccessToken token, String login, Event event)
			throws AuthFault, ServerFault, AuthorizationException {
		client.removeEvent(token, login, event.getUid());

		Event ret = client.getEventFromId(token, login, event.getUid());
		if (ret != null) {
			throw new AuthorizationException(
					"You don't have the right to delete this event");
		}
	}

	public void updateParticipationState(AccessToken token, String userId,
			Event event, String participationState) throws AuthFault,
			ServerFault {
		CalendarInfo ci = getMyCalendar(token, userId);
		for (Attendee att : event.getAttendees()) {
			if (getMyCalendar(token, userId).getMail().equals(att.getEmail())) {
				if (Constants.PARTICIPATION_STATE_ACCEPTED
						.equals(participationState)) {
					att.setState(ParticipationState.ACCEPTED);
				} else if (Constants.PARTICIPATION_STATE_DECLINED
						.equals(participationState)) {
					att.setState(ParticipationState.DECLINED);
				} else if (Constants.PARTICIPATION_STATE_NEEDSACTION
						.equals(participationState)) {
					att.setState(ParticipationState.NEEDSACTION);
				}
			}
		}
		client.modifyEvent(token, ci.getUid(), event, true);
	}
}
