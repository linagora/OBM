package fr.aliacom.obm.common.calendar;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;
import org.obm.sync.server.mailer.EventChangeMailer;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;

public class EventChangeHandler {

	private static final Log logger = LogFactory
	.getLog(EventChangeHandler.class);
	private EventChangeMailer eventChangeMailer;

	@Inject
	/* package */ EventChangeHandler(EventChangeMailer eventChangeMailer) {
		this.eventChangeMailer = eventChangeMailer;
	}
	
	public void create(final AccessToken at, final Event event, Locale locale) throws NotificationException {
		if (eventCreationInvolveNotification(event)) {
			Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
			notifyCreate(at, attendees, event, locale);
		}
	}
	
	private void notifyCreate(final AccessToken at, Collection<Attendee> attendees, final Event event, Locale locale){
		Map<ParticipationState, ? extends Set<Attendee>> attendeeGroups = computeParticipationStateGroups(attendees);
		
		Set<Attendee> accepted = attendeeGroups.get(ParticipationState.ACCEPTED);
		if(accepted != null && !accepted.isEmpty()){
			eventChangeMailer.notifyAcceptedNewUsers(accepted, event, locale);
		}
		
		Set<Attendee> notAccepted = attendeeGroups.get(ParticipationState.NEEDSACTION);
		if (notAccepted != null && !notAccepted.isEmpty()) {
			eventChangeMailer.notifyNeedActionNewUsers(at, notAccepted, event, locale);
		}
		
	}

	public void update(final AccessToken at, final Event previous, final Event current, Locale locale) throws NotificationException {
		Map<AttendeeStateValue, ? extends Set<Attendee>> attendeeGroups = 
			computeUpdateNotificationGroups(previous, current);
		Set<Attendee> removedUsers = attendeeGroups.get(AttendeeStateValue.Old);
		if (!removedUsers.isEmpty()) {
			eventChangeMailer.notifyRemovedUsers(at, removedUsers, current, locale);
		}
		Set<Attendee> addedUsers = attendeeGroups.get(AttendeeStateValue.New);
		if (!addedUsers.isEmpty()) {
			notifyCreate(at, addedUsers, current, locale);
		}
		Set<Attendee> currentUsers = attendeeGroups.get(AttendeeStateValue.Current);
		if (!currentUsers.isEmpty()) {
			eventChangeMailer.notifyUpdateUsers(at, currentUsers, previous, current, locale);
		}
	}
	
 	public void delete(final AccessToken at, final Event event, Locale locale) throws NotificationException {
 		if (eventDeletionInvolveNotification(event)) {
 			Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
 			Map<ParticipationState, ? extends Set<Attendee>> attendeeGroups = computeParticipationStateGroups(attendees);
 			Set<Attendee> notify = Sets.union(attendeeGroups.get(ParticipationState.NEEDSACTION), attendeeGroups.get(ParticipationState.ACCEPTED));
 			if(notify.size() >0){
 				eventChangeMailer.notifyRemovedUsers(at, notify, event, locale);
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
	
	private Map<ParticipationState, ? extends Set<Attendee>> computeParticipationStateGroups(Collection<Attendee> attendees) {
		Set<Attendee> acceptedAttendees = Sets.newLinkedHashSet();
		Set<Attendee> needActionAttendees = Sets.newLinkedHashSet();
		Set<Attendee> declinedAttendees = Sets.newLinkedHashSet();
		
		Set<Attendee> tentativeAttendees = Sets.newLinkedHashSet();
		Set<Attendee> delegatedAttendees = Sets.newLinkedHashSet();
		
		Set<Attendee> completedAttendees = Sets.newLinkedHashSet();
		Set<Attendee> inprogressAttendees = Sets.newLinkedHashSet();
		
		for(Attendee att : attendees){
			switch (att.getState()) {
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
		Builder<ParticipationState, Set<Attendee>> ret = ImmutableMap.builder();
		ret.put(ParticipationState.ACCEPTED, acceptedAttendees);
		ret.put(ParticipationState.NEEDSACTION, needActionAttendees);
		ret.put(ParticipationState.DECLINED, declinedAttendees);
		ret.put(ParticipationState.TENTATIVE, tentativeAttendees);
		ret.put(ParticipationState.DELEGATED, delegatedAttendees);
		ret.put(ParticipationState.COMPLETED, completedAttendees);
		ret.put(ParticipationState.INPROGRESS, inprogressAttendees);
		return ret.build();
	}
	
	private Map<AttendeeStateValue, ? extends Set<Attendee>> computeUpdateNotificationGroups(Event previous, Event current) {
		Set<Attendee> removedAttendees = ImmutableSet.of();
		Set<Attendee> newAttendees = ImmutableSet.of();
		Set<Attendee> stableAttendees = ImmutableSet.of();
		
		if (!previous.isEventInThePast() || !current.isEventInThePast()) {
			ImmutableSet<Attendee> previousAttendees = ImmutableSet.copyOf(filterOwner(previous, previous.getAttendees()));
			ImmutableSet<Attendee> currentAttendees = ImmutableSet.copyOf(filterOwner(current, current.getAttendees()));
			removedAttendees = Sets.difference(previousAttendees, currentAttendees);
			newAttendees = Sets.difference(currentAttendees, previousAttendees);
			stableAttendees = Sets.intersection(previousAttendees, currentAttendees);
		}
		
		return ImmutableMap.of(
				AttendeeStateValue.Old, removedAttendees,
				AttendeeStateValue.Current, stableAttendees,
				AttendeeStateValue.New, newAttendees);
	}

	private static enum AttendeeStateValue {
		New, Current, Old;
	}
	
	public void updateParticipationState(
			Event event, ObmUser calendarOwner, ParticipationState state,
			Locale locale) {
		
		if( ParticipationState.ACCEPTED.equals(state) || ParticipationState.DECLINED.equals(state)){
			Attendee organizer = findOrganizer(event);
			if (organizer != null) {
				if(!ParticipationState.DECLINED.equals(organizer.getState())&& !StringUtils.isEmpty(organizer.getEmail()) 
						&& !organizer.getEmail().equalsIgnoreCase(calendarOwner.getEmailAtDomain())){
					eventChangeMailer.notifyUpdateParticipationState(event, organizer, calendarOwner, state, locale);
				} 
			} else {
				logger.error("Can't find organizer, email won't send");
			}
		}
	}

	private Attendee findOrganizer(Event event) {
		for(Attendee att : event.getAttendees()){
			if(att.isOrganizer()){
				return  att;
			}
		}
		return null;
	}
	
}
