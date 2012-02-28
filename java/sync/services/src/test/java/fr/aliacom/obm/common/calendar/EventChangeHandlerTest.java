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

import org.easymock.EasyMock;
import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;

import fr.aliacom.obm.ToolBox;

public class EventChangeHandlerTest {

	@Test
	public void testNegativeException() {
		Attendee attendee = ToolBox.getFakeAttendee("james.jesus.angleton@cia.gov");
		attendee.setState(ParticipationState.ACCEPTED);

		int previousSequence = 0;
		Date eventDate = after();
		AccessToken token = ToolBox.mockAccessToken();
		Event previousEvent = ToolBox.getFakeDailyRecurrentEvent(eventDate, previousSequence,
				attendee);
		previousEvent.setOwner(attendee.getEmail());

		int currentSequence = previousSequence + 1;
		Event currentEvent = ToolBox.getFakeDailyRecurrentEvent(eventDate, currentSequence,
				attendee);
		Date exceptionDate = new DateTime(eventDate).plusMonths(1).toDate();
		currentEvent.addException(exceptionDate);
		currentEvent.setOwner(attendee.getEmail());

		Event negativeExceptionEvent = ToolBox.getFakeNegativeExceptionEvent(currentEvent,
				exceptionDate);

		MessageQueueService jmsService = EasyMock.createMock(MessageQueueService.class);
		jmsService.writeIcsInvitationCancel(token, negativeExceptionEvent);

		EventNotificationService notifService = EasyMock.createMock(EventNotificationService.class);
		notifService.notifyDeletedEvent(negativeExceptionEvent, token);

		EasyMock.replay(token, jmsService, notifService);

		boolean notification = true;

		EventChangeHandler handler = new EventChangeHandler(jmsService, notifService);
		handler.update(previousEvent, currentEvent, notification, token);

		EasyMock.verify(jmsService, notifService);
	}
}
