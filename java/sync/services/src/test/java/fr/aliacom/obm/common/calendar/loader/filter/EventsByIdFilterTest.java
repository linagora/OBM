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

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EventsByIdFilterTest {

	@Test
	public void testEventsByIdFilter() {
		Set<Integer> idsToFilter = Sets.newHashSet(3, 10, 6);

		Map<EventObmId, Event> expectedEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(newEntryWithId(1))
				.put(newEntryWithId(2))
				.put(newEntryWithId(4))
				.put(newEntryWithId(5))
				.put(newEntryWithId(7))
				.put(newEntryWithId(8))
				.put(newEntryWithId(9))
				.build();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(newEntryWithId(1))
				.put(newEntryWithId(2))
				.put(newEntryWithId(3))
				.put(newEntryWithId(4))
				.put(newEntryWithId(5))
				.put(newEntryWithId(6))
				.put(newEntryWithId(7))
				.put(newEntryWithId(8))
				.put(newEntryWithId(9))
				.put(newEntryWithId(10))
				.build();

		EventFilter filter = new EventsByIdFilter(idsToFilter);
		final Map<EventObmId, Event> filteredEvents = filter.filter(unfilteredEvents);
		assertThat(filteredEvents).isEqualTo(expectedEvents);
	}

	@Test
	public void testEventsByIdFilterNothing() {
		Set<Integer> idsToFilter = Sets.newHashSet(11, 0, 12);
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(newEntryWithId(1))
				.put(newEntryWithId(2))
				.put(newEntryWithId(3))
				.put(newEntryWithId(4))
				.put(newEntryWithId(5))
				.put(newEntryWithId(6))
				.put(newEntryWithId(7))
				.put(newEntryWithId(8))
				.put(newEntryWithId(9))
				.put(newEntryWithId(10))
				.build();

		EventFilter filter = new EventsByIdFilter(idsToFilter);
		final Map<EventObmId, Event> filteredEvents = filter.filter(unfilteredEvents);
		assertThat(filteredEvents).isEqualTo(unfilteredEvents);		
	}

	@Test
	public void testEventsByIdFilterNothingWithEmptyIdsToFilter() {
		Set<Integer> idsToFilter = Sets.newHashSet();
		Map<EventObmId, Event> unfilteredEvents = new ImmutableMap.Builder<EventObmId, Event>()
				.put(newEntryWithId(1))
				.put(newEntryWithId(2))
				.put(newEntryWithId(3))
				.put(newEntryWithId(4))
				.put(newEntryWithId(5))
				.put(newEntryWithId(6))
				.put(newEntryWithId(7))
				.put(newEntryWithId(8))
				.put(newEntryWithId(9))
				.put(newEntryWithId(10))
				.build();

		EventFilter filter = new EventsByIdFilter(idsToFilter);
		final Map<EventObmId, Event> filteredEvents = filter.filter(unfilteredEvents);
		assertThat(filteredEvents).isEqualTo(unfilteredEvents);		
	}

	private Map.Entry<EventObmId, Event> newEntryWithId(int id) {
		Event event = new Event();
		EventObmId eventId = new EventObmId(id);
		event.setUid(eventId);
		return Maps.immutableEntry(eventId, event);
	}
}
