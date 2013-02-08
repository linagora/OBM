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
package org.obm.sync.services;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obm.sync.NotAllowedException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
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

public interface ICalendar {

	/**
	 * List all Calendars on which authenticated user as some rights
	 */
	CalendarInfo[] listCalendars(AccessToken token) throws ServerFault;


	/**
	 * List all resources on which authenticated user as some rights
	 */
	ResourceInfo[] listResources(AccessToken token) throws ServerFault;

	/**
	 * Returns calendar metadata for a list of given calendars.
	 * 
	 * @param token
	 *            must contains a valid session to execute this service
	 * @param calendars
	 * 			  the OBM emails associated to the calendars we want to retrieve.
	 */
	CalendarInfo[] getCalendarMetadata(AccessToken token, String[] calendars)
			throws ServerFault;

	/**
	 * Returns resource metadata for a list of given resources.
	 * 
	 * @param token
	 * 			  must contains a valid session to execute this service
	 * @param resources
	 * 			  the OBM emails associated to the resources we want to retrieve.
	 */
	ResourceInfo[] getResourceMetadata(AccessToken token, String[] resources)
			throws ServerFault;

	/**
	 * Remove an event from database if user has enough access rights on the
	 * event referenced by eventId.
	 * 
	 * @param token
	 *            must contains a valid session to execute this service
	 * @param calendar
	 *            is ignored
	 * @param eventId
	 *            the id of the event to remove
	 * @param notification
	 *            send email notification if needed
	 * @return the removed event on success, the found event if access rights
	 *         are too low to remove but enough to read, and null if event was
	 *         not found
	 */
	void removeEventById(AccessToken token, String calendar, EventObmId eventId,
			int sequence, boolean notification) throws ServerFault, EventNotFoundException, NotAllowedException;

	/**
	 * FIXME: remove this service
	 */
	Event removeEventByExtId(AccessToken token, String calendar, EventExtId extId,
			int sequence, boolean notification) throws ServerFault, NotAllowedException;

	/**
	 * FIXME: needs work
	 */
	Event modifyEvent(AccessToken token, String calendar, Event event,
			boolean updateAttendees, boolean notification) throws ServerFault, NotAllowedException;

	/**
	 * Creates an event
	 * 
	 * @param clientId
	 *            a SHA1, when this method is called many times with the same value for clientId,
	 *            only one event must be created.
	 *            Other calls returns the event created on first call. (idempotence)
	 *            This param is not mandatory, it must have a size of 40 characters when not null
	 *            
	 * @throws EventAlreadyExistException 
	 */
	EventObmId createEvent(AccessToken token, String calendar, Event event,
			boolean notification, String clientId) 
					throws ServerFault, EventAlreadyExistException, NotAllowedException;

	/**
	 * return every changes made to calendar since lastSync date for events into
	 * the sync range. This service treats participation changes as full
	 * changes. Logged user needs read rights on calendar.
	 */
	EventChanges getSyncInRange(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange) throws ServerFault, NotAllowedException;

	/**
	 * return every changes made to calendar since lastSync date. This service
	 * treats participation changes as full changes. Logged user needs read
	 * rights on calendar.
	 */
	EventChanges getSync(AccessToken token, String calendar, Date lastSync)
			throws ServerFault, NotAllowedException;

	/**
	 * return every changes made to calendar since lastSync date. This service
	 * treats participation changes as special changes in order to let client
	 * know if the event itself has been modified or not. Logged user needs read
	 * rights on calendar.
	 */
	EventChanges getSyncWithSortedChanges(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange) throws ServerFault, NotAllowedException;

	/**
	 * return every event in calendar the will happen after start date Logged
	 * user needs read rights on calendar
	 */
	EventChanges getSyncEventDate(AccessToken token, String calendar, Date start)
			throws ServerFault, NotAllowedException;

	/**
	 * Find an event from its id.
	 * 
	 * @return the event if user has read access to owner calendar or the event
	 *         is present in its calendar, null if event is not found or user
	 *         has not enough rights to read this event
	 */
	Event getEventFromId(AccessToken token, String calendar, EventObmId id) throws ServerFault, EventNotFoundException, NotAllowedException;

	/**
	 * Return id of events who have : - start date, subject and duration if not
	 * allday equals to the given event - start date day, subject if allday
	 * equals to the given event User needs read access on selected calendar to
	 * execute this service.
	 */
	KeyList getEventTwinKeys(AccessToken token, String calendar, Event event)
			throws ServerFault, NotAllowedException;

	/**
	 * get current user email based
	 */
	String getUserEmail(AccessToken token) throws ServerFault;

	/**
	 * list all eventId of event refused on calendar. User needs read access on
	 * selected calendar to execute this service.
	 */
	KeyList getRefusedKeys(AccessToken token, String calendar, Date since)
			throws ServerFault, NotAllowedException;

	/**
	 * List known categories.
	 */
	List<Category> listCategories(AccessToken at) throws ServerFault;

	/**
	 * @return the obm id of the event that have the given extId into the given
	 *         calendar
	 * @throws EventNotFoundException 
	 */
	EventObmId getEventObmIdFromExtId(AccessToken token, String calendar,
			EventExtId extId) throws ServerFault, EventNotFoundException, NotAllowedException;

	/**
	 * retrieve an event by its extId into specified calendar User needs read
	 * access on selected calendar to execute this service.
	 * @throws EventNotFoundException 
	 */
	Event getEventFromExtId(AccessToken token, String calendar, EventExtId extId) throws ServerFault, EventNotFoundException, NotAllowedException;

	/**
	 * retrieve all events between start and end date. User needs read access on
	 * selected calendar to execute this service.
	 */
	List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws ServerFault, NotAllowedException;

	/**
	 * retrieve all events of type evenType from calendar. User needs read
	 * access on selected calendar to execute this service.
	 */
	List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws ServerFault;

	/**
	 * List event with their ids and last update timestamp that the user didn't
	 * refused in a given time interval
	 */
	List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, NotAllowedException;

	/**
	 * Convert an event into an ICS
	 */
	String parseEvent(AccessToken token, Event event) throws ServerFault;

	/**
	 * Convert a list of event into an ICS
	 */
	String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault;

	/**
	 * Convert an ICS containing Events to a Event list
	 */
	List<Event> parseICS(AccessToken token, String ics) throws Exception,
			ServerFault;

	/**
	 * Convert an ICS Freebusy request into an FreeBusyRequest object
	 */
	FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics)
			throws ServerFault;

	/**
	 * Given a list of attendees and a time interval, this method computes the
	 * free time interval for each attendee and returns them.
	 * 
	 * Caller don't need any rights to call this as no sensible information is
	 * disclosed.
	 */
	List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fb)
			throws ServerFault;

	/**
	 * Convert a {@link FreeBusy} bean to an ICS
	 * 
	 * @return a {@link String} representing an ICS content
	 */
	String parseFreeBusyToICS(AccessToken token, FreeBusy fbr)
			throws ServerFault;

	/**
	 * List accepted events with an alert configured in the given time interval.
	 * Logged user must have read access on the calendar.
	 */
	List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault;

	/**
	 * Retrieve last update (event creation or update) for a given calendar.
	 * Logged user must have read access on the calendar.
	 */
	Date getLastUpdate(AccessToken token, String calendar) throws ServerFault, NotAllowedException;

	/**
	 * Check that logged user has access to a given calendar
	 */
	boolean isWritableCalendar(AccessToken token, String calendar)
			throws ServerFault, NotAllowedException;

	/**
	 * change user of given calendar participation state
	 */
	boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, Participation participation, int sequence,
			boolean notification) throws ServerFault, NotAllowedException;

	/**
	 * This method is used to change the participation state of a specific occurrence
	 * using the recurrenceId.
	 * @throws ParseException if recurrenceId is not in the right format.
	 */
	boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, RecurrenceId recurrenceId, Participation participation, int sequence,
			boolean notification) throws ServerFault, EventNotFoundException, ParseException, NotAllowedException;
	
	/**
	 * Import ics file in calendar's user Adding a new attendee (owner) if
	 * calendar owner not exist in data's ics
	 * 
	 * Return ImportICalendarException if import fails
	 */
	int importICalendar(AccessToken token, String calendar, String ics, String clientId)
			throws ImportICalendarException, ServerFault, NotAllowedException;

	/**
	 * remove all calendar's events older than 6 month
	 * 
	 */
	void purge(AccessToken token, String calendar) throws ServerFault, NotAllowedException;

	/**
	 * Returns the events using the resource in a range of three months before
	 * and six months after the date parameter.
	 */
	Collection<Event> getResourceEvents(String resourceEmail, Date date) throws ServerFault;
}
