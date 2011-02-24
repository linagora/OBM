package org.obm.sync.client.calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.CalendarItemsParser;
import org.obm.sync.calendar.CalendarItemsWriter;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.items.EventChanges;
import org.obm.sync.services.ICalendar;
import org.obm.sync.utils.DOMUtils;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;

/**
 * OBM sync client implementation for calendar synchronisations
 * 
 * @author tom
 * 
 */
public abstract class AbstractEventSyncClient extends AbstractClientImpl
		implements ICalendar {

	private CalendarItemsParser respParser;
	private CalendarItemsWriter ciw;
	private String type;

	public AbstractEventSyncClient(String type, String obmSyncServicesUrl) {
		super(obmSyncServicesUrl);
		respParser = new CalendarItemsParser();
		this.type = type;
		ciw = new CalendarItemsWriter();
	}

	public AbstractEventSyncClient(String type, String obmSyncServicesUrl, HttpClient cli) {
		super(obmSyncServicesUrl, cli);
		respParser = new CalendarItemsParser();
		this.type = type;
		ciw = new CalendarItemsWriter();
	}

	@Override
	public String createEvent(AccessToken token, String calendar, Event event)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("event", ciw.getEventString(event));

		Document doc = execute(type + "/createEvent", params);
		checkServerError(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public Event getEventFromId(AccessToken token, String calendar, String id)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("id", id);
		Document doc = execute(type + "/getEventFromId", params);
		if (doc.getDocumentElement().getNodeName().equals("event")) {
			return respParser.parseEvent(doc.getDocumentElement());
		}
		logger.warn("event " + id + " not found.");
		return null;
	}

	@Override
	public KeyList getEventTwinKeys(AccessToken token, String calendar,
			Event event) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("event", ciw.getEventString(event));

		Document doc = execute(type + "/getEventTwinKeys", params);
		checkServerError(doc);
		return respParser.parseKeyList(doc);
	}

	@Override
	public EventChanges getSyncWithSortedChanges(AccessToken token,
			String calendar, Date lastSync) throws AuthFault, ServerFault {
		return getSync(token, calendar, lastSync, "getSyncWithSortedChanges");
	}
	
	@Override
	public EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws AuthFault, ServerFault {
		return getSync(token, calendar, lastSync, "getSync");
	}
	
	private EventChanges getSync(AccessToken token, String calendar,
			Date lastSync, String methodName) throws ServerFault {
		if (logger.isDebugEnabled()) {
			logger.debug("getSync(" + token.getSessionId() + ", " + calendar
					+ ", " + lastSync + ")");
		}
		Map<String, String> params = new HashMap<String, String>();
		setToken(params, token);
		params.put("calendar", calendar);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}

		Document doc = execute(type + "/" + methodName, params);
		checkServerError(doc);
		return respParser.parseChanges(doc);
	}
	
	@Override
	public EventChanges getSyncEventDate(AccessToken token, String calendar,
			Date lastSync) throws AuthFault, ServerFault {
		if (logger.isDebugEnabled()) {
			logger.debug("getSyncEventDate(" + token.getSessionId() + ", " + calendar
					+ ", " + lastSync + ")");
		}
		Map<String, String> params = new HashMap<String, String>();
		setToken(params, token);
		params.put("calendar", calendar);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}

		Document doc = execute(type + "/getSyncEventDate", params);
		checkServerError(doc);
		return respParser.parseChanges(doc);
	}

	@Override
	public String getUserEmail(AccessToken token) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		Document doc = execute(type + "/getUserEmail", params);
		checkServerError(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public CalendarInfo[] listCalendars(AccessToken token) throws ServerFault,
			AuthFault {
		Map<String, String> params = new HashMap<String, String>();
		setToken(params, token);
		Document doc = execute(type + "/listCalendars", params);
		checkServerError(doc);
		return respParser.parseInfos(doc);
	}

	@Override
	public Event modifyEvent(AccessToken token, String calendar, Event event,
			boolean updateAttendees) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("event", ciw.getEventString(event));
		params.put("updateAttendees", "" + updateAttendees);
		Document doc = execute(type + "/modifyEvent", params);
		checkServerError(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public Event removeEvent(AccessToken token, String calendar, String uid)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("id", uid);

		Document doc = execute(type + "/removeEvent", params);
		checkServerError(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}
	
	@Override
	public Event removeEventByExtId(AccessToken token, String calendar, String extId)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId);

		Document doc = execute(type + "/removeEventByExtId", params);
		checkServerError(doc);
		return respParser.parseEvent(doc.getDocumentElement());
	}

	@Override
	public KeyList getRefusedKeys(AccessToken token, String calendar, Date since)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		if (since != null) {
			params.put("since", DateHelper.asString(since));
		} else {
			params.put("since", "0");
		}

		Document doc = execute(type + "/getRefusedKeys", params);
		checkServerError(doc);
		return respParser.parseKeyList(doc);
	}

	@Override
	public List<Category> listCategories(AccessToken at) throws AuthFault,
			ServerFault {
		List<Category> ret = new LinkedList<Category>();
		Map<String, String> params = initParams(at);
		Document doc = execute(type + "/listCategories", params);
		checkServerError(doc);
		ret.addAll(respParser.parseCategories(doc.getDocumentElement()));
		return ret;
	}

	@Override
	public Event getEventFromExtId(AccessToken token, String calendar,
			String extId) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId);
		Document doc = execute(type + "/getEventFromExtId", params);
		if (doc.getDocumentElement().getNodeName().equals("event")) {
			return respParser.parseEvent(doc.getDocumentElement());
		}
		logger.warn("event " + extId + " not found.");
		return null;
	}

	@Override
	public Integer getEventObmIdFromExtId(AccessToken token, String calendar, String extId)
		throws ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId);
		Document doc = execute(type + "/getEventObmIdFromExtId", params);
		
		if (documentIsError(doc)) {
			return null;
		}
		String value = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return Integer.parseInt(value);
	}

	
	@Override
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws AuthFault,
			ServerFault {
		List<Event> ret = new LinkedList<Event>();
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		params.put("end", Long.toString(end.getTime()));

		Document doc = execute(type + "/getListEventsFromIntervalDate", params);
		checkServerError(doc);
		ret.addAll(respParser.parseListEvents(doc.getDocumentElement()));
		return ret;
	}

	@Override
	public List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("eventType", eventType.name());

		Document doc = execute(type + "/getAllEvents", params);
		checkServerError(doc);
		return respParser.parseListEvents(doc.getDocumentElement());
	}

	@Override
	public List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		if (end != null) {
			params.put("end", Long.toString(end.getTime()));
		}
		Document doc = execute(type
				+ "/getEventTimeUpdateNotRefusedFromIntervalDate", params);
		checkServerError(doc);
		return respParser.parseListEventTimeUpdate(doc.getDocumentElement());
	}

	@Override
	public String parseEvent(AccessToken token, Event event)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("event", ciw.getEventString(event));

		Document doc = execute(type + "/parseEvent", params);
		checkServerError(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("events", ciw.getListEventString(events));

		Document doc = execute(type + "/parseEvents", params);
		checkServerError(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	public List<Event> parseICS(AccessToken token, String ics)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("ics", ics);
		Document doc = execute(type + "/parseICS", params);
		checkServerError(doc);
		return respParser.parseListEvents(doc.getDocumentElement());
	}

	@Override
	public FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("ics", ics);
		Document doc = execute(type + "/parseICSFreeBusy", params);
		checkServerError(doc);
		return respParser.parseFreeBusyRequest(doc.getDocumentElement());
	}

	@Override
	public List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("start", Long.toString(start.getTime()));
		params.put("end", Long.toString(end.getTime()));

		Document doc = execute(type
				+ "/getEventParticipationStateWithAlertFromIntervalDate",
				params);
		checkServerError(doc);
		return respParser.parseListEventParticipationState(doc
				.getDocumentElement());
	}

	@Override
	public Date getLastUpdate(AccessToken token, String calendar)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		Document doc = execute(type + "/getLastUpdate", params);
		checkServerError(doc);
		String date = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return new Date(new Long(date));
	}

	@Override
	public boolean isWritableCalendar(AccessToken token, String calendar)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		Document doc = execute(type + "/isWritableCalendar", params);
		checkServerError(doc);
		return "true".equalsIgnoreCase(DOMUtils.getElementText(doc
				.getDocumentElement(), "value"));
	}

	@Override
	public List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fbr)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("freebusyrequest", ciw.getFreeBusyRequestString(fbr));
		Document doc = execute(type + "/getFreeBusy", params);
		checkServerError(doc);
		return respParser.parseListFreeBusy(doc.getDocumentElement());
	}

	@Override
	public String parseFreeBusyToICS(AccessToken token, FreeBusy fb)
			throws ServerFault, AuthFault {
		Map<String, String> params = initParams(token);
		params.put("freebusy", ciw.getFreeBusyString(fb));

		Document doc = execute(type + "/parseFreeBusyToICS", params);
		checkServerError(doc);
		String ret = DOMUtils.getElementText(doc.getDocumentElement(), "value");
		return ret;
	}
	
	@Override
	public boolean changeParticipationState(AccessToken token, String calendar,
			String extId,
			ParticipationState participationState) throws ServerFault {
		Map<String, String> params = initParams(token);
		params.put("calendar", calendar);
		params.put("extId", extId);
		params.put("state", participationState.toString());
		Document doc = execute(type + "/changeParticipationState", params);
		checkServerError(doc);
		return Boolean.valueOf(DOMUtils.getElementText(doc.getDocumentElement(), "value"));
	}
}
