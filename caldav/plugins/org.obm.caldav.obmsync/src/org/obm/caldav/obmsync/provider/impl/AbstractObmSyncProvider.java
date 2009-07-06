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
import java.nio.channels.GatheringByteChannel;
import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.naming.event.EventContext;
import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.ObmSyncConfIni;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.server.exception.AuthorizationException;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.caldav.utils.Constants;
import org.obm.caldav.utils.FileUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.CalendarLocator;

@SuppressWarnings("unused")
public abstract class AbstractObmSyncProvider implements ICalendarProvider{

	private static final Log logger = LogFactory
			.getLog(AbstractObmSyncProvider.class);
	protected AbstractEventSyncClient client;

	public static AccessToken login(String username, String password) {
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
		this.client = getObmSyncClient(url);
	}

	protected abstract AbstractEventSyncClient getObmSyncClient(String url);

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

	public Event getEventFromExtId(AccessToken token, String calendar,
			String uid) throws AuthFault, ServerFault {
		return client.getEventFromExtId(token, calendar, uid);
	}

	public Event createEvent(AccessToken token, String calendar, Event event)
			throws AuthFault, ServerFault {
		String uid = client.createEvent(token, calendar, event);
		event.setUid(uid);
		return event;
	}

	public List<Event> createEventsFromICS(AccessToken token, String login,
			String ics, String extId) throws Exception {
		List<Event> events = client.parseICS(token, ics);
		for (Event event : events) {
			if (event.getDate() == null) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(new Date());
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				event.setDate(cal.getTime());
			}

			event.setExtId(extId);
			event.addAttendee(getAttendee(login));
			fixPrioriryForObm(event);
			String uid = client.createEvent(token, login, event);
			event.setUid(uid);
		}

		return events;
	}

	public List<Event> updateEventFromICS(AccessToken token, String login,
			String ics, Event eventOrigin) throws Exception {

		List<Event> ret = new LinkedList<Event>();

		String uid = eventOrigin.getUid();
		List<Event> events = client.parseICS(token, ics);
		for (Event event : events) {
			event.setExtId(eventOrigin.getExtId());
			event.setUid(uid);
			if (event.getRecurrence() != null) {
				for (Event exception : event.getRecurrence()
						.getEventExceptions()) {
					if (exception.getExtId() == null
							|| "".equals(exception.getExtId())) {
						exception.setExtId(CalDavUtils.generateExtId());
					}
					
					exception.setAttendees(eventOrigin.getAttendees());
				}
			}
			fixPrioriryForObm(event);
			event = client.modifyEvent(token, login, event, true);
			ret.add(event);
		}
		return ret;
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

	public List<Event> getAll(AccessToken token, String calendar,
			EventType eventType) throws ServerFault, AuthFault {

		List<Event> events = this.client.getAllEvents(token, calendar,
				eventType);

		for (Iterator<Event> it = events.iterator(); it.hasNext();) {
			Event ev = it.next();
			if (ev.getParentId() != 0) {
				it.remove();
			}
		}

		return events;
	}

	public Map<Event, String> getICSEventsFromExtId(AccessToken token,
			String calendar, Set<String> listUidEvent) throws AuthFault,
			ServerFault {
		Map<Event, String> listICS = new HashMap<Event, String>();

		for (String id : listUidEvent) {
				Event event = null;
				if (id != null && !"".equals(id)) {
					event = client.getEventFromExtId(token, calendar, id);
				}

				if (event != null) {
					fixPrioriryForTB(event);
					listICS.put(event, client.parseEvent(token, event));
				} else {
					event = new Event();
					event.setExtId(id);
					event.setTimeUpdate(new Date());
					listICS.put(event, "");
				}
		}
		return listICS;
	}

	public void remove(AccessToken token, String login, Event event)
			throws AuthFault, ServerFault, AuthorizationException {
		for (Event excpt : event.getRecurrence().getEventExceptions()) {

		}
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

	

	protected Attendee getAttendee(String email) {
		Attendee att = new Attendee();
		att.setEmail(email);
		att.setRequired(ParticipationRole.OPT);
		att.setState(ParticipationState.ACCEPTED);
		return att;
	}

	private void fixPrioriryForObm(Event event) {
		if (event.getPriority() >= 6) {
			event.setPriority(1);
		} else if (event.getPriority() >= 3 && event.getPriority() < 6) {
			event.setPriority(2);
		} else if (event.getPriority() >= 1 && event.getPriority() < 3) {
			event.setPriority(3);
		}
	}

	private void fixPrioriryForTB(Event event) {
		if (event.getPriority() == 1) {
			event.setPriority(9);
		} else if (event.getPriority() == 2) {
			event.setPriority(5);
		} else if (event.getPriority() == 3) {
			event.setPriority(1);
		}
	}

	public List<EventTimeUpdate> getAllEventTimeUpdate(AccessToken token,
			String calendar, EventType et) throws ServerFault, AuthFault {
		
		List<EventTimeUpdate> events = client.getAllEventTimeUpdate(token, calendar, et);

		for (Iterator<EventTimeUpdate> it = events.iterator(); it.hasNext();) {
			EventTimeUpdate ev = it.next();
			if (ev.getParentId() != 0) {
				it.remove();
			}
		}
		return events;
	}
}
