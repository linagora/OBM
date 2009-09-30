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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncEventProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncTodoProvider;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.share.filter.CompFilter;
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
	public List<EventTimeUpdate> getAllLastUpdateEvents(CompFilter cf) throws Exception{
		if(cf.getTimeRange() != null && (cf.getTimeRange().getStart() != null || cf.getTimeRange().getEnd() != null)){
			Date start = cf.getTimeRange().getStart();
			Date end = cf.getTimeRange().getEnd();
			if(start == null){
				GregorianCalendar gc = new GregorianCalendar();
				gc.set(Calendar.YEAR, gc.get(Calendar.YEAR)-1);
				start = gc.getTime();
			}
			
			if(end == null){
				GregorianCalendar gc = new GregorianCalendar();
				gc.set(Calendar.YEAR, gc.get(Calendar.YEAR)+1);
				end = gc.getTime();
			}

			return providerEvent.getEventTimeUpdateFromIntervalDate(token, calendar, cf.getTimeRange().getStart(), end);
		}
		return providerEvent.getAllEventTimeUpdate(token, calendar); 
	}
	
	@Override
	public List<EventTimeUpdate> getAllLastUpdateEvents() throws Exception {
		return getAllLastUpdateEvents(new CompFilter());
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

	@Override
	public boolean hasRightsOnCalendar(String calendarName) throws Exception {
		return providerEvent.hasRightsOnCalendar(token, calendarName);
	}

	@Override
	public String getLastUpdate() throws Exception {
		return providerEvent.getLastUpdate(token, calendar).toString();
	}
}
