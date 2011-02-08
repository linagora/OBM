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
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.items.EventChanges;

public interface ICalendar {

	/**
	 * List all Calendars on which authenticated user as some rights
	 */
	public CalendarInfo[] listCalendars(AccessToken token) throws ServerFault,
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
	 * @return the removed event on success, the found event if access rights
	 *         are too low to remove but enough to read, and null if event was
	 *         not found
	 */
	public Event removeEvent(AccessToken token, String calendar, String eventId)
			throws AuthFault, ServerFault;

	/**
	 * FIXME: remove this service
	 */
	public Event removeEventByExtId(AccessToken token, String calendar,
			String extId) throws AuthFault, ServerFault;

	/**
	 * FIXME: needs work
	 */
	public Event modifyEvent(AccessToken token, String calendar, Event event,
			boolean updateAttendees) throws AuthFault, ServerFault;

	/**
	 * FIXME: needs work
	 */
	public String createEvent(AccessToken token, String calendar, Event event)
			throws AuthFault, ServerFault;

	/**
	 * return every changes made to calendar since lastSync date Logged user
	 * needs read rights on calendar
	 */
	public EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws AuthFault, ServerFault;

	/**
	 * return every event in calendar the will happen after start date Logged
	 * user needs read rights on calendar
	 */
	public EventChanges getSyncEventDate(AccessToken token, String calendar,
			Date start) throws AuthFault, ServerFault;

	/**
	 * Find an event from its id.
	 * 
	 * @return the event if user has read access to owner calendar or the event
	 *         is present in its calendar, null if event is not found or user
	 *         has not enough rights to read this event
	 */
	public Event getEventFromId(AccessToken token, String calendar, String id)
			throws AuthFault, ServerFault;

	/**
	 * Return id of events who have : - start date, subject and duration if not
	 * allday equals to the given event - start date day, subject if allday
	 * equals to the given event User needs read access on selected calendar to
	 * execute this service.
	 */
	public KeyList getEventTwinKeys(AccessToken token, String calendar,
			Event event) throws AuthFault, ServerFault;

	/**
	 * get current user email based
	 */
	public String getUserEmail(AccessToken token) throws AuthFault, ServerFault;

	/**
	 * list all eventId of event refused on calendar. User needs read access on
	 * selected calendar to execute this service.
	 */
	public KeyList getRefusedKeys(AccessToken token, String calendar, Date since)
			throws AuthFault, ServerFault;

	/**
	 * List known categories.
	 */
	public List<Category> listCategories(AccessToken at) throws AuthFault,
			ServerFault;

	/**
	 * @return the obm id of the event that have the given extId into the given
	 *         calendar
	 */
	public Integer getEventObmIdFromExtId(AccessToken token, String calendar,
			String extId) throws ServerFault;

	/**
	 * retrieve an event by its extId into specified calendar User needs read
	 * access on selected calendar to execute this service.
	 */
	public Event getEventFromExtId(AccessToken token, String calendar,
			String extId) throws AuthFault, ServerFault;

	/**
	 * retrieve all events between start and end date. User needs read access on
	 * selected calendar to execute this service.
	 */
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws AuthFault,
			ServerFault;

	/**
	 * retrieve all events of type evenType from calendar. User needs read
	 * access on selected calendar to execute this service.
	 */
	public List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws AuthFault, ServerFault;

	/**
	 * List event with their ids and last update timestamp that the user didn't
	 * refused in a given time interval
	 */
	public List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, AuthFault;

	/**
	 * Convert an event into an ICS
	 */
	public String parseEvent(AccessToken token, Event event)
			throws ServerFault, AuthFault;

	/**
	 * Convert a list of event into an ICS
	 */
	public String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault, AuthFault;

	/**
	 * Convert an ICS containing Events to a Event list
	 */
	public List<Event> parseICS(AccessToken token, String ics)
			throws Exception, ServerFault;

	/**
	 * Convert an ICS Freebusy request into an FreeBusyRequest object
	 */
	public FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics)
			throws ServerFault, AuthFault;

	/**
	 * Given a list of attendees and a time interval, this method computes the
	 * free time interval for each attendee and returns them.
	 * 
	 * Caller don't need any rights to call this as no sensible information is
	 * disclosed.
	 */
	public List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fb)
			throws AuthFault, ServerFault;

	/**
	 * Convert a {@link FreeBusy} bean to an ICS
	 * 
	 * @return a {@link String} representing an ICS content
	 */
	public String parseFreeBusyToICS(AccessToken token, FreeBusy fbr)
			throws ServerFault, AuthFault;

	/**
	 * List accepted events with an alert configured in the given time interval.
	 * Logged user must have read access on the calendar.
	 */
	public List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, AuthFault;

	/**
	 * Retrieve last update (event creation or update) for a given calendar.
	 * Logged user must have read access on the calendar.
	 */
	public Date getLastUpdate(AccessToken token, String calendar)
			throws ServerFault, AuthFault;

	/**
	 * Check that logged user has access to a given calendar
	 */
	public boolean isWritableCalendar(AccessToken token, String calendar)
			throws ServerFault, AuthFault;
	
	/**
	 * change user of given calendar participation state
	 */
	public boolean changeParticipationState(AccessToken token, String calendar, String extId, ParticipationState participationState) throws ServerFault;
	
}
