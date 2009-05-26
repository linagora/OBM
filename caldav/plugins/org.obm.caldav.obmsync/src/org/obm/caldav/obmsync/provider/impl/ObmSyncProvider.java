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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.ObmSyncConfIni;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.service.IEventService;
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
		CalendarInfo myCalendar = getMyCalendar(token, userId);
		boolean find = false;
		for (Attendee att : event.getAttendees()) {
			if (myCalendar.getMail().equals(att.getEmail())) {
				find = true;
			}
		}
		if (!find) {
			Attendee att = new Attendee();
			att.setEmail(myCalendar.getMail());
			att.setState(ParticipationState.NEEDSACTION);
			att.setRequired(ParticipationRole.OPT);
			event.addAttendee(att);
		}
		cal.createEvent(token, getMyCalendar(token, userId).getUid(), event);
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

	@Override
	public void updateParticipationState(AccessToken token, String userId,
			Event event, String participationState) throws AuthFault,
			ServerFault {
		for (Attendee att : event.getAttendees()) {
			if (getMyCalendar(token, userId).getMail().equals(att.getEmail())) {
				if (IEventService.PARTICIPATION_STATE_ACCEPTED
						.equals(participationState)) {
					att.setState(ParticipationState.ACCEPTED);
				} else if (IEventService.PARTICIPATION_STATE_DECLINED
						.equals(participationState)) {
					att.setState(ParticipationState.DECLINED);
				} else if (IEventService.PARTICIPATION_STATE_NEEDSACTION
						.equals(participationState)) {
					att.setState(ParticipationState.NEEDSACTION);
				}
			}
		}
		cal.modifyEvent(token, getMyCalendar(token, userId).getUid(), event,
				true);
	}

	@Override
	public String getParticipationState(AccessToken token, String userId,
			Event event) throws AuthFault, ServerFault {
		for (Attendee att : event.getAttendees()) {
			if (getMyCalendar(token, userId).getMail().equals(att.getEmail())) {
				if (ParticipationState.ACCEPTED.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_ACCEPTED;
				} else if (ParticipationState.DECLINED.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_DECLINED;
				} else if (ParticipationState.DELEGATED.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_ACCEPTED;
				} else if (ParticipationState.TENTATIVE.equals(att.getState())) {
					return IEventService.PARTICIPATION_STATE_ACCEPTED;
				} else {
					return IEventService.PARTICIPATION_STATE_NEEDSACTION;
				}
			}
		}
		return "";
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
	public Set<String> getAllEvent(AccessToken token, String calendar)
			throws ServerFault, AuthFault {
		Date d = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 2);
		Date start = cal.getTime();

		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 4);
		Date end = cal.getTime();
		CalendarInfo c = getMyCalendar(token, calendar);
		List<Event> le = this.cal.getListEventsFromIntervalDate(token,
				getMyCalendar(token, calendar).getUid(), start, end);
		
		Set<String> isc = new HashSet<String>();
		for(Event ev : le){
			isc.add(this.cal.getICS(ev));
		}
		return isc;
		//return this.cal.getICS(le);
	}
}
