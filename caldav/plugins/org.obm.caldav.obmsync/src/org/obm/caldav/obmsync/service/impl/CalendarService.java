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
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncEventProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncTodoProvider;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.exception.AuthenticationException;
import org.obm.caldav.server.share.DavComponentName;
import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.caldav.utils.Constants;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.items.EventChanges;

public class CalendarService implements ICalendarService {
	
	protected Log logger = LogFactory.getLog(getClass());
	
	private Map<DavComponentName, ICalendarProvider> providers;
	private AccessToken token;
	private String calendar;
	private String loginAtDomaine;

	public CalendarService() {
		this.providers = new HashMap<DavComponentName, ICalendarProvider>();
		this.providers.put(DavComponentName.VCALENDAR, ObmSyncEventProvider
				.getInstance());
		this.providers.put(DavComponentName.VEVENT, ObmSyncEventProvider
				.getInstance());
		this.providers.put(DavComponentName.VTODO, ObmSyncTodoProvider
				.getInstance());
	}

	public List<Event> updateOrCreateEvent(String ics, String extId)
			throws Exception {
		Event event = getVCalendarProvider().getEventFromExtId(token, calendar,
				extId);
		if (event != null) {
			return getVCalendarProvider().updateEventFromICS(token, calendar,
					ics, event);
		} else {
			return getVCalendarProvider().createEventsFromICS(token, calendar,
					ics, extId);
		}
	}

	@Override
	public Map<Event, String> getICSFromExtId(Set<String> listExtIdEvent)
			throws Exception {
		return getVCalendarProvider().getICSEventsFromExtId(token, calendar,
				listExtIdEvent);
	}

	@Override
	public void removeOrUpdateParticipationState(String extId) throws Exception {
		Event event = getVCalendarProvider().getEventFromExtId(token, calendar,
				extId);
		if (event != null) {
			if (loginAtDomaine.equals(event.getOwnerEmail())) {
				getVCalendarProvider().remove(token, calendar, event);
			} else {
				getVCalendarProvider()
						.updateParticipationState(token, calendar, event,
								Constants.PARTICIPATION_STATE_DECLINED);
			}
		}
	}

	@Override
	public String getICSName(Event event) {
		return event.getExtId() + ".ics";
	}

	@Override
	public String getICSName(EventTimeUpdate etu) {
		return etu.getExtId() + ".ics";
	}

	@Override
	public List<Event> getAll(DavComponentName componant) throws Exception {
		return providers.get(componant).getAll(token, calendar);
	}

	@Override
	public List<EventTimeUpdate> getAllLastUpdate(CompFilter cf)
			throws Exception {
		return getAllLastUpdate(cf.getName(), cf);
	}

	@Override
	public List<EventTimeUpdate> getAllLastUpdate(DavComponentName componant)
			throws Exception {
		return getAllLastUpdate(componant, new CompFilter());
	}

	private List<EventTimeUpdate> getAllLastUpdate(DavComponentName componant,
			CompFilter cf) throws Exception {
		if (cf.getTimeRange() != null
				&& (cf.getTimeRange().getStart() != null || cf.getTimeRange()
						.getEnd() != null)) {
			Date start = cf.getTimeRange().getStart();
			Date end = cf.getTimeRange().getEnd();
			if (start == null) {
				GregorianCalendar gc = new GregorianCalendar();
				gc.set(Calendar.YEAR, gc.get(Calendar.YEAR) - 1);
				start = gc.getTime();
			}

			if (end == null) {
				GregorianCalendar gc = new GregorianCalendar();
				gc.set(Calendar.YEAR, gc.get(Calendar.YEAR) + 1);
				end = gc.getTime();
			}

			return providers.get(componant).getEventTimeUpdateFromIntervalDate(
					token, calendar, cf.getTimeRange().getStart(), end);
		}
		return providers.get(componant).getAllEventTimeUpdate(token, calendar);
	}

	@Override
	public boolean getSync(Date lastSync) throws Exception {
		EventChanges ec = providers.get(DavComponentName.VEVENT).getSync(token,
				calendar, lastSync);
		if (ec.getUpdated().length != 0 || ec.getRemoved().length != 0) {
			return true;
		} else {
			ec = providers.get(DavComponentName.VTODO).getSync(token, calendar,
					lastSync);
			if (ec.getUpdated().length != 0 || ec.getRemoved().length != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasRightsOnCalendar() throws Exception {
		return getVCalendarProvider().hasRightsOnCalendar(token, calendar);
	}

	@Override
	public String getLastUpdate() throws Exception {
		return getVCalendarProvider().getLastUpdate(token, calendar).toString();
	}

	@Override
	public void login(String loginAtDomaine, String password, String calendar)
			throws AuthenticationException {
		this.token = this.providers.get(DavComponentName.VEVENT).login(
				loginAtDomaine, password);
		if (this.token == null || this.token.getSessionId() == null
				|| "".equals(this.token.getSessionId())) {
			throw new AuthenticationException();
		}
		String[] split = loginAtDomaine.split("@");
		token.setUser(split[0]);
		token.setDomain(split[1]);
		this.calendar = calendar;
		this.loginAtDomaine = loginAtDomaine;
	}

	@Override
	public void logout() {
		this.providers.get(DavComponentName.VEVENT).logout(token);
	}

	@Override
	public Map<String, String> getFreeBuzy(String ics) throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		FreeBusyRequest fbr = getVCalendarProvider().getFreeBusyRequest(token,
				ics);
		List<FreeBusy> freeBusys = getVCalendarProvider().getFreeBusy(token,
				fbr);
		logger.info(freeBusys.size());
		for (FreeBusy freeBusy : freeBusys) {
			logger.info("freebusy " + freeBusy.getAtt().getEmail() + " dstart: " + fbr.getStart() + " dend: "+ fbr.getEnd());
			logger.info("freebusy found " + freeBusy.getFreeBusyIntervals().size() + " events.");
			String icsFB = getVCalendarProvider().parseFreeBusyToICS(token,
					freeBusy);
			ret.put(freeBusy.getAtt().getEmail(), icsFB);
		}
		return ret;
	}

	private ICalendarProvider getVCalendarProvider() {
		return this.providers.get(DavComponentName.VCALENDAR);
	}

}
