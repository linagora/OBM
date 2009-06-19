package org.obm.caldav.obmsync.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;



import org.obm.caldav.obmsync.provider.impl.AbstractObmSyncProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncEventProvider;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.utils.Constants;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventType;


public class CalendarService implements ICalendarService{

	private AbstractObmSyncProvider provider;
	private AccessToken token;
	private String calendar;
	
	
	public CalendarService(AccessToken token, String calendarName) {
		this.provider = ObmSyncEventProvider.getInstance();
		this.token = token;
		this.calendar = calendarName;
	}

	public Event getEventFromExtId(String externalUID) throws AuthFault,
			ServerFault {
		return provider.getEventFromExtId(token, calendar, externalUID);
	}
	
	public List<Event> updateOrCreateEvent(String ics, String extId)  throws Exception {
		Event event = provider.getEventFromExtId(token, calendar, extId);
		if (event != null) {
			return provider.updateEventFromICS(token, calendar, ics, event);
		} else {
			return provider.createEventsFromICS(token, calendar, ics, extId);
		}
	}

	public Event createEvent(Event event) throws AuthFault, ServerFault {
		return provider.createEvent(token, calendar, event);
	}

	public List<Event> getListEventsOfDays(Date day) throws AuthFault,
			ServerFault {
		Calendar cal = new GregorianCalendar();
		cal.setTime(day);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		Date end = cal.getTime();

		return provider.getListEventsFromIntervalDate(token, calendar, start, end);
	}

	public String getUserEmail() throws Exception {
		return provider.getUserEmail(token);
	}

	public boolean isConnected() {
		return this.token != null && this.token.getSessionId() != null;
	}

	
	public Map<String,String> getICSFromExtId(Set<String> listExtIdEvent) throws Exception {
		return provider.getICSEventsFromExtId(token, calendar, listExtIdEvent);
	}

	public void removeOrUpdateParticipationState(String extId) throws Exception{
		Event event = provider.getEventFromExtId(token, calendar, extId);
		if(getUserEmail().equals(event.getOwnerEmail())){
			provider.remove(token, calendar, event);
		} else {
			provider.updateParticipationState(token, calendar, event, Constants.PARTICIPATION_STATE_DECLINED);
		}
	}
	
	public String getICSName(Event event){
		return event.getExtId()+".ics";
	}
	
	public List<Event> getAllEvents() throws Exception {
		return provider.getAll(token, calendar, EventType.VEVENT);
	}


	@Override
	public List<Event> getAllTodos() throws Exception {
		return provider.getAll(token, calendar, EventType.VTODO);
	}
}
