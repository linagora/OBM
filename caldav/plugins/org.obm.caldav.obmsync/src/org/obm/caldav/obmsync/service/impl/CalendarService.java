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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncEventProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncTodoProvider;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.exception.AuthenticationException;
import org.obm.caldav.server.share.CalendarResource;
import org.obm.caldav.server.share.CalendarResourceICS;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.DavComponentType;
import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.caldav.utils.Constants;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.items.EventChanges;

public class CalendarService implements ICalendarService {

	protected Log logger = LogFactory.getLog(getClass());

	private Map<DavComponentType, ICalendarProvider> providers;
	private CalDavInfo caldavInfo;
	private CalDavRigth caldavRigth;

	public CalendarService() {
		this.providers = new HashMap<DavComponentType, ICalendarProvider>();
		this.providers.put(DavComponentType.VCALENDAR, ObmSyncEventProvider
				.getInstance());
		this.providers.put(DavComponentType.VEVENT, ObmSyncEventProvider
				.getInstance());
		this.providers.put(DavComponentType.VTODO, ObmSyncTodoProvider
				.getInstance());
	}
	
	@Override
	public DavComponent updateOrCreateEvent(String compUrl, String ics, String extId)
			throws Exception {
		Event event = getVCalendarProvider().getEventFromExtId(caldavInfo,
				extId);
		if (caldavRigth.isWritable()) {
			List<Event> listEvent;
			if (event != null) {
				listEvent = getVCalendarProvider().updateEventFromICS(caldavInfo,
						ics, event);
			} else {
				listEvent = getVCalendarProvider().createEventsFromICS(caldavInfo,
						ics, extId);
			}
			if(listEvent.iterator().hasNext()){
				return EventConverter.convert(listEvent.iterator().next(), compUrl);
			}
		}
		return new CalendarResource("", compUrl, new Date(), DavComponentType.VEVENT);
	}

	@Override
	public List<CalendarResourceICS> getICSFromExtId(String compUrl,Set<String> listExtIdEvent)
			throws Exception {
		LinkedList<CalendarResourceICS> ret = new LinkedList<CalendarResourceICS>();
		Map<Event, String> map = getVCalendarProvider().getICSEventsFromExtId(caldavInfo,
				listExtIdEvent);
		for(Entry<Event, String> entry : map.entrySet()){
			Event e = entry.getKey();
			String ics = entry.getValue();
			ret.add(EventConverter.convert(e, compUrl, ics));
		}
		return ret;
	}

	@Override
	public void removeOrUpdateParticipationState(String extId) throws Exception {
		Event event = getVCalendarProvider().getEventFromExtId(caldavInfo,
				extId);
		if (event != null) {
			if (caldavRigth.isWritable()) {
				if (caldavInfo.getCalendar().equals(event.getOwnerEmail())) {
					getVCalendarProvider().remove(caldavInfo, event);
				} else {
					getVCalendarProvider().updateParticipationState(caldavInfo,
							event, Constants.PARTICIPATION_STATE_DECLINED);
				}
			}
		}
	}

//	@Override
//	public List<Event> getAll(DavComponentType componant) throws Exception {
//		return providers.get(componant).getAll(caldavInfo);
//	}

	@Override
	public List<DavComponent> getAllLastUpdate(CompFilter cf)
			throws Exception {
		return getAllLastUpdate(cf.getDavComponentURL(), cf.getName(), cf);
	}

	@Override
	public List<DavComponent> getAllLastUpdate(String compUrl, DavComponentType type)
			throws Exception {
		return  getAllLastUpdate(compUrl, type, new CompFilter()); 
	}

	private List<DavComponent> getAllLastUpdate(String compUrl,DavComponentType componant,
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
			List<EventTimeUpdate> items = providers.get(componant).getEventTimeUpdateFromIntervalDate(
					caldavInfo, cf.getTimeRange().getStart(), end);
			return EventConverter.convert(items, compUrl, componant);
		}
		List<EventTimeUpdate> items = providers.get(componant).getAllEventTimeUpdate(caldavInfo); 
		return EventConverter.convert(items, compUrl, componant);
	}

	@Override
	public boolean getSync(Date lastSync) throws Exception {
		EventChanges ec = providers.get(DavComponentType.VEVENT).getSync(
				caldavInfo, lastSync);
		if (ec.getUpdated().length != 0 || ec.getRemoved().length != 0) {
			return true;
		} else {
			ec = providers.get(DavComponentType.VTODO).getSync(caldavInfo,
					lastSync);
			if (ec.getUpdated().length != 0 || ec.getRemoved().length != 0) {
				return true;
			}
		}
		return false;
	}

	private boolean hasRightsOnCalendar() throws Exception {
		if (this.caldavRigth == null) {
			CalendarInfo ci = getVCalendarProvider().getRightsOnCalendar(
					caldavInfo);
			if (ci != null) {
				this.caldavRigth = new CalDavRigth(ci.isRead(), ci.isWrite());
			} else {
				this.caldavRigth = new CalDavRigth();
			}
			logger.info("Rigth on calendar " + caldavInfo.getCalendar()
					+ " : Read[" + caldavRigth.isReadable() + "] Write["
					+ caldavRigth.isWritable() + "]");
		}
		return this.caldavRigth.isReadable() || this.caldavRigth.isWritable();
	}

//	@Override
	public String getLastUpdate() throws Exception {
		return getVCalendarProvider().getLastUpdate(caldavInfo).toString();
	}

	@Override
	public boolean login(String loginAtDomain, String password, String calendar,
			String calendarAtDomain) throws AuthenticationException {
		AccessToken token = this.providers.get(DavComponentType.VEVENT).login(
				loginAtDomain, password);
		if (token == null || token.getSessionId() == null
				|| "".equals(token.getSessionId())) {
			throw new AuthenticationException();
		}
		String[] split = loginAtDomain.split("@");
		token.setUser(split[0]);
		token.setDomain(split[1]);
		this.caldavInfo = new CalDavInfo(token, calendar, calendarAtDomain,
				loginAtDomain);
		try {
			return hasRightsOnCalendar();
		} catch (Exception e) {
			throw new AuthenticationException();
		}
	}

	@Override
	public void logout() {
		this.providers.get(DavComponentType.VCALENDAR).logout(
				caldavInfo.getToken());
	}

	@Override
	public Map<String, String> getFreeBuzy(String ics) throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		FreeBusyRequest fbr = getVCalendarProvider().getFreeBusyRequest(
				caldavInfo, ics);
		List<FreeBusy> freeBusys = getVCalendarProvider().getFreeBusy(
				caldavInfo, fbr);
		logger.info(freeBusys.size());
		for (FreeBusy freeBusy : freeBusys) {
			logger.info("freebusy " + freeBusy.getAtt().getEmail()
					+ " dstart: " + fbr.getStart() + " dend: " + fbr.getEnd());
			logger.info("freebusy found "
					+ freeBusy.getFreeBusyIntervals().size() + " events.");
			String icsFB = getVCalendarProvider().parseFreeBusyToICS(
					caldavInfo, freeBusy);
			ret.put(freeBusy.getAtt().getEmail(), icsFB);
		}
		return ret;
	}

	private ICalendarProvider getVCalendarProvider() {
		return this.providers.get(DavComponentType.VCALENDAR);
	}
}
