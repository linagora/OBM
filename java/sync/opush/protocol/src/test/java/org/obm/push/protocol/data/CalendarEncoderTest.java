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

package org.obm.push.protocol.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventException;
import org.obm.push.bean.MSEventExceptionBuilder;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.IntEncoder;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;


public class CalendarEncoderTest {
	private CalendarEncoder encoder;
	private TimeZoneEncoder timeZoneEncoder;
	private TimeZoneConverter timeZoneConverter;
	private TimeZone defaultTimeZone;
	private final static String AS_GMT = "AAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbgAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbgAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==";

	@Before
	public void prepareEventConverter() {
		timeZoneEncoder = new TimeZoneEncoderImpl(new IntEncoder(), new WCHAREncoder(), new SystemTimeEncoder());
		timeZoneConverter = new TimeZoneConverterImpl();
		encoder = new CalendarEncoder(timeZoneEncoder, timeZoneConverter);
		TimeZone.setDefault(null);
		defaultTimeZone = TimeZone.getTimeZone("GMT");
		// Locale should be inherited in future from UserSettings
		// standardName & dayLightName are locale dependent  
		Locale.setDefault(Locale.US);
	}

	private MSEvent getFakeMSEvent(TimeZone timeZone) {
		MSEvent msEvent = new MSEvent();
		msEvent.setSensitivity(CalendarSensitivity.NORMAL);
		msEvent.setBusyStatus(CalendarBusyStatus.FREE);
		msEvent.setAllDayEvent(false);
		msEvent.setUid(new MSEventUid("FAC000123D"));
		Calendar calendar = DateUtils.getEpochCalendar(timeZone);
		msEvent.setDtStamp(calendar.getTime());
		msEvent.setTimeZone(timeZone);
		return msEvent;
	}

	private String encodeMSEventAsString(MSEvent event) throws TransformerException {
		Document doc = DOMUtils.createDoc("test", "ApplicationData");
		encoder.encode(getFakeDevice(), doc.getDocumentElement(),
				event, true);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DOMUtils.serialize(doc, outputStream);
		return new String(outputStream.toByteArray());
	}
	
	@Test
	public void testEncodeEmptyAttendees() throws Exception {
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}

	@Test
	public void testEncodeTwoAttendees() throws Exception {
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		appendAttendee(msEvent, "adrien@test.tlse.lng", "Adrien Poupard",
				AttendeeStatus.ACCEPT, AttendeeType.REQUIRED);
		appendAttendee(msEvent, "adrien@test.tlse.lng", "Adrien Poupard",
				AttendeeStatus.NOT_RESPONDED, AttendeeType.REQUIRED);

		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<Calendar:Attendees>");
		expected.append("<Calendar:Attendee>");
		expected.append("<Calendar:AttendeeEmail>adrien@test.tlse.lng</Calendar:AttendeeEmail>");
		expected.append("<Calendar:AttendeeName>Adrien Poupard</Calendar:AttendeeName>");
		expected.append("<Calendar:AttendeeStatus>3</Calendar:AttendeeStatus>");
		expected.append("<Calendar:AttendeeType>1</Calendar:AttendeeType>");
		expected.append("</Calendar:Attendee>");
		expected.append("</Calendar:Attendees>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}

	@Test
	public void testTimeZoneEncoding() throws Exception {
		MSEvent msEvent = getFakeMSEvent(TimeZone.getTimeZone("Pacific/Auckland"));

		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>MP3//04AZQB3ACAAWgBlAGEAbABhAG4AZAAgAFMAdABhAG4AZABhAHIAZAAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAQAAAABAAMAAAAAAAAAAAAAAE4AZQB3ACAAWgBlAGEAbABhAG4AZAAgAEQAYQB5AGwAaQBnAGgAdAAgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAkAAAAFAAIAAAAAAAAAxP///w==</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}
	
	@Test
	public void testWithoutTimeZone() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		MSEvent msEvent = getFakeMSEvent(null);

		msEvent.setTimeZone(null);
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:DTStamp>19700101T000000Z</Calendar:DTStamp>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}
	
	private MSRecurrence getMSRecurrence(Date start) {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setDayOfMonth(15);
		msRecurrence.setStart(start);
		msRecurrence.setType(RecurrenceType.MONTHLY);
		msRecurrence.setInterval(1);
		return msRecurrence;
	}
	
	@Test
	public void testRecurrenceInDefaultTimeZone() throws Exception {
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		Calendar calendar = DateUtils.getEpochCalendar(defaultTimeZone);
		calendar.set(Calendar.YEAR, 2012);
		calendar.set(Calendar.MONTH, 5);
		calendar.set(Calendar.DAY_OF_MONTH, 15);
		calendar.set(Calendar.HOUR, 11);
		calendar.set(Calendar.MINUTE, 35);
		calendar.set(Calendar.SECOND, 12);
		msEvent.setStartTime(calendar.getTime());
		msEvent.setExceptions(Lists.<MSEventException>newArrayList());

		msEvent.setRecurrence(getMSRecurrence(msEvent.getStartTime()));
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:StartTime>20120615T133512Z</Calendar:StartTime>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Recurrence>");
		expected.append("<Calendar:RecurrenceType>2</Calendar:RecurrenceType>");
		expected.append("<Calendar:RecurrenceInterval>1</Calendar:RecurrenceInterval>");
		expected.append("<Calendar:RecurrenceDayOfMonth>15</Calendar:RecurrenceDayOfMonth>");
		expected.append("</Calendar:Recurrence>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}
	
	@Test
	public void testRecurrenceInSpecificTimeZone() throws Exception {
		MSEvent msEvent = getFakeMSEvent(TimeZone.getTimeZone("Europe/Paris"));
		Calendar calendar = DateUtils.getEpochCalendar(TimeZone.getTimeZone("Europe/Paris"));
		calendar.set(Calendar.YEAR, 2012);
		calendar.set(Calendar.MONTH, 5);
		calendar.set(Calendar.DAY_OF_MONTH, 15);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		msEvent.setStartTime(calendar.getTime());
		msEvent.setExceptions(Lists.<MSEventException>newArrayList());

		msEvent.setRecurrence(getMSRecurrence(msEvent.getStartTime()));
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>xP///0MAZQBuAHQAcgBhAGwAIABFAHUAcgBvAHAAZQBhAG4AIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAEMAZQBuAHQAcgBhAGwAIABFAHUAcgBvAHAAZQBhAG4AIABTAHUAbQBtAGUAcgAgAFQAaQBtAGUAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:StartTime>20120615T000000Z</Calendar:StartTime>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Recurrence>");
		expected.append("<Calendar:RecurrenceType>2</Calendar:RecurrenceType>");
		expected.append("<Calendar:RecurrenceInterval>1</Calendar:RecurrenceInterval>");
		expected.append("<Calendar:RecurrenceDayOfMonth>15</Calendar:RecurrenceDayOfMonth>");
		expected.append("</Calendar:Recurrence>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}
	
	private void appendAttendee(MSEvent event, String email, String name,
			AttendeeStatus status, AttendeeType type) {
		MSAttendee att = new MSAttendee();
		att.setAttendeeStatus(status);
		att.setAttendeeType(type);
		att.setEmail(email);
		att.setName(name);
		event.addAttendee(att);
	}

	private Device getFakeDevice() {
		return new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test
	public void testEncodeOneCategories() throws Exception {
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		msEvent.setCategories(ImmutableList.of("Cat"));
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Categories>");
		expected.append("<Calendar:Category>Cat</Calendar:Category>");
		expected.append("</Calendar:Categories>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}
	
	@Test
	public void testEncodeCategories() throws Exception {
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		msEvent.setCategories(ImmutableList.of("Cat1", "Cat2"));
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Categories>");
		expected.append("<Calendar:Category>Cat1</Calendar:Category>");
		expected.append("<Calendar:Category>Cat2</Calendar:Category>");
		expected.append("</Calendar:Categories>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}
	
	@Test
	public void testEncodeOneCategoriesInException() throws Exception {
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		MSEventException msEventException = getFakeMSEventException(defaultTimeZone);
		msEventException.setCategories(ImmutableList.of("Cat"));
		Calendar calendar = DateUtils.getEpochCalendar(defaultTimeZone);
		calendar.set(Calendar.YEAR, 2012);
		calendar.set(Calendar.MONTH, 5);
		calendar.set(Calendar.DAY_OF_MONTH, 15);
		calendar.set(Calendar.HOUR, 11);
		calendar.set(Calendar.MINUTE, 35);
		calendar.set(Calendar.SECOND, 12);
		msEvent.setStartTime(calendar.getTime());
		msEvent.setExceptions(ImmutableList.of(msEventException));
		msEvent.setRecurrence(getMSRecurrence(msEvent.getStartTime()));
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:StartTime>20120615T133512Z</Calendar:StartTime>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Recurrence>");
		expected.append("<Calendar:RecurrenceType>2</Calendar:RecurrenceType>");
		expected.append("<Calendar:RecurrenceInterval>1</Calendar:RecurrenceInterval>");
		expected.append("<Calendar:RecurrenceDayOfMonth>15</Calendar:RecurrenceDayOfMonth>");
		expected.append("</Calendar:Recurrence>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:Exceptions>");
		expected.append("<Calendar:Exception>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:Categories>");
		expected.append("<Calendar:Category>Cat</Calendar:Category>");
		expected.append("</Calendar:Categories>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("</Calendar:Exception>");
		expected.append("</Calendar:Exceptions>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}
	
	@Test
	public void testEncodeCategoriesInException() throws Exception {
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		MSEventException msEventException = getFakeMSEventException(defaultTimeZone);
		msEventException.setCategories(ImmutableList.of("Cat1", "Cat2"));
		Calendar calendar = DateUtils.getEpochCalendar(defaultTimeZone);
		calendar.set(Calendar.YEAR, 2012);
		calendar.set(Calendar.MONTH, 5);
		calendar.set(Calendar.DAY_OF_MONTH, 15);
		calendar.set(Calendar.HOUR, 11);
		calendar.set(Calendar.MINUTE, 35);
		calendar.set(Calendar.SECOND, 12);
		msEvent.setStartTime(calendar.getTime());
		msEvent.setExceptions(ImmutableList.of(msEventException));
		msEvent.setRecurrence(getMSRecurrence(msEvent.getStartTime()));
		
		String actual = encodeMSEventAsString(msEvent);
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("<Calendar:StartTime>20120615T133512Z</Calendar:StartTime>");
		expected.append("<Calendar:UID>FAC000123D</Calendar:UID>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Recurrence>");
		expected.append("<Calendar:RecurrenceType>2</Calendar:RecurrenceType>");
		expected.append("<Calendar:RecurrenceInterval>1</Calendar:RecurrenceInterval>");
		expected.append("<Calendar:RecurrenceDayOfMonth>15</Calendar:RecurrenceDayOfMonth>");
		expected.append("</Calendar:Recurrence>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:Exceptions>");
		expected.append("<Calendar:Exception>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:Body>");
		expected.append("<AirSyncBase:Type>1</AirSyncBase:Type>");
		expected.append("<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>");
		expected.append("</AirSyncBase:Body>");
		expected.append("<Calendar:Sensitivity>0</Calendar:Sensitivity>");
		expected.append("<Calendar:BusyStatus>0</Calendar:BusyStatus>");
		expected.append("<Calendar:AllDayEvent>0</Calendar:AllDayEvent>");
		expected.append("<Calendar:Categories>");
		expected.append("<Calendar:Category>Cat1</Calendar:Category>");
		expected.append("<Calendar:Category>Cat2</Calendar:Category>");
		expected.append("</Calendar:Categories>");
		expected.append("<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>");
		expected.append("</Calendar:Exception>");
		expected.append("</Calendar:Exceptions>");
		expected.append("<Calendar:MeetingStatus>0</Calendar:MeetingStatus>");
		expected.append("<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>");
		expected.append("</ApplicationData>");
		assertThat(actual).isEqualTo(expected.toString());
	}

	@Test
	public void testEncodeWhenNullExceptions() throws Exception {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setInterval(1);

		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		msEvent.setAllDayEvent(true);
		msEvent.setStartTime(date("2014-01-01T14:00:00"));
		msEvent.setEndTime(date("2014-01-01T18:00:00"));
		msEvent.setRecurrence(recurrence);
		msEvent.setExceptions(null);
		
		assertThat(encodeMSEventAsString(msEvent)).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<ApplicationData xmlns=\"test\">" +
					"<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>" +
					"<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>" +
					"<Calendar:StartTime>20140101T140000Z</Calendar:StartTime>" +
					"<Calendar:UID>FAC000123D</Calendar:UID>" +
					"<Calendar:EndTime>20140101T180000Z</Calendar:EndTime>" +
					"<AirSyncBase:Body>" +
					"<AirSyncBase:Type>1</AirSyncBase:Type>" +
					"<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>" +
					"</AirSyncBase:Body>" +
					"<Calendar:Recurrence>" +
						"<Calendar:RecurrenceType>0</Calendar:RecurrenceType>" +
						"<Calendar:RecurrenceInterval>1</Calendar:RecurrenceInterval>" +
					"</Calendar:Recurrence>" +
					"<Calendar:Sensitivity>0</Calendar:Sensitivity>" +
					"<Calendar:BusyStatus>0</Calendar:BusyStatus>" +
					"<Calendar:AllDayEvent>1</Calendar:AllDayEvent>" +
					"<Calendar:MeetingStatus>0</Calendar:MeetingStatus>" +
					"<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>" +
				"</ApplicationData>");
	}
	
	@Test
	public void testEncodeWhenEmptyExceptions() throws Exception {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setInterval(1);
		
		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		msEvent.setAllDayEvent(true);
		msEvent.setStartTime(date("2014-01-01T14:00:00"));
		msEvent.setEndTime(date("2014-01-01T18:00:00"));
		msEvent.setRecurrence(recurrence);
		msEvent.setExceptions(ImmutableList.<MSEventException>of());
		
		assertThat(encodeMSEventAsString(msEvent)).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
						"<ApplicationData xmlns=\"test\">" +
						"<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>" +
						"<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>" +
						"<Calendar:StartTime>20140101T140000Z</Calendar:StartTime>" +
						"<Calendar:UID>FAC000123D</Calendar:UID>" +
						"<Calendar:EndTime>20140101T180000Z</Calendar:EndTime>" +
						"<AirSyncBase:Body>" +
						"<AirSyncBase:Type>1</AirSyncBase:Type>" +
						"<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>" +
						"</AirSyncBase:Body>" +
						"<Calendar:Recurrence>" +
						"<Calendar:RecurrenceType>0</Calendar:RecurrenceType>" +
						"<Calendar:RecurrenceInterval>1</Calendar:RecurrenceInterval>" +
						"</Calendar:Recurrence>" +
						"<Calendar:Sensitivity>0</Calendar:Sensitivity>" +
						"<Calendar:BusyStatus>0</Calendar:BusyStatus>" +
						"<Calendar:AllDayEvent>1</Calendar:AllDayEvent>" +
						"<Calendar:MeetingStatus>0</Calendar:MeetingStatus>" +
						"<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>" +
				"</ApplicationData>");
	}
	
	@Test
	public void testTagOrderIsRecurrenceThenAllDayEventThenExceptions() throws Exception {
		MSRecurrence recurrence = new MSRecurrence();
		recurrence.setType(RecurrenceType.DAILY);
		recurrence.setInterval(1);

		MSEvent msEvent = getFakeMSEvent(defaultTimeZone);
		msEvent.setAllDayEvent(true);
		msEvent.setStartTime(date("2014-01-01T14:00:00"));
		msEvent.setEndTime(date("2014-01-01T18:00:00"));
		msEvent.setRecurrence(recurrence);
		msEvent.setExceptions(ImmutableList.of(new MSEventExceptionBuilder()
					.withDeleted(true)
					.withExceptionStartTime(date("2014-03-01T14:00:00"))
					.build()));
		
		assertThat(encodeMSEventAsString(msEvent)).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<ApplicationData xmlns=\"test\">" +
					"<Calendar:TimeZone>" + AS_GMT + "</Calendar:TimeZone>" +
					"<Calendar:DTStamp>19700101T010000Z</Calendar:DTStamp>" +
					"<Calendar:StartTime>20140101T140000Z</Calendar:StartTime>" +
					"<Calendar:UID>FAC000123D</Calendar:UID>" +
					"<Calendar:EndTime>20140101T180000Z</Calendar:EndTime>" +
					"<AirSyncBase:Body>" +
					"<AirSyncBase:Type>1</AirSyncBase:Type>" +
					"<AirSyncBase:EstimatedDataSize>0</AirSyncBase:EstimatedDataSize>" +
					"</AirSyncBase:Body>" +
					"<Calendar:Recurrence>" +
						"<Calendar:RecurrenceType>0</Calendar:RecurrenceType>" +
						"<Calendar:RecurrenceInterval>1</Calendar:RecurrenceInterval>" +
					"</Calendar:Recurrence>" +
					"<Calendar:Sensitivity>0</Calendar:Sensitivity>" +
					"<Calendar:BusyStatus>0</Calendar:BusyStatus>" +
					"<Calendar:AllDayEvent>1</Calendar:AllDayEvent>" +
					"<Calendar:Exceptions>" +
						"<Calendar:Exception>" +
							"<Calendar:ExceptionIsDeleted>1</Calendar:ExceptionIsDeleted>" +
							"<Calendar:MeetingStatus>5</Calendar:MeetingStatus>" +
							"<Calendar:ExceptionStartTime>20140301T140000Z</Calendar:ExceptionStartTime>" +
						"</Calendar:Exception>" +
					"</Calendar:Exceptions>" +
					"<Calendar:MeetingStatus>0</Calendar:MeetingStatus>" +
					"<AirSyncBase:NativeBodyType>1</AirSyncBase:NativeBodyType>" +
				"</ApplicationData>");
	}
	
	private MSEventException getFakeMSEventException(TimeZone timeZone) {
		MSEventException msEventException = new MSEventException();
		msEventException.setSensitivity(CalendarSensitivity.NORMAL);
		msEventException.setBusyStatus(CalendarBusyStatus.FREE);
		msEventException.setAllDayEvent(false);
		Calendar calendar = DateUtils.getEpochCalendar(timeZone);
		msEventException.setDtStamp(calendar.getTime());
		return msEventException;
	}
}
