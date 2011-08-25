package fr.aliacom.obm.common.calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import javax.jms.JMSException;

import org.apache.commons.lang.StringUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;
import org.obm.sync.server.mailer.EventChangeMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;
import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.common.user.UserSettings;
import fr.aliacom.obm.utils.Ical4jHelper;

public class EventChangeHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(EventChangeHandler.class);
	private final EventChangeMailer eventChangeMailer;
	private final SettingsService settingsService;
	private final UserService userService;
	private final Producer producer;
	private final Ical4jHelper ical4jHelper;

	@Inject
	/* package */ EventChangeHandler(EventChangeMailer eventChangeMailer,
			SettingsService settingsService, UserService userService, Producer producer, Ical4jHelper ical4jHelper) {
		this.eventChangeMailer = eventChangeMailer;
		this.settingsService = settingsService;
		this.userService = userService;
		this.producer = producer;
		this.ical4jHelper = ical4jHelper;
	}
	
	public void create(final ObmUser user, final Event event, boolean notification) throws NotificationException {
		Attendee owner = findOwner(event);
		Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
		String ics = ical4jHelper.buildIcsInvitationRequest(user, event);
		writeIcs(ics);
		if (notification && eventCreationInvolveNotification(event)) {
			notifyCreate(user, attendees, event, ics);
		}
		if (notification && !eventCreatedByOwner(user, event)) {
			notifyOwnerCreate(user, event, owner);
		}
	}
	
	private void notifyOwnerCreate(ObmUser user, Event event, Attendee owner) {
		UserSettings settings = settingsService.getSettings(user);
		Locale locale = settings.locale();
		TimeZone timezone = settings.timezone();
		
		Collection<Attendee> ownerAsCollection = new ArrayList<Attendee>(1);
		ownerAsCollection.add(owner);
		eventChangeMailer.notifyAcceptedNewUsers(ownerAsCollection, event, locale, timezone);
	}

	private boolean eventCreatedByOwner(ObmUser user, Event event) {
		return user.getEmail().equals(event.getOwnerEmail());
	}

	private void writeIcs(String... ics)  {
		try {
			for (String s: ics) {
				producer.write(s);	
			}
		} catch (JMSException e) {
			throw new NotificationException(e);
		}
	}
	
	private void notifyCreate(final ObmUser user, final Collection<Attendee> attendees, 
			final Event event, String ics) throws NotificationException {
		
		UserSettings settings = settingsService.getSettings(user);
		Locale locale = settings.locale();
		TimeZone timezone = settings.timezone();
		
		Map<ParticipationState, Set<Attendee>> attendeeGroups = computeParticipationStateGroups(attendees);
		
		Set<Attendee> accepted = attendeeGroups.get(ParticipationState.ACCEPTED);
		if(accepted != null && !accepted.isEmpty()){
			eventChangeMailer.notifyAcceptedNewUsers(accepted, event, locale, timezone);
		}
		
		Set<Attendee> notAccepted = attendeeGroups.get(ParticipationState.NEEDSACTION);
		if (notAccepted != null && !notAccepted.isEmpty()) {
			eventChangeMailer.notifyNeedActionNewUsers(notAccepted, event, locale, timezone, ics);
		}
	}

	public void update(final ObmUser user, final Event previous, final Event current, boolean notification) throws NotificationException {
		
		String addUserIcs = ical4jHelper.buildIcsInvitationRequest(user, current);
		String removedUserIcs = ical4jHelper.buildIcsInvitationCancel(user, current);
		String updateUserIcs = ical4jHelper.buildIcsInvitationRequest(user, current);
		
		writeIcs(updateUserIcs);
		
		if (notification) {
			
			UserSettings settings = settingsService.getSettings(user);
			TimeZone timezone = settings.timezone();
			Locale locale = settings.locale();

			final Map<AttendeeStateValue, Set<Attendee>> attendeeGroups = computeUpdateNotificationGroups(previous, current);
			final Set<Attendee> removedAttendees = attendeeGroups.get(AttendeeStateValue.REMOVED);
			if (!removedAttendees.isEmpty()) {
				eventChangeMailer.notifyRemovedUsers(removedAttendees, current, locale, timezone, removedUserIcs);
			}

			final Set<Attendee> addedAttendees = attendeeGroups.get(AttendeeStateValue.ADDED);
			if (!addedAttendees.isEmpty()) {
				notifyCreate(user, addedAttendees, current, addUserIcs);
			}

			final Set<Attendee> keptAttendees = attendeeGroups.get(AttendeeStateValue.KEPT);
			if (!keptAttendees.isEmpty()) {
				final Map<ParticipationState, Set<Attendee>> atts = computeParticipationStateGroups(keptAttendees);
				notifyAcceptedUpdateUsers(previous, current, locale, atts, timezone, updateUserIcs);
				notifyNeedActionUpdateUsers(previous, current, locale, atts, timezone, updateUserIcs);
			}
			Attendee owner = findOwner(current);
			if (owner != null && !eventCreatedByOwner(user, current)) {
				notifyOwnerUpdate(owner, previous, current, locale, timezone);
			}
		}
	}
	
	private void notifyAcceptedUpdateUsers(Event previous, Event current, Locale locale, 
			Map<ParticipationState, ? extends Set<Attendee>> atts, TimeZone timezone, String ics) {
		
		final Set<Attendee> accepted = atts.get(ParticipationState.ACCEPTED);
		if (accepted != null && !accepted.isEmpty()) {
			eventChangeMailer.notifyAcceptedUpdateUsers(accepted, previous, current, locale, timezone, ics);
		}	
	}
	
	private void notifyOwnerUpdate(Attendee owner, Event previous, Event current, Locale locale, TimeZone timezone) {
		eventChangeMailer.notifyOwnerUpdate(owner, previous, current, locale, timezone);
	}
	
	private void notifyNeedActionUpdateUsers(Event previous, Event current,
			Locale locale, Map<ParticipationState, Set<Attendee>> atts,
					TimeZone timezone, String ics) { 
		
		logger.info("Listing all event attendees for event with name=[" + current.getTitle() + "]");
		for (Entry<ParticipationState, Set<Attendee>> attendeesByState : atts.entrySet()) {
			logger.info("Attendees in state=[" + attendeesByState.getKey().name() + "]");
			for (Attendee attendee : attendeesByState.getValue())
			{
				logger.info("<" + attendee.getEmail() + "> is in [" + attendee.getState().name() + "]");
			}
		}
		
		final Set<Attendee> notAccepted = atts.get(ParticipationState.NEEDSACTION);

		if (notAccepted != null && !notAccepted.isEmpty()) {
			eventChangeMailer.notifyNeedActionUpdateUsers(notAccepted, previous, current, locale, timezone, ics);
		}
	}

	public void delete(final ObmUser user, final Event event, boolean notification) throws NotificationException {
		String removeUserIcs = ical4jHelper.buildIcsInvitationCancel(user, event);
		writeIcs(removeUserIcs);
		
 		if (notification && eventDeletionInvolveNotification(event)) {
 			
 			Attendee owner = findOwner(event); 			
 			
 			Collection<Attendee> attendees = filterOwner(event, ensureAttendeeUnicity(event.getAttendees()));
			Map<ParticipationState, Set<Attendee>> attendeeGroups = computeParticipationStateGroups(attendees);
			Set<Attendee> notify = Sets.union(attendeeGroups.get(ParticipationState.NEEDSACTION), attendeeGroups.get(ParticipationState.ACCEPTED));
 			if (!notify.isEmpty()) {
 				UserSettings settings = settingsService.getSettings(user);
 				eventChangeMailer.notifyRemovedUsers(notify, event, settings.locale(), settings.timezone(), removeUserIcs);
 			}
 			if (owner != null && !eventCreatedByOwner(user, event)) {
 				notifyOwnerDelete(user, event, owner);
 			}
 		}
 	}
 	
	private void notifyOwnerDelete(ObmUser user, Event event, Attendee owner) {
		UserSettings settings = settingsService.getSettings(user);
		Locale locale = settings.locale();
		TimeZone timezone = settings.timezone();
		
		Collection<Attendee> ownerAsCollection = new ArrayList<Attendee>(1);
		ownerAsCollection.add(owner);
		eventChangeMailer.notifyOwnerRemovedEvent(owner, event, locale, timezone);		
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
	
	private Attendee findOwner(final Event event) {
		for (Attendee attendee : event.getAttendees()) {
			boolean isOwner = attendee.getEmail().equalsIgnoreCase(event.getOwnerEmail());
			if (isOwner)
				return attendee;
		}
		return null;
	}
	
	private Map<ParticipationState, Set<Attendee>> computeParticipationStateGroups(Collection<Attendee> attendees) {
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
	
	/* package */ Map<AttendeeStateValue, Set<Attendee>> computeUpdateNotificationGroups(Event previous, Event current) {
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

	/* package */ static enum AttendeeStateValue {
		ADDED, KEPT, REMOVED;
	}
	
	public void updateParticipationState(final Event event, final ObmUser calendarOwner, 
			final ParticipationState state, boolean notification) {
		
		String ics = ical4jHelper.buildIcsInvitationReply(event, calendarOwner);
		writeIcs(ics);
		
		if (notification) {
			if (isHandledParticipationState(state)) {
				final Attendee organizer = event.findOrganizer();
				if (organizer != null) {
					if (updateParticipationStateNeedsNotification(calendarOwner, organizer)) {
						UserSettings settings = settingsService.getSettings(calendarOwner);
						eventChangeMailer.notifyUpdateParticipationState(event, 
								organizer, calendarOwner, state, settings.locale(), 
								settings.timezone(), ics);
					}
				} else {
					logger.error("Can't find organizer, email won't send");
				}
			}
		}
	}
	
	private boolean updateParticipationStateNeedsNotification(
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
		return !ParticipationState.DECLINED.equals(organizer.getState());
	}

	private boolean organizerHasEmailAddress(final Attendee organizer) {
		return !StringUtils.isEmpty(organizer.getEmail());
	}
	
	private boolean isHandledParticipationState(final ParticipationState state) {
		if (state == null) {
			return false;
		}
		switch (state) {
		case ACCEPTED:
		case DECLINED:
			return true;
		default:
			return false;
		}
	}
	
}
