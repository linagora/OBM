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
package org.obm.sync.client.calendar;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.TransformerException;

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
import org.obm.sync.calendar.CalendarItemsWriter;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.items.EventChanges;
import org.obm.sync.locators.Locator;
import org.obm.sync.services.ICalendar;
import org.obm.sync.utils.DateHelper;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;

public abstract class AbstractEventSyncClient extends AbstractClientImpl implements ICalendar {

	private CalendarItemsParser respParser;
	private CalendarItemsWriter ciw;
	private String type;
	private final Locator locator;

	public AbstractEventSyncClient(String type, SyncClientException syncClientException, 
			Locator locator, Logger obmSyncLogger) {
		super(syncClientException, obmSyncLogger);
		this.locator = locator;
		respParser = new CalendarItemsParser();
		this.type = type;
		ciw = new CalendarItemsWriter();
	}

	@Override
	public EventObmId createEvent(AccessToken token, String calendar, Event event, boolean notification, String clientId) throws ServerFault, EventAlreadyExistException, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}
		params.put("notification", String.valueOf(notification));
		params.put("clientId", clientId);
		Document doc = execute(token, type + "/createEvent", params);
		exceptionFactory.checkCreateEventException(doc);
		return new EventObmId(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}

	@Override
	public Event getEventFromId(AccessToken token, String calendar, EventObmId id) throws ServerFault, EventNotFoundException, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("id", id.serializeToString());
		Document doc = execute(token, type + "/getEventFromId", params);
		exceptionFactory.checkEventNotFoundException(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public KeyList getEventTwinKeys(AccessToken token, String calendar, Event event) throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}

		Document doc = execute(token, type + "/getEventTwinKeys", params);
		exceptionFactory.checkNotAllowedException(doc);
		return respParser.parseKeyList(doc);
	}

	@Override
	public EventChanges getSyncWithSortedChanges(AccessToken token,
			String calendar, Date lastSync, SyncRange syncRange) throws ServerFault, NotAllowedException {
		return getSync(token, calendar, lastSync, syncRange, "getSyncWithSortedChanges");
	}
	
	@Override
	public EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws ServerFault, NotAllowedException {
		return getSync(token, calendar, lastSync, null, "getSync");
	}
	
	@Override
	public EventChanges getSyncInRange(AccessToken token, String calendar, Date lastSync,
			SyncRange syncRange) throws ServerFault, NotAllowedException {
		return getSync(token, calendar, lastSync, syncRange, "getSyncInRange");
	}
	
	private EventChanges getSync(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange, String methodName) throws ServerFault, NotAllowedException {
		if (logger.isDebugEnabled()) {
			logger.debug("getSync(" + token.getSessionId() + ", " + calendar
					+ ", " + lastSync + ")");
		}
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}
		if(syncRange != null){
			if(syncRange.getAfter() != null){
				params.put("syncRangeAfter", DateHelper.asString(syncRange.getAfter()));
			}
			if(syncRange.getBefore() != null){
				params.put("syncRangeBefore", DateHelper.asString(syncRange.getBefore()));
			}
		}

		Document doc = execute(token, type + "/" + methodName, params);
		exceptionFactory.checkNotAllowedException(doc);
		return respParser.parseChanges(doc);
	}
	
	@Override
	public EventChanges getSyncEventDate(AccessToken token, String calendar, Date lastSync) throws ServerFault, NotAllowedException {
		if (logger.isDebugEnabled()) {
			logger.debug("getSyncEventDate(" + token.getSessionId() + ", " + calendar
					+ ", " + lastSync + ")");
		}
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}

		Document doc = execute(token, type + "/getSyncEventDate", params);
		exceptionFactory.checkNotAllowedException(doc);
		return respParser.parseChanges(doc);
	}

	@Override
	public String getUserEmail(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, type + "/getUserEmail", params);
		exceptionFactory.checkServerFaultException(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public CalendarInfo[] listCalendars(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, type + "/listCalendars", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseInfos(doc);
	}

	@Override
	public ResourceInfo[] listResources(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, type + "/listResources", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseResourceInfo(doc);
	}

	@Override
	public Event modifyEvent(AccessToken token, String calendar, Event event,
			boolean updateAttendees, boolean notification) throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}
		params.put("updateAttendees", "" + updateAttendees);
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/modifyEvent", params);
		exceptionFactory.checkNotAllowedException(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public void removeEventById(AccessToken token, String calendar, EventObmId uid, int sequence, boolean notification) 
			throws ServerFault, EventNotFoundException, NotAllowedException {
		
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("id", uid.serializeToString());
		params.put("sequence", String.valueOf(sequence));
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/removeEvent", params);
		exceptionFactory.checkEventNotFoundException(doc);
	}
	
	@Override
	public Event removeEventByExtId(AccessToken token, String calendar, EventExtId extId, int sequence, boolean notification)
			throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		params.put("sequence", String.valueOf(sequence));
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/removeEventByExtId", params);
		exceptionFactory.checkNotAllowedException(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public KeyList getRefusedKeys(AccessToken token, String calendar, Date since) throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		if (since != null) {
			params.put("since", DateHelper.asString(since));
		} else {
			params.put("since", "0");
		}

		Document doc = execute(token, type + "/getRefusedKeys", params);
		exceptionFactory.checkNotAllowedException(doc);
		return respParser.parseKeyList(doc);
	}

	@Override
	public List<Category> listCategories(AccessToken at) throws ServerFault {
		List<Category> ret = new LinkedList<Category>();
		Multimap<String, String> params = initParams(at);
		Document doc = execute(at, type + "/listCategories", params);
		exceptionFactory.checkServerFaultException(doc);
		ret.addAll(respParser.parseCategories(doc.getDocumentElement()));
		return ret;
	}

	@Override
	public Event getEventFromExtId(AccessToken token, String calendar, EventExtId extId) throws ServerFault, EventNotFoundException, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		Document doc = execute(token, type + "/getEventFromExtId", params);
		exceptionFactory.checkEventNotFoundException(doc);
		if (doc.getDocumentElement().getNodeName().equals("event")) {
			return respParser.parseEvent(doc.getDocumentElement());
		}
		logger.warn("event " + extId + " not found.");
		return null;
	}

	@Override
	public EventObmId getEventObmIdFromExtId(AccessToken token, String calendar, EventExtId extId) throws ServerFault, EventNotFoundException, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		Document doc = execute(token, type + "/getEventObmIdFromExtId", params);
		exceptionFactory.checkEventNotFoundException(doc);
		String value = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return new EventObmId(value);
	}

	@Override
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws ServerFault, NotAllowedException {
		List<Event> ret = new LinkedList<Event>();
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		params.put("end", Long.toString(end.getTime()));

		Document doc = execute(token, type + "/getListEventsFromIntervalDate", params);
		exceptionFactory.checkNotAllowedException(doc);
		ret.addAll(respParser.parseListEvents(doc.getDocumentElement()));
		return ret;
	}

	@Override
	public List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("eventType", eventType.name());

		Document doc = execute(token, type + "/getAllEvents", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListEvents(doc.getDocumentElement());
	}

	@Override
	public List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		if (end != null) {
			params.put("end", Long.toString(end.getTime()));
		}
		Document doc = execute(token, type + "/getEventTimeUpdateNotRefusedFromIntervalDate", params);
		exceptionFactory.checkNotAllowedException(doc);
		return respParser.parseListEventTimeUpdate(doc.getDocumentElement());
	}

	@Override
	public String parseEvent(AccessToken token, Event event) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		try {
			params.put("event", ciw.getEventString(event));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}

		Document doc = execute(token, type + "/parseEvent", params);
		exceptionFactory.checkServerFaultException(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault {
		Multimap<String, String> params = initParams(token);
		try {
			params.put("events", ciw.getListEventString(events));
		} catch (TransformerException e) {
			throw new IllegalArgumentException(e);
		}
		Document doc = execute(token, type + "/parseEvents", params);
		exceptionFactory.checkServerFaultException(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public List<Event> parseICS(AccessToken token, String ics)
			throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("ics", ics);
		Document doc = execute(token, type + "/parseICS", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListEvents(doc.getDocumentElement());
	}

	@Override
	public FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("ics", ics);
		Document doc = execute(token, type + "/parseICSFreeBusy", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseFreeBusyRequest(doc.getDocumentElement());
	}

	@Override
	public List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		params.put("end", Long.toString(end.getTime()));

		Document doc = execute(token, type
				+ "/getEventParticipationStateWithAlertFromIntervalDate",
				params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListEventParticipationState(doc
				.getDocumentElement());
	}

	@Override
	public Date getLastUpdate(AccessToken token, String calendar) throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		Document doc = execute(token, type + "/getLastUpdate", params);
		exceptionFactory.checkNotAllowedException(doc);
		String date = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return new Date(new Long(date));
	}

	@Override
	public boolean isWritableCalendar(AccessToken token, String calendar) throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		Document doc = execute(token, type + "/isWritableCalendar", params);
		exceptionFactory.checkNotAllowedException(doc);
		return "true".equalsIgnoreCase(DOMUtils.getElementText(doc
				.getDocumentElement(), "value"));
	}

	@Override
	public List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fbr) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("freebusyrequest", ciw.getFreeBusyRequestString(fbr));
		Document doc = execute(token, type + "/getFreeBusy", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListFreeBusy(doc.getDocumentElement());
	}

	@Override
	public String parseFreeBusyToICS(AccessToken token, FreeBusy fb) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("freebusy", ciw.getFreeBusyString(fb));

		Document doc = execute(token, type + "/parseFreeBusyToICS", params);
		exceptionFactory.checkServerFaultException(doc);
		String ret = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return ret;
	}
	
	@Override
	public boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, Participation participation,
			int sequence, boolean notification) throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		params.put("state", participation.toString());
		params.put("sequence", String.valueOf(sequence));
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/changeParticipationState", params);
		exceptionFactory.checkNotAllowedException(doc);
		return Boolean.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}	
	
	@Override
	public boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, RecurrenceId recurrenceId, Participation participation, 
			int sequence, boolean notification) throws ServerFault, NotAllowedException {
		Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId.serializeToString());
		if (recurrenceId != null) {
			params.put("recurrenceId", recurrenceId.serializeToString());
		}
		params.put("state", participation.toString());
		params.put("sequence", String.valueOf(sequence));
		params.put("notification", String.valueOf(notification));
		Document doc = execute(token, type + "/changeParticipationState", params);
		exceptionFactory.checkNotAllowedException(doc);
		return Boolean.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}
	
	@Override
	public int importICalendar(final AccessToken token, final String calendar, final String ics, String clientId) 
			throws ServerFault, NotAllowedException {
		final Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("ics", ics);
		params.put("clientId", clientId);
		
		final Document doc = execute(token, type + "/importICalendar", params);
		exceptionFactory.checkNotAllowedException(doc);
		return Integer.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}
	
	@Override
	public void purge(final AccessToken token, final String calendar) throws ServerFault, NotAllowedException {
		final Multimap<String, String> params = initParams(token);
		params.put("calendar", calendar);
		final Document doc = execute(token, type + "/purge", params);
		exceptionFactory.checkNotAllowedException(doc);
	}

	@Override
	public CalendarInfo[] getCalendarMetadata(AccessToken token,
			String[] calendars) throws ServerFault {
		final Multimap<String, String> params = initParams(token);
		for (String calendar : calendars)
			params.put("calendar", calendar);
		final Document doc = execute(token, type + "/getCalendarMetadata", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseInfos(doc);
	}

	@Override
	public ResourceInfo[] getResourceMetadata(AccessToken token,
			String[] resources) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		for (String resource : resources) {
			params.put("resource", resource);
		}
		Document doc = execute(token, type + "/getResourceMetadata", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseResourceInfo(doc);
	}

	@Override
	public Collection<Event> getResourceEvents(String resourceEmail, Date date) throws ServerFault {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Locator getLocator() {
		return locator;
	}
	
}
