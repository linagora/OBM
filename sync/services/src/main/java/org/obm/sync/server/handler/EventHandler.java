/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server.handler;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.lang.StringUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.CalendarItemsParser;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;
import org.obm.sync.server.ParametersSource;
import org.obm.sync.server.XmlResponder;
import org.obm.sync.services.ImportICalendarException;
import org.obm.sync.utils.DOMUtils;
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
	public void handle(String method, ParametersSource params,
			XmlResponder responder) throws Exception {
		
		AccessToken at = getCheckedToken(params);
		String res = searchAndInvokeMethod(method, params, responder, at);
		logger.debug(LogUtils.prefix(at) + res);
	}

	private String searchAndInvokeMethod(String method, ParametersSource params, 
			XmlResponder responder, AccessToken at) 
		throws ServerFault, SAXException,
			IOException, FactoryConfigurationError, Exception {
		if (method.equals("getSync")) {
			return getSync(at, params, responder);
		} else if (method.equals("getSyncInRange")) {
			return getSyncInRange(at, params, responder);
		} else if (method.equals("getSyncWithSortedChanges")) {
			return getSyncWithSortedChanges(at, params, responder);
		} else if (method.equals("getSyncEventDate")) {
			return getSyncEventDate(at, params, responder);
		} else if (method.equals("removeEvent")) {
			return removeEvent(at, params, responder);
		} else if (method.equals("removeEventByExtId")) {
			return removeEventByExtId(at, params, responder);
		} else if (method.equals("modifyEvent")) {
			return modifyEvent(at, params, responder);
		} else if (method.equals("createEvent")) {
			return createEvent(at, params, responder);
		} else if (method.equals("getEventFromId")) {
			return getEventFromId(at, params, responder);
		} else if (method.equals("listCalendars")) {
			return listCalendars(at, responder);
		} else if (method.equals("listCategories")) {
			return listCategories(at, responder);
		} else if (method.equals("getEventTwinKeys")) {
			return getEventTwinKeys(at, params, responder);
		} else if (method.equals("getRefusedKeys")) {
			return getRefusedKeys(at, params, responder);
		} else if (method.equals("getUserEmail")) {
			return getUserEmail(at, responder);
		} else if (method.equals("getEventFromExtId")) {
			return getEventFromExtId(at, params, responder);
		} else if (method.equals("getEventObmIdFromExtId")) {
			return getEventObmIdFromExtId(at, params, responder);
		} else if (method.equals("getListEventsFromIntervalDate")) {
			return getListEventsFromIntervalDate(at, params, responder);
		} else if (method.equals("getAllEvents")) {
			return getAllEvents(at, params, responder);
		} else if (method.equals("parseEvent")) {
			return parseEvent(at, params, responder);
		} else if (method.equals("parseEvents")) {
			return parseEvents(at, params, responder);
		} else if (method.equals("parseICS")) {
			return parseICS(at, params, responder);
		} else if (method.equals("parseICSFreeBusy")) {
			return parseICSFreeBusy(at, params, responder);
		} else if (method
				.equals("getEventParticipationStateWithAlertFromIntervalDate")) {
			return getEventParticipationStateWithAlertFromIntervalDate(at, params,
					responder);
		} else if (method
				.equals("getEventTimeUpdateNotRefusedFromIntervalDate")) {
			return getEventTimeUpdateNotRefusedFromIntervalDate(at, params, responder);
		} else if (method.equals("getLastUpdate")) {
			return getLastUpdate(at, params, responder);
		} else if (method.equals("isWritableCalendar")) {
			return isWritableCalendar(at, params, responder);
		} else if (method.equals("getFreeBusy")) {
			return getFreeBusy(at, params, responder);
		} else if (method.equals("parseFreeBusyToICS")) {
			return parseFreeBusyToICS(at, params, responder);
		} else if (method.equals("changeParticipationState")) {
			return changeParticipationState(at, params, responder);
		} else if (method.equals("importICalendar")) {
			return importICalendar(at, params, responder);
		} else if (method.equals("purge")) {
			return purge(at, params, responder);
		}
		else if (method.equals("getCalendarMetadata")) {
			return getCalendarMetadata(at, params, responder);
		} else {
			logger.error(LogUtils.prefix(at) + "cannot handle method '" + method + "'");
			return "";
		}
	} 

	private String getCalendarMetadata(AccessToken at, ParametersSource params,
			XmlResponder responder) throws ServerFault {
		String[] calendarEmails = params.getParameterValues("calendar");
		CalendarInfo[] lc = binding.getCalendarMetadata(at, calendarEmails);
		return responder.sendCalendarInformations(lc);
	}

	private String parseFreeBusyToICS(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		String fbAsString = params.getParameter("freebusy");
		Document doc = DOMUtils.parse(fbAsString);
		FreeBusy fb = cip.parseFreeBusy(doc.getDocumentElement());
		String ics = binding.parseFreeBusyToICS(at, fb);
		return responder.sendString(ics);
	}

	private String getFreeBusy(
			AccessToken at, ParametersSource params,	XmlResponder responder) 
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		String fbAsString = params.getParameter("freebusyrequest");
		Document doc = DOMUtils.parse(fbAsString);
		FreeBusyRequest fb = cip.parseFreeBusyRequest(doc.getDocumentElement());
		List<FreeBusy> ev = binding.getFreeBusy(at, fb);
		return responder.sendListFreeBusy(ev);
	}

	private String parseICSFreeBusy(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault {
		String ics = params.getParameter("ics");
		FreeBusyRequest freeBusy = binding.parseICSFreeBusy(at, ics);
		return responder.sendFreeBusyRequest(freeBusy);
	}

	private String getCalendar(AccessToken accessToken, ParametersSource params) {
		String calendar = params.getParameter("calendar");
		if (calendar.contains("@")) {
			return calendar;
		} else {
			return calendar + "@" + accessToken.getDomain();
		}
	}
	
	private String getLastUpdate(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault {
		Date d = binding.getLastUpdate(at, getCalendar(at, params));
		return responder.sendLong(d.getTime());
	}

	private String getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken at, ParametersSource params, XmlResponder responder) throws ServerFault {
		String calendar = getCalendar(at, params);
		Date start = DateHelper.asDate(params.getParameter("start"));
		Date end = null;
		if (StringUtils.isNotEmpty("end")) {
			end = DateHelper.asDate(params.getParameter("end"));
		}
		List<EventTimeUpdate> e = 
			binding.getEventTimeUpdateNotRefusedFromIntervalDate(at, calendar, start, end);

		return responder.sendListEventTimeUpdate(e);
	}

	private String getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault {
		List<EventParticipationState> e = 
			binding.getEventParticipationStateWithAlertFromIntervalDate(at,
				getCalendar(at, params), 
				DateHelper.asDate(params.getParameter("start")),
				DateHelper.asDate(params.getParameter("end")));

		return responder.sendListEventParticipationState(e);
	}

	private String parseICS(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault, Exception {
		String ics = params.getParameter("ics");
		List<Event> events = binding.parseICS(at, ics);
		return responder.sendListEvent(events);
	}

	private String parseEvents(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		List<Event> events = getEvents(params);
		String ics = binding.parseEvents(at, events);
		return responder.sendString(ics);
	}

	private String parseEvent(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws SAXException, IOException, FactoryConfigurationError, ServerFault {
		Event event = getEvent(params);
		String ics = binding.parseEvent(at, event);
		return responder.sendString(ics);
	}

	private String getAllEvents(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault {
			List<Event> e = 
				binding.getAllEvents(at, getCalendar(at, params), 
						EventType.valueOf(params.getParameter("eventType")));
			return responder.sendListEvent(e);
	}

	private String getListEventsFromIntervalDate(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault {
		List<Event> e = binding.getListEventsFromIntervalDate(at, 
				getCalendar(at, params), 
				DateHelper.asDate(params.getParameter("start")), 
				DateHelper.asDate(params.getParameter("end")));

		return responder.sendListEvent(e);
	}

	private String getEventObmIdFromExtId(AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault, EventNotFoundException {
		
		Integer id = binding.getEventObmIdFromExtId(at, getCalendar(at, params), params.getParameter("extId"));
		if (id != null) {
			return responder.sendInt(id);
		}
		return responder.sendError("not found");		
	}
	
	private String getEventFromExtId(AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault, EventNotFoundException {
		Event e = binding.getEventFromExtId(at, getCalendar(at, params), params.getParameter("extId"));
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

	private String getRefusedKeys(AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault {
		KeyList ret = binding.getRefusedKeys(at, 
				getCalendar(at, params), 
				DateHelper.asDate(params.getParameter("since")));
		return responder.sendKeyList(ret);
	}

	private String getEventTwinKeys(AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault, SAXException, IOException, FactoryConfigurationError {
		KeyList kl = binding.getEventTwinKeys(at, getCalendar(at, params), getEvent(params));
		return responder.sendKeyList(kl);
	}

	private String listCalendars(AccessToken at, XmlResponder responder) throws ServerFault {
		CalendarInfo[] lc = binding.listCalendars(at);
		return responder.sendCalendarInformations(lc);
	}

	private String getEventFromId(AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault {
		Event e = binding.getEventFromId(at, 
				getCalendar(at, params), 
				params.getParameter("id"));
		if (e != null) {
			return responder.sendEvent(e);
		}
		return responder.sendError("not found");
	}

	private String createEvent(
			AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault, SAXException, IOException, FactoryConfigurationError, EventAlreadyExistException {
		String ev = binding.createEvent(at,	getCalendar(at, params), getEvent(params), getNotificationOption(params));
		return responder.sendString(ev);
	}
	
	private Event getEvent(ParametersSource params) throws SAXException,
			IOException, FactoryConfigurationError {
		Document doc = DOMUtils.parse(p(params, "event"));
		Event toModify = cip.parseEvent(doc.getDocumentElement());
		return toModify;
	}

	private boolean getNotificationOption(ParametersSource params) {
		String notificationParam = params.getParameter("notification");
		if (notificationParam != null) {
			return Boolean.valueOf(notificationParam);
		}
		return true;
	}
	
	private List<Event> getEvents(ParametersSource params) throws SAXException,
			IOException, FactoryConfigurationError {
		Document doc = DOMUtils.parse(p(params, "events"));
		List<Event> toModify = cip.parseListEvents(doc.getDocumentElement());
		return toModify;
	}

	private String modifyEvent(
			AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault, SAXException, IOException, FactoryConfigurationError {
		Event ev = binding.modifyEvent(at, getCalendar(at, params),
				getEvent(params), Boolean.valueOf(params.getParameter("updateAttendees")), 
				getNotificationOption(params)
				);
		if (ev != null) {
			return responder.sendEvent(ev);
		}
		return responder.sendError("Event did not exist.");
	}
	
	private String removeEvent(
			AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault {
		Event ev = binding.removeEvent(at, getCalendar(at, params),
				params.getParameter("id"), 
				i(params, "sequence", 0),
				getNotificationOption(params));
		if (ev != null) {
			return responder.sendEvent(ev);
		}
		return responder.sendError("Event did not exist.");

	}
	
	private String removeEventByExtId(
			AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault {
		Event ev = binding.removeEventByExtId(at, getCalendar(at, params),
				params.getParameter("extId"), 
				i(params, "sequence", 0),
				getNotificationOption(params));
		if (ev != null) {
			return responder.sendEvent(ev);
		}
		return responder.sendError("Event did not exist.");

	}

	private String getSyncEventDate(
			AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault {
		EventChanges ret = binding.getSyncEventDate(at, 
				getCalendar(at, params), 
				DateHelper.asDate(params.getParameter("lastSync")));
		return responder.sendCalendarChanges(ret);
	}

	private String getSyncWithSortedChanges(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault {
			EventChanges ret = binding.getSyncWithSortedChanges(at, 
					getCalendar(at, params), 
					DateHelper.asDate(params.getParameter("lastSync")));
			return responder.sendCalendarChanges(ret);
	}

	
	private String getSync(
			AccessToken at, ParametersSource params, XmlResponder responder) 
			throws ServerFault {
			EventChanges ret = binding.getSync(at, 
					getCalendar(at, params), 
					DateHelper.asDate(params.getParameter("lastSync")));
			return responder.sendCalendarChanges(ret);
	}
	
	private String getSyncInRange(
			AccessToken at, ParametersSource params, XmlResponder responder) 
	throws ServerFault {
		final Date after = DateHelper.asDate(params.getParameter("syncRangeAfter"));
		final Date before = DateHelper.asDate(params.getParameter("syncRangeBefore"));
		SyncRange syncRange = null;
		if(after != null || before != null){
			syncRange = new SyncRange(before, after);
		}
		EventChanges ret = binding.getSyncInRange(at, 
				getCalendar(at, params), 
				DateHelper.asDate(params.getParameter("lastSync")), syncRange);
		return responder.sendCalendarChanges(ret);
	}

	private String isWritableCalendar(
			AccessToken at, ParametersSource params, XmlResponder responder) 
		throws ServerFault {
		boolean ret = binding.isWritableCalendar(at, getCalendar(at, params));
		logger.info("isWritable(" + at.getEmail() + ", "
				+ getCalendar(at, params) + ") => " + ret);
		return responder.sendBoolean(ret);
	}
	
	private String changeParticipationState(AccessToken at,
			ParametersSource params, XmlResponder responder) throws ServerFault {
		boolean success = binding.changeParticipationState(at, getCalendar(at, params),
				params.getParameter("extId"),
				ParticipationState.getValueOf(params.getParameter("state")),
				i(params, "sequence", 0),
				getNotificationOption(params));
		return responder.sendBoolean(success);
	}
	
	private String importICalendar(final AccessToken token, final ParametersSource params, 
			XmlResponder responder) throws ImportICalendarException, ServerFault {
		final String calendar = params.getParameter("calendar");
		final String ics = params.getParameter("ics");
		
		int countEvent = binding.importICalendar(token, calendar, ics);
		return responder.sendInt(countEvent);
	}
	
	private String purge(final AccessToken at, final ParametersSource params, final XmlResponder responder) throws ServerFault {
		binding.purge(at, getCalendar(at, params));
		return responder.sendBoolean(true);
	}
	
}
