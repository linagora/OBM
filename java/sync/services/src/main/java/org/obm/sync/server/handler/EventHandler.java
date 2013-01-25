/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server.handler;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.NotAllowedException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.CalendarItemsParser;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.Participation.State;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;
import org.obm.sync.server.Request;
import org.obm.sync.server.XmlResponder;
import org.obm.sync.services.ImportICalendarException;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.calendar.CalendarBindingImpl;
import fr.aliacom.obm.common.session.SessionManagement;
import fr.aliacom.obm.utils.LogUtils;

/**
 * Handles the following urls :
 * 
 * <code>/calendar/getSync?sid=xxx</code>
 * <code>/calendar/removeEvent?sid=xxx</code>
 * <code>/calendar/removeEventByExtId?sid=xxx</code>
 * <code>/calendar/modifyEvent?sid=xxx</code>
 * <code>/calendar/createEvent?sid=xxx</code>
 * <code>/calendar/getEventFromId?sid=xxx</code>
 * <code>/calendar/listCalendars?sid=xxx</code>
 * <code>/calendar/getEventTwinKeys?sid=xxx</code>
 * <code>/calendar/getRefusedKeys?sid=xxx</code>
 * <code>/calendar/getUserEmail?sid=xxx</code>
 * <code>/calendar/getEventFromExtId?sid=xxx</code>
 * <code>/calendar/getListEventsFromIntervalDate?sid=xxx</code>
 * <code>/calendar/getAllEvents?sid=xxx</code>
 * <code>/calendar/parseEvent?sid=xxx</code>
 * <code>/calendar/parseEvents?sid=xxx</code>
 * <code>/calendar/parseICS?sid=xxx</code>
 * <code>/calendar/getEventParticipationStateFromIntervalDate?sid=xxx</code>
 * <code>/calendar/getEventTimeUpdateFromIntervalDate?sid=xxx</code>
 * <code>/calendar/getLastUpdate?sid=xxx</code>
 * <code>/calendar/parseICSFreeBusy?sid=xxx</code>
 * <code>/calendar/getFreeBusy?sid=xxx</code>
 * <code>/calendar/parseFreeBusyToICS?sid=xxx</code>
 * <code>/calendar/importICalendar</code>
 * <code>/calendar/purge</code>
 * <code>/calendar/getCalendarMetadata?sid=xxx&amp;calendar=calendar_obm_email1&amp;calendar=calendar_obm_email2&amp;...</code>
 */
@Singleton
public class EventHandler extends SecureSyncHandler {

	private CalendarBindingImpl binding;
	private CalendarItemsParser cip;

	@Inject
	public EventHandler(SessionManagement sessionManagement, CalendarBindingImpl calendarBindingImpl) {
		super(sessionManagement);
		binding = calendarBindingImpl;
		cip = new CalendarItemsParser();
	}

	@Override
	public void handle(Request request,
			XmlResponder responder) throws Exception {
		
		AccessToken at = getCheckedToken(request);
		String res = searchAndInvokeMethod(request, responder, at);
		logger.debug(LogUtils.prefix(at) + res);
	}

	private String searchAndInvokeMethod(Request request, XmlResponder responder, AccessToken at)
		throws ServerFault, SAXException,
			IOException, FactoryConfigurationError, Exception {
		String method = request.getMethod();
		if (method.equals("getSync")) {
			return getSync(at, request, responder);
		} else if (method.equals("getSyncInRange")) {
			return getSyncInRange(at, request, responder);
		} else if (method.equals("getSyncWithSortedChanges")) {
			return getSyncWithSortedChanges(at, request, responder);
		} else if (method.equals("getSyncEventDate")) {
			return getSyncEventDate(at, request, responder);
		} else if (method.equals("removeEvent")) {
			return removeEvent(at, request, responder);
		} else if (method.equals("removeEventByExtId")) {
			return removeEventByExtId(at, request, responder);
		} else if (method.equals("modifyEvent")) {
			return modifyEvent(at, request, responder);
		} else if (method.equals("createEvent")) {
			return createEvent(at, request, responder);
		} else if (method.equals("getEventFromId")) {
			return getEventFromId(at, request, responder);
		} else if (method.equals("listCalendars")) {
			return listCalendars(at, responder);
		} else if (method.equals("listResources")) {
			return listResources(at, responder);
		} else if (method.equals("listCategories")) {
			return listCategories(at, responder);
		} else if (method.equals("getEventTwinKeys")) {
			return getEventTwinKeys(at, request, responder);
		} else if (method.equals("getRefusedKeys")) {
			return getRefusedKeys(at, request, responder);
		} else if (method.equals("getUserEmail")) {
			return getUserEmail(at, responder);
		} else if (method.equals("getEventFromExtId")) {
			return getEventFromExtId(at, request, responder);
		} else if (method.equals("getEventObmIdFromExtId")) {
			return getEventObmIdFromExtId(at, request, responder);
		} else if (method.equals("getListEventsFromIntervalDate")) {
			return getListEventsFromIntervalDate(at, request, responder);
		} else if (method.equals("getAllEvents")) {
			return getAllEvents(at, request, responder);
		} else if (method.equals("parseEvent")) {
			return parseEvent(at, request, responder);
		} else if (method.equals("parseEvents")) {
			return parseEvents(at, request, responder);
		} else if (method.equals("parseICS")) {
			return parseICS(at, request, responder);
		} else if (method.equals("parseICSFreeBusy")) {
			return parseICSFreeBusy(at, request, responder);
		} else if (method
				.equals("getEventParticipationStateWithAlertFromIntervalDate")) {
			return getEventParticipationStateWithAlertFromIntervalDate(at, request,
					responder);
		} else if (method
				.equals("getEventTimeUpdateNotRefusedFromIntervalDate")) {
			return getEventTimeUpdateNotRefusedFromIntervalDate(at, request, responder);
		} else if (method.equals("getLastUpdate")) {
			return getLastUpdate(at, request, responder);
		} else if (method.equals("isWritableCalendar")) {
			return isWritableCalendar(at, request, responder);
		} else if (method.equals("getFreeBusy")) {
			return getFreeBusy(at, request, responder);
		} else if (method.equals("parseFreeBusyToICS")) {
			return parseFreeBusyToICS(at, request, responder);
		} else if (method.equals("changeParticipationState")) {
			return changeParticipationState(at, request, responder);
		} else if (method.equals("importICalendar")) {
			return importICalendar(at, request, responder);
		} else if (method.equals("purge")) {
			return purge(at, request, responder);
		}
		else if (method.equals("getCalendarMetadata")) {
			return getCalendarMetadata(at, request, responder);
		}
		else if (method.equals("getResourceMetadata")) {
			return getResourceMetadata(at, request, responder);
		}
		else {
			logger.error(LogUtils.prefix(at) + "cannot handle method '" + method + "'");
			return "";
		}
	} 

	private String getCalendarMetadata(AccessToken at, Request request,
			XmlResponder responder) throws ServerFault {
		String[] calendarEmails = request.getParameterValues("calendar");
		CalendarInfo[] lc = binding.getCalendarMetadata(at, calendarEmails);
		return responder.sendCalendarInformations(lc);
	}

	private String getResourceMetadata(AccessToken at, Request request,
			XmlResponder responder) throws ServerFault {
		String[] resourceEmails = request.getParameterValues("resource");
		ResourceInfo[] ri = binding.getResourceMetadata(at, resourceEmails);
		return responder.sendResourceInformation(ri);
	}

	private String parseFreeBusyToICS(
			AccessToken at, Request request, XmlResponder responder)
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		String fbAsString = request.getParameter("freebusy");
		Document doc = DOMUtils.parse(fbAsString);
		FreeBusy fb = cip.parseFreeBusy(doc.getDocumentElement());
		String ics = binding.parseFreeBusyToICS(at, fb);
		return responder.sendString(ics);
	}

	private String getFreeBusy(
			AccessToken at, Request request, XmlResponder responder)
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		String fbAsString = request.getParameter("freebusyrequest");
		Document doc = DOMUtils.parse(fbAsString);
		FreeBusyRequest fb = cip.parseFreeBusyRequest(doc.getDocumentElement());
		List<FreeBusy> ev = binding.getFreeBusy(at, fb);
		return responder.sendListFreeBusy(ev);
	}

	private String parseICSFreeBusy(
			AccessToken at, Request request, XmlResponder responder)
			throws ServerFault {
		String ics = request.getParameter("ics");
		FreeBusyRequest freeBusy = binding.parseICSFreeBusy(at, ics);
		return responder.sendFreeBusyRequest(freeBusy);
	}

	private String getCalendar(Request request) {
		return request.getParameter("calendar");
	}
	
	private String getLastUpdate(
			AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, NotAllowedException {
		Date d = binding.getLastUpdate(at, getCalendar(request));
		return responder.sendLong(d.getTime());
	}

	private String getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken at, Request request, XmlResponder responder) throws ServerFault, NotAllowedException {
		String calendar = getCalendar(request);
		Date start = DateHelper.asDate(request.getParameter("start"));
		Date end = null;
		if (StringUtils.isNotEmpty("end")) {
			end = DateHelper.asDate(request.getParameter("end"));
		}
		List<EventTimeUpdate> e = 
			binding.getEventTimeUpdateNotRefusedFromIntervalDate(at, calendar, start, end);

		return responder.sendListEventTimeUpdate(e);
	}

	private String getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken at, Request request, XmlResponder responder)
			throws ServerFault {
		List<EventParticipationState> e = 
			binding.getEventParticipationStateWithAlertFromIntervalDate(at,
				getCalendar(request),
				DateHelper.asDate(request.getParameter("start")),
				DateHelper.asDate(request.getParameter("end")));

		return responder.sendListEventParticipationState(e);
	}

	private String parseICS(
			AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, Exception {
		String ics = request.getParameter("ics");
		List<Event> events = binding.parseICS(at, ics);
		return responder.sendListEvent(events);
	}

	private String parseEvents(
			AccessToken at, Request request, XmlResponder responder)
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		List<Event> events = getEvents(request);
		String ics = binding.parseEvents(at, events);
		return responder.sendString(ics);
	}

	private String parseEvent(
			AccessToken at, Request request, XmlResponder responder)
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		Event event = getEvent(request);
		String ics = binding.parseEvent(at, event);
		return responder.sendString(ics);
	}

	private String getAllEvents(
			AccessToken at, Request request, XmlResponder responder)
			throws ServerFault {
			List<Event> e = 
				binding.getAllEvents(at, getCalendar(request),
						EventType.valueOf(request.getParameter("eventType")));
			return responder.sendListEvent(e);
	}

	private String getListEventsFromIntervalDate(
			AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, NotAllowedException {
		List<Event> e = binding.getListEventsFromIntervalDate(at, 
				getCalendar(request),
				DateHelper.asDate(request.getParameter("start")),
				DateHelper.asDate(request.getParameter("end")));

		return responder.sendListEvent(e);
	}

	private String getEventObmIdFromExtId(AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, EventNotFoundException, NotAllowedException {
		
		EventObmId id = binding.getEventObmIdFromExtId(at, getCalendar(request), getExtId(request, "extId"));
		if (id != null) {
			return responder.sendInt(id.getObmId());
		}
		return responder.sendError("not found");		
	}
	
	private String getEventFromExtId(AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, EventNotFoundException, NotAllowedException {
		Event e = binding.getEventFromExtId(at, getCalendar(request), getExtId(request, "extId"));
		return responder.sendEvent(e);
	}

	private String getUserEmail(AccessToken at, XmlResponder responder) throws ServerFault {
		String ue = binding.getUserEmail(at);
		return responder.sendString(ue);
	}

	private String listCategories(AccessToken at, XmlResponder responder) throws ServerFault {
		List<Category> ret = binding.listCategories(at);
		return responder.sendCategories(ret);
	}

	private String getRefusedKeys(AccessToken at, Request request, XmlResponder responder)
		throws ServerFault, NotAllowedException {
		KeyList ret = binding.getRefusedKeys(at, 
				getCalendar(request),
				DateHelper.asDate(request.getParameter("since")));
		return responder.sendKeyList(ret);
	}

	private String getEventTwinKeys(AccessToken at, Request request, XmlResponder responder)
		throws ServerFault, SAXException, IOException, FactoryConfigurationError, NotAllowedException {
		KeyList kl = binding.getEventTwinKeys(at, getCalendar(request), getEvent(request));
		return responder.sendKeyList(kl);
	}

	private String listCalendars(AccessToken at, XmlResponder responder) throws ServerFault {
		CalendarInfo[] lc = binding.listCalendars(at);
		return responder.sendCalendarInformations(lc);
	}

	private String listResources(AccessToken at, XmlResponder responder) throws ServerFault {
		ResourceInfo[] resourceInfo = binding.listResources(at);
		return responder.sendResourceInformation(resourceInfo);
	}

	private String getEventFromId(AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, EventNotFoundException, NotAllowedException {
		Event e = binding.getEventFromId(at, getCalendar(request), getObmId(request, "id"));
		return responder.sendEvent(e);
	}

	private String createEvent(
			AccessToken at, Request request, XmlResponder responder)
		throws ServerFault, SAXException, IOException, FactoryConfigurationError, EventAlreadyExistException, NotAllowedException {
		EventObmId ev = binding.createEvent(at,	getCalendar(request), getEvent(request), getNotificationOption(request));
		return responder.sendInt(ev.getObmId());
	}
	
	private Event getEvent(Request request) throws SAXException,
			IOException, FactoryConfigurationError {
		Document doc = DOMUtils.parse(p(request, "event"));
		Event toModify = cip.parseEvent(doc.getDocumentElement());
		return toModify;
	}

	private boolean getNotificationOption(Request request) {
		String notificationParam = request.getParameter("notification");
		if (notificationParam != null) {
			return Boolean.valueOf(notificationParam);
		}
		return true;
	}
	
	private List<Event> getEvents(Request request) throws SAXException,
			IOException, FactoryConfigurationError {
		Document doc = DOMUtils.parse(p(request, "events"));
		List<Event> toModify = cip.parseListEvents(doc.getDocumentElement());
		return toModify;
	}

	private String modifyEvent(
			AccessToken at, Request request, XmlResponder responder)
		throws ServerFault, SAXException, IOException, FactoryConfigurationError, NotAllowedException {
		Event ev = binding.modifyEvent(at, getCalendar(request),
				getEvent(request), Boolean.valueOf(request.getParameter("updateAttendees")),
				getNotificationOption(request)
				);
		if (ev != null) {
			return responder.sendEvent(ev);
		}
		return responder.sendError("Event did not exist.");
	}
	
	private String removeEvent(AccessToken at, Request request, XmlResponder responder) 
			throws ServerFault, EventNotFoundException, NotAllowedException {
		
		String calendar = getCalendar(request);
		EventObmId obmId = getObmId(request, "id");
		int sequence = i(request, "sequence", 0);
		binding.removeEventById(at, calendar, obmId, sequence, getNotificationOption(request));
		return responder.sendBoolean(true);
	}
	
	private String removeEventByExtId(
			AccessToken at, Request request, XmlResponder responder)
		throws ServerFault, NotAllowedException {
		Event ev = binding.removeEventByExtId(at, getCalendar(request),
				getExtId(request, "extId"),
				i(request, "sequence", 0),
				getNotificationOption(request));
		if (ev != null) {
			return responder.sendEvent(ev);
		}
		return responder.sendError("Event did not exist.");

	}

	private String getSyncEventDate(
			AccessToken at, Request request, XmlResponder responder)
		throws ServerFault, NotAllowedException {
		EventChanges ret = binding.getSyncEventDate(at, 
				getCalendar(request),
				DateHelper.asDate(request.getParameter("lastSync")));
		return responder.sendCalendarChanges(ret);
	}

	private String getSyncWithSortedChanges(AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, NotAllowedException {
		SyncRange syncRange = null;
		Date after = DateHelper.asDate(request.getParameter("syncRangeAfter"), null);
		Date before = DateHelper.asDate(request.getParameter("syncRangeBefore"), null);
		
		if (after != null) {
			syncRange = new SyncRange(before, after);
		}
		
		EventChanges ret = binding.getSyncWithSortedChanges(at, 
							getCalendar(request),
							DateHelper.asDate(request.getParameter("lastSync")), syncRange);
		
		return responder.sendCalendarChanges(ret);
	}

	private String getSync(
			AccessToken at, Request request, XmlResponder responder)
			throws ServerFault, NotAllowedException {
			EventChanges ret = binding.getSync(at, 
					getCalendar(request),
					DateHelper.asDate(request.getParameter("lastSync")));
			return responder.sendCalendarChanges(ret);
	}
	
	private String getSyncInRange(
			AccessToken at, Request request, XmlResponder responder)
	throws ServerFault, NotAllowedException {
		final Date after = DateHelper.asDate(request.getParameter("syncRangeAfter"));
		final Date before = DateHelper.asDate(request.getParameter("syncRangeBefore"));
		SyncRange syncRange = null;
		if(after != null || before != null){
			syncRange = new SyncRange(before, after);
		}
		EventChanges ret = binding.getSyncInRange(at, 
				getCalendar(request),
				DateHelper.asDate(request.getParameter("lastSync")), syncRange);
		return responder.sendCalendarChanges(ret);
	}

	private String isWritableCalendar(
			AccessToken at, Request request, XmlResponder responder)
		throws ServerFault {
		boolean ret = binding.isWritableCalendar(at, getCalendar(request));
		logger.info("isWritable(" + at.getUserEmail() + ", "
				+ getCalendar(request) + ") => " + ret);
		return responder.sendBoolean(ret);
	}
	
	private String changeParticipationState(AccessToken at,
			Request request, XmlResponder responder) throws ServerFault, ParseException, EventNotFoundException, NotAllowedException {
		
		boolean recursive = getRecursive(request);
		boolean success = false;
		
		if (recursive) {
			success = binding.changeParticipationState(at, getCalendar(request),
					getExtId(request, "extId"), 
					getParticipation(request),
					i(request, "sequence", 0),
					getNotificationOption(request));
		} else {
			success = binding.changeParticipationState(at, getCalendar(request),
					getExtId(request, "extId"), getRecurrenceId(request),
					getParticipation(request),
					i(request, "sequence", 0),
					getNotificationOption(request));
		}
		return responder.sendBoolean(success);
	}
	
	private String importICalendar(final AccessToken token, final Request request,
			XmlResponder responder) throws ImportICalendarException, ServerFault, NotAllowedException {
		final String calendar = request.getParameter("calendar");
		final String ics = request.getParameter("ics");
		
		int countEvent = binding.importICalendar(token, calendar, ics);
		return responder.sendInt(countEvent);
	}
	
	private String purge(final AccessToken at, final Request request, final XmlResponder responder) throws ServerFault, NotAllowedException {
		binding.purge(at, getCalendar(request));
		return responder.sendBoolean(true);
	}
	
	private EventExtId getExtId(Request request, String tagName) {
		return new EventExtId(request.getParameter(tagName));
	}
	
	private EventObmId getObmId(Request request, String tagName) {
		return new EventObmId(request.getParameter(tagName));
	}
	
	private RecurrenceId getRecurrenceId(Request request) {
		String recurrenceIdParam = request.getParameter("recurrenceId");
		if (recurrenceIdParam != null) {
				return new RecurrenceId(recurrenceIdParam);
		}
		return null;
	}
	
	private Boolean getRecursive(Request request) {
		String recursiveParam = request.getParameter("recursive");
		if(recursiveParam != null) {
			return Boolean.valueOf(recursiveParam);
		}
		return true;
	}

	private Participation getParticipation(Request request) {
		return Participation.builder()
							.state(State.getValueOf(request.getParameter("state")))
							.comment(request.getParameter("comment"))
							.build();
	}

}
