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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.service.impl.CalDavInfo;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.caldav.utils.Constants;
import org.obm.locator.client.LocatorClient;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;

public abstract class AbstractObmSyncProvider implements ICalendarProvider {

	protected static final Log logger = LogFactory
			.getLog(AbstractObmSyncProvider.class);

	protected String urlSync;

	protected String getObmSyncUrl(String loginAtDomain) {
		if (urlSync == null) {
			LocatorClient lc = new LocatorClient();
			String serverName = lc.locateHost("sync/obm_sync", loginAtDomain);
			if (serverName == null || "".equals(serverName)) {
				return null;
			}
			urlSync = "http://" + serverName + ":8080/obm-sync/services";
			logger.info("locator returned the following url: " + urlSync);
		}
		return urlSync;
	}

	protected abstract AbstractEventSyncClient getClient(String loginAtDomain);

	protected AbstractEventSyncClient getClient(CalDavInfo caldavInfo) {
		return getClient(caldavInfo.getToken().getUser() + "@" + caldavInfo.getToken().getDomain());
	}

	public AccessToken login(String username, String password) {
		logger.info("login in obm-sync");
		String url = getObmSyncUrl(username);
		CalendarClient client = new CalendarClient(url);
		return client.login(username, password, "obm-caldav");
	}

	public void logout(AccessToken token) {
		if (token != null) {
			logger.info("logout in obm-sync " + token.getUser() + "@"
					+ token.getDomain());
			String url = getObmSyncUrl(token.getUser() + "@"
					+ token.getDomain());
			CalendarClient client = new CalendarClient(url);
			client.logout(token);
		}
	}

	protected AbstractObmSyncProvider() {
	}

	public Event getEventFromExtId(CalDavInfo caldavInfo,
			String extId) throws AuthFault, ServerFault {
		logger.info("search event with " + extId + "from obm-sync");
		return getClient(caldavInfo).getEventFromExtId(caldavInfo.getToken(), caldavInfo.getCalendar(), extId);
	}

	public Event createEvent(CalDavInfo caldavInfo, Event event)
			throws AuthFault, ServerFault {
		String uid = getClient(caldavInfo).createEvent(caldavInfo.getToken(), caldavInfo.getCalendar(), event);
		event.setUid(uid);
		return event;
	}

	public List<Event> createEventsFromICS(CalDavInfo caldavInfo,
			String ics, String extId) throws Exception {
		logger.info("Parse event ics");
		List<Event> events = getClient(caldavInfo).parseICS(caldavInfo.getToken(), ics);
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
			event.addAttendee(getAttendee(caldavInfo.getLoginAtDomain()));
			fixPrioriryForObm(event);
			logger.info("Create event with extId " + extId);
			String uid = getClient(caldavInfo).createEvent(caldavInfo.getToken(), caldavInfo.getCalendar(), event);
			event.setUid(uid);
		}

		return events;
	}

	public List<Event> updateEventFromICS(CalDavInfo caldavInfo,
			String ics, Event eventOrigin) throws Exception {
		logger.info("update event from ics data");
		List<Event> ret = new LinkedList<Event>();

		String uid = eventOrigin.getUid();
		List<Event> events = getClient(caldavInfo).parseICS(caldavInfo.getToken(), ics);
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
				}
			}
			fixPrioriryForObm(event);
			event = getClient(caldavInfo).modifyEvent(caldavInfo.getToken(), caldavInfo.getCalendar(), event, true);
			ret.add(event);
		}
		return ret;
	}

	public List<Event> getAll(CalDavInfo caldavInfo,
			EventType eventType) throws ServerFault, AuthFault {
		logger.info("Get all event from obm-sync");
		List<Event> events = getClient(caldavInfo).getAllEvents(caldavInfo.getToken(), caldavInfo.getCalendar(),
				eventType);

		for (Iterator<Event> it = events.iterator(); it.hasNext();) {
			Event ev = it.next();
			if (ev.getRecurrenceId() != null) {
				it.remove();
			}
		}
		return events;
	}

	public Map<Event, String> getICSEventsFromExtId(CalDavInfo caldavInfo, Set<String> listUidEvent) throws AuthFault,
			ServerFault {
		Map<Event, String> listICS = new HashMap<Event, String>();

		for (String extId : listUidEvent) {
			logger.info("Get ics event with extId " + extId + " from obm-sync");
			Event event = getClient(caldavInfo).getEventFromExtId(caldavInfo.getToken(), caldavInfo.getCalendar(),
					extId);
			if (event != null) {
				fixPrioriryForTB(event);
				listICS.put(event, getClient(caldavInfo).parseEvent(caldavInfo.getToken(), event));
			} else {
				event = new Event();
				event.setExtId(extId);
				event.setTimeUpdate(new Date());
				listICS.put(event, "");
			}
		}
		return listICS;
	}

	public void remove(CalDavInfo caldavInfo, Event event)
			throws AuthFault, ServerFault{
		logger.info("delete event with uid " + event.getUid()
				+ " from obm-sync");
		getClient(caldavInfo).removeEvent(caldavInfo.getToken(), caldavInfo.getCalendar(), event.getUid());
	}

	public void updateParticipationState(CalDavInfo caldavInfo,
			Event event, String participationState) throws AuthFault,
			ServerFault {
		for (Attendee att : event.getAttendees()) {
			if (caldavInfo.getCalendarAtDomain().equalsIgnoreCase(att.getEmail())) {
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
		getClient(caldavInfo).modifyEvent(caldavInfo.getToken(), caldavInfo.getCalendar(), event, true);
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

	public List<EventTimeUpdate> getAllEventTimeUpdate(CalDavInfo caldavInfo, EventType et) throws ServerFault, AuthFault {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		cal.setTimeInMillis(0);
		return getEventTimeUpdateFromIntervalDate(caldavInfo, cal
				.getTime(), null);
	}

	public List<EventTimeUpdate> getEventTimeUpdateFromIntervalDate(
			CalDavInfo caldavInfo, Date start, Date end)
			throws ServerFault, AuthFault {
		List<EventTimeUpdate> events = getClient(caldavInfo)
				.getEventTimeUpdateNotRefusedFromIntervalDate(caldavInfo.getToken(), caldavInfo.getCalendar(),
						start, end);
		for (Iterator<EventTimeUpdate> it = events.iterator(); it.hasNext();) {
			EventTimeUpdate ev = it.next();
			if (ev.getRecurrenceId() != null) {
				it.remove();
			}
		}
		return events;
	}

	public List<Event> getListEventsFromIntervalDate(CalDavInfo caldavInfo, Date start, Date end) throws AuthFault,
			ServerFault {
		return getClient(caldavInfo).getListEventsFromIntervalDate(caldavInfo.getToken(), caldavInfo.getCalendar(),
				start, end);
	}

	public CalendarInfo getRightsOnCalendar(CalDavInfo caldavInfo)
			throws AuthFault, ServerFault {
		CalendarInfo[] calInfos = getClient(caldavInfo).listCalendars(caldavInfo.getToken());
		for(CalendarInfo ci : calInfos){
			if(ci.getMail().equalsIgnoreCase(caldavInfo.getCalendarAtDomain())){
				return ci;
			}
		}
		return null;
	}

	public Date getLastUpdate(CalDavInfo caldavInfo)
			throws ServerFault, AuthFault {
		return getClient(caldavInfo).getLastUpdate(caldavInfo.getToken(), caldavInfo.getCalendar());
	}

	public FreeBusyRequest getFreeBusyRequest(CalDavInfo caldavInfo, String ics)
			throws ServerFault, AuthFault {
		return getClient(caldavInfo).parseICSFreeBusy(caldavInfo.getToken(), ics);
	}

	public List<FreeBusy> getFreeBusy(CalDavInfo caldavInfo, FreeBusyRequest fbr)
			throws ServerFault, AuthFault {
		return getClient(caldavInfo).getFreeBusy(caldavInfo.getToken(), fbr);
	}

	public String parseFreeBusyToICS(CalDavInfo caldavInfo, FreeBusy freeBusy)
			throws ServerFault, AuthFault {
		return getClient(caldavInfo).parseFreeBusyToICS(caldavInfo.getToken(), freeBusy);
	}
}
