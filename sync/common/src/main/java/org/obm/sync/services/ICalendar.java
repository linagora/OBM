package org.obm.sync.services;

import java.util.Date;
import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;

public interface ICalendar {

	/**
	 * List all Calendars on which authenticated user as some rights
	 */
	CalendarInfo[] listCalendars(AccessToken token) throws ServerFault,
			AuthFault;

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
	 * 			  send email notification if needed
	 * @return the removed event on success, the found event if access rights
	 *         are too low to remove but enough to read, and null if event was
	 *         not found
	 */
	Event removeEvent(AccessToken token, String calendar, String eventId, int sequence, boolean notification)
			throws AuthFault, ServerFault;

	/**
	 * FIXME: remove this service
	 */
	Event removeEventByExtId(AccessToken token, String calendar,
			String extId, int sequence, boolean notification) throws AuthFault, ServerFault;

	/**
	 * FIXME: needs work
	 */
	Event modifyEvent(AccessToken token, String calendar, Event event,
			boolean updateAttendees, boolean notification) throws AuthFault, ServerFault;

	/**
	 * FIXME: needs work
	 */
	String createEvent(AccessToken token, String calendar, Event event, boolean notification)
			throws AuthFault, ServerFault;

	/**
	 * return every changes made to calendar since lastSync date for events into the sync range.
	 * This service treats participation changes as full changes.
	 * Logged user needs read rights on calendar.
	 */
	EventChanges getSyncInRange(AccessToken token, String calendar, Date lastSync,
			SyncRange syncRange) throws AuthFault, ServerFault;
	
	/**
	 * return every changes made to calendar since lastSync date.
	 * This service treats participation changes as full changes.
	 * Logged user needs read rights on calendar.
	 */
	EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws AuthFault, ServerFault;

	/**
	 * return every changes made to calendar since lastSync date.
	 * This service treats participation changes as special changes
	 * in order to let client know if the event itself has been modified
	 * or not.
	 * Logged user needs read rights on calendar.
	 */
	EventChanges getSyncWithSortedChanges(AccessToken token, String calendar,
			Date lastSync) throws AuthFault, ServerFault;
	
	/**
	 * return every event in calendar the will happen after start date Logged
	 * user needs read rights on calendar
	 */
	EventChanges getSyncEventDate(AccessToken token, String calendar,
			Date start) throws AuthFault, ServerFault;

	/**
	 * Find an event from its id.
	 * 
	 * @return the event if user has read access to owner calendar or the event
	 *         is present in its calendar, null if event is not found or user
	 *         has not enough rights to read this event
	 */
	Event getEventFromId(AccessToken token, String calendar, String id)
			throws AuthFault, ServerFault;

	/**
	 * Return id of events who have : - start date, subject and duration if not
	 * allday equals to the given event - start date day, subject if allday
	 * equals to the given event User needs read access on selected calendar to
	 * execute this service.
	 */
	KeyList getEventTwinKeys(AccessToken token, String calendar,
			Event event) throws AuthFault, ServerFault;

	/**
	 * get current user email based
	 */
	String getUserEmail(AccessToken token) throws AuthFault, ServerFault;

	/**
	 * list all eventId of event refused on calendar. User needs read access on
	 * selected calendar to execute this service.
	 */
	KeyList getRefusedKeys(AccessToken token, String calendar, Date since)
			throws AuthFault, ServerFault;

	/**
	 * List known categories.
	 */
	List<Category> listCategories(AccessToken at) throws AuthFault,
			ServerFault;

	/**
	 * @return the obm id of the event that have the given extId into the given
	 *         calendar
	 */
	Integer getEventObmIdFromExtId(AccessToken token, String calendar,
			String extId) throws ServerFault;

	/**
	 * retrieve an event by its extId into specified calendar User needs read
	 * access on selected calendar to execute this service.
	 */
	Event getEventFromExtId(AccessToken token, String calendar,
			String extId) throws AuthFault, ServerFault;

	/**
	 * retrieve all events between start and end date. User needs read access on
	 * selected calendar to execute this service.
	 */
	List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws AuthFault,
			ServerFault;

	/**
	 * retrieve all events of type evenType from calendar. User needs read
	 * access on selected calendar to execute this service.
	 */
	List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws AuthFault, ServerFault;

	/**
	 * List event with their ids and last update timestamp that the user didn't
	 * refused in a given time interval
	 */
	List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, AuthFault;

	/**
	 * Convert an event into an ICS
	 */
	String parseEvent(AccessToken token, Event event)
			throws ServerFault, AuthFault;

	/**
	 * Convert a list of event into an ICS
	 */
	String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault, AuthFault;

	/**
	 * Convert an ICS containing Events to a Event list
	 */
	List<Event> parseICS(AccessToken token, String ics)
			throws Exception, ServerFault;

	/**
	 * Convert an ICS Freebusy request into an FreeBusyRequest object
	 */
	FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics)
			throws ServerFault, AuthFault;

	/**
	 * Given a list of attendees and a time interval, this method computes the
	 * free time interval for each attendee and returns them.
	 * 
	 * Caller don't need any rights to call this as no sensible information is
	 * disclosed.
	 */
	List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fb)
			throws AuthFault, ServerFault;

	/**
	 * Convert a {@link FreeBusy} bean to an ICS
	 * 
	 * @return a {@link String} representing an ICS content
	 */
	String parseFreeBusyToICS(AccessToken token, FreeBusy fbr)
			throws ServerFault, AuthFault;

	/**
	 * List accepted events with an alert configured in the given time interval.
	 * Logged user must have read access on the calendar.
	 */
	List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, AuthFault;

	/**
	 * Retrieve last update (event creation or update) for a given calendar.
	 * Logged user must have read access on the calendar.
	 */
	Date getLastUpdate(AccessToken token, String calendar)
			throws ServerFault, AuthFault;

	/**
	 * Check that logged user has access to a given calendar
	 */
	boolean isWritableCalendar(AccessToken token, String calendar)
			throws ServerFault, AuthFault;
	
	/**
	 * change user of given calendar participation state
	 */
	boolean changeParticipationState(AccessToken token, String calendar, String extId, 
			ParticipationState participationState, int sequence, boolean notification) throws ServerFault;
	
	/**
	 * Import ics file in calendar's user
	 * Adding a new attendee (owner) if calendar owner not exist in data's ics
	 * 
	 * Return ImportICalendarException if import fails
	 */
	int importICalendar(AccessToken token, String calendar, String ics) 
		throws ImportICalendarException, AuthFault, ServerFault;
	
	/**
	 * remove all calendar's events older than 6 month 
	 * 
	 */
	void purge(AccessToken token, String calendar) throws ServerFault;

}
