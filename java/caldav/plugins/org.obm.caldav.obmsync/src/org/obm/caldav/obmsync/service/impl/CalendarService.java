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

import java.util.ArrayList;
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
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.exception.AuthenticationException;
import org.obm.caldav.server.share.CalDavRigth;
import org.obm.caldav.server.share.CalDavToken;
import org.obm.caldav.server.share.CalendarResource;
import org.obm.caldav.server.share.CalendarResourceICS;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.DavComponentType;
import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.items.EventChanges;

public class CalendarService implements ICalendarService {

	protected Log logger = LogFactory.getLog(getClass());

	public CalendarService() {
	}

	@Override
	public DavComponent updateOrCreateEvent(CalDavToken token, String compUrl, String ics, String extId)
			throws Exception {
		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(DavComponentType.VCALENDAR, token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		try {
			Event event = obmClient.getEventFromExtId(obmToken, token
					.getCalendarName(), extId);
			if (hasRightsOnCalendar(token).isWritable()) {
				List<Event> listEvent = new LinkedList<Event>();
				if (event != null) {
					logger.info("update event from ics data");

					String uid = event.getUid();
					List<Event> events = obmClient.parseICS(obmToken, ics);
					for (Event ev : events) {
						ev.setExtId(event.getExtId());
						ev.setUid(uid);
						if (ev.getRecurrence() != null) {
							for (Event exception : ev.getRecurrence()
									.getEventExceptions()) {
								if (exception.getExtId() == null
										|| "".equals(exception.getExtId())) {
									exception.setExtId(CalDavUtils
											.generateExtId());
								}
							}
						}
						addAttendee(token, ev);
						ev = obmClient.modifyEvent(obmToken, token
								.getCalendarName(), ev, true);
						listEvent.add(ev);
					}
				} else {
					logger.info("Parse event ics");
					List<Event> events = obmClient.parseICS(obmToken, ics);
					for (Event ev : events) {
						if (ev.getDate() == null) {
							Calendar cal = new GregorianCalendar();
							cal.setTime(new Date());
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
							ev.setDate(cal.getTime());
						}
						ev.setExtId(extId);

						addAttendee(token, ev);
						logger.info("Create event with extId " + extId);
						obmClient.createEvent(obmToken,
								token.getCalendarName(), ev);
						ev = obmClient.getEventFromExtId(obmToken, token
								.getCalendarName(), ev.getExtId());
						listEvent.add(ev);
					}
				}
				if (listEvent.iterator().hasNext()) {
					return EventConverter.convert(listEvent.iterator().next(),
							compUrl);
				}
			}
		} catch (Exception e) {
			throw new Exception();
		} finally {
			logout(obmClient, obmToken);
		}
		return new CalendarResource("", compUrl, new Date(),
				DavComponentType.VEVENT);
	}

	@Override
	public List<CalendarResourceICS> getICSFromExtId(CalDavToken token,
			String compUrl, Set<String> listExtIdEvent) throws Exception {
		LinkedList<CalendarResourceICS> ret = new LinkedList<CalendarResourceICS>();
		Map<Event, String> map = new HashMap<Event, String>();

		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(DavComponentType.VCALENDAR,
						token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		try {
			for (String extId : listExtIdEvent) {
				logger.info("Get ics event with extId " + extId
						+ " from obm-sync");
				Event event = obmClient.getEventFromExtId(obmToken, token
						.getCalendarName(), extId);
				if (event != null) {
					map.put(event, obmClient.parseEvent(obmToken, event));
				} else {
					event = new Event();
					event.setExtId(extId);
					event.setTimeUpdate(new Date());
					map.put(event, "");
				}
			}
			for (Entry<Event, String> entry : map.entrySet()) {
				Event e = entry.getKey();
				String ics = entry.getValue();
				ret.add(EventConverter.convert(e, compUrl, ics));
			}
		} catch (Exception e) {
			throw new Exception();
		} finally {
			logout(obmClient, obmToken);
		}
		return ret;
	}

	@Override
	public void remove(CalDavToken token, String extId) throws Exception {
		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(DavComponentType.VCALENDAR,
						token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		try {
			Event event = obmClient.getEventFromExtId(obmToken, token
					.getCalendarName(), extId);
			if (event != null) {
				if (hasRightsOnCalendar(token).isWritable()) {
					if (token.getCalendarName().equals(event.getOwnerEmail())) {
						obmClient.removeEvent(obmToken,
								token.getCalendarName(), event.getUid());
					} else {
						for (Attendee att : event.getAttendees()) {
							if (token.getCalendarNameAtDomain()
									.equalsIgnoreCase(att.getEmail())) {
								att.setState(ParticipationState.DECLINED);
							}
						}
						obmClient.modifyEvent(obmToken,
								token.getCalendarName(), event, true);
					}
				}
			}
		} catch (Exception e) {
			throw new Exception();
		} finally {
			logout(obmClient, obmToken);
		}
	}

	@Override
	public List<DavComponent> getAllLastUpdate(CalDavToken token, CompFilter cf)
			throws Exception {
		return getAllLastUpdate(token, cf.getDavComponentURL(), cf.getName(),
				cf);
	}

	@Override
	public List<DavComponent> getAllLastUpdate(CalDavToken token,
			String compUrl, DavComponentType type) throws Exception {
		return getAllLastUpdate(token, compUrl, type, new CompFilter());
	}

	private List<DavComponent> getAllLastUpdate(CalDavToken token,
			String compUrl, DavComponentType componant, CompFilter cf)
			throws Exception {
		Date start = null;
		Date end = null;

		if (cf.getTimeRange() != null
				&& (cf.getTimeRange().getStart() != null || cf.getTimeRange()
						.getEnd() != null)) {
			start = cf.getTimeRange().getStart();
			end = cf.getTimeRange().getEnd();
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
		} else {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			cal.setTimeInMillis(0);
			start = cal.getTime();
			end = null;
		}
		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(componant, token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		List<EventTimeUpdate> items = new ArrayList<EventTimeUpdate>(0);
		try {
			items = obmClient.getEventTimeUpdateNotRefusedFromIntervalDate(
					obmToken, token.getCalendarName(), start, end);
			for (Iterator<EventTimeUpdate> it = items.iterator(); it.hasNext();) {
				EventTimeUpdate ev = it.next();
				if (ev.getRecurrenceId() != null) {
					it.remove();
				}
			}
		} catch (Exception e) {
			throw new Exception();
		} finally {
			logout(obmClient, obmToken);
		}
		return EventConverter.convert(items, compUrl, componant);
	}

	@Override
	public boolean getSync(CalDavToken token, Date lastSync) throws Exception {
		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(DavComponentType.VEVENT,
						token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		try {
			EventChanges ec = obmClient.getSync(obmToken, token
					.getCalendarName(), lastSync);
			if (ec.getUpdated().length != 0 || ec.getRemoved().length != 0) {
				return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			logout(obmClient, obmToken);
		}
		try {
			obmClient = ObmSyncProviderFactory.getInstance().getClient(
					DavComponentType.VTODO, token.getLoginAtDomain());
			obmToken = login(obmClient, token);
			EventChanges ec = obmClient.getSync(obmToken, token
					.getCalendarName(), lastSync);
			if (ec.getUpdated().length != 0 || ec.getRemoved().length != 0) {
				return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			logout(obmClient, obmToken);
		}
		return false;
	}

	@Override
	public Date getLastUpdate(CalDavToken token) throws Exception {
		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(DavComponentType.VCALENDAR,
						token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		try {
			return obmClient.getLastUpdate(obmToken, token.getCalendarName());
		} catch (Exception e) {
			throw e;
		} finally {
			logout(obmClient, obmToken);
		}
	}

	@Override
	public Map<String, String> getFreeBusy(CalDavToken token, String ics)
			throws Exception {
		Map<String, String> ret = new HashMap<String, String>();
		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(DavComponentType.VCALENDAR,
						token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		try {
			FreeBusyRequest fbr = obmClient.parseICSFreeBusy(obmToken, ics);

			List<FreeBusy> freeBusys = obmClient.getFreeBusy(obmToken, fbr);
			for (FreeBusy freeBusy : freeBusys) {
				logger.info("freebusy " + freeBusy.getAtt().getEmail()
						+ " dstart: " + fbr.getStart() + " dend: "
						+ fbr.getEnd());
				logger.info("freebusy found "
						+ freeBusy.getFreeBusyIntervals().size() + " events.");
				String icsFB = obmClient.parseFreeBusyToICS(obmToken, freeBusy);
				ret.put(freeBusy.getAtt().getEmail(), icsFB);
			}
			return ret;
		} catch (Exception e) {
			throw e;
		} finally {
			logout(obmClient, obmToken);
		}
	}

	@Override
	public CalDavRigth hasRightsOnCalendar(CalDavToken token) throws Exception {
		AbstractEventSyncClient obmClient = ObmSyncProviderFactory
				.getInstance().getClient(DavComponentType.VCALENDAR,
						token.getLoginAtDomain());
		AccessToken obmToken = login(obmClient, token);
		CalDavRigth ret = null;
		CalendarInfo ci = null;
		try {
			CalendarInfo[] calInfos = obmClient.listCalendars(obmToken);
			for (CalendarInfo info : calInfos) {
				if (info.getMail().equalsIgnoreCase(
						token.getCalendarNameAtDomain())) {
					ci = info;
				}
			}
			if (ci != null) {
				ret = new CalDavRigth(ci.isRead(), ci.isWrite());
			} else {
				ret = new CalDavRigth();
			}
			logger.info("Rigth on calendar " + token.getCalendarName()
					+ " : Read[" + ret.isReadable() + "] Write["
					+ ret.isWritable() + "]");
			return ret;
		} catch (Exception e) {
			throw e;
		} finally {
			logout(obmClient, obmToken);
		}
	}

	private void addAttendee(CalDavToken token, Event event) {
		boolean find = false;
		for (Attendee att : event.getAttendees()) {
			if (token.getLoginAtDomain().equals(att.getEmail())) {
				find = true;
				break;
			}
		}
		if (!find) {
			Attendee att = new Attendee();
			att.setEmail(token.getLoginAtDomain());
			att.setRequired(ParticipationRole.OPT);
			att.setState(ParticipationState.ACCEPTED);
			event.addAttendee(att);
		}
	}

	private AccessToken login(AbstractEventSyncClient obmSyncClient,
			CalDavToken caldavToken) throws AuthenticationException {
		AccessToken token = obmSyncClient.login(caldavToken.getLoginAtDomain(),
				caldavToken.getPassword(), "obm-caldav");
		if (token == null || token.getSessionId() == null
				|| "".equals(token.getSessionId())) {
			throw new AuthenticationException();
		}
		String[] split = caldavToken.getLoginAtDomain().split("@");
		token.setUser(split[0]);
		token.setDomain(split[1]);
		return token;
	}

	private void logout(AbstractEventSyncClient obmSyncClient, AccessToken token) {
		obmSyncClient.logout(token);
	}

}
