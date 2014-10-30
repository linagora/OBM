/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Comment;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.Participation.State;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.UserAttendee;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;

public class ParticipationServiceTest {

	IMocksControl mocks;
	CalendarDao calendarDao;
	ParticipationService testee;
	
	ObmUser calendarOwner;
	AccessToken token;

	@Before
	public void setUp() {
		mocks = createControl();
		calendarDao = mocks.createMock(CalendarDao.class);
		testee = new ParticipationService(calendarDao);
		
		calendarOwner = ToolBox.getDefaultObmUser();
		token = new AccessToken(0, "origin");
	}

	@Test
	public void changeOnEventShouldResetComment() throws Exception {
		EventExtId extId = new EventExtId("0000");
		Event currentEvent = new Event();
		currentEvent.setSequence(0);
		currentEvent.addAttendee(UserAttendee.builder()
				.asAttendee()
				.email(calendarOwner.getEmail())
				.participation(Participation.needsAction()).build());
		Participation participation = Participation.builder()
				.state(State.ACCEPTED)
				.comment("a comment").build();
		
		expect(calendarDao.changeParticipation(token, calendarOwner, extId, participation)).andReturn(true);

		mocks.replay();
		boolean changed = testee.changeOnEvent(token, extId, participation, 0, calendarOwner, currentEvent);
		mocks.verify();

		assertThat(changed).isTrue();
		assertThat(participation.getComment()).isEqualTo(Comment.EMPTY);
	}

	@Test
	public void changeOnEventShouldDoNothinIfSameState() throws Exception {
		EventExtId extId = new EventExtId("0000");
		Event currentEvent = new Event();
		currentEvent.setSequence(0);
		currentEvent.addAttendee(UserAttendee.builder()
				.asAttendee()
				.email(calendarOwner.getEmail())
				.participation(Participation.accepted()).build());
		Participation participation = Participation.builder()
				.state(State.ACCEPTED)
				.comment("a comment").build();
		
		mocks.replay();
		boolean changed = testee.changeOnEvent(token, extId, participation, 0, calendarOwner, currentEvent);
		mocks.verify();

		assertThat(changed).isFalse();
		assertThat(participation.getComment()).isEqualTo(new Comment("a comment"));
	}

	@Test
	public void changeOnOccurrenceShouldResetComment() throws Exception {
		RecurrenceId recurrenceId = new RecurrenceId("recId");
		EventExtId extId = new EventExtId("0000");
		Event currentEvent = new Event();
		currentEvent.setSequence(0);
		currentEvent.addAttendee(UserAttendee.builder()
				.asAttendee()
				.email(calendarOwner.getEmail())
				.participation(Participation.needsAction())
				.build());
		Participation participation = Participation.builder()
				.state(State.ACCEPTED)
				.comment("a comment")
				.build();

		expect(calendarDao.changeParticipation(token, calendarOwner, extId, recurrenceId , participation)).andReturn(true);

		mocks.replay();
		boolean changed = testee.changeOnOccurrence(token, extId, recurrenceId, participation, 0, calendarOwner, currentEvent);
		mocks.verify();

		assertThat(changed).isTrue();
		assertThat(participation.getComment()).isEqualTo(Comment.EMPTY);
	}

	@Test
	public void changeOnOccurrenceShouldDoNothinIfSameState() throws Exception {
		RecurrenceId recurrenceId = new RecurrenceId("recId");
		EventExtId extId = new EventExtId("0000");
		Event currentEvent = new Event();
		currentEvent.setSequence(0);
		currentEvent.addAttendee(UserAttendee.builder()
				.asAttendee()
				.email(calendarOwner.getEmail())
				.participation(Participation.accepted())
				.build());
		Participation participation = Participation.builder()
				.state(State.ACCEPTED)
				.comment("a comment")
				.build();
		
		mocks.replay();
		boolean changed = testee.changeOnOccurrence(token, extId, recurrenceId, participation, 0, calendarOwner, currentEvent);
		mocks.verify();

		assertThat(changed).isFalse();
		assertThat(participation.getComment()).isEqualTo(new Comment("a comment"));
	}
}
