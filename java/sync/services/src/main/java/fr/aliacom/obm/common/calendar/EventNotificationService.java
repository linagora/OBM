package fr.aliacom.obm.common.calendar;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;

import fr.aliacom.obm.common.user.ObmUser;

public interface EventNotificationService {

	void notifyUpdatedEvent(Event previous, Event current, AccessToken token);

	void notifyCreatedEvent(Event event, AccessToken token);

	void notifyDeletedEvent(Event event, AccessToken token);

	void notifyUpdatedParticipationStateAttendees(Event event, ObmUser calendarOwner, ParticipationState state, AccessToken token);

}
