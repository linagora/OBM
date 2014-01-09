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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.calendar.ConsistencyEventChangesLogger;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.calendar.CalendarItemsParser;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.push.calendar.ConsistencyEventChangesLogger.NotConsistentEventChanges;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;



public class ConsistencyEventChangesLoggerTest {

	private Logger logger;
	private ConsistencyEventChangesLogger consistencyEventChangesLogger;

	@Before
	public void setUp() {
		logger = createStrictMock(Logger.class);
		consistencyEventChangesLogger = new ConsistencyEventChangesLogger();
	}
	
	@Test
	public void testOnEmtpyChanges() {
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.participationChanges(ImmutableList.<ParticipationChanges>of())
			.updates(ImmutableList.<Event>of())
			.deletes(ImmutableList.<DeletedEvent>of())
			.build();

		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);
		
		assertThat(duplicateChanges.getDuplicatesEntries()).isEmpty();
	}

	@Test
	public void testOnNoDuplicateUpdate() {
		Event event1 = new Event();
		event1.setUid(new EventObmId(1));
		event1.setExtId(new EventExtId("a"));
		
		Event event2 = new Event();
		event2.setUid(new EventObmId(2));
		event2.setExtId(new EventExtId("b"));
		
		Event event3 = new Event();
		event3.setUid(new EventObmId(3));
		event3.setExtId(new EventExtId("c"));

		Event event4 = new Event();
		event4.setUid(new EventObmId(4));
		event4.setExtId(new EventExtId("d"));
		
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of(event1, event2, event3, event4))
			.deletes(ImmutableList.<DeletedEvent>of())
			.build();

		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);
		
		assertThat(duplicateChanges.getDuplicatesEntries()).isEmpty();
	}

	@Test
	public void testOnNoDuplicateDelete() {
		DeletedEvent event1 = DeletedEvent.builder().eventObmId(1).eventExtId("a").build();
		DeletedEvent event2 = DeletedEvent.builder().eventObmId(2).eventExtId("b").build();
		DeletedEvent event3 = DeletedEvent.builder().eventObmId(3).eventExtId("c").build();
		DeletedEvent event4 = DeletedEvent.builder().eventObmId(4).eventExtId("d").build();
		
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of())
			.deletes(ImmutableList.<DeletedEvent>of(event1, event2, event3, event4))
			.build();

		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);
		
		assertThat(duplicateChanges.getDuplicatesEntries()).isEmpty();
	}

	@Test
	public void testOnDuplicateUpdate() {
		Event event1 = new Event();
		event1.setUid(new EventObmId(1));
		event1.setExtId(new EventExtId("a"));
		
		Event event2 = new Event();
		event2.setUid(new EventObmId(2));
		event2.setExtId(new EventExtId("b"));
		
		Event event3 = new Event();
		event3.setUid(new EventObmId(3));
		event3.setExtId(new EventExtId("c"));

		Event event4 = new Event();
		event4.setUid(new EventObmId(4));
		event4.setExtId(new EventExtId("d"));

		Event event2Again = new Event();
		event2Again.setUid(new EventObmId(2));
		event2Again.setExtId(new EventExtId("b"));
		
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of(event1, event2, event3, event4, event2Again))
			.deletes(ImmutableList.<DeletedEvent>of())
			.build();

		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);
		
		assertThat(duplicateChanges.getDuplicatesEntries()).isEmpty();
	}

	@Test
	public void testOnDuplicateUpdateWhenOnlyUid() {
		Event event1 = new Event();
		event1.setUid(new EventObmId(1));
		event1.setExtId(new EventExtId("a"));
		
		Event event2 = new Event();
		event2.setUid(new EventObmId(2));
		event2.setExtId(new EventExtId("b"));
		
		Event event3 = new Event();
		event3.setUid(new EventObmId(3));
		event3.setExtId(new EventExtId("c"));

		Event event4 = new Event();
		event4.setUid(new EventObmId(4));
		event4.setExtId(new EventExtId("d"));

		Event event2Again = new Event();
		event2Again.setUid(new EventObmId(2));
		event2Again.setExtId(new EventExtId("e"));
		
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of(event1, event2, event3, event4, event2Again))
			.deletes(ImmutableList.<DeletedEvent>of())
			.build();

		logger.error(anyObject(String.class));
		expectLastCall().times(2);
		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);

		assertThat(duplicateChanges.getDuplicatesEntries()).hasSize(1);
		assertThat(duplicateChanges.getDuplicatesEntries().get(new EventObmId(2)))
			.containsOnly(event2, event2Again);
	}

	@Test
	public void testOnDuplicateDelete() {
		DeletedEvent event1 = DeletedEvent.builder().eventObmId(1).eventExtId("a").build();
		DeletedEvent event2 = DeletedEvent.builder().eventObmId(2).eventExtId("b").build();
		DeletedEvent event3 = DeletedEvent.builder().eventObmId(3).eventExtId("c").build();
		DeletedEvent event4 = DeletedEvent.builder().eventObmId(4).eventExtId("d").build();
		DeletedEvent event2Again = DeletedEvent.builder().eventObmId(2).eventExtId("b").build();
		
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of())
			.deletes(ImmutableList.<DeletedEvent>of(event1, event2, event3, event4, event2Again))
			.build();

		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);

		assertThat(duplicateChanges.getDuplicatesEntries()).isEmpty();
	}

	@Test
	public void testOnDuplicateDeleteWhenOnlyUid() {
		DeletedEvent event1 = DeletedEvent.builder().eventObmId(1).eventExtId("a").build();
		DeletedEvent event2 = DeletedEvent.builder().eventObmId(2).eventExtId("b").build();
		DeletedEvent event3 = DeletedEvent.builder().eventObmId(3).eventExtId("c").build();
		DeletedEvent event4 = DeletedEvent.builder().eventObmId(4).eventExtId("d").build();
		DeletedEvent event2Again = DeletedEvent.builder().eventObmId(2).eventExtId("e").build();
		
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of())
			.deletes(ImmutableList.<DeletedEvent>of(event1, event2, event3, event4, event2Again))
			.build();

		logger.error(anyObject(String.class));
		expectLastCall().times(2);
		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);

		assertThat(duplicateChanges.getDuplicatesEntries()).hasSize(1);
		assertThat(duplicateChanges.getDuplicatesEntries().get(new EventObmId(2)))
			.containsOnly(event2, event2Again);
	}

	@Test
	public void testOnDuplicateWhenNoDeletedUidIsInUpdatedUid() {
		DeletedEvent eventDeleted1 = DeletedEvent.builder().eventObmId(1).eventExtId("a").build();
		DeletedEvent eventDeleted2 = DeletedEvent.builder().eventObmId(2).eventExtId("b").build();
		DeletedEvent eventDeleted3 = DeletedEvent.builder().eventObmId(3).eventExtId("c").build();
		DeletedEvent eventDeleted4 = DeletedEvent.builder().eventObmId(4).eventExtId("d").build();
		
		Event event1 = new Event();
		event1.setUid(new EventObmId(5));
		event1.setExtId(new EventExtId("e"));

		Event event2 = new Event();
		event2.setUid(new EventObmId(6));
		event2.setExtId(new EventExtId("f"));

		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of(event1, event2))
			.deletes(ImmutableList.<DeletedEvent>of(eventDeleted1, eventDeleted2, eventDeleted3, eventDeleted4))
			.build();

		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);

		assertThat(duplicateChanges.getDuplicatesEntries()).isEmpty();
	}

	@Test
	public void testOnDuplicateWhenADeletedUidIsAlsoInUpdatedUid() {
		DeletedEvent eventDeleted1 = DeletedEvent.builder().eventObmId(1).eventExtId("a").build();
		DeletedEvent eventDeleted2 = DeletedEvent.builder().eventObmId(2).eventExtId("b").build();
		DeletedEvent eventDeleted3 = DeletedEvent.builder().eventObmId(3).eventExtId("c").build();
		DeletedEvent eventDeleted4 = DeletedEvent.builder().eventObmId(4).eventExtId("d").build();
		
		Event event1 = new Event();
		event1.setUid(new EventObmId(1));
		event1.setExtId(new EventExtId("e"));

		Event event2 = new Event();
		event2.setUid(new EventObmId(5));
		event2.setExtId(new EventExtId("f"));

		Event event3 = new Event();
		event3.setUid(new EventObmId(4));
		event3.setExtId(new EventExtId("g"));
		
		EventChanges eventChanges = EventChanges.builder()
			.lastSync(date("2012-01-01T11:22:33"))
			.updates(ImmutableList.<Event>of(event1, event2, event3))
			.deletes(ImmutableList.<DeletedEvent>of(eventDeleted1, eventDeleted2, eventDeleted3, eventDeleted4))
			.build();

		logger.error(anyObject(String.class));
		expectLastCall().times(2);
		replay(logger);
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);
		consistencyEventChangesLogger.log(logger, duplicateChanges);
		verify(logger);

		assertThat(duplicateChanges.getDuplicatesEntries()).hasSize(2);
		assertThat(duplicateChanges.getDuplicatesEntries().get(new EventObmId(1)))
			.containsOnly(event1, eventDeleted1);
		assertThat(duplicateChanges.getDuplicatesEntries().get(new EventObmId(4)))
			.containsOnly(event3, eventDeleted4);
	}
	
	@Test
	public void testOnRealEventChangesDump() throws Exception {
		InputStream dumpStream = ClassLoader.getSystemClassLoader().getResourceAsStream("data/notConsistentEventChanges.xml");
		Document dump = DOMUtils.parse(dumpStream);
		EventChanges eventChanges = new CalendarItemsParser().parseChanges(dump);
		
		NotConsistentEventChanges duplicateChanges = consistencyEventChangesLogger.build(eventChanges);

		assertThat(duplicateChanges.getDuplicatesEntries()).isEmpty();
	}
}
