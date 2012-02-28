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
package fr.aliacom.obm;

import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class ToolBox {

	public static AccessToken mockAccessToken() {
		ObmUser user = getDefaultObmUser();
		return mockAccessToken(user.getLogin(), user.getDomain());
	}

	public static AccessToken mockAccessToken(String login, ObmDomain domain) {
		AccessToken accessToken = EasyMock.createMock(AccessToken.class);
		EasyMock.expect(accessToken.getDomain()).andReturn(domain).atLeastOnce();
		EasyMock.expect(accessToken.getUserLogin()).andReturn(login).anyTimes();
		EasyMock.expect(accessToken.getOrigin()).andReturn("unittest").anyTimes();
		EasyMock.expect(accessToken.getConversationUid()).andReturn(1).anyTimes();
		return accessToken;
	}

	public static ObmUser mockObmUser(String userEmail, ObmDomain domain) {
		ObmUser user = EasyMock.createMock(ObmUser.class);
		EasyMock.expect(user.getEmail()).andReturn(userEmail).atLeastOnce();
		EasyMock.expect(user.getDomain()).andReturn(domain).anyTimes();
		return user;
	}

	public static ObmDomain getDefaultObmDomain() {
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName("test.tlse.lng");
		obmDomain.setUuid("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		return obmDomain;
	}

	public static ObmUser getDefaultObmUser() {
		ObmDomain obmDomain = getDefaultObmDomain();
		ObmUser obmUser = new ObmUser();
		obmUser.setFirstName("Obm");
		obmUser.setLastName("User");
		obmUser.setLogin("user");
		obmUser.setEmail("user@test");
		obmUser.setDomain(obmDomain);
		return obmUser;
	}

	public static Attendee getFakeAttendee(String userEmail) {
		Attendee att = new Attendee();
		att.setEmail(userEmail);
		return att;
	}

	public static List<Attendee> getFakeListOfAttendees() {
		Attendee beriaAttendee = ToolBox.getFakeAttendee("beria");
		beriaAttendee.setState(ParticipationState.NEEDSACTION);
		Attendee hooverAttendee = ToolBox.getFakeAttendee("hoover");
		hooverAttendee.setState(ParticipationState.NEEDSACTION);
		Attendee mccarthyAttendee = ToolBox.getFakeAttendee("mccarthy");
		mccarthyAttendee.setState(ParticipationState.NEEDSACTION);
		return Lists.newArrayList(beriaAttendee, hooverAttendee, mccarthyAttendee);
	}

	public static Event getFakeDailyRecurrentEvent(Date date, int sequence, Attendee... attendees) {
		Event event = new Event();
		event.setDate(date);
		event.setSequence(sequence);
		event.setExtId(new EventExtId("extId"));

		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		event.setRecurrence(recurrence);

		event.setAttendees(Lists.newArrayList(attendees));

		return event;
	}

	public static Event getFakeNegativeExceptionEvent(Event event, Date exceptionDate) {
		Event ex = event.clone();
		ex.setDate(exceptionDate);
		ex.setRecurrenceId(exceptionDate);
		ex.setRecurrence(new EventRecurrence());
		return ex;
	}
}
