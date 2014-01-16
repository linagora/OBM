/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.calendar;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.items.EventChanges;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ConsistencyEventChangesLogger {

	public static class NotConsistentEventChanges {
		
		private final Map<EventObmId, Collection<Object>> duplicatesEntries;

		public NotConsistentEventChanges(Map<EventObmId, Collection<Object>> duplicatesEntries) {
			this.duplicatesEntries = ImmutableMap.copyOf(duplicatesEntries);
		}

		public Map<EventObmId, Collection<Object>> getDuplicatesEntries() {
			return duplicatesEntries;
		}

		public boolean hasConsistencyProblem() {
			return !duplicatesEntries.isEmpty();
		}

		public String representation() {
			StringBuilder representation = new StringBuilder();
			for (Entry<EventObmId, Collection<Object>> entry : duplicatesEntries.entrySet()) {
				representation
					.append("Id ")
					.append(entry.getKey().serializeToString())
					.append(" has duplicates entries : ")
					.append(entry.getValue().toString())
					.append("\n");
			}
			return representation.toString();
		}
	}
	
	@Inject
	@VisibleForTesting ConsistencyEventChangesLogger() {
		super();
	}
	
	public void log(Logger logger, EventChanges changes) {
		log(logger, build(changes));
	}

	@VisibleForTesting void log(Logger logger, NotConsistentEventChanges notConsistentEventChanges) {
		if (notConsistentEventChanges.hasConsistencyProblem()) {
			logger.error("There is some consistency problems on the given event changes.");
			logger.error(notConsistentEventChanges.representation());
		}
	}
	
	@VisibleForTesting NotConsistentEventChanges build(EventChanges changes) {
		Map<EventObmId, Collection<Object>> allEntries = buildAllEntriesMultimap(changes);
		Map<EventObmId, Collection<Object>> duplicatesEntries = Maps.filterEntries(allEntries, alLeastTwoElements());
		return new NotConsistentEventChanges(duplicatesEntries);
	}

	private Predicate<Entry<EventObmId, Collection<Object>>> alLeastTwoElements() {
		return new Predicate<Entry<EventObmId, Collection<Object>>>() {
			@Override
			public boolean apply(Entry<EventObmId, Collection<Object>> input) {
				return input.getValue().size() > 1;
			}
		};
	}

	private Map<EventObmId, Collection<Object>> buildAllEntriesMultimap(EventChanges changes) {
		return ImmutableListMultimap.<EventObmId, Object>builder()
				.putAll(buildDeletionsMultimap(changes))
				.putAll(buildUpdatesMultimap(changes))
				.build()
				.asMap();
	}

	private ImmutableListMultimap<EventObmId, Event> buildUpdatesMultimap(EventChanges changes) {
		return Multimaps.index(changes.getUpdated(), new Function<Event, EventObmId>() {
			@Override
			public EventObmId apply(Event input) {
				return input.getObmId();
			}
		});
	}

	private ListMultimap<EventObmId, DeletedEvent> buildDeletionsMultimap(EventChanges changes) {
		return Multimaps.index(changes.getDeletedEvents(), new Function<DeletedEvent, EventObmId>() {
			@Override
			public EventObmId apply(DeletedEvent input) {
				return input.getId();
			}
		});
	}
}
