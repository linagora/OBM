/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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
