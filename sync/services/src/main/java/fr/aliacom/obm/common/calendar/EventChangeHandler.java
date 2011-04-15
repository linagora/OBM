package fr.aliacom.obm.common.calendar;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;

import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserSettings;

public class EventChangeHandler {

	private static final Log logger = LogFactory.getLog(EventChangeHandler.class);
	private final EventChangeMailer eventChangeMailer;
	private final SettingsService settingsService;

	@Inject
	/* package */ EventChangeHandler(EventChangeMailer eventChangeMailer,
			SettingsService settingsService) {
		this.eventChangeMailer = eventChangeMailer;
		this.settingsService = settingsService;
	}
	
	public void create(final ObmUser user, final Event event) throws NotificationException {
		if (eventCreationInvolveNotification(event)) {
			Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
			UserSettings settings = settingsService.getSettings(user);
			notifyCreate(user, attendees, event, settings.locale(), settings.timezone());
		}
	}
	
	private void notifyCreate(final ObmUser user, final Collection<Attendee> attendees, 
			final Event event, final Locale locale, final TimeZone timezone){
		
		Map<ParticipationState, ? extends Set<Attendee>> attendeeGroups = computeParticipationStateGroups(attendees);
		
		Set<Attendee> accepted = attendeeGroups.get(ParticipationState.ACCEPTED);
		if(accepted != null && !accepted.isEmpty()){
			eventChangeMailer.notifyAcceptedNewUsers(accepted, event, locale, timezone);
		}
		
		Set<Attendee> notAccepted = attendeeGroups.get(ParticipationState.NEEDSACTION);
		if (notAccepted != null && !notAccepted.isEmpty()) {
			eventChangeMailer.notifyNeedActionNewUsers(user, notAccepted, event, locale, timezone);
		}
		
	}

	public void update(final ObmUser user, final Event previous, final Event current) throws NotificationException {
		
		UserSettings settings = settingsService.getSettings(user);
		TimeZone timezone = settings.timezone();
		Locale locale = settings.locale();
		
		final Map<AttendeeStateValue, ? extends Set<Attendee>> attendeeGroups = computeUpdateNotificationGroups(previous, current);
		final Set<Attendee> removedUsers = attendeeGroups.get(AttendeeStateValue.Old);
		if (!removedUsers.isEmpty()) {
			eventChangeMailer.notifyRemovedUsers(user, removedUsers, current, locale, timezone);
		}
		
		final Set<Attendee> addedUsers = attendeeGroups.get(AttendeeStateValue.New);
		if (!addedUsers.isEmpty()) {
			notifyCreate(user, addedUsers, current, locale, timezone);
		}
		
		final Set<Attendee> currentUsers = attendeeGroups.get(AttendeeStateValue.Current);
		if (!currentUsers.isEmpty()) {
			final Map<ParticipationState, ? extends Set<Attendee>> atts = computeParticipationStateGroups(currentUsers);
			notifyAcceptedUpdateUsers(previous, current, locale, atts, timezone);
			notifyNeedActionUpdateUsers(user, previous, current, locale, atts, timezone);
		}
		
	}
	
	private void notifyAcceptedUpdateUsers(final Event previous, final Event current, 
			final Locale locale, final Map<ParticipationState, ? extends Set<Attendee>> atts, TimeZone timezone) {
		final Set<Attendee> accepted = atts.get(ParticipationState.ACCEPTED);
		if(accepted != null && !accepted.isEmpty()){
			eventChangeMailer.notifyAcceptedUpdateUsers(accepted, previous, current, locale, timezone);
		}	
	}
	
	private void notifyNeedActionUpdateUsers(final ObmUser user, final Event previous, final Event current, final Locale locale, final Map<ParticipationState, ? extends Set<Attendee>> atts, TimeZone timezone) { 
		final Set<Attendee> notAccepted = atts.get(ParticipationState.NEEDSACTION);
		if (notAccepted != null && !notAccepted.isEmpty()) {
			eventChangeMailer.notifyNeedActionUpdateUsers(user, notAccepted, previous, current, locale, timezone);
		}
	}

	public void delete(final ObmUser user, final Event event) throws NotificationException {
 		if (eventDeletionInvolveNotification(event)) {
 			Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
 			Map<ParticipationState, ? extends Set<Attendee>> attendeeGroups = computeParticipationStateGroups(attendees);
 			Set<Attendee> notify = Sets.union(attendeeGroups.get(ParticipationState.NEEDSACTION), attendeeGroups.get(ParticipationState.ACCEPTED));
 			if (!notify.isEmpty()) {
 				UserSettings settings = settingsService.getSettings(user);
 				eventChangeMailer.notifyRemovedUsers(user, notify, event, settings.locale(), settings.timezone());
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
	
	public void updateParticipationState(final Event event, final ObmUser calendarOwner, final ParticipationState state) {
		if (ParticipationState.ACCEPTED.equals(state) || ParticipationState.DECLINED.equals(state)) {
			final Attendee organizer = event.findOrganizer();
			if (organizer != null) {
				if (!ParticipationState.DECLINED.equals(organizer.getState())&& !StringUtils.isEmpty(organizer.getEmail()) 
						&& !organizer.getEmail().equalsIgnoreCase(calendarOwner.getEmailAtDomain())) {
					UserSettings settings = settingsService.getSettings(calendarOwner);					
					eventChangeMailer.notifyUpdateParticipationState(event, organizer, calendarOwner, state, settings.locale(), settings.timezone());
				}
			} else {
				logger.error("Can't find organizer, email won't send");
			}
		}
	}
	
}
