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
package fr.aliacom.obm.common.calendar;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
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
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.ResourceInfo;
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
	
	Event findEventByExtIdAndRecurrenceId(AccessToken token, ObmUser calendarUser, EventExtId eventExtId, RecurrenceId recurrenceId) throws ParseException;

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

	Collection<Event> getResourceEvents(ResourceInfo resourceInfo,
			SyncRange syncRange) throws FindException;

	ResourceInfo getResource(String resourceEmail) throws FindException;

	EventChanges getSync(AccessToken token, ObmUser calendarUser,
			Date lastSync, SyncRange syncRange, EventType typeFilter, boolean onEventDate);

	Collection<CalendarInfo> listCalendars(ObmUser user, Integer limit, Integer offset) throws FindException;

	Collection<ResourceInfo> listResources(ObmUser user) throws FindException;

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
	
	boolean changeParticipation(AccessToken token, ObmUser calendarOwner, EventExtId extId, RecurrenceId recurrenceId, Participation participation) throws SQLException, ParseException ;

	Collection<CalendarInfo> getCalendarMetadata(ObmUser user, Collection<String> calendars)
			throws FindException;

	Collection<ResourceInfo> getResourceMetadata(ObmUser user, Collection<String> resources)
			throws FindException;

	boolean changeParticipation(AccessToken token, ObmUser calendar,
			EventExtId extId, Participation participation)
			throws SQLException;

	Integer getEventAlertForUser(EventObmId eventId, Integer userId);
}