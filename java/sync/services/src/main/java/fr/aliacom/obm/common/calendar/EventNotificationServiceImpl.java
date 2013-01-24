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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.Participation;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;
import org.obm.sync.server.mailer.EventChangeMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserSettings;

@Singleton
public class EventNotificationServiceImpl implements EventNotificationService {

	private static final Logger logger = LoggerFactory.getLogger(EventChangeHandler.class);
	
	private final UserService userService;
	private final SettingsService settingsService;
	private final EventChangeMailer eventChangeMailer;

	private final Ical4jHelper ical4jHelper;

	private final ICalendarFactory calendarFactory;

	@Inject
	@VisibleForTesting EventNotificationServiceImpl(EventChangeMailer eventChangeMailer, SettingsService settingsService, UserService userService, 
			Ical4jHelper ical4jHelper, ICalendarFactory calendarFactory) {
		
		this.userService = userService;
		this.settingsService = settingsService;
		this.eventChangeMailer = eventChangeMailer;
		this.ical4jHelper = ical4jHelper;
		this.calendarFactory = calendarFactory;
	}

	@Override
	public void notifyCreatedEvent(Event event, AccessToken token) {
		ObmUser user = userService.getUserFromAccessToken(token);
		Ical4jUser buildIcal4jUser = calendarFactory.createIcal4jUserFromObmUser(user);
		String ics = ical4jHelper.buildIcsInvitationRequest(buildIcal4jUser, event, token);
		Attendee owner = findOwner(event);
		
		Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
		if (eventCreationInvolveNotification(event)) {
			notifyCreate(user, attendees, event, ics, token);
		}
		if (!isUserEventOwner(user, event)) {
			notifyOwnerCreate(user, event, owner, token);
		}
		
	}

	@Override
	public void notifyDeletedEvent(Event event, AccessToken token) {
		if (eventDeletionInvolveNotification(event)) {
			ObmUser user = userService.getUserFromAccessToken(token);
			Ical4jUser buildIcal4jUser = calendarFactory.createIcal4jUserFromObmUser(user);
			String ics = ical4jHelper.buildIcsInvitationCancel(buildIcal4jUser, event, token);

			Attendee owner = findOwner(event);

			Collection<Attendee> attendees = filterOwner(event,
					ensureAttendeeUnicity(event.getAttendees()));
			Map<Participation, Set<Attendee>> attendeeGroups = computeParticipationGroups(attendees);
			Set<Attendee> notify = Sets.union(
					attendeeGroups.get(Participation.needsAction()),
					attendeeGroups.get(Participation.accepted()));
			if (!notify.isEmpty()) {
				UserSettings settings = settingsService.getSettings(user);
				eventChangeMailer.notifyRemovedUsers(user, notify, event,
						settings.locale(), settings.timezone(), ics,
						token);
			}
			if (owner != null && !isUserEventOwner(user, event)) {
				notifyOwnerDelete(user, event, owner, token);
			}
		}
	}

	@Override
	public void notifyUpdatedParticipationAttendees(Event event,
			ObmUser calendarOwner, Participation participation, AccessToken token) {
		
		if (isHandledParticipation(participation)) {

			Ical4jUser replyIcal4jUser = calendarFactory.createIcal4jUserFromObmUser(calendarOwner);
			String ics = ical4jHelper.buildIcsInvitationReply(event, replyIcal4jUser, token);

			final Attendee organizer = event.findOrganizer();
			if (organizer != null) {
				if (updateParticipationNeedsNotification(calendarOwner, organizer)) {
					UserSettings settings = settingsService.getSettings(calendarOwner);
					eventChangeMailer.notifyUpdateParticipation(event, 
							organizer, calendarOwner, participation, settings.locale(), 
							settings.timezone(), ics, token);
				}
			} else {
				logger.error("Can't find organizer, email won't send");
			}
		}		
	}
	
	@Override
	public void notifyUpdatedEvent(Event previous, Event current, AccessToken token) {
		
		ObmUser user = userService.getUserFromAccessToken(token);
		Ical4jUser buildIcal4jUser = calendarFactory.createIcal4jUserFromObmUser(user);
		
		String addUserIcs = ical4jHelper.buildIcsInvitationRequest(buildIcal4jUser, current, token);
		String removedUserIcs = ical4jHelper.buildIcsInvitationCancel(buildIcal4jUser, current, token);
		String updateUserIcs = ical4jHelper.buildIcsInvitationRequest(buildIcal4jUser, current, token);
		
		boolean notifyImportantChanges = previous.getSequence() != current.getSequence();
		UserSettings settings = settingsService.getSettings(user);
		
		TimeZone timezone = settings.timezone();
		Locale locale = settings.locale();

		Map<AttendeeStateValue, Set<Attendee>> attendeeGroups = computeUpdateNotificationGroups(previous, current);
		final Set<Attendee> removedAttendees = attendeeGroups.get(AttendeeStateValue.REMOVED);
		if (!removedAttendees.isEmpty()) {
			eventChangeMailer.notifyRemovedUsers(user, removedAttendees, current, locale, timezone, removedUserIcs, token);
		}

		Set<Attendee> addedAttendees = attendeeGroups.get(AttendeeStateValue.ADDED);
		if (!addedAttendees.isEmpty()) {
			notifyCreate(user, addedAttendees, current, addUserIcs, token);
		}

		Set<Attendee> keptAttendees = attendeeGroups.get(AttendeeStateValue.KEPT);
		if (!keptAttendees.isEmpty() && notifyImportantChanges) {
			Map<Participation, Set<Attendee>> atts = computeParticipationGroups(keptAttendees);
			notifyAcceptedUpdateUsers(user, previous, current, locale, atts, timezone, updateUserIcs, token);
			notifyNeedActionUpdateUsers(user, previous, current, locale, atts, timezone, updateUserIcs, token);
		}
		
		Attendee owner = findOwner(current);
		if (owner != null && !isUserEventOwner(user, current) && notifyImportantChanges) {
			notifyOwnerUpdate(user, owner, previous, current, locale, timezone, token);
		}
	}
	
	private boolean updateParticipationNeedsNotification(
			final ObmUser calendarOwner, final Attendee organizer) {

		return organizerHasEmailAddress(organizer) && organizerMayAttend(organizer) &&
		organizerExpectParticipationEmails(organizer, calendarOwner.getDomain()) &&
		!organizer.getEmail().equalsIgnoreCase(calendarOwner.getEmail());
	}

	private boolean organizerExpectParticipationEmails(Attendee organizer, ObmDomain domain) {
		ObmUser user = userService.getUserFromLogin(organizer.getEmail(), domain.getName());
		if(user == null){
			return true;
		}
		UserSettings settings = settingsService.getSettings(user);
		return settings.expectParticipationEmailNotification();
	}

	private boolean organizerMayAttend(final Attendee organizer) {
		return !Participation.declined().equals(organizer.getParticipation());
	}

	private boolean organizerHasEmailAddress(final Attendee organizer) {
		return !StringUtils.isEmpty(organizer.getEmail());
	}
	
	private boolean isHandledParticipation(final Participation participation) {
		if (participation == null) {
			return false;
		}
		switch (participation.getState()) {
		case ACCEPTED:
		case DECLINED:
			return true;
		default:
			return false;
		}
	}
	private void notifyOwnerDelete(ObmUser user, Event event, Attendee owner, AccessToken token) {
		UserSettings settings = settingsService.getSettings(user);
		Locale locale = settings.locale();
		TimeZone timezone = settings.timezone();
		
		Collection<Attendee> ownerAsCollection = new ArrayList<Attendee>(1);
		ownerAsCollection.add(owner);
		eventChangeMailer.notifyOwnerRemovedEvent(user, owner, event, locale, timezone, token);		
	}

	private boolean eventDeletionInvolveNotification(final Event event) {
		return !event.isEventInThePast();
	}
	
	private boolean eventCreationInvolveNotification(final Event event) {
		return !event.isEventInThePast();
	}
	
	private Collection<Attendee> ensureAttendeeUnicity(List<Attendee> attendees) {
		return ImmutableSet.copyOf(attendees);
	}
	
	private void notifyOwnerCreate(ObmUser user, Event event, Attendee owner, AccessToken token) {
		UserSettings settings = settingsService.getSettings(user);
		Locale locale = settings.locale();
		TimeZone timezone = settings.timezone();
		
		Collection<Attendee> ownerAsCollection = new ArrayList<Attendee>(1);
		ownerAsCollection.add(owner);
		eventChangeMailer.notifyAcceptedNewUsers(user, ownerAsCollection, event, locale, timezone, token);
	}
	
	private void notifyCreate(final ObmUser user, final Collection<Attendee> attendees, 
			final Event event, String ics, AccessToken token) throws NotificationException {
		
		UserSettings settings = settingsService.getSettings(user);
		Locale locale = settings.locale();
		TimeZone timezone = settings.timezone();
		
		Map<Participation, Set<Attendee>> attendeeGroups = computeParticipationGroups(attendees);
		Set<Attendee> accepted = attendeeGroups.get(Participation.accepted());
		if(accepted != null && !accepted.isEmpty()){
			eventChangeMailer.notifyAcceptedNewUsers(user, accepted, event, locale, timezone, token);
		}
		
		Set<Attendee> notAccepted = attendeeGroups.get(Participation.needsAction());
		if (notAccepted != null && !notAccepted.isEmpty()) {
			eventChangeMailer.notifyNeedActionNewUsers(user, notAccepted, event, locale, timezone, ics, token);
		}
	}
	
	private boolean isUserEventOwner(ObmUser user, Event event) {
		return user.getEmail().equals(event.getOwnerEmail());
	}
	
	private Attendee findOwner(final Event event) {
		for (Attendee attendee : event.getAttendees()) {
			boolean isOwner = attendee.getEmail().equalsIgnoreCase(event.getOwnerEmail());
			if (isOwner)
				return attendee;
		}
		return null;
	}
	
	private void notifyOwnerUpdate(ObmUser user, Attendee owner, Event previous, Event current, Locale locale, TimeZone timezone, AccessToken token) {
		eventChangeMailer.notifyOwnerUpdate(user, owner, previous, current, locale, timezone, token);
	}
	
	private void notifyNeedActionUpdateUsers(ObmUser user, Event previous, Event current,
			Locale locale, Map<Participation, Set<Attendee>> atts,
					TimeZone timezone, String ics, AccessToken token) { 
		
		logger.info("Listing all event attendees for event with name=[" + current.getTitle() + "]");
		for (Entry<Participation, Set<Attendee>> attendeesByState : atts.entrySet()) {
			logger.info("Attendees in state=[" + attendeesByState.getKey().getState().name() + "]");
			for (Attendee attendee : attendeesByState.getValue())
			{
				logger.info("<" + attendee.getEmail() + "> is in [" + attendee.getParticipation().getState().name() + "]");
			}
		}
		
		final Set<Attendee> notAccepted = atts.get(Participation.needsAction());

		if (notAccepted != null && !notAccepted.isEmpty()) {
			eventChangeMailer.notifyNeedActionUpdateUsers(user, notAccepted, previous, current, locale, timezone, ics, token);
		}
	}
	private Collection<Attendee> filterCanWriteOnCalendar(Set<Attendee> attendees) {
		return Collections2.filter(attendees, new Predicate<Attendee>() {
			@Override
			public boolean apply(Attendee attendee) {
				return attendee.isCanWriteOnCalendar();
			}
		});
	}
	
	private void notifyAcceptedUpdateUsers(ObmUser user, Event previous, Event current, Locale locale, 
			Map<Participation, ? extends Set<Attendee>> atts, TimeZone timezone, String ics, AccessToken token) {
		
		Set<Attendee> attendeesAccepted = atts.get(Participation.accepted());
		if (attendeesAccepted != null) {
			Collection<Attendee> attendeesCanWriteOnCalendar = filterCanWriteOnCalendar(attendeesAccepted);
			if (attendeesCanWriteOnCalendar != null && !attendeesCanWriteOnCalendar.isEmpty()) {
				eventChangeMailer.notifyAcceptedUpdateUsersCanWriteOnCalendar(user, attendeesCanWriteOnCalendar, previous, 
						current, locale, timezone, token);
			}
			
			attendeesAccepted.removeAll(attendeesCanWriteOnCalendar);
			if (!attendeesAccepted.isEmpty()) {
				eventChangeMailer.notifyAcceptedUpdateUsers(user, attendeesAccepted, previous, current, locale, timezone, ics, token);
			}
		}
	}
	
	@VisibleForTesting Map<AttendeeStateValue, Set<Attendee>> computeUpdateNotificationGroups(Event previous, Event current) {
		
		if (previous.isEventInThePast() && current.isEventInThePast()) {
			Set<Attendee> emptyAttendeesSet = ImmutableSet.of();
			return ImmutableMap.of(
					AttendeeStateValue.REMOVED, emptyAttendeesSet,
					AttendeeStateValue.KEPT, emptyAttendeesSet,
					AttendeeStateValue.ADDED, emptyAttendeesSet);
		}
		
		ImmutableSet<Attendee> previousAttendees = ImmutableSet.copyOf(filterOwner(previous, previous.getAttendees()));
		ImmutableSet<Attendee> currentAttendees = ImmutableSet.copyOf(filterOwner(current, current.getAttendees()));
		SetView<Attendee> removedAttendees = Sets.difference(previousAttendees, currentAttendees);
		SetView<Attendee> addedAttendees = Sets.difference(currentAttendees, previousAttendees);
		Set<Attendee> keptAttendees = Sets.difference(currentAttendees, addedAttendees);
		
		return ImmutableMap.of(
				AttendeeStateValue.REMOVED, removedAttendees,
				AttendeeStateValue.KEPT, keptAttendees,
				AttendeeStateValue.ADDED, addedAttendees);
	}
	
	private Collection<Attendee> filterOwner(final Event event, Collection<Attendee> attendees) {
		return Collections2.filter(attendees, new Predicate<Attendee>() {
			@Override
			public boolean apply(Attendee at) {
				return !at.getEmail().equalsIgnoreCase(event.getOwnerEmail());
			}
		});
	}
	
	private Map<Participation, Set<Attendee>> computeParticipationGroups(Collection<Attendee> attendees) {
		Set<Attendee> acceptedAttendees = Sets.newLinkedHashSet();
		Set<Attendee> needActionAttendees = Sets.newLinkedHashSet();
		Set<Attendee> declinedAttendees = Sets.newLinkedHashSet();
		
		Set<Attendee> tentativeAttendees = Sets.newLinkedHashSet();
		Set<Attendee> delegatedAttendees = Sets.newLinkedHashSet();
		
		Set<Attendee> completedAttendees = Sets.newLinkedHashSet();
		Set<Attendee> inprogressAttendees = Sets.newLinkedHashSet();
		
		for(Attendee att : attendees){
			switch (att.getParticipation().getState()) {
			case ACCEPTED:
				acceptedAttendees.add(att);
				break;
			case NEEDSACTION:
				needActionAttendees.add(att);
				break;
			case DECLINED:
				declinedAttendees.add(att);
				break;
			case TENTATIVE:
				tentativeAttendees.add(att);
				break;
			case DELEGATED:
				delegatedAttendees.add(att);
				break;
			case COMPLETED:
				completedAttendees.add(att);
				break;
			case INPROGRESS:
				inprogressAttendees.add(att);
				break;
			}
		}
		Builder<Participation, Set<Attendee>> ret = ImmutableMap.builder();
		ret.put(Participation.accepted(), acceptedAttendees);
		ret.put(Participation.needsAction(), needActionAttendees);
		ret.put(Participation.declined(), declinedAttendees);
		ret.put(Participation.tentative(), tentativeAttendees);
		ret.put(Participation.delegated(), delegatedAttendees);
		ret.put(Participation.completed(), completedAttendees);
		ret.put(Participation.inProgress(), inprogressAttendees);
		return ret.build();
	}

	/* package */ static enum AttendeeStateValue {
		ADDED, KEPT, REMOVED;
	}

}
