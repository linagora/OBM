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

import static fr.aliacom.obm.ToolBox.getDefaultObmUser;
import static fr.aliacom.obm.ToolBox.getDefaultProducer;
import static fr.aliacom.obm.ToolBox.getDefaultSettingsService;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.after;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.before;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.createRequiredAttendee;
import static fr.aliacom.obm.common.calendar.EventChangeHandlerTestsTools.createRequiredAttendees;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.obm.icalendar.Ical4jHelper;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;
import org.obm.sync.server.mailer.EventChangeMailer;

import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;


public abstract class AbstractEventChangeHandlerTest {

	private static final String ICSDATA = "ics data";
	
	public AbstractEventChangeHandlerTest() {
		super();
	}

	private EventChangeHandler newEventChangeHandler(EventChangeMailer mailer) {
		UserService userService = EasyMock.createMock(UserService.class);
		SettingsService defaultSettingsService = getDefaultSettingsService();
		Producer producer = getDefaultProducer();
		EasyMock.replay(userService, defaultSettingsService);
		Ical4jHelper ical4jHelper = EasyMock.createMock(Ical4jHelper.class);
		return new EventChangeHandler(mailer, defaultSettingsService, userService, producer, ical4jHelper);
	}
	
	protected abstract void processEvent(EventChangeHandler eventChangeHandler, Event event, ObmUser obmUser) throws NotificationException;
	
	public void testDefaultEvent()  {
	
		Event event = new Event();
		event.setDate(after());
		ObmUser defaultObmUser = getDefaultObmUser();
		
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}

	public void testNoAttendee() {
		Event event = new Event();
		event.setDate(after());
		event.setAttendees(new ArrayList<Attendee>());
		ObmUser defaultObmUser = getDefaultObmUser();
		
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}

	public void testOnlyOwnerIsAttendee() {
		ObmUser defaultObmUser = getDefaultObmUser();
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}

	public void testObmUserIsNotOwner() {
		ObmUser defaultObmUser = getDefaultObmUser();
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@domain.net";
		Attendee owner = createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED);
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(owner);
		
		EventChangeMailer mailer = expectationObmUserIsNotOwner(defaultObmUser, owner);

		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}
	
	public void testEventInThePast() {
		ObmUser defaultObmUser = getDefaultObmUser();
		Event event = new Event();
		event.setDate(before());
		event.setOwnerEmail("user@test");
		event.addAttendee(createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED));
		EventChangeMailer mailer = EasyMock.createMock(EventChangeMailer.class);
		EasyMock.replay(mailer);
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}
	
	public void testAcceptedAttendee() {
		Attendee attendeeAccepted = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeAccepted);

		ObmUser obmUSer = getDefaultObmUser();
		EventChangeMailer mailer = expectationAcceptedAttendees(attendeeAccepted, event, obmUSer);
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event, obmUSer);
		EasyMock.verify(mailer);
	}
	
	protected abstract EventChangeMailer expectationAcceptedAttendees(Attendee attendeeAccepted, Event event, ObmUser obmUser);
	
	protected abstract EventChangeMailer expectationObmUserIsNotOwner(ObmUser synchronizer, Attendee owner);
	
	public void testNeedActionAttendee() {
		Attendee attendeeNeedAction = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeNeedAction);

		ObmUser defaultObmUser = getDefaultObmUser();
		EventChangeMailer mailer = expectationNeedActionAttendees(attendeeNeedAction, ICSDATA, event, defaultObmUser);
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}
	
	protected abstract EventChangeMailer expectationNeedActionAttendees(Attendee attendeeNeedAction, String icsData, Event event, ObmUser obmUser);
	
	public void testDeclinedAttendee() {
		Attendee attendeeDeclined = createRequiredAttendee("attendee1@test", ParticipationState.DECLINED);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeDeclined);

		ObmUser defaultObmUser = getDefaultObmUser();
		
		EventChangeMailer mailer = expectationDeclinedAttendees(attendeeDeclined, event, defaultObmUser);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}
	
	protected abstract EventChangeMailer expectationDeclinedAttendees(Attendee attendeeDeclined, Event event, ObmUser obmUser);

	public void testTwoAttendee() {
		Attendee attendeeAccepted = createRequiredAttendee("attendee1@test", ParticipationState.ACCEPTED);
		
		Attendee attendeeNotAccepted = createRequiredAttendee("attendee2@test", ParticipationState.NEEDSACTION);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeAccepted);
		event.addAttendee(attendeeNotAccepted);
		
		ObmUser defaultObmUser = getDefaultObmUser();
		EventChangeMailer mailer = expectationTwoAttendees(attendeeAccepted, attendeeNotAccepted, event, defaultObmUser);
	
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationTwoAttendees(Attendee attendeeAccepted, Attendee attendeeNotAccepted, Event event,
			ObmUser obmUser);

	public void testSameAttendeeTwice() {
		Attendee attendeeOne = createRequiredAttendee("attendee1@test", ParticipationState.NEEDSACTION);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendee(attendeeOne);
		event.addAttendee(attendeeOne);
		
		ObmUser defaultObmUser = getDefaultObmUser();
		EventChangeMailer mailer = expectationSameAttendeeTwice(attendeeOne, event, defaultObmUser);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationSameAttendeeTwice(Attendee attendee, Event event, ObmUser defaultObmUser);

	
	public void testManyAttendees() {
		List<Attendee> accpetedAttendees = createRequiredAttendees("attendee", "@test", ParticipationState.ACCEPTED,0,  5);
		List<Attendee> needActionAttendees = createRequiredAttendees("attendee", "@test", ParticipationState.NEEDSACTION, 5, 5);
		
		Event event = new Event();
		event.setDate(after());
		String ownerEmail = "user@test";
		event.setOwnerEmail(ownerEmail);
		event.addAttendee(createRequiredAttendee(ownerEmail, ParticipationState.ACCEPTED));
		event.addAttendees(needActionAttendees);
		event.addAttendees(accpetedAttendees);
		
		ObmUser defaultObmUser = getDefaultObmUser();
		EventChangeMailer mailer = expectationManyAttendee(needActionAttendees, accpetedAttendees, event, defaultObmUser);
		
		EventChangeHandler eventChangeHandler = newEventChangeHandler(mailer);
		processEvent(eventChangeHandler, event, defaultObmUser);
		EasyMock.verify(mailer);
	}

	protected abstract EventChangeMailer expectationManyAttendee(List<Attendee> needActionAttendees, List<Attendee> accpetedAttendees, Event event, ObmUser defaultObmUser);
	
}