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

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateTime;

import org.apache.commons.lang.StringUtils;
import org.obm.annotations.transactional.Transactional;
import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.NotAllowedException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventParticipationState;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.services.ICalendar;
import org.obm.sync.services.ImportICalendarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.HelperService;
import fr.aliacom.obm.utils.LogUtils;

public class CalendarBindingImpl implements ICalendar {

	private static final Logger logger = LoggerFactory
			.getLogger(CalendarBindingImpl.class);

	private EventType type;
	
	private final CalendarDao calendarDao;
	private final CategoryDao categoryDao;
	
	private final DomainService domainService;
	private final UserService userService;
	private final EventChangeHandler eventChangeHandler;
	
	private final HelperService helperService;
	private final Ical4jHelper ical4jHelper;

	private final ICalendarFactory calendarFactory;
	

	private CalendarInfo makeOwnCalendarInfo(ObmUser user) {
		CalendarInfo myself = new CalendarInfo();
		myself.setMail(helperService.constructEmailFromList(user.getEmail(), user
				.getDomain().getName()));
		myself.setUid(user.getLogin());
		myself.setFirstname(user.getFirstName());
		myself.setLastname(user.getLastName());
		myself.setRead(true);
		myself.setWrite(true);
		return myself;
	}
	
	@Inject
	protected CalendarBindingImpl(EventChangeHandler eventChangeHandler,
			DomainService domainService, UserService userService,
			CalendarDao calendarDao,
			CategoryDao categoryDao, HelperService helperService, 
			Ical4jHelper ical4jHelper, ICalendarFactory calendarFactory) {
		this.eventChangeHandler = eventChangeHandler;
		this.domainService = domainService;
		this.userService = userService;
		this.calendarDao = calendarDao;
		this.categoryDao = categoryDao;
		this.helperService = helperService;
		this.ical4jHelper = ical4jHelper;
		this.calendarFactory = calendarFactory;
	}

	@Override
	@Transactional
	public CalendarInfo[] listCalendars(AccessToken token) throws ServerFault {
		try {
			Collection<CalendarInfo> calendarInfos = getRights(token);
			CalendarInfo[] ret = calendarInfos.toArray(new CalendarInfo[0]);
			logger.info(LogUtils.prefix(token) + "Returning " + ret.length
					+ " calendar infos.");
			return ret;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public CalendarInfo[] getCalendarMetadata(AccessToken token, String[] calendars) throws ServerFault {
		try {
			ObmUser user = userService.getUserFromAccessToken(token);

			// Since multidomain emails won't have the @domain part after them in the database,
			// add the stripped version of the calendar emails (minus the @domain part) to the query
			
			// Create a new list using Arrays.asList(calendars), since asList returns an unmodifiable list
			List<String> calendarEmails = new ArrayList<String>();
			boolean hasUserEmail = false;
			String userEmail = user.getEmail();
			for (String calendarEmail : calendars) {
				// We'll add the user manually later
				if (calendarEmail.equals(userEmail)) {
					hasUserEmail = true;
					continue;
				}
				int atPosition = calendarEmail.indexOf('@');
				if (atPosition > 0) {
					String strippedCalendarEmail = calendarEmail.substring(0, atPosition);
					calendarEmails.add(calendarEmail);
					calendarEmails.add(strippedCalendarEmail);
				}
				else {
					logger.warn(LogUtils.prefix(token) + "Got an invalid email address: " + calendarEmail);
				}
			}


			Collection<CalendarInfo> calendarInfos;
			if (calendarEmails.size() > 0) {
				calendarInfos = calendarDao.getCalendarMetadata(user, calendarEmails);
            }
			else {
				calendarInfos = new HashSet<CalendarInfo>();
            }

			if (hasUserEmail) {
				// Add the calendar of the current user if needed, since the user's permissions over her own calendars
				// will not be listed in the database
				CalendarInfo myself = makeOwnCalendarInfo(user);
				calendarInfos.add(myself);
			}
			CalendarInfo[] ret = calendarInfos.toArray(new CalendarInfo[0]);
			logger.info(LogUtils.prefix(token) + "Returning " + ret.length
					+ " calendar infos.");
			return ret;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Collection<CalendarInfo> getRights(AccessToken t) throws FindException {
		Collection<CalendarInfo> rights = t.getCalendarRights();
		if (rights == null) {
			rights = listCalendarsImpl(t);
			t.setCalendarRights(rights);
		}
		return rights;
	}

	private Collection<CalendarInfo> listCalendarsImpl(AccessToken token)
			throws FindException {
		ObmUser user = userService.getUserFromAccessToken(token);
		Collection<CalendarInfo> calendarInfos = calendarDao.listCalendars(user);
		CalendarInfo myself = makeOwnCalendarInfo(user);
		calendarInfos.add(myself);
		return calendarInfos;
	}

	@Override
	@Transactional
	public void removeEventById(AccessToken token, String calendar, EventObmId eventId, int sequence, boolean notification)
			throws ServerFault, EventNotFoundException, NotAllowedException {
		
		try {
			Event ev = calendarDao.findEventById(token, eventId);

			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			ObmUser owner = userService.getUserFromLogin(ev.getOwner(), token.getDomain().getName());
			if (owner != null) {
				if (helperService.canWriteOnCalendar(token, calendar)) {
					if (owner.getEmail().equals(calendarUser.getEmail())) {
						cancelEvent(token, calendar, notification, eventId, ev);
					} else {
						changeParticipationStateInternal(
								token, calendar, ev.getExtId(), ParticipationState.DECLINED, sequence, notification);
					}
				} else {
					throw new NotAllowedException(calendar + " has no write right to remove event " + ev.getTitle());
				}
			} else {
				throw new NotAllowedException("It's not possible to remove an event without owner " + ev.getTitle());
			}
		} catch (ServerFault e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		} catch (FindException e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		} catch (SQLException e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}
	}

	private void cancelEvent(AccessToken token, String calendar,
			boolean notification, EventObmId uid, Event ev) throws SQLException,
			FindException, EventNotFoundException, ServerFault {
		
		Event removed = calendarDao.removeEventById(token, uid, ev.getType(), ev.getSequence() + 1);
		logger.info(LogUtils.prefix(token) + "Calendar : event[" + uid + "] removed");
		notifyOnRemoveEvent(token, calendar, removed, notification);
	}

	private Event cancelEventByExtId(AccessToken token, ObmUser calendar, Event event, boolean notification) throws SQLException, FindException {
		EventExtId extId = event.getExtId();
		Event removed = calendarDao.removeEventByExtId(token, calendar, extId, event.getSequence() + 1);
		logger.info(LogUtils.prefix(token) + "Calendar : event[" + extId + "] removed");
		notifyOnRemoveEvent(token, calendar.getEmail(), removed, notification);
		return removed;
	}

	
	private void notifyOnRemoveEvent(AccessToken token, String calendar, Event ev, boolean notification) throws FindException {
		if (ev.isInternalEvent()) {
			ObmUser user = userService.getUserFromAccessToken(token);
			eventChangeHandler.delete(user, ev, notification, token);
		} else {
			notifyOrganizerForExternalEvent(token, calendar, ev, ParticipationState.DECLINED, notification);
		}
	}

	@Override
	@Transactional
	public Event removeEventByExtId(AccessToken token, String calendar,
			EventExtId extId, int sequence, boolean notification) throws ServerFault {
		try {
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			final Event ev = calendarDao.findEventByExtId(token, calendarUser, extId);

			if (ev == null) {
				logger.info(LogUtils.prefix(token) + "Calendar : event[" + extId + "] not removed, it doesn't exist");
				return ev;
			} else {

				if (!helperService.canWriteOnCalendar(token, calendar)) {
					logger.info(LogUtils.prefix(token) + "remove not allowed of " + ev.getTitle());
					return ev;
				}

				ObmUser owner = userService.getUserFromLogin(ev.getOwner(), token.getDomain().getName());
				if (owner == null) {
					logger.info(LogUtils.prefix(token) + "error, trying to remove an event without any owner : " + ev.getTitle());
					return ev;
				}
				
				if (owner.getEmail().equals(calendarUser.getEmail())) {
					return cancelEventByExtId(token, calendarUser, ev, notification);
				} else {
					changeParticipationStateInternal(token, calendar, ev.getExtId(), ParticipationState.DECLINED, sequence, notification);
					return calendarDao.findEventByExtId(token, calendarUser, extId);
				}
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Event modifyEvent(AccessToken token, String calendar, Event event, boolean updateAttendees, boolean notification) 
		throws ServerFault {

		if (event == null) {
			logger.warn(LogUtils.prefix(token) + "Modify on NULL event: doing nothing");
			return null;
		}
		
		try {
			
			final ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			final Event before = loadCurrentEvent(token, calendarUser, event);

			if (before == null) {
				logger.warn(LogUtils.prefix(token) + "Event[uid:"+ event.getObmId() + "extId:" + event.getExtId() +
						"] doesn't exist in database: : doing nothing");
				return null;
			}
			
			if (before.getOwner() != null && !helperService.canWriteOnCalendar(token, before.getOwner())) {
				logger.info(LogUtils.prefix(token) + "Calendar : "
						+ token.getUserLogin() + " cannot modify event["
						+ before.getTitle() + "] because not owner"
						+ " or no write right on owner " + before.getOwner()+". ParticipationState will be updated.");
				return before;
				
			} else {
				
				assignDelegationRightsOnAttendees(token, event);
				if (before.isInternalEvent()) {
					return modifyInternalEvent(token, calendar, before, event, updateAttendees, notification);
				} else {
					return modifyExternalEvent(token, calendar, event, updateAttendees, notification);
				}
			}

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}

	}
	
	private Event loadCurrentEvent(AccessToken token, ObmUser calendarUser, Event event) throws EventNotFoundException, ServerFault {
		if (event.getObmId() == null && event.getExtId() != null && event.getExtId().getExtId() != null) {
			final Event currentEvent = calendarDao.findEventByExtId(token, calendarUser, event.getExtId());
			if (currentEvent != null) {
				event.setUid(currentEvent.getObmId());
			}
			return currentEvent;
		} else {
			return calendarDao.findEventById(token, event.getObmId());
		}
	}

	private Event modifyInternalEvent(AccessToken token, String calendar, Event before, Event event, boolean updateAttendees,
			boolean notification) throws ServerFault {
		
		try {
			boolean hasImportantChanges = hasImportantChanges(before, event);

			if (before.hasImportantChangesExceptedEventException(event)) {
                changePartipationState(event);
                if(event.isRecurrent()) {
	                EventRecurrence eventRecurrence = event.getRecurrence();
	                List<Event> eventsExceptions = eventRecurrence.getEventExceptions();
	                changePartipationStateOfException(eventsExceptions);
                }
            }
			
			List<Event> exceptionWithChanges = event.getExceptionsWithImportantChanges(before);
			changePartipationStateOfException(exceptionWithChanges);
			
			final Event after = calendarDao.modifyEventForcingSequence(
					token, calendar, event, updateAttendees, event.getSequence(), true);

			applyDelegationRightsOnAttendeesToEvent(token, after);
			
			if (after != null) {
				logger.info(LogUtils.prefix(token) + "Calendar : internal event[" + after.getTitle() + "] modified");
			}

            ObmUser user = userService.getUserFromAccessToken(token);
            if(before.hasChangesOnEventAttributesExceptedEventException(after)) {
            	eventChangeHandler.update(user, before, after, notification, hasImportantChanges, token);
            } else if(after != null){
				for(Event exception : exceptionWithChanges) {
					Event previousException = before.getEventInstanceWithRecurrenceId(exception.getRecurrenceId());
					eventChangeHandler.update(user, previousException, exception, notification, hasImportantChanges, token);
				}
            }

			return after;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private void changePartipationStateOfException(List<Event> exceptionWithChanges) {
		for(Event exception : exceptionWithChanges) {
			changePartipationState(exception);
		}
	}

	private boolean hasImportantChanges(Event before, Event after) {
		boolean hasImportantChanges = before.getSequence() != after.getSequence();
		return hasImportantChanges;
	}

	private Event modifyExternalEvent(AccessToken token, String calendar, 
			Event event, boolean updateAttendees, boolean notification) throws ServerFault {
		try {
			Attendee attendee = calendarOwnerAsAttendee(token, calendar, event);
			if (isEventDeclinedForCalendarOwner(attendee)) {
				ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
				calendarDao.removeEventByExtId(token, calendarOwner, event.getExtId(), event.getSequence());
				notifyOrganizerForExternalEvent(token, calendar, event, notification);
				logger.info(LogUtils.prefix(token) + "Calendar : External event[" + event.getTitle() + 
						"] removed, calendar owner won't attende to it");
				return event;
			} else {
				Event after = calendarDao.modifyEvent(token,  calendar, event, updateAttendees, false);
				if (after != null) {
					logger.info(LogUtils.prefix(token) + "Calendar : External event[" + after.getTitle() + "] modified");
				}
				notifyOrganizerForExternalEvent(token, calendar, after, notification);
				return after;
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public EventObmId createEvent(AccessToken token, String calendar, Event event, boolean notification)
			throws ServerFault, EventAlreadyExistException {

		try {
			if (event == null) {
				logger.warn(LogUtils.prefix(token)
						+ "creating NULL event, returning fake id 0");
				throw new ServerFault(
						"event creation without any data");
			}
			if (event.getObmId() != null) {
				logger.error(LogUtils.prefix(token)
						+ "event creation with an event coming from OBM");
				throw new ServerFault(
						"event creation with an event coming from OBM");
			}

			if (isEventExists(token, calendar, event)) {
				final String message = String
				.format("Calendar : duplicate with same extId found for event [%s, %s, %d, %s]",
						event.getTitle(), event.getDate()
						.toString(), event.getDuration(),
						event.getExtId());
				logger.info(LogUtils.prefix(token) + message);
				throw new EventAlreadyExistException(message);
			}

			if (!helperService.canWriteOnCalendar(token, calendar)) {
				String message = "[" + token.getUserLogin() + "] Calendar : "
						+ token.getUserLogin() + " cannot create event on "
						+ calendar + "calendar : no write right";
				logger.info(LogUtils.prefix(token) + message);
				throw new ServerFault(message);
			}
			
			assignDelegationRightsOnAttendees(token, event);
			Event ev = null;
			if (event.isInternalEvent()) {
				ev = createInternalEvent(token, calendar, event, notification);
			} else {
				ev = createExternalEvent(token, calendar, event, notification);
			}
			return ev.getObmId();
		} catch (FindException e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Event createExternalEvent(AccessToken token, String calendar, Event event, boolean notification) throws ServerFault {
		try {
			Attendee attendee = calendarOwnerAsAttendee(token, calendar, event);
			if (attendee == null) {
				String message = "Calendar : external event["+ event.getTitle() + "] doesn't involve calendar owner, ignoring creation";
				logger.info(LogUtils.prefix(token) + message);
				throw new ServerFault(message);
			}
			
			if (isEventDeclinedForCalendarOwner(attendee)) {
				logger.info(LogUtils.prefix(token) + "Calendar : external event["+ event.getTitle() + "] refused, mark event as deleted");
				Event ev = createEventAsDeleted(token, calendar, event);
				notifyOrganizerForExternalEvent(token, calendar, ev, notification);
				return ev;
			} else {
				changeOrganizerParticipationStateToAccepted(event);
				Event ev = calendarDao.createEvent(token, calendar, event, false);
				logger.info(LogUtils.prefix(token) + "Calendar : external event["+ ev.getTitle() + "] created");
				notifyOrganizerForExternalEvent(token, calendar, ev, notification);
				return ev;
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	private Event createEventAsDeleted(AccessToken token, String calendar, Event event) throws SQLException, FindException, ServerFault {
		Event ev = calendarDao.createEvent(token, calendar, event, false);
		return calendarDao.removeEvent(token, ev, event.getType(), event.getSequence());
	}

	private void changeOrganizerParticipationStateToAccepted(Event event) {
		for(Attendee att : event.getAttendees()){
			if(att.isOrganizer()){
				att.setState(ParticipationState.ACCEPTED);
			}
		}
	}

	private Attendee calendarOwnerAsAttendee(AccessToken token, String calendar, Event event) 
		throws FindException {
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		Attendee userAsAttendee = event.findAttendeeFromEmail(calendarOwner.getEmail());
		return userAsAttendee;
	}

	private boolean isEventDeclinedForCalendarOwner(Attendee userAsAttendee) {
		return userAsAttendee != null && userAsAttendee.getState() == ParticipationState.DECLINED;
	}

	private void notifyOrganizerForExternalEvent(AccessToken token,
			String calendar, Event ev, ParticipationState state, boolean notification) throws FindException {
		logger.info(LogUtils.prefix(token) + 
				"Calendar : sending participation notification to organizer of event ["+ ev.getTitle() + "]");
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		eventChangeHandler.updateParticipationState(ev, calendarOwner, state, notification, token);
	}

	
	private void notifyOrganizerForExternalEvent(AccessToken token, String calendar, Event ev,
			boolean notification) throws FindException {
		Attendee calendarOwnerAsAttendee = calendarOwnerAsAttendee(token, calendar, ev);
		notifyOrganizerForExternalEvent(token, calendar, ev, calendarOwnerAsAttendee.getState(), notification);
	}

	private Event createInternalEvent(AccessToken token, String calendar, Event event, boolean notification) throws ServerFault {
		try{
			changePartipationState(event);
			Event ev = calendarDao.createEvent(token, calendar, event, true);
			ev = calendarDao.findEventById(token, ev.getObmId());
			ObmUser user = userService.getUserFromAccessToken(token);
			eventChangeHandler.create(user, ev, notification, token);
			logger.info(LogUtils.prefix(token) + "Calendar : internal event["
				+ ev.getTitle() + "] created");
			return ev;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	private void changePartipationState(Event event){
		for (Attendee att: event.getAttendees()) {
			if (att.isCanWriteOnCalendar()) {
				if (ParticipationState.NEEDSACTION.equals(att.getState())) {
					att.setState(ParticipationState.ACCEPTED);
				}
			} else {
				att.setState(ParticipationState.NEEDSACTION);
			}
		}
	}
	
	private void assignDelegationRightsOnAttendees(AccessToken token, Event event) {
		applyDelegationRightsOnAttendeesToEvent(token, event);
		List<Event> eventsExceptions = event.getEventsExceptions();
		for (Event eventException : eventsExceptions) {
			applyDelegationRightsOnAttendeesToEvent(token, eventException);
		}
	}
	
	private void applyDelegationRightsOnAttendeesToEvent(AccessToken token, Event event) {
		for (Attendee att: event.getAttendees()) {
			boolean isWriteOnCalendar = !StringUtils.isEmpty(att.getEmail()) && helperService.canWriteOnCalendar(token,  att.getEmail());
			att.setCanWriteOnCalendar(isWriteOnCalendar);
		}
	}
	
	@Override
	@Transactional
	public EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws ServerFault {
		return getSync(token, calendar, lastSync, null, false);
	}
	
	@Override
	@Transactional
	public EventChanges getSyncInRange(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange) throws ServerFault {
		return getSync(token, calendar, lastSync, syncRange, false);
	}

	@Override
	@Transactional
	public EventChanges getSyncWithSortedChanges(AccessToken token,
			String calendar, Date lastSync) throws ServerFault {

		EventChanges changes = getSync(token, calendar, lastSync, null, false);
		
		//sort between update and participation update based on event timestamp
		sortUpdatedEvents(changes, lastSync);
		
		return changes;
	}
	
	private void sortUpdatedEvents(EventChanges changes, Date lastSync) {
		List<Event> updated = new ArrayList<Event>();
		List<Event> participationChanged = new ArrayList<Event>();
		
		for (Event event: changes.getUpdated()) {
			// As Lightning does not know how to differentiate fake Exdate 
			// from true Exdate from OBMFULL-3116, we put every recurrent event
			// into updated tag for a getSync response.
			if (event.modifiedSince(lastSync) || event.isRecurrent()) {
				updated.add(event);
			} else {
				//means that only participation changed
				participationChanged.add(event);
			}
			
		}
		changes.setParticipationUpdated(eventsToParticipationUpdateArray(participationChanged));
		changes.setUpdated(updated.toArray(new Event[0]));
	}

	private ParticipationChanges[] eventsToParticipationUpdateArray(List<Event> participationChanged) {
		return Lists.transform(participationChanged, new Function<Event, ParticipationChanges>() {
			@Override
			public ParticipationChanges apply(Event event) {
				ParticipationChanges participationChanges = new ParticipationChanges(); 
				participationChanges.setAttendees(event.getAttendees());
				participationChanges.setEventExtId(event.getExtId());
				if(event.getRecurrenceId() != null) {
					String recurrenceId = getRecurrenceIdToIcalFormat(event.getRecurrenceId());
					participationChanges.setRecurrenceId(new RecurrenceId(recurrenceId));
				}
				participationChanges.setEventId(event.getObmId());
				return participationChanges;
			}
		}).toArray(new ParticipationChanges[0]);
	}

	private String getRecurrenceIdToIcalFormat(Date recurrenceId) {
		DateTime recurrenceIdToIcalFormat = new DateTime(recurrenceId);
		recurrenceIdToIcalFormat.setUtc(true);
		return recurrenceIdToIcalFormat.toString();
	}
	
	@Override
	@Transactional
	public EventChanges getSyncEventDate(AccessToken token, String calendar,
			Date lastSync) throws ServerFault {
		return getSync(token, calendar, lastSync, null, true);
	}

	private EventChanges getSync(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange, boolean onEventDate) throws ServerFault {

		logger.info(LogUtils.prefix(token) + "Calendar : getSync(" + calendar
				+ ", " + lastSync + ")");

		ObmUser calendarUser = null;
		try {
			calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		} catch (FindException e) {
			throw new ServerFault(e.getMessage());
		}

		if (!helperService.canReadCalendar(token, calendar)) {
			logger.error(LogUtils.prefix(token) + "user " + token.getUserLogin()
					+ " tried to sync calendar " + calendar
					+ " => permission denied");
			throw new ServerFault("Read permission denied for "
					+ token.getUserLogin() + " on " + calendar);
		}

		try {
			EventChanges ret = calendarDao.getSync(token, calendarUser,
					lastSync, syncRange, type, onEventDate);
			logger.info(LogUtils.prefix(token) + "Calendar : getSync("
					+ calendar + ") => " + ret.getUpdated().length + " upd, "
					+ ret.getRemoved().length + " rmed.");
			return ret;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Event getEventFromId(AccessToken token, String calendar, EventObmId eventId) throws ServerFault, EventNotFoundException {
		Event event = calendarDao.findEventById(token, eventId);
		String owner = event.getOwner();
		if (owner == null) {
			throw new ServerFault("Owner not found for event " + eventId);
		}
		
		if (!helperService.canReadCalendar(token, owner) && !helperService.attendeesContainsUser(event.getAttendees(), token)) {
			logger.info(LogUtils.prefix(token) + " event " + eventId + " not readable by this user");
			throw new EventNotFoundException(eventId);
		}
		return event;
	}

	@Override
	@Transactional
	public KeyList getEventTwinKeys(AccessToken token, String calendar,
			Event event) throws ServerFault {
		if (!helperService.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		try {
			ObmDomain domain = domainService
					.findDomainByName(token.getDomain().getName());
			List<String> keys = calendarDao.findEventTwinKeys(calendar,
					event, domain);
			logger.info(LogUtils.prefix(token) + "found " + keys.size()
					+ " twinkeys ");
			return new KeyList(keys);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public KeyList getRefusedKeys(AccessToken token, String calendar, Date since)
			throws ServerFault {
		if (!helperService.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		try {
			ObmUser user = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			List<String> keys = calendarDao.findRefusedEventsKeys(user, since);
			return new KeyList(keys);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<Category> listCategories(AccessToken token) throws ServerFault {
		try {
			List<Category> c = categoryDao.getCategories(token);
			return c;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public String getUserEmail(AccessToken token) throws ServerFault {
		try {
			ObmUser obmuser = userService.getUserFromAccessToken(token);
			if (obmuser != null) {
				return obmuser.getEmail();
			}
			return "";

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public EventObmId getEventObmIdFromExtId(AccessToken token, String calendar,
			EventExtId extId) throws ServerFault, EventNotFoundException {
		Event event = getEventFromExtId(token, calendar, extId);
		return event.getObmId();
	}

	@Override
	@Transactional
	public Event getEventFromExtId(AccessToken token, String calendar, EventExtId extId) 
			throws ServerFault, EventNotFoundException {
		
		if (!helperService.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		try {
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			Event event = calendarDao.findEventByExtId(token, calendarUser, extId);
			if (event == null) {
				throw new EventNotFoundException("Event from extId " + extId + " not found.");
			} 
			return event;
		} catch (FindException e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws ServerFault {
		if (!helperService.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		ObmUser calendarUser = null;
		try {
			calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		} catch (FindException e1) {
			throw new ServerFault(e1.getMessage());
		}
		try {
			return calendarDao.listEventsByIntervalDate(token,
					calendarUser, start, end, type);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws ServerFault {
		try {
			if (helperService.canReadCalendar(token, calendar)) {
				ObmUser calendarUser = userService.getUserFromCalendar(calendar,
						token.getDomain().getName());
				return calendarDao.findAllEvents(token, calendarUser,
						eventType);
			}
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public String parseEvent(AccessToken token, Event event) throws ServerFault {
		return ical4jHelper.parseEvent(event, createIcal4jUserFrom(token));
	}

	@Override
	@Transactional
	public String parseEvents(AccessToken token, List<Event> events) throws ServerFault {
		return ical4jHelper.parseEvents(createIcal4jUserFrom(token), events);
	}

	@Override
	@Transactional
	public List<Event> parseICS(AccessToken token, String ics)
			throws Exception, ServerFault {
		String fixedIcs = fixIcsAttendees(ics);
		String calendar = getDefaultCalendarFromToken(token);
		List<Event> events = ical4jHelper.parseICSEvent(fixedIcs, createIcal4jUserFrom(token));
		for (Event event: events) {
			try {
				EventObmId id = getEventObmIdFromExtId(token, calendar, event.getExtId());
				event.setUid(id);
			} catch (EventNotFoundException e) {
				//not found in database, so EventObmId doesn't exist
			}
		}
		return events;
	}

	private String fixIcsAttendees(String ics) {
		// Used to fix a bug in ical4j
		// Error parsing ATTENDEES in delegated VTODOS - ID: 2833134
		String modifiedIcs = ics;
		int i = ics.indexOf("RECEIVED-SEQUENCE");
		while (i > 0) {
			int ie = ics.indexOf(";", i) + 1;
			if (ie <= 0) {
				ie = ics.indexOf(":", i);
			}
			modifiedIcs = ics.substring(0, i) + ics.substring(ie);
			i = ics.indexOf("RECEIVED-SEQUENCE");
		}
		i = ics.indexOf("RECEIVED-DTSTAMP");
		while (i > 0) {
			int ie = ics.indexOf(";", i + 1);
			if (ie <= 0) {
				ie = ics.indexOf(":", i);
			}
			modifiedIcs = ics.substring(0, i - 1) + ics.substring(ie);
			i = ics.indexOf("RECEIVED-DTSTAMP");
		}
		return modifiedIcs;
	}

	@Override
	@Transactional
	public FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics)
			throws ServerFault {
		try {
			return ical4jHelper.parseICSFreeBusy(ics);
		} catch (Exception e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}

	}

	@Override
	@Transactional
	public List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String specificCalendar, Date start, Date end)
			throws ServerFault {

		try {
			String calendar = getCalendarOrDefault(token, specificCalendar);
			if (helperService.canReadCalendar(token, calendar)) {
				ObmUser calendarUser = userService.getUserFromCalendar(calendar,
						token.getDomain().getName());
				return calendarDao
						.getEventParticipationStateWithAlertFromIntervalDate(
								token, calendarUser, start, end, type);
			}
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private String getDefaultCalendarFromToken(AccessToken token) {
		return token.getUserLogin();
	}

	
	private String getCalendarOrDefault(AccessToken token, String calendar) {
		if (StringUtils.isEmpty(calendar)) {
			return getDefaultCalendarFromToken(token);
		}
		return calendar;
	}

	@Override
	@Transactional
	public List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault {
		try {
			if (helperService.canReadCalendar(token, calendar)) {
				ObmUser calendarUser = userService.getUserFromCalendar(calendar,
						token.getDomain().getName());
				return calendarDao
						.getEventTimeUpdateNotRefusedFromIntervalDate(token,
								calendarUser, start, end, type);
			}
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Date getLastUpdate(AccessToken token, String calendar)
			throws ServerFault {
		try {
			if (helperService.canReadCalendar(token, calendar)) {
				return calendarDao.findLastUpdate(token, calendar);
			}
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public boolean isWritableCalendar(AccessToken token, String calendar)
			throws ServerFault {
		try {
			return helperService.canWriteOnCalendar(token, calendar);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fb)
			throws ServerFault {
		try {
			return calendarDao.getFreeBusy(token.getDomain(), fb);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public String parseFreeBusyToICS(AccessToken token, FreeBusy fbr)
			throws ServerFault {
		try {
			return ical4jHelper.parseFreeBusy(fbr);
		} catch (Exception e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	public void setEventType(EventType type) {
		this.type = type;
	}

	@Override
	@Transactional
	public boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, ParticipationState participationState, int sequence, boolean notification) throws ServerFault {
		String userEmail = userService.getUserFromAccessToken(token).getEmail();
		if (helperService.canWriteOnCalendar(token, calendar)) {
			try {
				boolean wasDone = changeParticipationStateInternal(token, calendar, extId, participationState, sequence,
						notification);
				if (!wasDone) {
					logger.warn("Change of participation state failed to " + participationState + " (got sequence number " +
						sequence + ", probably stale) on calendar " + calendar + " on event "+ extId + " by user " + userEmail);
				}
				return wasDone;
			} catch (FindException e) {
				throw new ServerFault("no user found with calendar " + calendar);
			} catch (SQLException e) {
				throw new ServerFault(e);
			}
		}
		else {
			throw new ServerFault("The user " + userEmail + " has no write rights on calendar " + calendar);
		}
	}
	

	private boolean changeParticipationStateInternal(AccessToken token,
            String calendar, EventExtId extId,
            ParticipationState participationState, int sequence, boolean notification)
            throws FindException, SQLException {
    
	    ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
	    Event currentEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
	    boolean changed = false;
	    if (currentEvent != null) {
	             changed = applyParticipationChange(token, extId, participationState,
	                            sequence, calendarOwner, currentEvent);
	    }
	    
	    Event newEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
	    if (newEvent != null) {
	            eventChangeHandler.updateParticipationState(newEvent, calendarOwner, participationState, notification, token);
	    } else {
	            logger.error("event with extId : "+ extId + " is no longer in database, ignoring notification");
	    }
	    return changed;
	}
	
	@Override
	@Transactional
	public boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, RecurrenceId recurrenceId, ParticipationState participationState, int sequence, boolean notification) throws ServerFault, EventNotFoundException, ParseException {
		String userEmail = userService.getUserFromAccessToken(token).getEmail();
		if (helperService.canWriteOnCalendar(token, calendar)) {
			try {
				boolean wasDone = changeParticipationStateForRecursiveEvent(token, calendar, extId, recurrenceId, participationState, sequence,
						notification);
				if (!wasDone) {
					logger.warn("Change of participation state failed to " + participationState + " (got sequence number " +
						sequence + ", probably stale) on calendar " + calendar + " on event "+ extId + " by user " + userEmail);
				}
				return wasDone;
			} catch (FindException e) {
				throw new ServerFault("no user found with calendar " + calendar);
			} catch (SQLException e) {
				throw new ServerFault(e);
			} catch (ParseException e) {
				throw new ParseException(e.getMessage(), 0);
			}
		}
		else {
			throw new ServerFault("The user " + userEmail + " has no write rights on calendar " + calendar);
		}
	}
	
	private boolean changeParticipationStateForRecursiveEvent(AccessToken token,
			String calendar, EventExtId extId, RecurrenceId recurrenceId,
			ParticipationState participationState, int sequence, boolean notification)
			throws FindException, SQLException, EventNotFoundException, ServerFault, ParseException {
		
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		Event currentEvent = null;
		if (recurrenceId != null) {
			currentEvent = calendarDao.findEventByExtIdAndRecurrenceId(token, calendarOwner, extId, recurrenceId);
		} else {
			currentEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
		}

		boolean changed = false;
		if (currentEvent != null) {
			if (recurrenceId != null) {
				changed = applyParticipationChange(token, extId, recurrenceId, participationState, 
					sequence, calendarOwner, currentEvent);
			} else {
				changed = applyParticipationChange(token, extId, participationState, 
						sequence, calendarOwner, currentEvent);
			}
		} else {
			Event parentEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
			if(parentEvent != null) {
				String owner = parentEvent.getOwner();
				Event eventException = parentEvent.clone();
				EventRecurrence recurrence = new EventRecurrence();
				recurrence.setKind(RecurrenceKind.lookup("none"));
				eventException.setRecurrence(recurrence);
				if (recurrenceId != null) {
					eventException.setDate(new DateTime(recurrenceId.getRecurrenceId()));
					eventException.setRecurrenceId(new DateTime(recurrenceId.getRecurrenceId()));					
				}	
				parentEvent.getRecurrence().addEventException(eventException);
				parentEvent = calendarDao.modifyEventForcingSequence(token, owner, parentEvent, true, parentEvent.getSequence(), true);
				changed = applyParticipationChange(token, extId, recurrenceId, participationState, 
						sequence, calendarOwner, eventException);
			}
		}
		
		Event newEvent = null;
		if (recurrenceId != null) {
			newEvent = calendarDao.findEventByExtIdAndRecurrenceId(token, calendarOwner, extId, recurrenceId);
		} else {
			newEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
		}

		if (newEvent != null) {
			eventChangeHandler.updateParticipationState(newEvent, calendarOwner, participationState, notification, token);
		} else {
			if (recurrenceId != null) {
				logger.error("event with extId : {} and recurrenceId : {} is no longer in database, ignoring notification", extId, recurrenceId);
			} else {
				logger.error("event with extId : {} is no longer in database, ignoring notification", extId);
			}
		}
		return changed;
	}

	private boolean applyParticipationChange(AccessToken token, EventExtId extId,
			ParticipationState participationState, int sequence,
			ObmUser calendarOwner, Event currentEvent) throws SQLException {
		
		if (currentEvent.getSequence() == sequence) {
			boolean changed = false;
			changed = calendarDao.changeParticipationState(token, calendarOwner, extId, participationState);
				logger.info(LogUtils.prefix(token) + 
						"Calendar : event[extId:{}] change participation state for user {} " 
						+ "new state : {}", new Object[]{extId, calendarOwner.getEmail(), participationState});

			return changed;
		} else {
			logger.info(LogUtils.prefix(token) + 
					"Calendar : event[extId:" + extId + "] ignoring new participation state for user " + 
					calendarOwner.getEmail() + " as sequence number is different from current event (got " + sequence + ", expected " + currentEvent.getSequence());
			return false;
		}
	}
	
	private boolean applyParticipationChange(AccessToken token, EventExtId extId,
			RecurrenceId recurrenceId, ParticipationState participationState, int sequence,
			ObmUser calendarOwner, Event currentEvent) throws SQLException, ParseException {
		
		if (currentEvent.getSequence() == sequence) {
			boolean changed = false;
			changed = calendarDao.changeParticipationState(token, calendarOwner, extId, recurrenceId, participationState);
				logger.info(LogUtils.prefix(token) + 
						"Calendar : event[extId:{} and recurrenceId:{}] change participation state for user {} " 
						+ "new state : {}", new Object[]{extId, recurrenceId, calendarOwner.getEmail(), participationState});

			return changed;
		} else {
			logger.info(LogUtils.prefix(token) + 
					"Calendar : event[extId:" + extId + "] ignoring new participation state for user " + 
					calendarOwner.getEmail() + " as sequence number is different from current event (got " + sequence + ", expected " + currentEvent.getSequence());
			return false;
		}
	}

	@Override
	@Transactional
	public int importICalendar(final AccessToken token, final String calendar, final String ics) 
		throws ImportICalendarException, ServerFault {

		if (!helperService.canWriteOnCalendar(token, calendar)) {
			String message = "[" + token.getUserLogin() + "] Calendar : "
					+ token.getUserLogin() + " cannot create event on "
					+ calendar + "calendar : no write right";
			logger.info(LogUtils.prefix(token) + message);
			throw new ServerFault(message);
		}
		
		final List<Event> events = parseICSEvent(token, ics);
		int countEvent = 0;
		for (final Event event: events) {

			removeAttendeeWithNoEmail(event);
			if (!isAttendeeExistForCalendarOwner(token, calendar, event.getAttendees())) {
				addAttendeeForCalendarOwner(token, calendar, event);
			}
			
			if(event.isEventInThePast()){
				changeCalendarOwnerAttendeeParticipationStateToAccepted(token, calendar, event);
			}

			if (createEventIfNotExists(token, calendar, event)) {
				countEvent += 1;
			}
		}
		return countEvent;
	}

	private void changeCalendarOwnerAttendeeParticipationStateToAccepted(
			AccessToken at, String calendar, Event event) {
		for (final Attendee attendee: event.getAttendees()) {
			if (isAttendeeExistForCalendarOwner(at, calendar, attendee)) {
				attendee.setState(ParticipationState.ACCEPTED);
			}	
		}
	}

	private void removeAttendeeWithNoEmail(Event event) { 
		final List<Attendee> newAttendees = new ArrayList<Attendee>();
		for (final Attendee attendee: event.getAttendees()) {
			if (attendee.getEmail() != null) {
				newAttendees.add(attendee);
			}
		}
		event.setAttendees(newAttendees);
	}

	private boolean createEventIfNotExists(final AccessToken token, final String calendar, final Event event)
			throws ImportICalendarException {
		try {
			if (!isEventExists(token, calendar, event)) {
				final Event newEvent = calendarDao.createEvent(token, calendar, event, false);
				if (newEvent != null) {
					return true;
				}
			}
		} catch (FindException e) {
			throw new ImportICalendarException(e);
		} catch (SQLException e) {
			throw new ImportICalendarException(e);
		} catch (ServerFault e) {
			throw new ImportICalendarException(e);
		}
		return false;
	}

	private boolean isEventExists(final AccessToken token, final String calendar, final Event event) throws FindException {
		final ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		if (event.getExtId() != null && event.getExtId().getExtId() != null) {
			final Event eventExist = calendarDao.findEventByExtId(token, calendarUser, event.getExtId());
			if (eventExist != null) {
				return true;
			}
		}
		return false;
	}
	
	private List<Event> parseICSEvent(final AccessToken token, final String icsToString) throws ImportICalendarException {
		try {
			return ical4jHelper.parseICSEvent(icsToString, createIcal4jUserFrom(token));
		} catch (IOException e) {
			throw new ImportICalendarException(e);
		} catch (ParserException e) {
			throw new ImportICalendarException(e);
		}
	}
	
	private void addAttendeeForCalendarOwner(final AccessToken token, final String calendar, final Event event) throws ImportICalendarException {
		try {
			final ObmUser obmUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			final Attendee attendee = new Attendee();
			attendee.setEmail(obmUser.getEmail());
			event.getAttendees().add(attendee);
		} catch (FindException e) {
			throw new ImportICalendarException("user " + calendar + " not found");
		}
	}

	private boolean isAttendeeExistForCalendarOwner(final AccessToken at, final String calendar, final List<Attendee> attendees) {
		for (final Attendee attendee: attendees) {
			if (isAttendeeExistForCalendarOwner(at, calendar, attendee)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isAttendeeExistForCalendarOwner(final AccessToken at, final String calendar, final Attendee attendee) {
		final ObmUser obmUser = userService.getUserFromAttendee(attendee,
				at.getDomain().getName());
		if (obmUser != null) {
			if (obmUser.getLogin().equals(calendar)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void purge(final AccessToken token, final String calendar) throws ServerFault {
		if (!helperService.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar " + calendar);
		}
		try {
			final ObmUser obmUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			
			final Calendar endDate = Calendar.getInstance();
			endDate.add(Calendar.MONTH, -6);
		
			final List<Event> events = calendarDao.listEventsByIntervalDate(token, obmUser, new Date(0), endDate.getTime(), type);
			for (final Event event: events) {
				boolean eventHasOtherAttendees = event.getAttendees().size() > 1;
				if (eventHasOtherAttendees) {
					Attendee ownerAsAttendee = calendarOwnerAsAttendee(token, calendar, event);
					ParticipationState participation = ownerAsAttendee.getState();
					if (!participation.equals(ParticipationState.DECLINED)) {
						this.changeParticipationStateInternal(token, calendar, event.getExtId(), ParticipationState.DECLINED, 
								event.getSequence(), false);
					}
				}
				else {
					removeEventById(token, calendar, event.getObmId(), event.getSequence() + 1, false);
				}
			}
			
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}		
	}
	
	private Ical4jUser createIcal4jUserFrom(AccessToken accessToken) {
		ObmUser user = userService.getUserFromAccessToken(accessToken);
		return calendarFactory.createIcal4jUserFromObmUser(user);
	}
	
}
