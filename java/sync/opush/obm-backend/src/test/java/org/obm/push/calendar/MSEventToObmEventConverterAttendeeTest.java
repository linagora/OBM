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

import java.util.Date;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventBuilder;
import org.obm.push.bean.User;
import org.obm.push.exception.ConversionException;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;

import com.google.common.collect.Sets;


public class MSEventToObmEventConverterAttendeeTest {

	private MSEventToObmEventConverterImpl converter;

	private User user;
	
	@Before
	public void setUp() {
		converter = new MSEventToObmEventConverterImpl();
		String mailbox = "user@domain";
	    user = User.Factory.create()
				.createUser(mailbox, mailbox, null);
	}

	@Test(expected=ConversionException.class)
	public void testConvertAttendeeAttributeNameOnly() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setName("Any name");
		attendee.setEmail(null);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		convertToOBMEvent(msEvent);
	}

	@Test
	public void testConvertAttendeeAttributeEmailOnly() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setName(null);
		attendee.setEmail("anyuser@domain");
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee).isNotNull();
		Assertions.assertThat(theAttendee.getEmail()).isEqualTo(attendee.getEmail());
	}
	
	@Test
	public void testConvertAttendeeAttributeNameAndEmail() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setName("Any Name");
		attendee.setEmail("anyuser@domain");
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee).isNotNull();
		Assertions.assertThat(theAttendee.getDisplayName()).isEqualTo(attendee.getName());
		Assertions.assertThat(theAttendee.getEmail()).isEqualTo(attendee.getEmail());
	}

	@Test
	public void testConvertAttendeeAttributeStatusAccepted() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeStatus(AttendeeStatus.ACCEPT);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipation()).isEqualTo(Participation.accepted());
	}

	@Test
	public void testConvertAttendeeAttributeStatusDecline() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeStatus(AttendeeStatus.DECLINE);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipation()).isEqualTo(Participation.declined());
	}

	@Test
	public void testConvertAttendeeAttributeStatusNotResponded() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeStatus(AttendeeStatus.NOT_RESPONDED);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipation()).isEqualTo(Participation.needsAction());
	}
	
	@Test
	public void testConvertAttendeeAttributeStatusResponseUnknown() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeStatus(AttendeeStatus.RESPONSE_UNKNOWN);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipation()).isEqualTo(Participation.needsAction());
	}

	@Test
	public void testConvertAttendeeAttributeStatusTentative() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeStatus(AttendeeStatus.TENTATIVE);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipation()).isEqualTo(Participation.tentative());
	}
	
	@Test
	public void testConvertAttendeeAttributeStatusNull() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeStatus(null);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipation()).isEqualTo(Participation.needsAction());
	}

	@Test
	public void testConvertAttendeeAttributeTypeRequired() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeType(AttendeeType.REQUIRED);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipationRole()).isEqualTo(ParticipationRole.REQ);
	}

	@Test
	public void testConvertAttendeeAttributeTypeOptional() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeType(AttendeeType.OPTIONAL);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipationRole()).isEqualTo(ParticipationRole.OPT);
	}
	
	@Test
	public void testConvertAttendeeAttributeTypeResource() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeType(AttendeeType.RESOURCE);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipationRole()).isEqualTo(ParticipationRole.CHAIR);
	}
	
	@Test
	public void testConvertAttendeeAttributeTypeNull() throws ConversionException {
		MSAttendee attendee = new MSAttendee();
		attendee.setEmail("anyuser@domain");
		attendee.setAttendeeType(null);
		MSEvent msEvent = makeEventWithAttendee(attendee);
		
		Event converted = convertToOBMEvent(msEvent);
		
		Attendee theAttendee = converted.findAttendeeFromEmail(attendee.getEmail());
		Assertions.assertThat(theAttendee.getParticipationRole()).isEqualTo(ParticipationRole.REQ);
	}
	
	private MSEvent makeEventWithAttendee(MSAttendee attendee) {
		MSEvent msEvent = new MSEventBuilder()
				.withStartTime(date("2004-12-11T11:15:10Z"))
				.withEndTime(date("2004-12-12T11:15:10Z"))
				.withSubject("Any Subject")
				.withAttendees(Sets.newHashSet(attendee))
				.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
				.build();
		return msEvent;
	}

	private Event convertToOBMEvent(MSEvent msEvent) throws ConversionException {
		return convertToOBMEventWithEditingEvent(msEvent, null);
	}
	
	private Event convertToOBMEventWithEditingEvent(MSEvent msEvent, Event editingEvent) throws ConversionException {
		return converter.convert(user, editingEvent, msEvent, false);
	}
	
	private Date date(String date) {
		return DateUtils.date(date);
	}
}
