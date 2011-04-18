/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.calendar;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.Category;
import org.obm.sync.base.KeyList;
import org.obm.sync.calendar.Attendee;
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
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.server.transactional.Transactional;
import org.obm.sync.services.ICalendar;
import org.obm.sync.services.ImportICalendarException;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.Helper;
import fr.aliacom.obm.utils.Ical4jHelper;
import fr.aliacom.obm.utils.LogUtils;

public class CalendarBindingImpl implements ICalendar {

	private static final Log logger = LogFactory.getLog(CalendarBindingImpl.class);

	private EventType type;
	
	private final CalendarDao calendarDao;
	private final CategoryDao categoryDao;
	
	private final DomainService domainService;
	private final UserService userService;
	private final EventChangeHandler eventChangeHandler;
	
	private final Helper helper;

	@Inject
	protected CalendarBindingImpl(EventChangeHandler eventChangeHandler,
			DomainService domainService, UserService userService,
			CalendarDao calendarDao,
			CategoryDao categoryDao, Helper helper) {
		this.eventChangeHandler = eventChangeHandler;
		this.domainService = domainService;
		this.userService = userService;
		this.calendarDao = calendarDao;
		this.categoryDao = categoryDao;
		this.helper = helper;
	}

	@Override
	@Transactional
	public CalendarInfo[] listCalendars(AccessToken token) throws ServerFault,
			AuthFault {
		try {
			List<CalendarInfo> l = getRights(token);
			CalendarInfo[] ret = l.toArray(new CalendarInfo[0]);
			logger.info(LogUtils.prefix(token) + "Returning " + ret.length
					+ " calendar infos.");
			return ret;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private List<CalendarInfo> getRights(AccessToken t) throws FindException {
		List<CalendarInfo> rights = t.getCalendarRights();
		if (rights == null) {
			rights = listCalendarsImpl(t);
			t.setCalendarRights(rights);
		}
		return rights;
	}

	private List<CalendarInfo> listCalendarsImpl(AccessToken token)
			throws FindException {
		ObmUser u = userService.getUserFromAccessToken(token);
		return calendarDao.listCalendars(u);
	}

	@Override
	@Transactional
	public Event removeEvent(AccessToken token, String calendar, String eventId, int sequence, boolean notification)
			throws AuthFault, ServerFault {
		try {
			int uid = Integer.valueOf(eventId);
			Event ev = calendarDao.findEvent(token, uid);

			if (ev == null) {
				logger.info(LogUtils.prefix(token) + "event with id : "
						+ eventId + "not found");
				return null;
			}

			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain());
			ObmUser owner = userService.getUserFromLogin(ev.getOwner(), token.getDomain());
			if (owner != null) {
				if (helper.canWriteOnCalendar(token, calendar)) {
					if (owner.getEmailAtDomain().equals(calendarUser.getEmailAtDomain())) {
						return cancelEvent(token, calendar, notification, uid, ev);
					} else {
						changeParticipationStateInternal(token, calendar, ev.getExtId(), ParticipationState.DECLINED, sequence, notification);
						return calendarDao.findEvent(token, uid);
					}
				}
				
				logger.info(LogUtils.prefix(token) + "remove not allowed of " + ev.getTitle());
				return ev;
			} else {
				logger.info(LogUtils.prefix(token)
						+ "try to remove an event without owner "
						+ ev.getTitle());
			}

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}
		return null;
	}

	private Event cancelEvent(AccessToken token, String calendar,
			boolean notification, int uid, Event ev) throws SQLException,
			FindException {
		Event removed = calendarDao.removeEvent(token, uid, ev.getType(), ev.getSequence() + 1);
		logger.info(LogUtils.prefix(token) + "Calendar : event[" + uid + "] removed");
		if (notification) {
			notifyOnRemoveEvent(token, calendar, removed);
		}
		return removed;
	}

	private Event cancelEventByExtId(AccessToken token, ObmUser calendar, Event event, boolean notification) throws SQLException, FindException {
		String extId = event.getExtId();
		Event removed = calendarDao.removeEventByExtId(token, calendar, extId, event.getSequence() + 1);
		logger.info(LogUtils.prefix(token) + "Calendar : event[" + extId + "] removed");
		
		if (notification) {
			notifyOnRemoveEvent(token, calendar.getEmailAtDomain(), removed);
		}
		return removed;
	}

	
	private void notifyOnRemoveEvent(AccessToken token, String calendar, Event ev) throws FindException {
		if (ev.isInternalEvent()) {
			ObmUser user = userService.getUserFromAccessToken(token);
			eventChangeHandler.delete(user, ev);
		} else {
			notifyOrganizerForExternalEvent(token, calendar, ev, ParticipationState.DECLINED);
		}
	}

	@Override
	@Transactional
	public Event removeEventByExtId(AccessToken token, String calendar,
			String extId, int sequence, boolean notification) throws AuthFault, ServerFault {
		try {
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain());
			final Event ev = calendarDao.findEventByExtId(token, calendarUser, extId);

			if (ev == null) {
				logger.info(LogUtils.prefix(token) + "Calendar : event[" + extId + "] not removed, it doesn't exist");
				return ev;
			} else {

				if (!helper.canWriteOnCalendar(token, calendar)) {
					logger.info(LogUtils.prefix(token) + "remove not allowed of " + ev.getTitle());
					return ev;
				}

				ObmUser owner = userService.getUserFromLogin(ev.getOwner(), token.getDomain());
				if (owner == null) {
					logger.info(LogUtils.prefix(token) + "error, trying to remove an event without any owner : " + ev.getTitle());
					return ev;
				}
				
				if (owner.getEmailAtDomain().equals(calendarUser.getEmailAtDomain())) {
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
		throws AuthFault, ServerFault {

		if (event == null) {
			logger.warn(LogUtils.prefix(token) + "Modify on NULL event: doing nothing");
			return null;
		}
		
		try {
			
			final ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain());
			final Event before = loadCurrentEvent(token, calendarUser, event);

			if (before == null) {
				logger.warn(LogUtils.prefix(token) + "Event[uid:"+ event.getUid() + "extId:" + event.getExtId() +
						"] doesn't exist in database: : doing nothing");
				return null;
			}
			
			if (before.getOwner() != null && !helper.canWriteOnCalendar(token, before.getOwner())) {
				logger.info(LogUtils.prefix(token) + "Calendar : "
						+ token.getUser() + " cannot modify event["
						+ before.getTitle() + "] because not owner"
						+ " or no write right on owner " + before.getOwner()+". ParticipationState will be updated.");
				return event;
				
			} else {
				
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
	
	private Event loadCurrentEvent(AccessToken token, ObmUser calendarUser, Event event) {
		if (Strings.isNullOrEmpty(event.getUid())) {
			final Event currentEvent = calendarDao.findEventByExtId(token, calendarUser, event.getExtId());
			event.setUid(currentEvent.getUid());
			return currentEvent;
		} else {
			int uid = Integer.valueOf(event.getUid());
			return calendarDao.findEvent(token, uid);
		}
	}

	private Event modifyInternalEvent(AccessToken token, String calendar, Event before,  Event event,
			boolean updateAttendees, boolean notification) throws ServerFault {
		try{
			prepareParticipationStateForNewEventAttendees(token, before, event);
			
	     final Event after = calendarDao.modifyEventForcingSequence(
	    		 token, calendar, event, updateAttendees, before.getSequence() + 1, true);
	     
			if (after != null) {
				logger.info(LogUtils.prefix(token) + "Calendar : internal event[" + after.getTitle() + "] modified");
			}
			
			if (notification) {
				ObmUser user = userService.getUserFromAccessToken(token);
				eventChangeHandler.update(user, before, after);
			}
			
			return after;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Event modifyExternalEvent(AccessToken token, String calendar, 
			Event event, boolean updateAttendees, boolean notification) throws ServerFault {
		try {
			if (isEventDeclinedForCalendarOwner(token, calendar, event)) {
				ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain());
				calendarDao.removeEventByExtId(token, calendarOwner, event.getExtId(), event.getSequence());
				notifyOrganizerForExternalEvent(token, calendar, event);
				logger.info(LogUtils.prefix(token) + "Calendar : External event[" + event.getTitle() + 
						"] removed, calendar owner won't attende to it");
				return event;
			} else {
				Event after = calendarDao.modifyEvent(token,  calendar, event, updateAttendees, false);
				if (after != null) {
					logger.info(LogUtils.prefix(token) + "Calendar : External event[" + after.getTitle() + "] modified");
				}
				if (notification) {
					notifyOrganizerForExternalEvent(token, calendar, after);
				}
				return after;
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public String createEvent(AccessToken token, String calendar, Event event, boolean notification)
			throws AuthFault, ServerFault {

		try {
			if (event == null) {
				logger.warn(LogUtils.prefix(token)
						+ "creating NULL event, returning fake id 0");
				return "0";
			}
			if (!Strings.isNullOrEmpty(event.getUid())) {
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

			if (!helper.canWriteOnCalendar(token, calendar)) {
				String message = "[" + token.getUser() + "] Calendar : "
						+ token.getUser() + " cannot create event on "
						+ calendar + "calendar : no write right";
				logger.info(LogUtils.prefix(token) + message);
				throw new ServerFault(message);
			}
			Event ev = null;
			if (event.isInternalEvent()) {
				ev = createInternalEvent(token, calendar, event, notification);
			} else {
				ev = createExternalEvent(token, calendar, event, notification);
			}
			return (String.valueOf(ev.getDatabaseId()));
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Event createExternalEvent(AccessToken token, String calendar, Event event, boolean notification) throws ServerFault {
		try {
			if (isEventDeclinedForCalendarOwner(token, calendar, event)) {
				logger.info(LogUtils.prefix(token) + "Calendar : external event["+ event.getTitle() + "] refused, mark event as deleted");
				calendarDao.removeEvent(token, event, event.getType(), event.getSequence());
				if (notification) {
					notifyOrganizerForExternalEvent(token, calendar, event);
				}
				return event;
			} else {
				Event ev = calendarDao.createEvent(token, calendar, event, false);
				logger.info(LogUtils.prefix(token) + "Calendar : external event["+ ev.getTitle() + "] created");
				if (notification) {
					notifyOrganizerForExternalEvent(token, calendar, ev);
				}
				return ev;
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private boolean isEventDeclinedForCalendarOwner(AccessToken token,
			String calendar, Event event) throws FindException {
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain());
		Attendee userAsAttendee = event.findAttendeeForUser(calendarOwner.getEmailAtDomain());
		return userAsAttendee != null && userAsAttendee.getState() == ParticipationState.DECLINED;
	}

	private void notifyOrganizerForExternalEvent(AccessToken token,
			String calendar, Event ev, ParticipationState state) throws FindException {
		logger.info(LogUtils.prefix(token) + 
				"Calendar : sending participation notification to organizer of event ["+ ev.getTitle() + "]");
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain());
		eventChangeHandler.updateParticipationState(ev, calendarOwner, state);
	}

	
	private void notifyOrganizerForExternalEvent(AccessToken token, String calendar, Event ev) throws FindException {
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain());
		Attendee calendarOwnerAsAttendee = ev.findAttendeeForUser(calendarOwner.getEmailAtDomain());
		notifyOrganizerForExternalEvent(token, calendar, ev, calendarOwnerAsAttendee.getState());
	}

	private Event createInternalEvent(AccessToken token, String calendar, Event event, boolean notification) throws ServerFault {
		try{
			changePartipationStateOnWritableCalendar(token, event);
			Event ev = calendarDao.createEvent(token, calendar, event, true);
			ev = calendarDao.findEvent(token, ev.getDatabaseId());
			if (notification) {
				ObmUser user = userService.getUserFromAccessToken(token);
			    eventChangeHandler.create(user, ev);
			}
			logger.info(LogUtils.prefix(token) + "Calendar : internal event["
				+ ev.getTitle() + "] created");
			return ev;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	private void changePartipationStateOnWritableCalendar(AccessToken token, Event event){
		for(Attendee att : event.getAttendees()){
			if(ParticipationState.NEEDSACTION.equals(att.getState()) && !StringUtils.isEmpty(att.getEmail())) {
				try {
					acceptePartipationStateIfCanWriteOnCalendar(token, att);
				} catch (Exception e) {
					logger.error("Error while checks right on calendar: "+att.getEmail(), e);
				}
			}
		}
	}
	
	private void prepareParticipationStateForNewEventAttendees(final AccessToken token,  final Event before, final Event event) {
		for (final Attendee currentAtt : event.getAttendees()) {
			for (final Attendee beforeAtt: before.getAttendees()) {
				if (currentAtt.equals(beforeAtt)) {
					prepareParticipationStateForAttendee(token, currentAtt);
					break;
				}	
			}
		}
	}
	
	private void prepareParticipationStateForAttendee(final AccessToken token, final Attendee att) {
		try {
			if (helper.canWriteOnCalendar(token,  att.getEmail())) {
				att.setState(ParticipationState.ACCEPTED);
			} else {
				att.setState(ParticipationState.NEEDSACTION);
			}
		} catch (Exception e) {
			logger.error("Error while checks right on calendar: "+ att.getEmail(), e);
		}
	}
	
	private void acceptePartipationStateIfCanWriteOnCalendar(AccessToken token, Attendee att){
		try {
			if (helper.canWriteOnCalendar(token,  att.getEmail())) {
				att.setState(ParticipationState.ACCEPTED);
			}
		} catch (Exception e) {
			logger.error("Error while checks right on calendar: "+att.getEmail(), e);
		}
	}
	
	@Override
	@Transactional
	public EventChanges getSync(AccessToken token, String calendar,
			Date lastSync) throws AuthFault, ServerFault {
		return getSync(token, calendar, lastSync, null, false);
	}
	
	@Override
	@Transactional
	public EventChanges getSyncInRange(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange) throws AuthFault, ServerFault {
		return getSync(token, calendar, lastSync, syncRange, false);
	}

	@Override
	@Transactional
	public EventChanges getSyncWithSortedChanges(AccessToken token,
			String calendar, Date lastSync) throws AuthFault, ServerFault {

		EventChanges changes = getSync(token, calendar, lastSync, null, false);
		
		//sort between update and participation update based on event timestamp
		sortUpdatedEvents(changes, lastSync);
		
		return changes;
	}
	
	private void sortUpdatedEvents(EventChanges changes, Date lastSync) {
		List<Event> updated = new ArrayList<Event>();
		List<Event> participationChanged = new ArrayList<Event>();
		
		for (Event event: changes.getUpdated()) {
			if (event.modifiedSince(lastSync)) {
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
				participationChanges.setEventId(event.getDatabaseId());
				return participationChanges;
			}
		}).toArray(new ParticipationChanges[0]);
	}

	
	@Override
	@Transactional
	public EventChanges getSyncEventDate(AccessToken token, String calendar,
			Date lastSync) throws AuthFault, ServerFault {
		return getSync(token, calendar, lastSync, null, true);
	}

	private EventChanges getSync(AccessToken token, String calendar,
			Date lastSync, SyncRange syncRange, boolean onEventDate) throws ServerFault {

		logger.info(LogUtils.prefix(token) + "Calendar : getSync(" + calendar
				+ ", " + lastSync + ")");

		ObmUser calendarUser = null;
		try {
			calendarUser = userService.getUserFromCalendar(calendar, token.getDomain());
		} catch (FindException e) {
			throw new ServerFault(e.getMessage());
		}

		if (!helper.canReadCalendar(token, calendar)) {
			logger.error(LogUtils.prefix(token) + "user " + token.getUser()
					+ " tried to sync calendar " + calendar
					+ " => permission denied");
			throw new ServerFault("Read permission denied for "
					+ token.getUser() + " on " + calendar);
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
	public Event getEventFromId(AccessToken token, String calendar,
			String eventId) throws AuthFault, ServerFault {
		try {
			int uid = Integer.valueOf(eventId);
			Event evt = calendarDao.findEvent(token, uid);
			if(evt == null){
				return null;
			}
			String owner = evt.getOwner();
			if (owner == null) {
				logger.info(LogUtils.prefix(token)
						+ "try to get an event without owner " + evt.getTitle());
				return null;
			}
			if (helper.canReadCalendar(token, owner)
					|| helper.attendeesContainsUser(evt.getAttendees(), token)) {
				return evt;
			}
			logger.info(LogUtils.prefix(token) + "read not allowed for "
					+ evt.getTitle());
			return null;

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public KeyList getEventTwinKeys(AccessToken token, String calendar,
			Event event) throws AuthFault, ServerFault {
		if (!helper.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		try {
			ObmDomain domain = domainService
					.findDomainByName(token.getDomain());
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
			throws AuthFault, ServerFault {
		if (!helper.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		try {
			ObmUser user = userService.getUserFromCalendar(calendar, token.getDomain());
			List<String> keys = calendarDao.findRefusedEventsKeys(user, since);
			return new KeyList(keys);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<Category> listCategories(AccessToken token) throws ServerFault,
			AuthFault {
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
	public String getUserEmail(AccessToken token) throws AuthFault, ServerFault {
		try {
			ObmUser obmuser = userService.getUserFromAccessToken(token);
			if (obmuser != null) {
				return helper.constructEmailFromList(obmuser.getEmail(),
						token.getDomain());
			}
			return "";

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Integer getEventObmIdFromExtId(AccessToken token, String calendar,
			String extId) throws ServerFault {

		Event event = getEventFromExtId(token, calendar, extId);
		if (event != null) {
			return event.getDatabaseId();
		}
		return null;
	}

	@Override
	@Transactional
	public Event getEventFromExtId(AccessToken token, String calendar,
			String extId) throws ServerFault {
		if (!helper.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		try {
			ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain());
			return calendarDao.findEventByExtId(token, calendarUser, extId);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws AuthFault,
			ServerFault {
		if (!helper.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar "
					+ calendar);
		}
		ObmUser calendarUser = null;
		try {
			calendarUser = userService.getUserFromCalendar(calendar, token.getDomain());
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
			EventType eventType) throws AuthFault, ServerFault {
		try {
			if (helper.canReadCalendar(token, calendar)) {
				ObmUser calendarUser = userService.getUserFromCalendar(calendar,
						token.getDomain());
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
	public String parseEvent(AccessToken token, Event event)
			throws ServerFault, AuthFault {
		ObmUser user = userService.getUserFromAccessToken(token);
		return Ical4jHelper.parseEvent(event, user);
	}

	@Override
	@Transactional
	public String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault, AuthFault {
		ObmUser user = userService.getUserFromAccessToken(token);
		return Ical4jHelper.parseEvents(user, events);
	}

	@Override
	@Transactional
	public List<Event> parseICS(AccessToken token, String ics)
			throws Exception, ServerFault {
		String fixedIcs = fixIcsAttendees(ics);
		ObmUser user = userService.getUserFromAccessToken(token);
		return Ical4jHelper.parseICSEvent(fixedIcs, user);
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
			throws ServerFault, AuthFault {
		try {
			return Ical4jHelper.parseICSFreeBusy(ics);
		} catch (Exception e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}

	}

	@Override
	@Transactional
	public List<EventParticipationState> getEventParticipationStateWithAlertFromIntervalDate(
			AccessToken token, String specificCalendar, Date start, Date end)
			throws ServerFault, AuthFault {

		try {
			String calendar = getCalendarOrDefault(token, specificCalendar);
			if (helper.canReadCalendar(token, calendar)) {
				ObmUser calendarUser = userService.getUserFromCalendar(calendar,
						token.getDomain());
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

	private String getCalendarOrDefault(AccessToken token, String calendar) {
		if (StringUtils.isEmpty(calendar)) {
			return token.getUser();
		}
		return calendar;
	}

	@Override
	@Transactional
	public List<EventTimeUpdate> getEventTimeUpdateNotRefusedFromIntervalDate(
			AccessToken token, String calendar, Date start, Date end)
			throws ServerFault, AuthFault {
		try {
			if (helper.canReadCalendar(token, calendar)) {
				ObmUser calendarUser = userService.getUserFromCalendar(calendar,
						token.getDomain());
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
			throws ServerFault, AuthFault {
		try {
			if (helper.canReadCalendar(token, calendar)) {
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
			throws AuthFault, ServerFault {
		try {
			return helper.canWriteOnCalendar(token, calendar);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
	
	@Override
	@Transactional
	public List<FreeBusy> getFreeBusy(AccessToken token, FreeBusyRequest fb)
			throws AuthFault, ServerFault {
		try {
			ObmDomain domain = domainService.findDomainByName(token.getDomain());
			return calendarDao.getFreeBusy(domain, fb);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public String parseFreeBusyToICS(AccessToken token, FreeBusy fbr)
			throws ServerFault, AuthFault {
		try {
			return Ical4jHelper.parseFreeBusy(fbr);
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
			String extId, ParticipationState participationState, int sequence, boolean notification) throws ServerFault {
		if (helper.canWriteOnCalendar(token, calendar)) {
			try {
				return changeParticipationStateInternal(token, calendar, extId, participationState, sequence, notification);
			} catch (Exception e) {
				throw new ServerFault("no user found with calendar " + calendar);
			}
		}
		throw new ServerFault("user has no write rights on calendar " + calendar);
	}

	private boolean changeParticipationStateInternal(AccessToken token,
			String calendar, String extId,
			ParticipationState participationState, int sequence, boolean notification)
			throws FindException, SQLException {
		
		ObmUser calendarOwner = userService.getUserFromCalendar(calendar, token.getDomain());
		Event currentEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
		boolean changed = false;
		if (currentEvent != null) {
			 changed = applyParticipationChange(token, extId, participationState, 
					sequence, calendarOwner, currentEvent);
		}
		
		Event newEvent = calendarDao.findEventByExtId(token, calendarOwner, extId);
		if (newEvent != null) {
			if (notification) {
				eventChangeHandler.updateParticipationState(newEvent, calendarOwner, participationState);
			}
		} else {
			logger.error("event with extId : "+ extId + " is no longer in database, ignoring notification");
		}
		return changed;
	}

	private boolean applyParticipationChange(AccessToken token, String extId,
			ParticipationState participationState, int sequence,
			ObmUser calendarOwner, Event currentEvent) throws SQLException {
		
		if (currentEvent.getSequence() == sequence) {
			boolean changed = calendarDao.changeParticipationState(token, calendarOwner, extId, participationState);
			logger.info(LogUtils.prefix(token) + 
					"Calendar : event[extId:" + extId + "] change participation state for user " + 
					calendarOwner.getEmailAtDomain() + " new state : " + participationState);
			return changed;
		} else {
			logger.info(LogUtils.prefix(token) + 
					"Calendar : event[extId:" + extId + "] ignoring new participation state for user " + 
					calendarOwner.getEmailAtDomain() + " as sequence number is older than current event");
			return false;
		}
	}

	@Override
	@Transactional
	public int importICalendar(final AccessToken token, final String calendar, final String ics) 
		throws ImportICalendarException, AuthFault, ServerFault {

		if (!helper.canWriteOnCalendar(token, calendar)) {
			String message = "[" + token.getUser() + "] Calendar : "
					+ token.getUser() + " cannot create event on "
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

			if (createEventIfNotExists(token, calendar, event)) {
				countEvent += 1;
			}
		}
		return countEvent;
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
		}
		return false;
	}

	private boolean isEventExists(final AccessToken token, final String calendar, final Event event) throws FindException {
		final ObmUser calendarUser = userService.getUserFromCalendar(calendar, token.getDomain());
		if (StringUtils.isNotEmpty(event.getExtId())) {
			final Event eventExist = calendarDao.findEventByExtId(token, calendarUser, event.getExtId());
			if (eventExist != null) {
				return true;
			}
		}
		return false;
	}
	
	private List<Event> parseICSEvent(final AccessToken token, final String icsToString) throws ImportICalendarException {
		try {
			ObmUser user = userService.getUserFromAccessToken(token);
			return Ical4jHelper.parseICSEvent(icsToString, user);
		} catch (IOException e) {
			throw new ImportICalendarException(e);
		} catch (ParserException e) {
			throw new ImportICalendarException(e);
		}
	}
	
	private void addAttendeeForCalendarOwner(final AccessToken token, final String calendar, final Event event) throws ImportICalendarException {
		try {
			final ObmUser obmUser = userService.getUserFromCalendar(calendar, token.getDomain());
			final Attendee attendee = new Attendee();
			attendee.setEmail(obmUser.getEmailAtDomain());
			event.getAttendees().add(attendee);
		} catch (FindException e) {
			throw new ImportICalendarException("user " + calendar + " not found");
		}
	}

	private boolean isAttendeeExistForCalendarOwner(final AccessToken at, final String calendar, final List<Attendee> attendees) {
		for (final Attendee attendee: attendees) {
			final ObmUser obmUser = userService.getUserFromAttendee(attendee, at.getDomain());
			if (obmUser != null) {
				if (obmUser.getLogin().equals(calendar)) {
					return true;
				}	
			}
		}
		return false;
	}

	@Override
	public void purge(final AccessToken token, final String calendar) throws ServerFault {
		if (!helper.canReadCalendar(token, calendar)) {
			throw new ServerFault("user has no read rights on calendar " + calendar);
		}
		try {
			final ObmUser obmUser = userService.getUserFromCalendar(calendar, token.getDomain());
			
			final Calendar endDate = Calendar.getInstance();
			endDate.add(Calendar.MONTH, -6);
		
			final List<Event> events = calendarDao.listEventsByIntervalDate(token, obmUser, new Date(0), endDate.getTime(), type);
			for (final Event event: events) {
				removeEvent(token, calendar, event.getUid(), event.getSequence() + 1, false);
			}
			
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}		
	}
	
}
