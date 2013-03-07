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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;

import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class ToolBox {

	public static AccessToken mockAccessToken() {
		ObmUser user = getDefaultObmUser();
		
		return mockAccessToken(user.getLogin(), user.getDomain());
	}
	
	public static AccessToken mockAccessToken(IMocksControl mocksControl) {
		ObmUser user = getDefaultObmUser();
		
		return mockAccessToken(user.getLogin(), user.getDomain(), mocksControl);
	}
	
	public static AccessToken mockAccessToken(String login, ObmDomain domain) {
		AccessToken accessToken = EasyMock.createMock(AccessToken.class);
		
		return mockAccessTokenMethods(accessToken, login, domain);
	}

	public static AccessToken mockAccessToken(String login, ObmDomain domain, IMocksControl mocksControl) {
		AccessToken accessToken = mocksControl.createMock(AccessToken.class);
		
		return mockAccessTokenMethods(accessToken, login, domain);
	}
	
	private static AccessToken mockAccessTokenMethods(AccessToken accessToken, String login, ObmDomain domain) {
		EasyMock.expect(accessToken.getObmId()).andReturn(0).anyTimes();
		EasyMock.expect(accessToken.getDomain()).andReturn(domain).anyTimes();
		EasyMock.expect(accessToken.getUserLogin()).andReturn(login).anyTimes();
		EasyMock.expect(accessToken.getUserEmail()).andReturn(login + '@' + domain.getName()).anyTimes();
		EasyMock.expect(accessToken.getOrigin()).andReturn("unittest").anyTimes();
		EasyMock.expect(accessToken.getConversationUid()).andReturn(1).anyTimes();
		EasyMock.expect(accessToken.getSessionId()).andReturn("sessionId").anyTimes();
		EasyMock.expect(accessToken.getVersion()).andReturn(new MavenVersion("0", "0", "0")).anyTimes();
		
		return accessToken;
	}

	public static ObmUser mockObmUser(String userEmail, ObmDomain domain) {
		ObmUser user = EasyMock.createMock(ObmUser.class);
		EasyMock.expect(user.getEmail()).andReturn(userEmail).atLeastOnce();
		EasyMock.expect(user.getDomain()).andReturn(domain).anyTimes();
		return user;
	}

	public static ObmDomain getDefaultObmDomain() {
		return ObmDomain
				.builder()
				.name("test.tlse.lng")
				.uuid("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6")
				.build();
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
		return UserAttendee.builder().email(userEmail).build();
	}

	public static List<Attendee> getFakeListOfAttendees() {
		Attendee beriaAttendee = ToolBox.getFakeAttendee("beria");
		beriaAttendee.setParticipation(Participation.needsAction());
		Attendee hooverAttendee = ToolBox.getFakeAttendee("hoover");
		hooverAttendee.setParticipation(Participation.needsAction());
		Attendee mccarthyAttendee = ToolBox.getFakeAttendee("mccarthy");
		mccarthyAttendee.setParticipation(Participation.needsAction());
		return Lists.newArrayList(beriaAttendee, hooverAttendee, mccarthyAttendee);
	}

	public static Event getFakeDailyRecurrentEvent(Date date, int sequence, Attendee... attendees) {
		Event event = new Event();
		event.setStartDate(date);
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
		ex.setStartDate(exceptionDate);
		ex.setRecurrenceId(exceptionDate);
		ex.setRecurrence(new EventRecurrence());
		return ex;
	}
	
	public static Event getFakeEvent( int id){
		Event event = new Event();
		Date date = new Date();
		
		String evtid = Integer.toString(id);
		
		event.setExtId(new EventExtId("fake_extId_"+evtid));
		event.setTimeUpdate(date);
		event.setTimeCreate(date);
		event.setOpacity(EventOpacity.getValueOf("fake_event_opacity"));
		event.setCategory("fake_eventcategory1_label");
		event.setTitle("fake_event_title_"+evtid);
		event.setLocation("fake_event_location");
		event.setStartDate(date);
		event.setDuration(3600);
		event.setPriority(9);
		event.setDescription("fake_event_description");
		event.setAttendees(getFakeListOfAttendees());

		return event;
	}
	
	public static Collection<Event> getFakeEventCollection(int collectionSize){
		Collection<Event> collection = new ArrayList<Event>();
		for ( int i = 0 ; i < collectionSize; i++){
			collection.add(getFakeEvent(i));
		}
		return collection;
	}
	
}
