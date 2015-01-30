/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
import static com.google.common.base.Predicates.instanceOf;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.DateTime;

import org.joda.time.Months;
import org.obm.annotations.transactional.Transactional;
import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.NotAllowedException;
import org.obm.sync.PermissionException;
import org.obm.sync.Right;
import org.obm.sync.addition.CommitedElement;
import org.obm.sync.addition.Kind;
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
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.ResourceInfo;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.services.AttendeeService;
import org.obm.sync.services.ICalendar;
import org.obm.sync.services.ImportICalendarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.addition.CommitedOperationDao;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import fr.aliacom.obm.utils.CalendarRights;
import fr.aliacom.obm.utils.CalendarRightsPair;
import fr.aliacom.obm.utils.HelperService;

public class CalendarBindingImpl implements ICalendar {

	private static final Logger logger = LoggerFactory
			.getLogger(CalendarBindingImpl.class);

	private EventType type;
	
	private final CalendarDao calendarDao;
	private final CategoryDao categoryDao;
	private final CommitedOperationDao commitedOperationDao;
	
	private final DomainService domainService;
	private final UserService userService;
	private final EventChangeHandler eventChangeHandler;
	private final ParticipationService participationService;
	
	private final HelperService helperService;
	private final Ical4jHelper ical4jHelper;

	private final ICalendarFactory calendarFactory;
	private final AttendeeService attendeeService;
	private final AnonymizerService anonymizerService;
	private final ObmSyncConfigurationService configuration;
	@Inject
	protected CalendarBindingImpl(EventChangeHandler eventChangeHandler,
			DomainService domainService,
			UserService userService,
			CalendarDao calendarDao,
			CategoryDao categoryDao,
			CommitedOperationDao commitedOperationDao,
			HelperService helperService, 
			ParticipationService participationService,
			Ical4jHelper ical4jHelper,
			ICalendarFactory calendarFactory,
			AttendeeService attendeeService,
			AnonymizerService anonymizerService,
			ObmSyncConfigurationService configuration) {
		this.eventChangeHandler = eventChangeHandler;
		this.domainService = domainService;
		this.userService = userService;
		this.calendarDao = calendarDao;
		this.categoryDao = categoryDao;
		this.commitedOperationDao = commitedOperationDao;
		this.helperService = helperService;
		this.participationService = participationService;
		this.ical4jHelper = ical4jHelper;
		this.calendarFactory = calendarFactory;
		this.attendeeService = attendeeService;
		this.configuration = configuration;
		this.anonymizerService = anonymizerService;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<CalendarInfo> listCalendars(AccessToken token, Integer limit, Integer offset, String pattern) throws ServerFault {
		try {
			Collection<CalendarInfo> calendarInfos = calendarDao.listCalendars(userService.getUserFromAccessToken(token), limit, offset, pattern);

			logger.info("Returning " + calendarInfos.size() + " calendar infos.");

			return calendarInfos;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);

			throw new ServerFault(e);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<ResourceInfo> listResources(AccessToken token, Integer limit, Integer offset, String pattern) throws ServerFault {
		try {
			Collection<ResourceInfo> resourceInfo = calendarDao.listResources(userService.getUserFromAccessToken(token), limit, offset, pattern);

			logger.info("Returning {} resource info", resourceInfo.size());

			return resourceInfo;
		} catch (Exception e) {
			throw new ServerFault(e);
		}
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<CalendarInfo> getCalendarMetadata(AccessToken token, String[] calendars) throws ServerFault {
		try {
			ObmUser user = userService.getUserFromAccessToken(token);

			// Since multidomain emails won't have the @domain part after them in the database,
			// add the stripped version of the calendar emails (minus the @domain part) to the query
			
			// Create a new list using Arrays.asList(calendars), since asList returns an unmodifiable list
			List<String> calendarEmails = new ArrayList<String>();
			String userEmail = user.getEmailAtDomain();
			for (String calendarEmail : calendars) {
				// We'll add the user manually later
				if (calendarEmail.equals(userEmail)) {
					calendarEmails.add(userEmail);
					continue;
				}
				int atPosition = calendarEmail.indexOf('@');
				if (atPosition > 0) {
					String strippedCalendarEmail = calendarEmail.substring(0, atPosition);
					calendarEmails.add(calendarEmail);
					calendarEmails.add(strippedCalendarEmail);
				}
				else {
					logger.warn("Got an invalid email address: " + calendarEmail);
				}
			}


			Collection<CalendarInfo> calendarInfos;
			if (calendarEmails.size() > 0) {
				calendarInfos = calendarDao.getCalendarMetadata(user, calendarEmails);
            }
			else {
				calendarInfos = new HashSet<CalendarInfo>();
            }

			logger.info("Returning " + calendarInfos.size() + " calendar infos.");

			return calendarInfos;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}


	@Override
	@Transactional(readOnly=true)
	public Collection<ResourceInfo> getResourceMetadata(AccessToken token, String[] resourceEmails) throws ServerFault {
		if (resourceEmails == null || resourceEmails.length == 0) {
			return ImmutableList.of();
		}

		ObmUser user = userService.getUserFromAccessToken(token);

		try {
			return calendarDao.getResourceMetadata(user, Arrays.asList(resourceEmails));
		} catch (FindException e) {
			throw new ServerFault(e);
		}
	}

	@Override
	@Transactional
	public void removeEventById(AccessToken token, String calendar, EventObmId eventId, int sequence, boolean notification)
			throws ServerFault, EventNotFoundException, NotAllowedException {
		assertUserCanWriteOnCalendar(token, calendar);
		
		try {
			Event ev = calendarDao.findEventById(token, eventId);

			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			ObmUser owner = userService.getUserFromLogin(ev.getOwner(), token.getDomain().getName());
			if (owner != null) {
				if (ev.isInternalEvent() && owner.getEmailAtDomain().equals(calendarUser.getEmailAtDomain())) {
					cancelInternalEventById(token, calendar, notification, ev);
				} else {
					changeParticipation(token, calendar, ev.getExtId(), Participation.declined(), sequence, notification);
				}
			} else {
				throw new NotAllowedException("It's not possible to remove an event without owner " + ev.getTitle());
			}
		} catch (ServerFault e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e);
		} catch (FindException e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e);
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e);
		}
	}

	private Event cancelInternalEventById(AccessToken token, String calendar, boolean notification, Event toRemoveEvent)
			throws SQLException, EventNotFoundException, ServerFault {
		
		return cancelInternalEvent(token, calendar, notification, toRemoveEvent, new RemoveEventFunction() {
			
			@Override
			public Event remove(AccessToken token, Event event) throws SQLException, EventNotFoundException, ServerFault {
				return calendarDao.removeEventById(token, event.getObmId(), event.getType(), event.getSequence() + 1);
			}
		});
	}

	private Event cancelInternalEventByExtId(AccessToken token, final ObmUser obmUser, Event event, boolean notification)
			throws SQLException, EventNotFoundException, ServerFault {
		
		return cancelInternalEvent(token, obmUser.getEmailAtDomain(), notification, event, new RemoveEventFunction() {

			@Override
			public Event remove(AccessToken token, Event event) throws SQLException, EventNotFoundException, ServerFault {
				return calendarDao.removeEventByExtId(token, obmUser, event.getExtId(), event.getSequence() + 1);
			}
		});
	}

	private Event cancelInternalEvent(AccessToken token, String calendar, boolean notification, Event toRemoveEvent,
			RemoveEventFunction removeEventFunction)
			throws SQLException, EventNotFoundException, ServerFault {
		
		Event removedEvent = removeEventFunction.remove(token, toRemoveEvent);
		logger.info(String.format("Calendar : event[uid:%s, extId:%s] removed",
				toRemoveEvent.getUid().serializeToString(), toRemoveEvent.getExtId().getExtId()));
		changeCalendarOwnerParticipation(calendar, removedEvent, Participation.declined());
		notifyOnRemoveInternalEvent(token, removedEvent, notification);
		return removedEvent;
	}
	
	public interface RemoveEventFunction {
		Event remove(AccessToken token, Event event) throws SQLException, EventNotFoundException, ServerFault;
	}

	private void notifyOnRemoveInternalEvent(AccessToken token, Event event, boolean notification) {
		eventChangeHandler.delete(event, notification, token);
	}

	private void changeCalendarOwnerParticipation(String ownerEmail, Event event, Participation participation) {
		Attendee calendarOwnerAsAttendee = event.findAttendeeFromEmail(ownerEmail);
		if (calendarOwnerAsAttendee != null) {
			calendarOwnerAsAttendee.setParticipation(participation);
		}
	}

	@Override
	@Transactional
	public Event removeEventByExtId(AccessToken token, String calendar,
			EventExtId extId, int sequence, boolean notification) throws ServerFault, NotAllowedException {
		assertUserCanWriteOnCalendar(token, calendar);
		
		try {
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			Event ev = calendarDao.findEventByExtId(token, calendarUser, extId);
			if (ev == null) {
				logger.info("Calendar : event[" + extId + "] not removed, it doesn't exist");
				return ev;
			}
			
			ObmUser owner = userService.getUserFromLogin(ev.getOwner(), token.getDomain().getName());
			if (owner == null) {
				logger.info("error, trying to remove an event without any owner : " + ev.getTitle());
				return ev;
			}
			
			if (ev.isInternalEvent() && owner.getEmailAtDomain().equals(calendarUser.getEmailAtDomain())) {
				return cancelInternalEventByExtId(token, calendarUser, ev, notification);
			} else {
				changeParticipation(token, calendar, ev.getExtId(), Participation.declined(), sequence, notification);
				return calendarDao.findEventByExtId(token, calendarUser, extId);
			}
		} catch (Throwable e) {
			Throwables.propagateIfInstanceOf(e, NotAllowedException.class);
			
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Event modifyEvent(AccessToken token, String calendar, Event event, boolean updateAttendees, boolean notification) 
		throws ServerFault, NotAllowedException, PermissionException {

		if (event == null) {
			logger.warn("Modify on NULL event: doing nothing");
			return null;
		}
		
		try {
			
			final ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			final Event before = loadCurrentEvent(token, calendarUser, event);

			if (before == null) {
				logger.warn("Event[uid:"+ event.getObmId() + "extId:" + event.getExtId() +
						"] doesn't exist in database: : doing nothing");
				return null;
			}
			
			assertEventCanBeModified(token, calendarUser, before);
			convertAttendees(event, calendarUser);
			
			Event toReturn = modifyEvent(token, calendar, event, updateAttendees, notification, before);

			return inheritAlertFromOwnerIfNotSet(token.getObmId(), calendarUser.getUid(), toReturn);
		} catch (Throwable e) {
			Throwables.propagateIfInstanceOf(e, NotAllowedException.class);
			Throwables.propagateIfInstanceOf(e, PermissionException.class);
			
			logger.error(e.getMessage(), e);
			throw new ServerFault(e);
		}

	}

	private Event modifyEvent(AccessToken token, String calendar, Event event, boolean updateAttendees, boolean notification, Event before) throws ServerFault, PermissionException {
		if (before.isInternalEvent()) {
			return modifyInternalEvent(token, calendar, before, event, updateAttendees, notification);
		} else {
			return modifyExternalEvent(token, calendar, event, updateAttendees, notification);
		}
	}
	
	private void assertUserCanWriteOnCalendar(AccessToken token, String calendar) throws NotAllowedException {
		if (!helperService.canWriteOnCalendar(token, calendar)) {
			throwNotAllowedException(token, calendar, Right.WRITE);
		}
	}
	
	private void assertUserCanReadCalendar(AccessToken token, String calendar) throws NotAllowedException {
		if (!helperService.canReadCalendar(token, calendar)) {
			throwNotAllowedException(token, calendar, Right.READ);
		}
	}
	
	private void throwNotAllowedException(AccessToken token, String calendar, Right right) throws NotAllowedException {
		throw new NotAllowedException("User " + token.getUserLogin() + " has no " + right + " rights on calendar " + calendar + ".");
	}

	private void throwNotAllowedException(AccessToken token, String calendar, Right right, Event event) throws NotAllowedException {
		String message = String.format("User with token %s has no %s right on calendar %s for event %s with attendees %s",
				token, right, calendar, event, event.getAttendees());
				
		throw new NotAllowedException(message);
	}

	@VisibleForTesting void assertEventCanBeModified(AccessToken token, ObmUser calendarUser, Event event) throws NotAllowedException {
		String calendar = calendarUser.getEmailAtDomain();
		
		assertUserCanWriteOnCalendar(token, calendar);
		
		if (!helperService.eventBelongsToCalendar(event, calendar) && !eventBelongsToUser(event, token)) {
			throwNotAllowedException(token, calendar, Right.WRITE, event);
		}
	}
	
	private boolean eventBelongsToUser(Event event, AccessToken token) {
		return token.getUserEmail().equalsIgnoreCase(event.getOwnerEmail());
	}

	@VisibleForTesting
	protected Event loadCurrentEvent(AccessToken token, ObmUser calendarUser, Event event) throws EventNotFoundException, ServerFault {
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
			boolean notification) throws ServerFault, PermissionException {
		
		try {
			assignDelegationRightsOnAttendees(token, event);
			initDefaultParticipation(event);
			inheritsParticipationFromExistingEventForObmUsers(before, event);
			applyParticipationModifications(before, event);
			
			Event after = calendarDao.modifyEventForcingSequence(
					token, calendar, event, updateAttendees, event.getSequence(), true);
			logger.info("Calendar : internal event[" + after.getTitle() + "] modified");

			assignDelegationRightsOnAttendees(token, after);
            notifyOnUpdateEvent(token, notification, before, after);
            
			return after;
		} catch (Throwable e) {
			Throwables.propagateIfInstanceOf(e, PermissionException.class);
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@VisibleForTesting
	void initDefaultParticipation(Event event) {
		initDefaultParticipationOnEvent(event);
		for (Event eventException : event.getEventsExceptions()) {
			initDefaultParticipationOnEvent(eventException);
		}
	}
	
	private void initDefaultParticipationOnEvent(Event event) {
		List<Attendee> attendees = event.getAttendees();
		
		if (attendees != null) {
			for (Attendee attendee : attendees) {
				if ( attendee.isObmUser() ) {
					attendee.setParticipation(Participation.needsAction());
				}
			}
		}
	}
	
	private void ensureNewAttendeesWithDelegationAreAccepted(Event before, Event event) {
		ensureNewAttendeesWithDelegationAreAcceptedOnEvent(before, event);
		
		Set<Event> eventExceptions = event.getEventsExceptions();
		SortedMap<Event, Event> beforeExceptions = buildSortedMap(before.getEventsExceptions());
		
		for (Event eventException : eventExceptions) {
			ensureNewAttendeesWithDelegationAreAcceptedOnEvent(beforeExceptions.get(eventException), eventException);
		}
	}
	
	private void ensureNewAttendeesWithDelegationAreAcceptedOnEvent(Event before, Event event) {
		List<Attendee> newAttendees = new ArrayList<Attendee>(event.getAttendees());
		
		if (before != null) {
			newAttendees.removeAll(before.getAttendees());
		}
		
		for (Attendee attendee : newAttendees) {
			if (attendee.isCanWriteOnCalendar()) {
				attendee.setParticipation(Participation.accepted());
			}
		}
	}

	@VisibleForTesting void inheritsParticipationFromExistingEventForObmUsers(Event before, Event event) {
		if (before.getAttendees() != null && event.getAttendees() != null) {
			for (Attendee beforeAttendee : before.getAttendees()) {
				if( beforeAttendee.isObmUser() ) {
					inheritsParticipationForSpecificAttendee(event, beforeAttendee);
				}
			}
		}
		
		inheritsParticipationOnExceptions(before, event);
	}

	@VisibleForTesting void inheritsParticipationForSpecificAttendee(Event event, Attendee beforeAttendee) {
		int indexOf = event.getAttendees().indexOf(beforeAttendee);
		if (indexOf != -1) {
			Attendee attendee = event.getAttendees().get(indexOf);
			attendee.setParticipation(beforeAttendee.getParticipation());
		}
	}

	@VisibleForTesting void inheritsParticipationOnExceptions(Event before, Event event) {
		Set<Event> beforeExceptions = before.getEventsExceptions();
		SortedMap<Event, Event> eventExceptions = buildSortedMap(event.getEventsExceptions());
		
		for (Event beforeException : beforeExceptions) {
			if (eventExceptions.containsKey(beforeException)) {
				inheritsParticipationFromExistingEventForObmUsers(beforeException, eventExceptions.get(beforeException));
			}
		}
	}

	@VisibleForTesting SortedMap<Event, Event> buildSortedMap(Set<Event> events) {
		if (events == null) {
			return new TreeMap<Event, Event>();
		}
		SortedMap<Event, Event> treeMap = Maps.<Event, Event, Event>newTreeMap(new RecurrenceIdComparator());
		for (Event event : events) {
			treeMap.put(event, event);
		}
		return treeMap;
	}
	
	private static class RecurrenceIdComparator implements Serializable, Comparator<Event> {
		public int compare(Event first, Event second) {
			return Ordering.natural().nullsFirst().compare(first.getRecurrenceId(), second.getRecurrenceId());
		}
	}

	private void notifyOnUpdateEvent(AccessToken token, boolean notification, Event before, Event after) {
		eventChangeHandler.update(before, after, notification, token);
	}

	@VisibleForTesting protected void applyParticipationModifications(Event before, Event event) {
		if (event.hasImportantChangesExceptedEventException(before)) {
		    event.updateParticipation();
		} else {
			List<Event> exceptionWithChanges = event.getEventExceptionsWithImportantChanges(before);
			for (Event exception: exceptionWithChanges) {
				exception.updateParticipation();
			}
		}
		
		ensureNewAttendeesWithDelegationAreAccepted(before, event);
	}

	private Event modifyExternalEvent(AccessToken token, String calendar, 
			Event event, boolean updateAttendees, boolean notification) throws ServerFault {
		try {
			Attendee attendee = calendarOwnerAsAttendee(token, calendar, event);
			if (isEventDeclinedForCalendarOwner(attendee)) {
				ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
				calendarDao.removeEventByExtId(token, calendarOwner, event.getExtId(), event.getSequence());
				notifyOrganizerForExternalEvent(token, calendar, event, notification);
				logger.info("Calendar : External event[" + event.getTitle() + 
						"] removed, calendar owner won't attende to it");
				return event;
			} else {
				Event after = calendarDao.modifyEvent(token,  calendar, event, updateAttendees, false);
				logger.info("Calendar : External event[" + after.getTitle() + "] modified");
				notifyOrganizerForExternalEvent(token, calendar, after, notification);
				return after;
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public EventObmId createEvent(AccessToken token, String calendar, Event event, boolean notification, String clientId)
			throws ServerFault, EventAlreadyExistException, NotAllowedException, PermissionException {

		assertEventNotNull(token, event);
		assertEventIsNew(token, calendar, event);

		return createEventNoExistenceCheck(token, calendar, event, notification, clientId).getObmId();
	}

	private Event createEventNoExistenceCheck(AccessToken token, String calendar, Event event, boolean notification, String clientId) throws ServerFault, NotAllowedException, PermissionException {
		assertUserCanWriteOnCalendar(token, calendar);

		try {
			convertAttendees(event, calendar, token.getDomain());
			assignDelegationRightsOnAttendees(token, event);
			
			Event ev = commitedOperationDao.findAsEvent(token, clientId);
			if (ev == null) {
				if (event.isInternalEvent()) {
					ev = createInternalEvent(token, calendar, event, notification);
				} else {
					ev = createExternalEvent(token, calendar, event, notification);
				}
			}

			commitOperation(token, ev, clientId);
			return ev;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@VisibleForTesting void commitOperation(AccessToken token, Event event, String clientId) throws SQLException, ServerFault {
		if (!Strings.isNullOrEmpty(clientId)) {
			commitedOperationDao.store(token, 
					CommitedElement.builder()
						.clientId(clientId)
						.entityId(event.getEntityId())
						.kind(Kind.VEVENT)
						.build());
		}
	}

	private void assertEventIsNew(AccessToken token, String calendar, Event event) throws ServerFault, EventAlreadyExistException {
		if (event.getObmId() != null) {
			logger.error("event creation with an event coming from OBM");
			throw new ServerFault(
					"event creation with an event coming from OBM");
		}
		try {
			if (isEventExists(token, calendar, event)) {
				final String message = String
				.format("Calendar : duplicate with same extId found for event [%s, %s, %d, %s]",
						event.getTitle(), event.getStartDate()
						.toString(), event.getDuration(),
						event.getExtId());
				logger.info(message);
				throw new EventAlreadyExistException(message);
			}
		} catch (FindException e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private void assertEventNotNull(AccessToken token, Event event) throws ServerFault {
		if (event == null) {
			logger.warn("creating NULL event, returning fake id 0");
			throw new ServerFault(
					"event creation without any data");
		}
	}

	private Event createExternalEvent(AccessToken token, String calendar, Event event, boolean notification) 
			throws ServerFault {
		
		try {
			Attendee attendee = calendarOwnerAsAttendee(token, calendar, event);
			if (attendee == null) {
				String message = "Calendar : external event["+ event.getTitle() + "] doesn't involve calendar owner, ignoring creation";
				logger.info(message);
				throw new ServerFault(message);
			}
			
			if (isEventDeclinedForCalendarOwner(attendee)) {
				logger.info("Calendar : external event["+ event.getTitle() + "] refused, mark event as deleted");
				Event ev = createEventAsDeleted(token, calendar, event);
				notifyOrganizerForExternalEvent(token, calendar, ev, notification);
				return ev;
			} else {
				changeOrganizerParticipationToAccepted(event);
				Event ev = calendarDao.createEvent(token, calendar, event, false);
				logger.info("Calendar : external event["+ ev.getTitle() + "] created");
				notifyOrganizerForExternalEvent(token, calendar, ev, notification);
				return ev;
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	private Event createEventAsDeleted(AccessToken token, String calendar, Event event) 
			throws SQLException, FindException, ServerFault {
		
		Event ev = calendarDao.createEvent(token, calendar, event, false);
		return calendarDao.removeEvent(token, ev, event.getType(), event.getSequence());
	}

	private void changeOrganizerParticipationToAccepted(Event event) {
		for(Attendee att : event.getAttendees()){
			if(att.isOrganizer()){
				att.setParticipation(Participation.accepted());
			}
		}
	}

	private Attendee calendarOwnerAsAttendee(AccessToken token, String calendar, Event event) 
		throws FindException {
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		Attendee userAsAttendee = event.findAttendeeFromEmail(calendarOwner.getEmailAtDomain());
		return userAsAttendee;
	}

	private boolean isEventDeclinedForCalendarOwner(Attendee userAsAttendee) {
		return userAsAttendee != null && Participation.declined().equals(userAsAttendee.getParticipation());
	}

	private void notifyOrganizerForExternalEvent(AccessToken token,
			String calendar, Event ev, Participation state, boolean notification) throws FindException {
		logger.info(
				"Calendar : sending participation notification to organizer of event ["+ ev.getTitle() + "]");
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		eventChangeHandler.updateParticipation(ev, calendarOwner, state, notification, token);
	}

	
	private void notifyOrganizerForExternalEvent(AccessToken token, String calendar, Event ev,
			boolean notification) throws FindException {
		
		Attendee calendarOwnerAsAttendee = calendarOwnerAsAttendee(token, calendar, ev);
		notifyOrganizerForExternalEvent(token, calendar, ev, calendarOwnerAsAttendee.getParticipation(), notification);
	}

	@VisibleForTesting protected Event createInternalEvent(AccessToken token, String calendar, Event event, boolean notification) 
			throws ServerFault {
		try{
			event.updateParticipation();
			Event ev = calendarDao.createEvent(token, calendar, event, true);
			ev = calendarDao.findEventById(token, ev.getObmId());
			eventChangeHandler.create(ev, notification, token);
			logger.info("Calendar : internal event["
				+ ev.getTitle() + "] created");
			return ev;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@VisibleForTesting
	protected void assignDelegationRightsOnAttendees(AccessToken token, Event event) throws PermissionException {
		applyDelegationRightsOnAttendeesToEvent(token, event);

		for (Event eventException : event.getEventsExceptions()) {
			applyDelegationRightsOnAttendeesToEvent(token, eventException);
		}
	}
	
	private void applyDelegationRightsOnAttendeesToEvent(AccessToken token, Event event) throws PermissionException {
		Iterable<Attendee> userAttendees = findUserAttendees(event.getAttendees());
		Iterable<Attendee> attendeesWithEmails = filterAttendeesWithEmail(userAttendees);
		Map<String, Attendee> emailToAttendee = emailToAttendee(attendeesWithEmails);
		CalendarRights emailToRights = helperService.listRightsOnCalendars(token, emailToAttendee.keySet());

		for (CalendarRightsPair pair : emailToRights) {
			String email = pair.getCalendar();
			Set<Right> rights = pair.getRights();

			if (!rights.contains(Right.ACCESS) && !rights.contains(Right.WRITE)) {
				throw new PermissionException(email, Right.ACCESS, event.getTitle());
			}

			if (rights.contains(Right.WRITE)) {
				emailToAttendee.get(email).setCanWriteOnCalendar(true);
			}
		}
	}

	private static Iterable<Attendee> filterAttendeesWithEmail(Iterable<Attendee> attendees) {
		return Iterables.filter(attendees, new Predicate<Attendee>() {

			@Override
			public boolean apply(Attendee attendee) {
				return !Strings.isNullOrEmpty(attendee.getEmail());
			}

		});
	}

	private static Iterable<Attendee> findUserAttendees(Iterable<Attendee> attendees) {
		return Iterables.filter(attendees, instanceOf(UserAttendee.class));
	}

	private static Map<String, Attendee> emailToAttendee(Iterable<Attendee> attendees) {
		ImmutableMap.Builder<String, Attendee> builder = ImmutableMap.builder();

		for (Attendee attendee : attendees) {
			builder.put(attendee.getEmail(), attendee);
		}

		return builder.build();
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<Event> getResourceEvents(String resourceEmail, Date date, SyncRange syncRange) throws ServerFault {
		try {
			ResourceInfo resourceInfo = calendarDao.getResource(resourceEmail);
			if (resourceInfo == null) {
				throw new ResourceNotFoundException(String.format("No such resource %s", resourceEmail));
			}
			final Date newDate = date;

			return calendarDao.getResourceEvents(
					resourceInfo,
					Optional.fromNullable(syncRange).or(
							new Supplier<SyncRange>() {
							@Override
							public SyncRange get() {
								return defaultSyncRange(newDate);
							}
					}));
		} catch (FindException ex) {
			throw new ServerFault(ex);
		}
	}
	
	private SyncRange defaultSyncRange(Date date) {
		return new SyncRange(
				new org.joda.time.DateTime(date).plus(Months.SIX).toDate(),
				new org.joda.time.DateTime(date).minus(Months.THREE).toDate());
	}
	
	@Override
	@Transactional(readOnly=true)
	public EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws ServerFault, NotAllowedException {
		return getSync(token, calendar, lastSync, null, false);
	}
	
	@Override
	@Transactional(readOnly=true)
	public EventChanges getFirstSync(AccessToken token, String calendar,
			Date lastSync) throws ServerFault, NotAllowedException {
		
		EventChanges allEventChanges = getSync(token, calendar, lastSync);
		return EventChanges.builder()
				.lastSync(allEventChanges.getLastSync())
				.participationChanges(allEventChanges.getParticipationUpdated())
				.updates(allEventChanges.getUpdated())
				.build();
	}
	
	@Override
	@Transactional(readOnly=true)
	public EventChanges getSyncInRange(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange) throws ServerFault, NotAllowedException {
		return getSync(token, calendar, lastSync, syncRange, false);
	}

	@Override
	@Transactional(readOnly=true)
	public EventChanges getSyncWithSortedChanges(AccessToken token,
			String calendar, Date lastSync, SyncRange syncRange) throws ServerFault, NotAllowedException {

		EventChanges changes = getSync(token, calendar, lastSync, syncRange, false);
		return sortUpdatedEvents(changes, lastSync);
	}
	
	@VisibleForTesting EventChanges sortUpdatedEvents(EventChanges changes, Date lastSync) {
		List<Event> updated = new ArrayList<Event>();
		List<Event> participationChanged = new ArrayList<Event>();
		
		for (Event event: changes.getUpdated()) {
			// As Lightning does not know how to differentiate fake Exdate 
			// from true Exdate from OBMFULL-3116, we put every recurrent event
			// into updated tag for a getSync response.
			if (event.modifiedSince(lastSync) || event.isRecurrent()) {
				updated.add(event);
			} else {
				participationChanged.add(event);
			}
			
		}
		
		return EventChanges.builder()
					.lastSync(changes.getLastSync())
					.deletes(changes.getDeletedEvents())
					.participationChanges(eventsToParticipationUpdateArray(participationChanged))
					.updates(updated)
					.build();
	}

	private List<ParticipationChanges> eventsToParticipationUpdateArray(List<Event> participationChanged) {
		return Lists.transform(participationChanged, new Function<Event, ParticipationChanges>() {
			@Override
			public ParticipationChanges apply(Event event) {
				if (event.getRecurrenceId() == null) {
					return ParticipationChanges.builder()
							.eventObmId(event.getObmId().getObmId())
							.eventExtId(event.getExtId().getExtId())
							.attendees(event.getAttendees())
							.build(); 
				}
				return ParticipationChanges.builder()
							.eventObmId(event.getObmId().getObmId())
							.eventExtId(event.getExtId().getExtId())
							.attendees(event.getAttendees())
							.recurrenceId(getRecurrenceIdToIcalFormat(event.getRecurrenceId()))
							.build();
			}
		});
	}
	
	private String getRecurrenceIdToIcalFormat(Date recurrenceId) {
		DateTime recurrenceIdToIcalFormat = new DateTime(recurrenceId);
		recurrenceIdToIcalFormat.setUtc(true);
		return recurrenceIdToIcalFormat.toString();
	}
	
	@Override
	@Transactional(readOnly=true)
	public EventChanges getSyncEventDate(AccessToken token, String calendar,
			Date lastSync) throws ServerFault, NotAllowedException {
		return getSync(token, calendar, lastSync, null, true);
	}
	
	@Override
	@Transactional(readOnly=true)
	public EventChanges getFirstSyncEventDate(AccessToken token, String calendar,
			Date lastSync) throws ServerFault, NotAllowedException {
		
		EventChanges allEventChanges = getSyncEventDate(token, calendar, lastSync);
		return EventChanges.builder()
				.lastSync(allEventChanges.getLastSync())
				.participationChanges(allEventChanges.getParticipationUpdated())
				.updates(allEventChanges.getUpdated())
				.build();
	}

	@VisibleForTesting
	EventChanges getSync(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange, boolean onEventDate) throws ServerFault, NotAllowedException {

		logger.info("Calendar : getSync(" + calendar
				+ ", " + lastSync + ")");
		
		assertUserCanReadCalendar(token, calendar);

		try {
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			EventChanges changesFromDatabase = calendarDao.getSync(token, calendarUser,
					lastSync, syncRange, type, onEventDate);
			logger.info("Calendar : getSync("
					+ calendar + ") => " + changesFromDatabase.getUpdated().size() + " upd, "
					+ changesFromDatabase.getDeletedEvents().size() + " rmed.");

			EventChanges changesToSend = moveConfidentalEventsOnDelegation(token, calendarUser, changesFromDatabase)
					.subtractLastSyncBy(configuration.getTransactionToleranceTimeoutInSeconds());

			changesToSend = anonymizerService.anonymize(changesToSend, calendar, token);

			return inheritAlertsFromOwnerIfNotSet(token.getObmId(), calendarUser.getUid(), changesToSend);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private EventChanges moveConfidentalEventsOnDelegation(AccessToken token,
			ObmUser calendarUser, EventChanges changesFromDatabase) {
		String userEmailOfToken = token.getUserEmail();
		boolean isNotCalendarOfLoggedUser = !userEmailOfToken.equals(calendarUser.getEmailAtDomain());
		EventChanges changesToSend = isNotCalendarOfLoggedUser ?
			changesFromDatabase.moveConfidentialEventsToRemovedEvents(userEmailOfToken) :
			changesFromDatabase;
		return changesToSend;
	}

	private EventChanges inheritAlertsFromOwnerIfNotSet(Integer userId, Integer ownerId, EventChanges eventChanges) {
		return EventChanges
				.builder()
				.lastSync(eventChanges.getLastSync())
				.deletes(eventChanges.getDeletedEvents())
				.updates(inheritAlertsFromOwnerIfNotSet(userId, ownerId, eventChanges.getUpdated()))
				.participationChanges(eventChanges.getParticipationUpdated())
				.build();
	}

	private <T extends Iterable<Event>> T inheritAlertsFromOwnerIfNotSet(Integer userId, Integer ownerId, T events) {
		for (Event event : events) {
			inheritAlertFromOwnerIfNotSet(userId, ownerId, event);
		}

		return events;
	}

	@VisibleForTesting Event inheritAlertFromOwnerIfNotSet(Integer userId, Integer ownerId, Event event) {
		if (event.getAlert() == null && !Objects.equal(userId, ownerId)) {
			event.setAlert(calendarDao.getEventAlertForUser(event.getObmId(), ownerId));
		}

		return event;
	}

	@Override
	@Transactional(readOnly=true)
	public Event getEventFromId(AccessToken token, String calendar, EventObmId eventId) throws ServerFault, EventNotFoundException, NotAllowedException {
		Event event = calendarDao.findEventById(token, eventId);
		String owner = event.getOwner();
		if (owner == null) {
			throw new ServerFault("Owner not found for event " + eventId);
		}
		
		if (!helperService.canReadCalendar(token, owner) && !helperService.attendeesContainsUser(event.getAttendees(), token)) {
			throwNotAllowedException(token, owner, Right.READ);
		}

		boolean delegation = helperService.canWriteOnCalendar(token, calendar);

		if (delegation) {
			ObmUser calendarUser = userService.getUserFromLogin(calendar, token.getDomain().getName());

			if (calendarUser != null) {
				return inheritAlertFromOwnerIfNotSet(token.getObmId(), calendarUser.getUid(), event);
			}
		}
		
		return event;
	}

	@Override
	@Transactional(readOnly=true)
	public KeyList getEventTwinKeys(AccessToken token, String calendar,
			Event event) throws ServerFault, NotAllowedException {
		assertUserCanReadCalendar(token, calendar);
		
		try {
			ObmDomain domain = domainService
					.findDomainByName(token.getDomain().getName());
			List<String> keys = calendarDao.findEventTwinKeys(calendar,
					event, domain);
			logger.info("found " + keys.size()
					+ " twinkeys ");
			return new KeyList(keys);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public KeyList getRefusedKeys(AccessToken token, String calendar, Date since)
			throws ServerFault, NotAllowedException {
		assertUserCanReadCalendar(token, calendar);
		
		try {
			ObmUser user = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			List<String> keys = calendarDao.findRefusedEventsKeys(user, since);
			return new KeyList(keys);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<Category> listCategories(AccessToken token) throws ServerFault {
		try {
			List<Category> c = categoryDao.getCategories(token);
			return c;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public String getUserEmail(AccessToken token) throws ServerFault {
		try {
			ObmUser obmuser = userService.getUserFromAccessToken(token);
			if (obmuser != null) {
				return obmuser.getEmailAtDomain();
			}
			return "";

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public EventObmId getEventObmIdFromExtId(AccessToken token, String calendar,
			EventExtId extId) throws ServerFault, EventNotFoundException, NotAllowedException {
		Event event = getEventFromExtId(token, calendar, extId);
		return event.getObmId();
	}

	@Override
	@Transactional(readOnly=true)
	public Event getEventFromExtId(AccessToken token, String calendar, EventExtId extId) 
			throws ServerFault, EventNotFoundException, NotAllowedException {
		
		assertUserCanReadCalendar(token, calendar);
		
		try {
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			Event event = calendarDao.findEventByExtId(token, calendarUser, extId);
			boolean delegation = helperService.canWriteOnCalendar(token, calendar);

			if (event == null) {
				throw new EventNotFoundException("Event from extId " + extId + " not found.");
			}

			return delegation ? inheritAlertFromOwnerIfNotSet(token.getObmId(), calendarUser.getUid(), event) : event;
		} catch (FindException e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws ServerFault, NotAllowedException {
		assertUserCanReadCalendar(token, calendar);
		
		ObmUser calendarUser = null;

		try {
			calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		} catch (FindException e1) {
			throw new ServerFault(e1.getMessage());
		}
		try {
			List<Event> events = calendarDao.listEventsByIntervalDate(token, calendarUser, start, end, type);
			boolean delegation = helperService.canWriteOnCalendar(token, calendar);

			return delegation ? inheritAlertsFromOwnerIfNotSet(token.getObmId(), calendarUser.getUid(), events) : events;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<Event> getAllEvents(AccessToken token, String calendar,
			EventType eventType) throws ServerFault {
		try {
			assertUserCanReadCalendar(token, calendar);
			
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			List<Event> events = calendarDao.findAllEvents(token, calendarUser, eventType);
			boolean delegation = helperService.canWriteOnCalendar(token, calendar);
			
			return delegation ? inheritAlertsFromOwnerIfNotSet(token.getObmId(), calendarUser.getUid(), events) : events;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public String parseEvent(AccessToken token, Event event) throws ServerFault {
		return ical4jHelper.parseEvent(event, createIcal4jUserFrom(token), token);
	}

	@Override
	@Transactional(readOnly=true)
	public String parseEvents(AccessToken token, List<Event> events) throws ServerFault {
		return ical4jHelper.parseEvents(createIcal4jUserFrom(token), events, token);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Event> parseICS(AccessToken token, String ics)
			throws Exception, ServerFault {
		String fixedIcs = fixIcsAttendees(ics);
		String calendar = getDefaultCalendarFromToken(token);
		List<Event> events = ical4jHelper.parseICS(fixedIcs, createIcal4jUserFrom(token), token.getObmId());
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
	@Transactional(readOnly=true)
	public FreeBusyRequest parseICSFreeBusy(AccessToken token, String ics)
			throws ServerFault {
		try {
			return ical4jHelper.parseICSFreeBusy(ics, token.getDomain(), token.getObmId());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}

	}

	private String getDefaultCalendarFromToken(AccessToken token) {
		return token.getUserLogin();
	}

	@Override
	@Transactional(readOnly=true)
	public Date getLastUpdate(AccessToken token, String calendar)
			throws ServerFault, NotAllowedException {
		try {
			assertUserCanReadCalendar(token, calendar);
			
			return calendarDao.findLastUpdate(token, calendar);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public boolean isWritableCalendar(AccessToken token, String calendar)
			throws ServerFault {
		try {
			return helperService.canWriteOnCalendar(token, calendar);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fb)
			throws ServerFault {
		try {
			return calendarDao.getFreeBusy(token.getDomain(), fb);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public String parseFreeBusyToICS(AccessToken token, FreeBusy fbr)
			throws ServerFault {
		try {
			return ical4jHelper.parseFreeBusy(fbr);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	public void setEventType(EventType type) {
		this.type = type;
	}

	@Override
	@Transactional
	public boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, Participation participation, int sequence, boolean notification) throws ServerFault, NotAllowedException {
		assertUserCanWriteOnCalendar(token, calendar);
		
		String userEmail = userService.getUserFromAccessToken(token).getEmailAtDomain();
		
		try {
			boolean wasDone = changeParticipation(token, calendar, extId, participation, sequence,
					notification);
			if (!wasDone) {
				logger.warn("Change of participation state failed to " + participation + " (got sequence number " +
					sequence + ", probably stale) on calendar " + calendar + " on event "+ extId + " by user " + userEmail);
			}
			return wasDone;
		} catch (FindException e) {
			throw new ServerFault("no user found with calendar " + calendar);
		} catch (SQLException e) {
			throw new ServerFault(e);
		} catch (ParseException e) {
			throw new ServerFault(e);
		}
	}

	@VisibleForTesting boolean changeParticipation(AccessToken token, String calendar, EventExtId extId, Participation participation,
			int sequence, boolean notification) throws FindException, SQLException, ParseException {

		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		Event currentEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
		boolean changed = false;
		if (currentEvent != null) {
			changed = participationService.changeOnEvent(token, extId, participation, sequence, calendarOwner, currentEvent);
		}

		Event newEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
		if (newEvent != null) {
			if (changed) {
				eventChangeHandler.updateParticipation(newEvent, calendarOwner, participation, notification, token);
			}
		} else {
			logger.error("event with extId : " + extId + " is no longer in database, ignoring notification");
		}
		return changed;
	}

	@Override
	@Transactional
	public boolean changeParticipationState(AccessToken token, String calendar,
			EventExtId extId, RecurrenceId recurrenceId, Participation participation, int sequence, boolean notification)
					throws ServerFault, EventNotFoundException, ParseException, NotAllowedException {
		assertUserCanWriteOnCalendar(token, calendar);
		
		String userEmail = userService.getUserFromAccessToken(token).getEmailAtDomain();
		
		try {
			boolean wasDone = changeParticipationForRecursiveEvent(token, calendar, extId, recurrenceId, participation, sequence,
					notification);
			if (!wasDone) {
				logger.warn("Change of participation state failed to " + participation + " (got sequence number " +
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
	
	private boolean changeParticipationForRecursiveEvent(AccessToken token,
			String calendar, EventExtId extId, RecurrenceId recurrenceId,
			Participation participation, int sequence, boolean notification)
			throws FindException, SQLException, EventNotFoundException, ServerFault, ParseException {
		
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		Event currentEvent = null;
		if (recurrenceId == null) {
			currentEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
		} else {
			currentEvent = calendarDao.findEventByExtIdAndRecurrenceId(token, calendarOwner, extId, recurrenceId);
			if (currentEvent == null) {
				Event parentEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
				if(parentEvent != null) {
					Event eventException = createOccurrence(parentEvent, recurrenceId);
					parentEvent.getRecurrence().addEventException(eventException);
					calendarDao.modifyEventForcingSequence(token, parentEvent.getOwner(), parentEvent, true, parentEvent.getSequence(), true);
					currentEvent = eventException;
				}
			}
		}

		boolean changed = false;
		if (currentEvent != null) {
			if (recurrenceId != null) {
				changed = participationService.changeOnOccurrence(token, extId, recurrenceId,
						participation, sequence, calendarOwner, currentEvent);
			} else {
				changed = participationService.changeOnEvent(token, extId, participation, sequence,
						calendarOwner, currentEvent);
			}
		}
		
		if (changed) {
			Event newEvent = null;
			if (recurrenceId != null) {
				newEvent = calendarDao.findEventByExtIdAndRecurrenceId(token, calendarOwner, extId, recurrenceId);
			} else {
				newEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
			}
			
			if (newEvent != null) {
				eventChangeHandler.updateParticipation(newEvent, calendarOwner, participation, notification, token);
			} else {
				if (recurrenceId != null) {
					logger.error("event with extId : {} and recurrenceId : {} is no longer in database, ignoring notification", extId, recurrenceId);
				} else {
					logger.error("event with extId : {} is no longer in database, ignoring notification", extId);
				}
			}
		}
		return changed;
	}

	private Event createOccurrence(Event parent, RecurrenceId recurrenceId) throws ParseException {
		Event eventException = parent.clone();
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.none);
		eventException.setRecurrence(recurrence);
		if (recurrenceId != null) {
			eventException.setStartDate(new DateTime(recurrenceId.getRecurrenceId()));
			eventException.setRecurrenceId(new DateTime(recurrenceId.getRecurrenceId()));
		}
		return eventException;
	}

	@VisibleForTesting boolean applyParticipationChange(AccessToken token, EventExtId extId,
			Participation participation, int sequence,
			ObmUser calendarOwner, Event currentEvent) throws SQLException {
		
		if (currentEvent.getSequence() == sequence) {
			boolean changed = false;
			Attendee attendee = currentEvent.findAttendeeFromEmail(calendarOwner.getEmailAtDomain());
			if (attendee.getParticipation().equals(participation)) {
				logger.info(
					"Calendar : event[extId:{}] change participation state for user {} with same state {} ignored", 
					new Object[]{extId, calendarOwner.getEmail(), participation});
			} else {
				participation.resetComment();
				changed = calendarDao.changeParticipation(token, calendarOwner, extId, participation);
				logger.info(
						"Calendar : event[extId:{}] change participation state for user {} " 
						+ "new state : {}", new Object[]{extId, calendarOwner.getEmailAtDomain(), participation});
			}

			return changed;
		} else {
			logger.info(
					"Calendar : event[extId:" + extId + "] ignoring new participation state for user " + 
					calendarOwner.getEmailAtDomain() + " as sequence number is different from current event (got " + sequence + ", expected " + currentEvent.getSequence());
			return false;
		}
	}

	@VisibleForTesting boolean applyParticipationChange(AccessToken token, EventExtId extId,
			RecurrenceId recurrenceId, Participation participation, int sequence,
			ObmUser calendarOwner, Event currentEvent) throws SQLException, ParseException {
		
		if (currentEvent.getSequence() == sequence) {
			boolean changed = false;
			participation.resetComment();
			changed = calendarDao.changeParticipation(token, calendarOwner, extId, recurrenceId, participation);
			logger.info(
					"Calendar : event[extId:{} and recurrenceId:{}] change participation state for user {} " 
					+ "new state : {}", new Object[]{extId, recurrenceId, calendarOwner.getEmailAtDomain(), participation});

			return changed;
		} else {
			logger.info(
					"Calendar : event[extId:" + extId + "] ignoring new participation state for user " + 
					calendarOwner.getEmailAtDomain() + " as sequence number is different from current event (got " + sequence + ", expected " + currentEvent.getSequence());
			return false;
		}
	}

	@Override
	@Transactional
	public int importICalendar(final AccessToken token, final String calendar, final String ics, String clientId) 
			throws ImportICalendarException, ServerFault, NotAllowedException {

		assertUserCanWriteOnCalendar(token, calendar);
		
		ObmUser calendarUser = null;
		
		try {
			calendarUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
		}
		catch (Exception e) {
			throw new ServerFault(e);
		}

		int countEvent = 0;
		List<Event> events = parseICSEvent(token, ics, calendarUser.getUid());
		LoadingCache<Attendee, Optional<ObmUser>> cache = newObmUserCache(token.getDomain().getName());

		for (final Event event: events) {

			removeAttendeeWithNoEmail(event);
			if (!isAttendeeExistForCalendarOwner(calendarUser, event.getAttendees(), cache)) {
				addAttendeeForCalendarOwner(token, calendar, event);
			}
			
			if(event.isEventInThePast()){
				changeCalendarOwnerAttendeeParticipationToAccepted(calendarUser, event, cache);
			}

			if (createEventIfNotExists(token, calendarUser, event)) {
				countEvent += 1;
			}
		}
		return countEvent;
	}

	private LoadingCache<Attendee, Optional<ObmUser>> newObmUserCache(final String domainName) {
		return CacheBuilder.newBuilder().build(new CacheLoader<Attendee, Optional<ObmUser>>() {
			@Override
			public Optional<ObmUser> load(Attendee key) throws Exception {
				return Optional.fromNullable(userService.getUserFromAttendee(key, domainName));
			}
		});
	}

	private void changeCalendarOwnerAttendeeParticipationToAccepted(ObmUser calendarUser, Event event, LoadingCache<Attendee, Optional<ObmUser>> cache) {
		for (Attendee attendee: event.getAttendees()) {
			if (isAttendeeExistForCalendarOwner(calendarUser, attendee, cache)) {
				attendee.setParticipation(Participation.accepted());
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

	private boolean createEventIfNotExists(final AccessToken token, ObmUser calendarUser, final Event event)
			throws ImportICalendarException {
		try {
			if (!isEventExists(token, calendarUser, event)) {
				final Event newEvent = calendarDao.createEvent(token, calendarUser.getLogin(), event, true);
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

		return isEventExists(token, calendarUser, event);
	}

	private boolean isEventExists(AccessToken token, ObmUser calendarUser, Event event) {
		if (event.getExtId() != null && event.getExtId().getExtId() != null) {
			return calendarDao.findEventByExtId(token, calendarUser, event.getExtId()) != null;
		}
	
		return false;
	}

	private List<Event> parseICSEvent(final AccessToken token, final String icsToString, Integer ownerId) throws ImportICalendarException {
		try {
			return ical4jHelper.parseICS(icsToString, createIcal4jUserFrom(token), ownerId);
		} catch (IOException e) {
			throw new ImportICalendarException(e);
		} catch (ParserException e) {
			throw new ImportICalendarException(e);
		}
	}
	
	private void addAttendeeForCalendarOwner(final AccessToken token, final String calendar, final Event event) {
		Attendee attendee = attendeeService.findUserAttendee(null, calendar, token.getDomain());
			
		if (Iterables.isEmpty(event.getAttendees())) {
			attendee = UserAttendee
					.builder()
					.asOrganizer()
					.canWriteOnCalendar(attendee.isCanWriteOnCalendar())
					.displayName(attendee.getDisplayName())
					.email(attendee.getEmail())
					.entityId(attendee.getEntityId())
					.participation(Participation.accepted())
					.participationRole(attendee.getParticipationRole())
					.percent(attendee.getPercent())
					.build();

			// Because we're handling the case where noone attends the event yet
			// So we're adding the calendar owner as the organizer and as such, he's the owner of the event
			event.setOwnerEmail(attendee.getEmail());
		}

		event.getAttendees().add(attendee);
	}

	private boolean isAttendeeExistForCalendarOwner(ObmUser calendarUser, List<Attendee> attendees, LoadingCache<Attendee, Optional<ObmUser>> cache) {
		for (Attendee attendee: attendees) {
			if (isAttendeeExistForCalendarOwner(calendarUser, attendee, cache)) {
				return true;
			}
		}

		return false;
	}
	
	private boolean isAttendeeExistForCalendarOwner(ObmUser calendarUser, Attendee attendee, LoadingCache<Attendee, Optional<ObmUser>> cache) {
		ObmUser obmUser = cache.getUnchecked(attendee).orNull();

		if (obmUser != null) {
			if (obmUser.getLogin().equals(calendarUser.getLogin())) {
				return true;
			}
		}

		return false;
	}

	@Override
	@Transactional
	public void purge(final AccessToken token, final String calendar) throws ServerFault, NotAllowedException {
		assertUserCanReadCalendar(token, calendar);
		
		try {
			final ObmUser obmUser = userService.getUserFromCalendar(calendar, token.getDomain().getName());
			
			final Calendar endDate = Calendar.getInstance();
			endDate.add(Calendar.MONTH, -6);
		
			final List<Event> events = calendarDao.listEventsByIntervalDate(token, obmUser, new Date(0), endDate.getTime(), type);
			for (final Event event: events) {
				boolean eventHasOtherAttendees = event.getAttendees().size() > 1;
				if (eventHasOtherAttendees) {
					Attendee ownerAsAttendee = calendarOwnerAsAttendee(token, calendar, event);
					Participation participation = ownerAsAttendee.getParticipation();
					if (!participation.equals(Participation.declined())) {
						this.changeParticipation(token, calendar, event.getExtId(), Participation.declined(),
								event.getSequence(), false);
					}
				}
				else {
					removeEventById(token, calendar, event.getObmId(), event.getSequence() + 1, false);
				}
			}
			
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}		
	}
	
	private Ical4jUser createIcal4jUserFrom(AccessToken accessToken) {
		ObmUser user = userService.getUserFromAccessToken(accessToken);
		return calendarFactory.createIcal4jUserFromObmUser(user);
	}
	
	@VisibleForTesting void convertAttendeesOnEvent(Event event, ObmUser owner) throws ServerFault {
		List<Attendee> typedAttendees = Lists.newArrayList();

		Attendee ownerAttendee = findRequiredOwnerAttendee(event, owner);
		
		for (Attendee attendee : event.getAttendees()) {
			Attendee typedAttendee = findAttendee(event, owner, ownerAttendee, attendee);

			typedAttendee.setCanWriteOnCalendar(attendee.isCanWriteOnCalendar());
			typedAttendee.setOrganizer(attendee.isOrganizer());
			typedAttendee.setParticipation(attendee.getParticipation());
			typedAttendee.setParticipationRole(attendee.getParticipationRole());
			typedAttendee.setPercent(attendee.getPercent());

			typedAttendees.add(typedAttendee);
		}
		
		event.setAttendees(typedAttendees);
	}

	private Attendee findRequiredOwnerAttendee(Event event, ObmUser owner) throws ServerFault {
		for (String emails : owner.expandAllEmailDomainTuples()) {
			Attendee attendeeFromEmailAlias = event.findAttendeeFromEmail(emails);
			if (attendeeFromEmailAlias != null) {
				return attendeeFromEmailAlias;
			}
		}
		throw new ServerFault("Cannot find owner attendee");
	}

	private Attendee findAttendee(Event event, ObmUser owner, Attendee ownerAttendee, Attendee attendee) {
		if (attendee.equals(ownerAttendee)) {
			return findUserAttendee(attendee, owner);
		} else if (event.isInternalEvent()) {
			return findTypedAttendee(attendee, owner);
		} else {
			return findContactAttendee(attendee, owner);
		}
	}
	
	private void convertAttendees(Event event, ObmUser owner) throws ServerFault {
		convertAttendeesOnEvent(event, owner);
		
		for (Event exception : event.getEventsExceptions()) {
			convertAttendeesOnEvent(exception, owner);
		}
	}
	
	private Attendee findTypedAttendee(Attendee attendee, ObmUser owner) {
		return attendeeService.findAttendee(attendee.getDisplayName(), attendee.getEmail(), true, owner.getDomain(), owner.getUid());
	}

	private Attendee findUserAttendee(Attendee attendee, ObmUser owner) {
		return attendeeService.findUserAttendee(attendee.getDisplayName(), attendee.getEmail(), owner.getDomain());
	}

	private Attendee findContactAttendee(Attendee attendee, ObmUser owner) {
		return attendeeService.findContactAttendee(attendee.getDisplayName(), attendee.getEmail(), true, owner.getDomain(), owner.getUid());
	}

	private void convertAttendees(Event event, String calendar, ObmDomain domain) throws ServerFault {
		try {
			convertAttendees(event, userService.getUserFromCalendar(calendar, domain.getName()));
		}
		catch (Exception e) {
			throw new ServerFault(e);
		}
	}

	@Override
	@Transactional
	public Event storeEvent(AccessToken token, String calendar, Event event, boolean notification, String clientId) throws ServerFault, NotAllowedException, PermissionException {
		assertEventNotNull(token, event);

		ObmUser calendarUser = userService.getUserFromLogin(calendar, token.getDomain().getName());

		if (calendarUser == null) {
			throw new ServerFault("Invalid calendar '" + calendar + "'.");
		}

		if (isEventExists(token, calendarUser, event)) {
			return modifyEvent(token, calendar, event, true, notification);
		}

		return createEventNoExistenceCheck(token, calendar, event, notification, clientId);
	}

}
