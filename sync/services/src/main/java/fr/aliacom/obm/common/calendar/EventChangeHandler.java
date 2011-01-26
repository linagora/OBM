package fr.aliacom.obm.common.calendar;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.EventChangeMailer.NotificationException;

public class EventChangeHandler {

	private EventChangeMailer eventChangeMailer;

	@Inject
	/* package */ EventChangeHandler(EventChangeMailer eventChangeMailer) {
		this.eventChangeMailer = eventChangeMailer;
	}
	
	public void create(final AccessToken at, final Event event, Locale locale) throws NotificationException {
		if (eventCreationInvolveNotification(event)) {
			Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
			if (!attendees.isEmpty()) {
				eventChangeMailer.notifyNewUsers(at, attendees, event, locale);
			}
		}
	}

	public void update(final AccessToken at, final Event previous, final Event current, Locale locale) throws NotificationException {
		Map<AttendeeStateValue, ? extends Set<Attendee>> attendeeGroups = 
			computeNotificationGroups(previous, current);
		Set<Attendee> removedUsers = attendeeGroups.get(AttendeeStateValue.Old);
		if (!removedUsers.isEmpty()) {
			eventChangeMailer.notifyRemovedUsers(at, removedUsers, current, locale);
		}
		Set<Attendee> addedUsers = attendeeGroups.get(AttendeeStateValue.New);
		if (!addedUsers.isEmpty()) {
			eventChangeMailer.notifyNewUsers(at, addedUsers, current, locale);
		}
		Set<Attendee> currentUsers = attendeeGroups.get(AttendeeStateValue.Current);
		if (!currentUsers.isEmpty()) {
			eventChangeMailer.notifyUpdateUsers(at, currentUsers, previous, current, locale);
		}
	}
	
 	public void delete(final AccessToken at, final Event event, Locale locale) throws NotificationException {
 		if (eventDeletionInvolveNotification(event)) {
 			Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
 			if (!attendees.isEmpty()) {
 				eventChangeMailer.notifyRemovedUsers(at, attendees, event, locale);
 			}
 		}
 	}
 	
	private boolean eventCreationInvolveNotification(final Event event) {
		return !event.isEventInThePast();
	}
	
	private boolean eventDeletionInvolveNotification(final Event event) {
		return !event.isEventInThePast();
	}
	
	private Collection<Attendee> ensureAttendeeUnicity(List<Attendee> attendees) {
		return ImmutableSet.copyOf(attendees);
	}
	
	private Collection<Attendee> filterOwner(final Event event, Collection<Attendee> attendees) {
		return Collections2.filter(attendees, new Predicate<Attendee>() {
			@Override
			public boolean apply(Attendee at) {
				return !at.getEmail().equalsIgnoreCase(event.getOwnerEmail());
			}
		});
	}
	
	private Map<AttendeeStateValue, ? extends Set<Attendee>> computeNotificationGroups(Event previous, Event current) {
		if (previous.isEventInThePast() && current.isEventInThePast()) {
			return ImmutableMap.of();
		}
		ImmutableSet<Attendee> previousAttendees = ImmutableSet.copyOf(filterOwner(previous, previous.getAttendees()));
		ImmutableSet<Attendee> currentAttendees = ImmutableSet.copyOf(filterOwner(current, current.getAttendees()));
		SetView<Attendee> removedAttendees = Sets.difference(previousAttendees, currentAttendees);
		SetView<Attendee> newAttendees = Sets.difference(currentAttendees, previousAttendees);
		SetView<Attendee> stableAttendees = Sets.intersection(previousAttendees, currentAttendees);
		return ImmutableMap.of(
				AttendeeStateValue.Old, removedAttendees,
				AttendeeStateValue.Current, stableAttendees,
				AttendeeStateValue.New, newAttendees);
	}

	private static enum AttendeeStateValue {
		New, Current, Old;
	}
	
}
