package org.obm.caldav.obmsync.service.impl;

import java.util.Date;
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
import org.obm.sync.items.EventChanges;


public class CalendarService implements ICalendarService{

	private AbstractObmSyncProvider provider;
	private AccessToken token;
	private String calendar;
	private String userEmail;
	
	public CalendarService(AccessToken token, String calendar, String userEmail) {
		this.provider = ObmSyncEventProvider.getInstance();
		this.token = token;
		this.calendar = calendar;
		this.userEmail = userEmail;
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

	public boolean isConnected() {
		return this.token != null && this.token.getSessionId() != null;
	}

	public Map<Event,String> getICSFromExtId(Set<String> listExtIdEvent) throws Exception {
		return provider.getICSEventsFromExtId(token, calendar, listExtIdEvent);
	}

	public void removeOrUpdateParticipationState(String extId) throws Exception{
		Event event = provider.getEventFromExtId(token, calendar, extId);
		if(userEmail.equals(event.getOwnerEmail())){
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

	@Override
	public EventChanges getSync(Date lastSync)
			throws Exception {
		return provider.getSync(token, calendar, lastSync);
	}
}
