package fr.aliacom.obm.common.calendar;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public interface CalendarDao {

	Event createEvent(AccessToken at, String calendar, Event event, Boolean useObmUser) throws FindException,  SQLException, ServerFault ;

	List<Event> findAllEvents(AccessToken token, ObmUser calendarUser, EventType typeFilter);

	Event findEventById(AccessToken token, EventObmId eventId) throws EventNotFoundException, ServerFault;

	Event findEventByExtId(AccessToken token, ObmUser calendarUser, EventExtId eventExtId);

	List<String> findEventTwinKeys(String calendar, Event event, ObmDomain domain);

	Date findLastUpdate(AccessToken token, String calendar);

	List<Event> listEventsByIntervalDate(AccessToken token, ObmUser obmUser, Date start, Date end, EventType typeFilter);

	List<String> findRefusedEventsKeys(ObmUser calendarUser, Date date);

	List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, ObmUser calendarUser, Date start, Date end,
			EventType typeFilter);

	List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, ObmUser calendarUser, Date start, Date end,
			EventType typeFilter);

	List<FreeBusy> getFreeBusy(ObmDomain domain, FreeBusyRequest request);

	EventChanges getSync(AccessToken token, ObmUser calendarUser,
			Date lastSync, SyncRange syncRange, EventType typeFilter, boolean onEventDate);

	Collection<CalendarInfo> listCalendars(ObmUser user) throws FindException;

	Event modifyEvent(AccessToken at, String calendar, Event event, boolean updateAttendees, Boolean useObmUser) throws FindException, SQLException, EventNotFoundException, ServerFault;

	Event modifyEventForcingSequence(AccessToken at, String calendar, Event ev,
			boolean updateAttendees, int sequence, Boolean useObmUser)
			throws SQLException, FindException, EventNotFoundException, ServerFault;
	
	Event removeEventById(AccessToken token, EventObmId eventId, EventType eventType, int sequence) throws SQLException, EventNotFoundException, ServerFault;

	Event removeEventById(Connection con, AccessToken token, EventObmId uid, EventType et, int sequence) throws SQLException, EventNotFoundException, ServerFault;

	Event removeEvent(AccessToken token, Event event, EventType eventType, int sequence) throws SQLException;
	
	Event removeEventByExtId(AccessToken token, ObmUser calendar, EventExtId eventExtId, int sequence) throws SQLException;

	Event createEvent(Connection con, AccessToken editor, String calendar, Event ev, Boolean useObmUser) throws SQLException, FindException, ServerFault;

	void modifyEvent(Connection con, AccessToken at,  String calendar, Event ev,
			boolean updateAttendees, Boolean useObmUser)
			throws SQLException, FindException, ServerFault, EventNotFoundException;

	void modifyEventForcingSequence(Connection con, AccessToken editor, String calendar,
			Event ev, boolean updateAttendees, int sequence, Boolean useObmUser)
			throws SQLException, FindException, ServerFault, EventNotFoundException;
	
	boolean changeParticipationState(AccessToken token, ObmUser calendarOwner, EventExtId extId, ParticipationState participationState) throws SQLException ;

	Collection<CalendarInfo> getCalendarMetadata(ObmUser user, Collection<String> calendars)
			throws FindException;
}