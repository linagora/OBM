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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.fortuna.ical4j.data.ParserException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
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
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.server.transactional.Transactional;
import org.obm.sync.services.ICalendar;
import org.obm.sync.services.ImportICalendarException;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.setting.SettingDao;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;
import fr.aliacom.obm.utils.Helper;
import fr.aliacom.obm.utils.Ical4jHelper;
import fr.aliacom.obm.utils.LogUtils;

public class CalendarBindingImpl implements ICalendar {

	private static final Log logger = LogFactory
			.getLog(CalendarBindingImpl.class);

	private EventType type;
	private DomainService domainService;
	private EventChangeHandler eventChangeHandler;
	private UserDao userDao;
	private CalendarDao calendarService;

	private CategoryDao categoryDao;
	private SettingDao settingsDao;
	private final Helper helper;

	@Inject
	protected CalendarBindingImpl(EventChangeHandler eventChangeHandler,
			DomainService domainService, UserDao userDao,
			CalendarDao calendarDao, SettingDao settingDao,
			CategoryDao categoryDao, Helper helper) {
		this.eventChangeHandler = eventChangeHandler;
		this.domainService = domainService;
		this.userDao = userDao;
		this.settingsDao = settingDao;
		this.calendarService = calendarDao;
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
		ObmUser u = getCalendarOwner(token.getUser(), token.getDomain());
		return calendarService.listCalendars(u);
	}

	/**
	 * Returns an ObmUser if the calendar@domainName has an email and is not
	 * archived
	 */
	private ObmUser getCalendarOwner(String calendar, String domainName)
			throws FindException {
		String username = Iterables.getFirst(Splitter.on('@')
				.omitEmptyStrings().split(calendar), null);
		if (username == null) {
			throw new FindException("invalid calendar string : " + calendar);
		}
		ObmDomain domain = domainService.findDomainByName(domainName);
		if(domain == null){
			logger.info("domain :" + domainName
					+ " not found");
			throw new FindException("Domain["+domainName+"] not exist or not valid");
		}
		ObmUser user = userDao.findUserByLogin(username, domain);
		if (user == null || StringUtils.isEmpty(user.getEmail())) {
			logger.info("user :" + calendar
					+ " not found, archived or have no email");
			throw new FindException("Calendar not exist or not valid");
		}
		return user;
	}

	@Override
	@Transactional
	public Event removeEvent(AccessToken token, String calendar, String eventId)
			throws AuthFault, ServerFault {
		try {
			// only remove if logged user is owner
			// or as write right on owner
			int uid = Integer.valueOf(eventId);
			Event ev = calendarService.findEvent(token, uid);

			if (ev == null) {
				logger.info(LogUtils.prefix(token) + "event with id : "
						+ eventId + "not found");
				return null;
			}

			String owner = ev.getOwner();
			if (owner != null) {
				if (helper.canWriteOnCalendar(token, owner)) {
					ev = calendarService.removeEvent(token, uid, ev.getType());
					logger.info(LogUtils.prefix(token) + "Calendar : event[" + uid + "] removed");
					if (ev.isInternalEvent()) {
						eventChangeHandler.delete(token, ev, settingsDao.getUserLanguage(token));
					} else {
						notifyOrganizerForExternalEvent(token, calendar, ev, ParticipationState.DECLINED);
					}
					return ev;
				}
				if (helper.canReadCalendar(token, owner)) {
					logger.info(LogUtils.prefix(token)
							+ "remove not allowed of " + ev.getTitle());
					return ev;
				}
				logger.info(LogUtils.prefix(token) + "read not allowed of "
						+ ev.getTitle());
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

	@Override
	@Transactional
	public Event removeEventByExtId(AccessToken token, String calendar,
			String extId) throws AuthFault, ServerFault {
		try {
			ObmUser calendarUser = getCalendarOwner(calendar, token.getDomain());
			// only remove if logged user is owner
			// or as write right on owner
			final Event ev = calendarService.findEventByExtId(token, calendarUser, extId);

			if (ev == null) {
				logger.info(LogUtils.prefix(token) + "Calendar : event[" + extId + "] not removed, it doesn't exist");
				return ev;
			} else {

				if (ev.getOwner() != null && !helper.canWriteOnCalendar(token, ev.getOwner())) {
					logger.info(LogUtils.prefix(token) + "remove not allowed of " + ev.getTitle());
					return ev;
				}

				calendarService.removeEventByExtId(token, calendarUser, extId);
				logger.info(LogUtils.prefix(token) + "Calendar : event[" + extId + "] removed");
				
				if (ev.isInternalEvent()) {
					eventChangeHandler.delete(token, ev, settingsDao.getUserLanguage(token));
				} else {
					notifyOrganizerForExternalEvent(token, calendar, ev, ParticipationState.DECLINED);
				}
				return ev;
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public Event modifyEvent(AccessToken token, String calendar, Event event,
			boolean updateAttendees) throws AuthFault, ServerFault {

		if (event == null) {
			logger.warn(LogUtils.prefix(token)
					+ "Modify on NULL event: doing nothing");
			return null;
		}
		try {
			ObmUser calendarUser = getCalendarOwner(calendar, token.getDomain());
			Event before = loadCurrentEvent(token, calendarUser, event);

			if (before == null) {
				logger.warn(LogUtils.prefix(token)
						+ "Event[uid:"+ event.getUid() + "extId:" + event.getExtId() +
						"] doesn't exist in database: : doing nothing");
				return null;
			}
			if (before.getOwner() != null
					&& !helper.canWriteOnCalendar(token, before.getOwner())) {
				logger.info(LogUtils.prefix(token) + "Calendar : "
						+ token.getUser() + " cannot modify event["
						+ before.getTitle() + "] because not owner"
						+ " or no write right on owner " + before.getOwner()+". ParticipationState will be updated.");
				return event;
			} else{
				if(before.isInternalEvent()){
					return modifyInternalEvent(token, calendar, before, event, updateAttendees);
				} else {
					return modifyExternalEvent(token, calendar, event, updateAttendees);
				}
			}

		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e);
		}

	}
	
	private Event loadCurrentEvent(AccessToken token, ObmUser calendarUser, Event event) {
		if (Strings.isNullOrEmpty(event.getUid())) {
			Event currentEvent = calendarService.findEventByExtId(token, calendarUser, event.getExtId());
			event.setUid(currentEvent.getUid());
			return currentEvent;
		} else {
			int uid = Integer.valueOf(event.getUid());
			return calendarService.findEvent(token, uid);
		}
	}

	private Event modifyInternalEvent(AccessToken token, String calendar, Event before,  Event event,
			boolean updateAttendees) throws ServerFault {
		try{
			changePartipationStateOnNewWritableCalendar(token, before, event);
			Event after = calendarService.modifyEvent(token, calendar, event, updateAttendees, true);

			if (after != null) {
				logger.info(LogUtils.prefix(token) + "Calendar : internal event["
						+ after.getTitle() + "] modified");
			}
			eventChangeHandler.update(token, before, after,
					settingsDao.getUserLanguage(token));

			return after;
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Event modifyExternalEvent(AccessToken token, String calendar, 
			Event event, boolean updateAttendees) throws ServerFault {
		try {
			if (isEventDeclinedForCalendarOwner(token, calendar, event)) {
				ObmUser calendarOwner = getCalendarOwner(calendar, token.getDomain());
				calendarService.removeEventByExtId(token, calendarOwner, event.getExtId());
				notifyOrganizerForExternalEvent(token, calendar, event);
				logger.info(LogUtils.prefix(token) + "Calendar : External event[" + event.getTitle() + 
						"] removed, calendar owner won't attende to it");
				return event;
			} else {
				Event after = calendarService.modifyEvent(token,  calendar, event, updateAttendees, false);
				if (after != null) {
					logger.info(LogUtils.prefix(token) + "Calendar : External event[" + after.getTitle() + "] modified");
				}
				notifyOrganizerForExternalEvent(token, calendar, after);
				return after;
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public String createEvent(AccessToken token, String calendar, Event event)
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

			ObmUser calendarUser = getCalendarOwner(calendar, token.getDomain());

			if (StringUtils.isNotEmpty(event.getExtId())) {
				Event duplicateByExtId = calendarService.findEventByExtId(
						token, calendarUser, event.getExtId());
				if (duplicateByExtId != null) {
					String message = String
							.format("Calendar : duplicate with same extId found for event [%s, %s, %d, %s]",
									event.getTitle(), event.getDate()
											.toString(), event.getDuration(),
									event.getExtId());
					logger.info(LogUtils.prefix(token) + message);
					throw new ServerFault(message);
				}
			}

			if (!helper.canWriteOnCalendar(token, calendar)) {
				// helper.resetAttendeesStatus(event);
				String message = "[" + token.getUser() + "] Calendar : "
						+ token.getUser() + " cannot create event on "
						+ calendar + "calendar : no write right";
				logger.info(LogUtils.prefix(token) + message);
				throw new ServerFault(message);
			}
			Event ev = null;
			if (event.isInternalEvent()) {
				ev = createInternalEvent(token, calendar, event);
			} else {
				ev = createExternalEvent(token, calendar, event);
			}
			return (String.valueOf(ev.getDatabaseId()));
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private Event createExternalEvent(AccessToken token, String calendar, Event event) throws ServerFault {
		try {
			if (isEventDeclinedForCalendarOwner(token, calendar, event)) {
				logger.info(LogUtils.prefix(token) + "Calendar : external event["+ event.getTitle() + "] refused, mark event as deleted");
				calendarService.removeEvent(token, event, event.getType());
				notifyOrganizerForExternalEvent(token, calendar, event);
				return event;
			} else {
				Event ev = calendarService.createEvent(token, calendar, event, false);
				logger.info(LogUtils.prefix(token) + "Calendar : external event["+ ev.getTitle() + "] created");
				notifyOrganizerForExternalEvent(token, calendar, ev);
				return ev;
			}
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	private boolean isEventDeclinedForCalendarOwner(AccessToken token,
			String calendar, Event event) throws FindException {
		ObmUser calendarOwner = getCalendarOwner(calendar, token.getDomain());
		Attendee userAsAttendee = event.findAttendeeForUser(calendarOwner.getEmailAtDomain());
		return userAsAttendee != null && userAsAttendee.getState() == ParticipationState.DECLINED;
	}

	private void notifyOrganizerForExternalEvent(AccessToken token,
			String calendar, Event ev, ParticipationState state) throws FindException {
		logger.info(LogUtils.prefix(token) + 
				"Calendar : sending participation notification to organizer of event ["+ ev.getTitle() + "]");
		ObmUser calendarOwner = getCalendarOwner(calendar, token.getDomain());
		eventChangeHandler.updateParticipationState(ev, calendarOwner, state, 
				settingsDao.getUserLanguage(token));
	}

	
	private void notifyOrganizerForExternalEvent(AccessToken token, String calendar, Event ev) throws FindException {
		ObmUser calendarOwner = getCalendarOwner(calendar, token.getDomain());
		Attendee calendarOwnerAsAttendee = ev.findAttendeeForUser(calendarOwner.getEmailAtDomain());
		notifyOrganizerForExternalEvent(token, calendar, ev, calendarOwnerAsAttendee.getState());
	}

	private Event createInternalEvent(AccessToken token, String calendar, Event event) throws ServerFault {
		try{
			changePartipationStateOnWritableCalendar(token, event);
			Event ev = calendarService.createEvent(token, calendar, event, true);
			eventChangeHandler.create(token, ev, settingsDao.getUserLanguage(token));
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
	
	private List<Attendee> changePartipationStateOnNewWritableCalendar(AccessToken token,  Event before, Event event) {
		List<Attendee> newAtt = Lists.newLinkedList();
		for(Attendee att : event.getAttendees()){
			if(!before.getAttendees().contains(att)){
				acceptePartipationStateIfCanWriteOnCalendar(token, att);
			}
		}
		return newAtt;
	}
	
	private void acceptePartipationStateIfCanWriteOnCalendar(AccessToken token, Attendee att){
		try {
			if(helper.canWriteOnCalendar(token,  att.getEmail())){
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
		return getSync(token, calendar, lastSync, false);
	}

	@Override
	@Transactional
	public EventChanges getSyncWithSortedChanges(AccessToken token,
			String calendar, Date lastSync) throws AuthFault, ServerFault {

		EventChanges changes = getSync(token, calendar, lastSync, false);
		
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
		return getSync(token, calendar, lastSync, true);
	}

	private EventChanges getSync(AccessToken token, String calendar,
			Date lastSync, boolean onEventDate) throws ServerFault {

		logger.info(LogUtils.prefix(token) + "Calendar : getSync(" + calendar
				+ ", " + lastSync + ")");

		ObmUser calendarUser = null;
		try {
			calendarUser = getCalendarOwner(calendar, token.getDomain());
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
			EventChanges ret = calendarService.getSync(token, calendarUser,
					lastSync, type, onEventDate);
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
			Event evt = calendarService.findEvent(token, uid);
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
			List<String> keys = calendarService.findEventTwinKeys(calendar,
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
			ObmDomain domain = domainService
					.findDomainByName(token.getDomain());
			ObmUser calendarUser = userDao.findUserByLogin(calendar, domain);
			List<String> keys = calendarService.findRefusedEventsKeys(
					calendarUser, since);
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
			ObmUser obmuser = userDao.findUserById(token.getObmId());
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
			ObmUser calendarUser = getCalendarOwner(calendar, token.getDomain());
			return calendarService.findEventByExtId(token, calendarUser, extId);
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
			calendarUser = getCalendarOwner(calendar, token.getDomain());
		} catch (FindException e1) {
			throw new ServerFault(e1.getMessage());
		}
		try {
			return calendarService.findListEventsFromIntervalDate(token,
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
				ObmUser calendarUser = getCalendarOwner(calendar,
						token.getDomain());
				return calendarService.findAllEvents(token, calendarUser,
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
		return Ical4jHelper.parseEvent(event, token);
	}

	@Override
	@Transactional
	public String parseEvents(AccessToken token, List<Event> events)
			throws ServerFault, AuthFault {
		return Ical4jHelper.parseEvents(token, events);
	}

	@Override
	@Transactional
	public List<Event> parseICS(AccessToken token, String ics)
			throws Exception, ServerFault {
		String fixedIcs = fixIcsAttendees(ics);
		return Ical4jHelper.parseICSEvent(fixedIcs, token);
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
				ObmUser calendarUser = getCalendarOwner(calendar,
						token.getDomain());
				return calendarService
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
				ObmUser calendarUser = getCalendarOwner(calendar,
						token.getDomain());
				return calendarService
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
				return calendarService.findLastUpdate(token, calendar);
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
			return calendarService.getFreeBusy(fb);
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
			String extId, ParticipationState participationState) throws ServerFault {
		if (helper.canWriteOnCalendar(token, calendar)) {
			try {
				//We should handle all this in a transaction, but we don't, so
				//when event is not found, we just don't send change event
				ObmUser calendarOwner = getCalendarOwner(calendar, token.getDomain());
				boolean changed = calendarService.changeParticipationState(token, calendarOwner, extId, participationState);
				Event newEvent = calendarService.findEventByExtId(token, calendarOwner, extId);
				if (newEvent != null) {
					eventChangeHandler.updateParticipationState(newEvent, calendarOwner, participationState,
							settingsDao.getUserLanguage(token));
					
				} else {
					logger.error("event with extId : "+ extId + " is no longer in database, ignoring notification");
				}
				return changed;
			} catch (Exception e) {
				throw new ServerFault("no user found with calendar " + calendar);
			}
		}
		throw new ServerFault("user has no write rights on calendar " + calendar);
	}

	@Override
	@Transactional
	public void importICalendar(final AccessToken token, final String calendar, final String ics) 
		throws ImportICalendarException, AuthFault, ServerFault {
		
		final List<Event> events = parseICSEvent(token, ics);
		for (final Event event: events) {
			
			if (!isAttendeeExistForCalendarOwner(calendar, event.getAttendees())) {
				addAttendeeForCalendarOwner(token, calendar, event);
			}
			
			createEvent(token, calendar, event);
		}
	}
	
	private List<Event> parseICSEvent(final AccessToken token, final String icsToString) throws ImportICalendarException {
		try {
			return Ical4jHelper.parseICSEvent(icsToString, token);
		} catch (IOException e) {
			throw new ImportICalendarException(e);
		} catch (ParserException e) {
			throw new ImportICalendarException(e);
		}
	}
	
	private void addAttendeeForCalendarOwner(final AccessToken token, final String calendar, final Event event) throws ImportICalendarException {
		final ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName(token.getDomain());
		obmDomain.setId(token.getDomainId());
		
		final ObmUser obmUser = getCalendarOwnerUser(calendar, obmDomain);
		final Attendee attendee = new Attendee();
		attendee.setEmail(obmUser.getEmailAtDomain());
		
		event.getAttendees().add(attendee);
	}

	private ObmUser getCalendarOwnerUser(final String calendar, final ObmDomain obmDomain) throws ImportICalendarException {
		final ObmUser obmUser = userDao.findUserByLogin(calendar, obmDomain);
		if (obmUser == null) {
			throw new ImportICalendarException("user " + calendar + " not found");
		}
		return obmUser;
	}
	
	private boolean isAttendeeExistForCalendarOwner(final String calendar, final List<Attendee> attendees) {
		for (final Attendee attendee: attendees) {
			final ObmUser obmUser = userDao.findUser(attendee.getEmail());
			if (obmUser != null) {
				if (obmUser.getLogin().equals(calendar)) {
					return true;
				}	
			}
		}
		return false;
	}

}