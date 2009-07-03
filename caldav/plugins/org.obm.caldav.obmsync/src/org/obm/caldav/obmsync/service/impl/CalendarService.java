package org.obm.caldav.obmsync.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncEventProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncTodoProvider;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.utils.Constants;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.items.EventChanges;


public class CalendarService implements ICalendarService{

	private ICalendarProvider providerEvent;
	private ICalendarProvider providerTodo;
	private AccessToken token;
	private String calendar;
	private String userEmail;
	
	public CalendarService(AccessToken token, String calendar, String userEmail) {
		this.providerEvent = ObmSyncEventProvider.getInstance();
		this.providerTodo = ObmSyncTodoProvider.getInstance();
		this.token = token;
		this.calendar = calendar;
		this.userEmail = userEmail;
	}

	public Event getEventFromExtId(String externalUID) throws AuthFault,
			ServerFault {
		return providerEvent.getEventFromExtId(token, calendar, externalUID);
	}
	
	public List<Event> updateOrCreateEvent(String ics, String extId)  throws Exception {
		Event event = providerEvent.getEventFromExtId(token, calendar, extId);
		if (event != null) {
			return providerEvent.updateEventFromICS(token, calendar, ics, event);
		} else {
			return providerEvent.createEventsFromICS(token, calendar, ics, extId);
		}
	}

	public Event createEvent(Event event) throws AuthFault, ServerFault {
		return providerEvent.createEvent(token, calendar, event);
	}

	public boolean isConnected() {
		return this.token != null && this.token.getSessionId() != null;
	}

	public Map<Event,String> getICSFromExtId(Set<String> listExtIdEvent) throws Exception {
		return providerEvent.getICSEventsFromExtId(token, calendar, listExtIdEvent);
	}

	public void removeOrUpdateParticipationState(String extId) throws Exception{
		Event event = providerEvent.getEventFromExtId(token, calendar, extId);
		if(event != null){
			if(userEmail.equals(event.getOwnerEmail())){
				providerEvent.remove(token, calendar, event);
			} else {
				providerEvent.updateParticipationState(token, calendar, event, Constants.PARTICIPATION_STATE_DECLINED);
			}
		}
	}
	
	public String getICSName(Event event){
		return event.getExtId()+".ics";
	}
	
	public String getICSName(EventTimeUpdate etu) {
		return etu.getExtId()+".ics";
	}
	
	public List<Event> getAllEvents() throws Exception {
		return providerEvent.getAll(token, calendar);
	}
	
	@Override
	public List<EventTimeUpdate> getAllLastUpdateEvents() throws Exception{
		return providerEvent.getAllEventTimeUpdate(token, calendar);
	}


	@Override
	public List<Event> getAllTodos() throws Exception {
		return providerTodo.getAll(token, calendar);
	}
	
	@Override
	public List<EventTimeUpdate> getAllLastUpdateTodos() throws Exception{
		return providerTodo.getAllEventTimeUpdate(token, calendar);
	}

	@Override
	public boolean getSync(Date lastSync)
			throws Exception {
		EventChanges ec = providerEvent.getSync(token, calendar, lastSync);
		if(ec.getUpdated().length != 0 || ec.getRemoved().length != 0){
			return true;
		} else {
			ec = providerTodo.getSync(token, calendar, lastSync);
			if(ec.getUpdated().length != 0 || ec.getRemoved().length != 0){
				return true;
			}
		}
		return false;
	}

}
