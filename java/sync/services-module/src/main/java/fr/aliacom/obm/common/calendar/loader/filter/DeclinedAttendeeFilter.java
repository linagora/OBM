package fr.aliacom.obm.common.calendar.loader.filter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.Participation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DeclinedAttendeeFilter implements EventFilter {
	private Attendee declinedAttendee;

	public DeclinedAttendeeFilter(Attendee declinedAttendee) {
		this.declinedAttendee = declinedAttendee;
	}

	@Override
	public Map<EventObmId, Event> filter(Map<EventObmId, Event> events) {
		if (this.declinedAttendee == null) {
			return events;
		}

		Map<EventObmId, Event> filteredEventsById = Maps.newHashMap();
		for (Event event : events.values()) {
			Collection<Event> filteredEvents = event.isRecurrent() ? filterRecurrentEvent(event)
					: filterNonRecurrentEvent(event);
			for (Event filteredEvent : filteredEvents) {
				filteredEventsById.put(filteredEvent.getObmId(), filteredEvent);
			}
		}
		return filteredEventsById;
	}

	private Collection<Event> filterNonRecurrentEvent(Event event) {
		Attendee att = event.findAttendeeFromEmail(declinedAttendee.getEmail());
		if (!Participation.declined().equals(att.getParticipation())) {
			return ImmutableList.of(event);
		} else {
			return ImmutableList.of();
		}
	}

	private Set<Event> filterRecurrentEvent(Event event) {
		event.getRecurrence().replaceUnattendedEventExceptionByException(
				this.declinedAttendee.getEmail());

		Attendee att = event.findAttendeeFromEmail(this.declinedAttendee.getEmail());
		boolean isParentEventDeclined = att == null
				|| Participation.declined().equals(att.getParticipation());
		if (isParentEventDeclined) {
			return event.getEventsExceptions();
		} else {
			return Sets.newHashSet(event);
		}
	}

}
