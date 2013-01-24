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
package fr.aliacom.obm.common.calendar;

import static fr.aliacom.obm.common.calendar.EventNotificationServiceTestTools.after;

import java.util.Date;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.Participation;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;

import org.obm.filter.SlowFilterRunner;

import com.google.common.collect.Iterables;

@RunWith(SlowFilterRunner.class)
public class EventChangeHandlerTest {


	private AccessToken token;
	private Event previousEvent;
	private MessageQueueService jmsService;
	private EventNotificationService eventNotificationService;

	@Before
	public void setUp() {
		token = ToolBox.mockAccessToken();
		previousEvent = getFakePreviousEvent();
		jmsService = createMock(MessageQueueService.class);
		eventNotificationService = createMock(EventNotificationService.class);
	}

	@Test
	public void testNegativeException() {
		Event currentEvent = previousEvent.clone();
		Date exceptionDate = new DateTime(after()).plusMonths(1).toDate();
		currentEvent.addException(exceptionDate);

		Event negativeExceptionEvent = ToolBox.getFakeNegativeExceptionEvent(currentEvent, exceptionDate);

		jmsService.writeIcsInvitationCancel(token, negativeExceptionEvent);
		eventNotificationService.notifyDeletedEvent(negativeExceptionEvent, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentEvent, jmsService, eventNotificationService);
	}

	@Test
	public void testAddedEventException() {
		previousEvent.addEventException(previousEvent.getOccurrence(previousEvent.getStartDate()));

		Event currentEvent = previousEvent.clone();
		Event AddedEventException = currentEvent.getOccurrence(new Date());
		currentEvent.addEventException(AddedEventException);

		jmsService.writeIcsInvitationRequest(token, AddedEventException);
		eventNotificationService.notifyUpdatedEvent(AddedEventException, AddedEventException, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentEvent, jmsService, eventNotificationService);	
	}

	@Test
	public void testUpdateParentEventAndAddAnEventException() {
		previousEvent.addEventException(previousEvent.getOccurrence(previousEvent.getStartDate()));

		Event currentEvent = previousEvent.clone();
		Event AddedEventException = currentEvent.getOccurrence(new Date());
		currentEvent.addEventException(AddedEventException);

		currentEvent.setLocation("a new location");

		jmsService.writeIcsInvitationRequest(token, currentEvent);
		eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, token);

		jmsService.writeIcsInvitationRequest(token, AddedEventException);
		eventNotificationService.notifyUpdatedEvent(AddedEventException, AddedEventException, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentEvent, jmsService, eventNotificationService);		
	}

	@Test
	public void testModifiedEventException() {
		Event previousEventException = previousEvent.getOccurrence(previousEvent.getStartDate());
		previousEvent.addEventException(previousEventException);

		Event currentParentEvent = previousEvent.clone();

		Event modifiedEventException = Iterables.getOnlyElement(currentParentEvent.getEventsExceptions());
		modifiedEventException.setLocation("a new Location");

		jmsService.writeIcsInvitationRequest(token, modifiedEventException);
		eventNotificationService.notifyUpdatedEvent(previousEventException, modifiedEventException, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentParentEvent, jmsService, eventNotificationService);	
	}

	@Test
	public void testUpdateParentEventAndModifiedAnEventException() {
		Event previousEventException = previousEvent.getOccurrence(previousEvent.getStartDate());
		previousEvent.addEventException(previousEventException);

		Event currentEvent = previousEvent.clone();

		Event modifiedEventException = Iterables.getOnlyElement(currentEvent.getEventsExceptions());
		modifiedEventException.setLocation("a new Location");

		currentEvent.setLocation("a new location");

		jmsService.writeIcsInvitationRequest(token, currentEvent);
		eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, token);

		jmsService.writeIcsInvitationRequest(token, modifiedEventException);
		eventNotificationService.notifyUpdatedEvent(previousEventException, modifiedEventException, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentEvent, jmsService, eventNotificationService);	
	}

	@Test
	public void testDeletedEventException() {
		previousEvent.addEventException(previousEvent.getOccurrence(previousEvent.getStartDate()));

		Event currentParentEvent = previousEvent.clone();
		Set<Event> eventsExceptions = currentParentEvent.getEventsExceptions();
		Event deletedEventException = Iterables.getOnlyElement(eventsExceptions);
		eventsExceptions.remove(deletedEventException);

		jmsService.writeIcsInvitationCancel(token, deletedEventException);
		eventNotificationService.notifyDeletedEvent(deletedEventException, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentParentEvent, jmsService, eventNotificationService);		
	}

	@Test
	public void testUpdateParentEventAndDeleteEventException() {
		previousEvent.addEventException(previousEvent.getOccurrence(previousEvent.getStartDate()));

		Event currentEvent = previousEvent.clone();
		Set<Event> eventsExceptions = currentEvent.getEventsExceptions();
		Event deletedEventException = Iterables.getOnlyElement(eventsExceptions);
		eventsExceptions.remove(deletedEventException);

		currentEvent.setLocation("a new location");

		jmsService.writeIcsInvitationRequest(token, currentEvent);
		eventNotificationService.notifyUpdatedEvent(previousEvent, currentEvent, token);

		jmsService.writeIcsInvitationCancel(token, deletedEventException);
		eventNotificationService.notifyDeletedEvent(deletedEventException, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentEvent, jmsService, eventNotificationService);		
	}

	@Test
	public void testUpdateOnlyParentEvent() {
		previousEvent.addEventException(previousEvent.getOccurrence(previousEvent.getStartDate()));

		Event currentParentEvent = previousEvent.clone();
		currentParentEvent.setLocation("a location");

		jmsService.writeIcsInvitationRequest(token, currentParentEvent);
		eventNotificationService.notifyUpdatedEvent(previousEvent, currentParentEvent, token);

		verifyEventChangeHandlerUpdate(token, previousEvent, currentParentEvent, jmsService, eventNotificationService);
	}

	@Test
	public void testOBMFULL4510updateParticipation() {
		ObmUser calendarOwner = new ObmUser();

		jmsService.writeIcsInvitationReply(token, previousEvent, calendarOwner);
		expectLastCall().once();
		eventNotificationService.notifyUpdatedParticipationAttendees(previousEvent, calendarOwner, Participation.accepted(), token);
		expectLastCall().once();

		replay(token, jmsService, eventNotificationService);
		EventChangeHandler handler = new EventChangeHandler(jmsService, eventNotificationService);
		handler.updateParticipation(previousEvent, calendarOwner, Participation.accepted(), true, token);
		verify(jmsService, eventNotificationService);
	}

	private Event getFakePreviousEvent() {
		Attendee attendee = ToolBox.getFakeAttendee("james.jesus.angleton@cia.gov");
		Date eventDate = after();
		Event previousEvent = ToolBox.getFakeDailyRecurrentEvent(eventDate, 0, attendee);
		previousEvent.setOwner(attendee.getEmail());
		return previousEvent;
	}

	private void verifyEventChangeHandlerUpdate(AccessToken token, Event previousEvent, Event currentEvent,
			MessageQueueService jmsService, EventNotificationService eventNotificationService) {
		replay(token, jmsService, eventNotificationService);

		EventChangeHandler handler = new EventChangeHandler(jmsService, eventNotificationService);
		handler.update(previousEvent, currentEvent, true, token);

		verify(jmsService, eventNotificationService);
	}
}
