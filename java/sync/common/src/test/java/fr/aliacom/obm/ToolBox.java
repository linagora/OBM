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
package fr.aliacom.obm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.obm.DateUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.dao.EntityId;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.ObmUser.Builder;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserIdentity;

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
		EasyMock.expect(accessToken.getObmId()).andReturn(1).anyTimes();
		EasyMock.expect(accessToken.getDomain()).andReturn(domain).anyTimes();
		EasyMock.expect(accessToken.getUserLogin()).andReturn(login).anyTimes();
		EasyMock.expect(accessToken.getUserEmail()).andReturn(login + '@' + domain.getName()).anyTimes();
		EasyMock.expect(accessToken.getOrigin()).andReturn("unittest").anyTimes();
		EasyMock.expect(accessToken.getConversationUid()).andReturn(1).anyTimes();
		EasyMock.expect(accessToken.getSessionId()).andReturn("sessionId").anyTimes();
		EasyMock.expect(accessToken.getVersion()).andReturn(new MavenVersion("0", "0", "0")).anyTimes();
		EasyMock.expect(accessToken.getUserWithDomain()).andReturn(login + '@' + domain.getName()).anyTimes();
		
		return accessToken;
	}

	public static ObmUser mockObmUser(String userEmail, ObmDomain domain) {
		ObmUser user = EasyMock.createMock(ObmUser.class);
		EasyMock.expect(user.getEmailAtDomain()).andReturn(userEmail).atLeastOnce();
		EasyMock.expect(user.getDomain()).andReturn(domain).anyTimes();
		return user;
	}

	public static ObmDomain getDefaultObmDomain() {
		return ObmDomain
				.builder()
				.id(1)
				.name("test.tlse.lng")
				.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
				.build();
	}

	public static ObmUser getDefaultObmUser() {
		return buildCommonObmUser().build();
	}
	
	public static ObmUser getDefaultObmUserWithEmails(String...userEmails) {
		String formatedUserEmails = Joiner.on(ObmUser.EMAIL_FIELD_SEPARATOR).join(userEmails);
		return buildCommonObmUser().emailAndAliases(formatedUserEmails).build();
	}

	private static Builder buildCommonObmUser() {
		return ObmUser.builder()
			.uid(1)
			.entityId(EntityId.valueOf(2))
			.login(UserLogin.valueOf("user"))
			.domain(getDefaultObmDomain())
			.emailAndAliases("user@test")
			.identity(UserIdentity.builder()
				.firstName("Obm")
				.lastName("User")
				.build());
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
	
	public static Event getFakeEvent(int id) {
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

	public static Contact getFakeContact(int id) {
		Contact c = new Contact();

		c.setLastname("Last");
		c.setFirstname("First");
		c.setCommonname("Common");
		c.setMiddlename("Middle");
		c.setBirthday(DateUtils.date("2000-01-01T00:00:00"));
		c.setUid(id);
		c.setEntityId(EntityId.valueOf(id));
		c.setAka("Aka");
		c.setTitle("Title");
		c.setManager("Manager");
		c.setAssistant("Assistant");
		c.setAnniversary(DateUtils.date("2000-01-01T00:00:00"));
		c.setCalUri("CalURI");
		c.setComment("Comment");
		c.setCompany("Company");
		c.setService("Service");
		c.setSpouse("Spouse");
		c.setSuffix("Suffix");
		c.addEmail("EmailLabel", EmailAddress.loginAtDomain("contact@obm.com"));
		c.addPhone("PhoneLabel", new Phone("PhoneNumber"));
		c.addAddress("AddressLabel", new Address("Street", "Zip", "ExpressPostal", "Town", "Country", "State"));
		c.addIMIdentifier("IMLabel", new InstantMessagingId("imProtocol", "imAddress"));
		c.addWebsite(new Website("WebsiteLabel", "WebsiteUrl"));

		return c;
	}

}
