package fr.aliacom.obm.common.calendar;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.items.EventChanges;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public interface CalendarDao {

	Event createEvent(AccessToken at, Event event, Boolean useObmUser);

	List<Event> findAllEvents(AccessToken token, ObmUser calendarUser, EventType typeFilter);

	Event findEvent(AccessToken token, int eventId);

	Event findEventByExtId(AccessToken token, ObmUser calendarUser, String eventExtId);

	List<String> findEventTwinKeys(String calendar, Event event, ObmDomain domain);

	Date findLastUpdate(AccessToken token, String calendar);

	List<Event> findListEventsFromIntervalDate(AccessToken token,
			ObmUser calendarUser, Date start, Date end, EventType typeFilter);

	List<String> findRefusedEventsKeys(ObmUser calendarUser, Date date);

	List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, ObmUser calendarUser, Date start, Date end,
			EventType typeFilter);

	List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, ObmUser calendarUser, Date start, Date end,
			EventType typeFilter);

	List<FreeBusy> getFreeBusy(FreeBusyRequest request);

	EventChanges getSync(AccessToken token, ObmUser calendarUser,
			Date lastSync, EventType typeFilter, boolean onEventDate);

	List<CalendarInfo> listCalendars(ObmUser user) throws FindException;

	int markEventAsUpdated(int databaseId) throws FindException;

	Event modifyEvent(AccessToken at, Event event, boolean onlyUpdateMyself, boolean updateAttendees, Boolean useObmUser);

	Event removeEvent(AccessToken token, int eventId, EventType eventType);

	Event removeEventByExtId(AccessToken token, ObmUser calendar, String eventExtId);

	Event createEvent(Connection con, AccessToken editor, Event ev, Boolean useObmUser) throws SQLException;

	void modifyEvent(Connection con, AccessToken at, Event ev,
			boolean onlyUpdateMyself, boolean updateAttendees, Boolean useObmUser)
			throws SQLException;

	Event removeEvent(Connection con, AccessToken token, int uid, EventType et);

	boolean changeParticipationState(AccessToken token, ObmUser calendarOwner, String extId, ParticipationState participationState);

}