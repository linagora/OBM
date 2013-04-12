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
package org.obm.sync.items;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.calendar.Anonymizable;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventPrivacy;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public final class EventChanges implements Anonymizable<EventChanges> {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private Iterable<DeletedEvent> deletes;
		private Iterable<Event> updates;
		private Iterable<ParticipationChanges> participationChanges;
		private Date lastSync;

		private Builder() {
			super();
		}
		
		public Builder deletes(Iterable<DeletedEvent> deletes) {
			this.deletes = deletes;
			return this;
		}
		
		public Builder updates(Iterable<Event> updates) {
			this.updates = updates;
			return this;
		}
		
		public Builder participationChanges(Iterable<ParticipationChanges> participationChanges) {
			this.participationChanges = participationChanges;
			return this;
		}
		
		public Builder lastSync(Date lastSync) {
			this.lastSync = lastSync;
			return this;
		}
		
		public EventChanges build() {
			Preconditions.checkState(lastSync != null);
			return new EventChanges(
					Objects.firstNonNull(deletes, ImmutableList.<DeletedEvent>of()),
					Objects.firstNonNull(updates, ImmutableList.<Event>of()),
					Objects.firstNonNull(participationChanges, ImmutableList.<ParticipationChanges>of()),
					lastSync
					);
		}
		
	}
	
	private final Set<DeletedEvent> deletedEvents;
	private final List<Event> updatedEvents;
	private final List<ParticipationChanges> participationUpdated;
	private final Date lastSync;

	public EventChanges() {
		this(Lists.<DeletedEvent>newArrayList(), Lists.<Event>newArrayList(), Lists.<ParticipationChanges>newArrayList(), null);
	}
	
	private EventChanges(Iterable<DeletedEvent> deletes, Iterable<Event> updates,
						Iterable<ParticipationChanges> participationChanges, Date lastSync) {
		this.deletedEvents = ImmutableSet.copyOf(deletes);
		this.updatedEvents = Lists.newArrayList(updates);
		this.participationUpdated = Lists.newArrayList(participationChanges);
		this.lastSync = lastSync;
	}

	public Set<DeletedEvent> getDeletedEvents() {
		return deletedEvents;
	}

	public List<Event> getUpdated() {
		return updatedEvents;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public List<ParticipationChanges> getParticipationUpdated() {
		return participationUpdated;
	}

	@Override
	public EventChanges anonymizePrivateItems() {
		List<Event> anonymizedUpdatedEvents = Lists.transform(this.updatedEvents,
				new Function<Event, Event>() {
					@Override
					public Event apply(Event event) {
						return event.anonymizePrivateItems();
					}

				});
		
		return EventChanges.builder()
					.lastSync(this.lastSync)
					.deletes(this.deletedEvents)
					.updates(anonymizedUpdatedEvents)
					.participationChanges(this.participationUpdated)
					.build();

	}
	
	public EventChanges removeNotAllowedConfidentialEvents(String loggedUserEmail) {
		List<Event> updatedEvents = Lists.<Event>newArrayList();
		Set<DeletedEvent> deletedEvents = Sets.<DeletedEvent>newHashSet(this.deletedEvents);
		
		for (Event event: this.updatedEvents) {
			if (hasAccessToConfidentialEvent(loggedUserEmail, event)) {
				deletedEvents.add(
						DeletedEvent.builder()
							.eventObmId(event.getObmId().getObmId())
							.eventExtId(event.getExtId().getExtId())
							.build());
			} else {
				updatedEvents.add(event);
			}
		}
		
		return EventChanges.builder()
					.lastSync(this.lastSync)
					.deletes(deletedEvents)
					.updates(updatedEvents)
					.participationChanges(this.participationUpdated)
					.build();
	}

	private boolean hasAccessToConfidentialEvent(String loggedUserEmail,
			Event event) {
		return event.getPrivacy().equals(EventPrivacy.CONFIDENTIAL)
				&& !loggedUserEmail.equals(event.getOwnerEmail())
				&& event.findAttendeeFromEmail(loggedUserEmail) == null;
	}

	@Override
	public final boolean equals(Object other) {
		if (other instanceof EventChanges) {
			EventChanges otherChanges = (EventChanges) other;
			return Objects.equal(this.lastSync, otherChanges.lastSync)
					&& Objects.equal(this.deletedEvents, otherChanges.deletedEvents)
					&& Objects.equal(this.participationUpdated, otherChanges.participationUpdated)
					&& Objects.equal(this.updatedEvents, otherChanges.updatedEvents);
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(this.lastSync, this.deletedEvents, this.participationUpdated,
				this.updatedEvents);
	}
}
