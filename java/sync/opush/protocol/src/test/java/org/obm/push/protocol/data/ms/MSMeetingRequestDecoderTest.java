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

package org.obm.push.protocol.data.ms;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestIntDBusyStatus;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceDayOfWeek;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestSensitivity;
import org.obm.push.exception.ConversionException;
import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.protocol.data.ASTimeZoneConverter;
import org.obm.push.protocol.data.Base64ASTimeZoneDecoder;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;


public class MSMeetingRequestDecoderTest {

	private MSMeetingRequestDecoder decoder;
	private Base64ASTimeZoneDecoder base64AsTimeZoneDecoder;
	private ASTimeZoneConverter asTimeZoneConverter;

	@Before
	public void setUp(){
		base64AsTimeZoneDecoder = createMock(Base64ASTimeZoneDecoder.class);
		asTimeZoneConverter = createMock(ASTimeZoneConverter.class);
		decoder = new MSMeetingRequestDecoder(base64AsTimeZoneDecoder, asTimeZoneConverter);
	}
	
	@Test
	public void parseAllDayFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<AllDayEvent>0</AllDayEvent>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.isAllDayEvent()).isFalse();
		
	}
	
	@Test
	public void parseAllDayTrue() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<AllDayEvent>1</AllDayEvent>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.isAllDayEvent()).isTrue();
	}
	
	@Test
	public void parseAllDayDefaultIsFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.isAllDayEvent()).isFalse();
	}
	
	@Test(expected=ParseException.class)
	public void parseDateNeedsPunctuation() throws Exception {
		decoder.date("20021126T160000Z");
	}
	
	@Test(expected=ParseException.class)
	public void parseDateNeedsTime() throws Exception {
		decoder.date("2002-11-26");
	}
	
	@Test
	public void parseDate() throws Exception {
		Date parsed = decoder.date("2000-12-25T08:35:00.000Z");
		assertThat(parsed).isEqualTo(date("2000-12-25T08:35:00+00"));
	}
	
	@Test
	public void parseDtStamp() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getDtStamp()).isEqualTo(date("2012-07-19T20:08:30+00"));
	}
	
	@Test(expected=ConversionException.class)
	public void parseDtStampIsRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		decoder.decode(doc.getDocumentElement());
	}
	
	@Test
	public void parseStartTime() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getStartTime()).isEqualTo(date("2014-12-01T09:00:00+00"));
	}

	@Test(expected=ConversionException.class)
	public void parseStartTimeIsRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		decoder.decode(doc.getDocumentElement());
	}
	
	@Test
	public void parseEndTime() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getEndTime()).isEqualTo(date("2014-12-01T10:00:00+00"));
	}

	@Test(expected=ConversionException.class)
	public void parseEndTimeIsRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		decoder.decode(doc.getDocumentElement());
	}
	
	@Test
	public void parseTimeZoneUtc() throws ConversionException {
		TimeZone expectedTimeZone = TimeZone.getTimeZone("UTC");
		String utc = 
				"AAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbg" +
				"AgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEcAcgBlAG" +
				"UAbgB3AGkAYwBoACAATQBlAGEAbgAgAFQAaQBtAGUA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"AAAAAAAAAAAAAAAAAAAA==";
		
		ASTimeZone asTimeZone = expectTimeZone(expectedTimeZone, utc);
		
		assertThat(decoder.timeZone(utc)).isEqualTo(expectedTimeZone);

		verify(base64AsTimeZoneDecoder, asTimeZoneConverter, asTimeZone);
	}

	@Test(expected=ConversionException.class)
	public void parseTimeZoneInvalid() throws ConversionException {
		String utc = 
				"AAAAAEcAcgBlAGUAbgB3AGkAYwBoACAATQBlAGEAbg" +
				"AgAFQAaQBtAGUAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
				"AAAAAAAAAAAAAAAAAAAA==";
		
		ASTimeZone asTimeZone = createMock(ASTimeZone.class);
		expect(base64AsTimeZoneDecoder.decode(aryEq(utc.getBytes(Charsets.UTF_8))))
			.andReturn(null).anyTimes();
		replay(base64AsTimeZoneDecoder, asTimeZoneConverter, asTimeZone);
		
		decoder.timeZone(utc);
	}

	@Test(expected=ConversionException.class)
	public void parseTimeNoValueTriggersException() throws ConversionException {
		decoder.timeZone(null);
	}

	@Test(expected=ConversionException.class)
	public void parseTimeEmptyTriggersException() throws ConversionException {
		decoder.timeZone("");
	}

	@Test
	public void parseInstanceTypeSingle() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getInstanceType()).isEqualTo(MSMeetingRequestInstanceType.SINGLE);
	}

	@Test
	public void parseInstanceTypeExceptionToRecurring() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>3</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getInstanceType()).isEqualTo(MSMeetingRequestInstanceType.EXCEPTION_TO_RECURRING);
	}

	@Test(expected=ConversionException.class)
	public void parseInstanceTypeIsRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		decoder.decode(doc.getDocumentElement());
	}

	@Test
	public void parseBusyStatusBusy() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<IntDBusyStatus>0</IntDBusyStatus>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getIntDBusyStatus()).isEqualTo(MSMeetingRequestIntDBusyStatus.BUSY);
	}

	@Test
	public void parseBusyStatusFree() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<IntDBusyStatus>1</IntDBusyStatus>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getIntDBusyStatus()).isEqualTo(MSMeetingRequestIntDBusyStatus.FREE);
	}

	@Test
	public void parseBusyStatusDefaultIsFree() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getIntDBusyStatus()).isEqualTo(MSMeetingRequestIntDBusyStatus.FREE);
	}

	@Test
	public void parseOrganizer() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<Organizer>organizer@domain.org</Organizer>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getOrganizer()).isEqualTo("organizer@domain.org");
	}

	@Test
	public void parseOrganizerIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getOrganizer()).isNull();
	}

	@Test(expected=IllegalArgumentException.class)
	public void parseOrganizerNeedsValidEmail() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<Organizer>I am the organizer</Organizer>" +
			"</MeetingRequest>");

		expectTimeZone();
		decoder.decode(doc.getDocumentElement());
	}

	@Test
	public void parseLocation() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<Location>In Ardèche men!</Location>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getLocation()).isEqualTo("In Ardèche men!");
	}

	@Test
	public void parseLocationIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getLocation()).isNull();
	}

	@Test
	public void parseReminderZero() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<Reminder>0</Reminder>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getReminder()).isEqualTo(0);
	}

	@Test
	public void parseReminderThousand() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<Reminder>1000</Reminder>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getReminder()).isEqualTo(1000);
	}

	@Test
	public void parseReminderIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getReminder()).isNull();
	}

	@Test
	public void parseResponseRequestedFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<ResponseRequested>0</ResponseRequested>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.isResponseRequested()).isFalse();
	}

	@Test
	public void parseResponseRequestedTrue() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<ResponseRequested>1</ResponseRequested>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.isResponseRequested()).isTrue();
	}

	@Test
	public void parseResponseRequestedDefaultIsFalse() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.isResponseRequested()).isFalse();
	}

	@Test
	public void parseGlobalObjId() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<GlobalObjId>a1b2</GlobalObjId>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getMsEventUid().serializeToString()).isEqualTo("a1b2");
	}

	@Test
	public void parseSensitivityPersonnal() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<Sensitivity>1</Sensitivity>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getSensitivity()).isEqualTo(MSMeetingRequestSensitivity.PERSONAL);
	}

	@Test
	public void parseSensitivityPrivate() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<Sensitivity>2</Sensitivity>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getSensitivity()).isEqualTo(MSMeetingRequestSensitivity.PRIVATE);
	}
	
	@Test(expected=ParseException.class)
	public void parseRecurrenceDateNeedsNoPunctuation() throws Exception {
		decoder.recurrenceDate("2002-11-26T16:00:00.000Z");
	}
	
	@Test(expected=ParseException.class)
	public void parseRecurrenceDateNeedsTime() throws Exception {
		decoder.date("20021126");
	}
	
	@Test
	public void parseRecurrenceDate() throws Exception {
		Date parsed = decoder.recurrenceDate("20001225T083500Z");
		assertThat(parsed).isEqualTo(date("2000-12-25T08:35:00+00"));
	}
	
	@Test
	public void parseRecurrenceId() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
				"<RecurrenceId>20120720T200830Z</RecurrenceId>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getRecurrenceId()).isEqualTo(date("2012-07-20T20:08:30+00"));
	}

	@Test
	public void parseRecurrenceIdIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<MeetingRequest>" +
				"<StartTime>2014-12-01T09:00:00.000Z</StartTime>" +
				"<EndTime>2014-12-01T10:00:00.000Z</EndTime>" +
				"<DTStamp>2012-07-19T20:08:30.000Z</DTStamp>" +
				"<InstanceType>0</InstanceType>" +
				"<TimeZone>" +
					"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
					"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
					"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
					"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==" +
				"</TimeZone>" +
			"</MeetingRequest>");

		expectTimeZone();
		MSMeetingRequest meeting = decoder.decode(doc.getDocumentElement());
		verifyTimeZone();
		
		assertThat(meeting.getRecurrenceId()).isNull();
	}

	@Test
	public void parseRecurrenceInterval() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getInterval()).isEqualTo(2);
	}

	@Test(expected=ConversionException.class)
	public void parseRecurrenceIntervalIsRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
				"</Recurrence>" +
			"</Recurrences>");

		decoder.recurrences(doc.getDocumentElement());
	}

	@Test
	public void parseRecurrenceTypeDaily() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getType()).isEqualTo(MSMeetingRequestRecurrenceType.DAILY);
	}

	@Test
	public void parseRecurrenceTypeMonthly() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>2</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getType()).isEqualTo(MSMeetingRequestRecurrenceType.MONTHLY);
	}

	@Test(expected=ConversionException.class)
	public void parseRecurrenceTypeIsRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		decoder.recurrences(doc.getDocumentElement());
	}

	@Test
	public void parseRecurrenceDayOfMonth() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
					"<Recurrence_DayOfMonth>30</Recurrence_DayOfMonth>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getDayOfMonth()).isEqualTo(30);
	}

	@Test
	public void parseRecurrenceDayOfMonthIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getDayOfMonth()).isNull();
	}

	@Test
	public void parseRecurrenceDayOfWeek() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
					"<Recurrence_DayOfWeek>34</Recurrence_DayOfWeek>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getDayOfWeek()).containsOnly(
				MSMeetingRequestRecurrenceDayOfWeek.MONDAY,
				MSMeetingRequestRecurrenceDayOfWeek.FRIDAY);
	}

	@Test
	public void parseRecurrenceDayOfWeekIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getDayOfWeek()).isEmpty();
	}

	@Test
	public void parseRecurrenceWeekOfMonth() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
					"<Recurrence_WeekOfMonth>2</Recurrence_WeekOfMonth>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getWeekOfMonth()).isEqualTo(2);
	}

	@Test
	public void parseRecurrenceWeekOfMonthIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getWeekOfMonth()).isNull();
	}

	@Test
	public void parseRecurrenceMonthOfYear() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
					"<Recurrence_MonthOfYear>5</Recurrence_MonthOfYear>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getMonthOfYear()).isEqualTo(5);
	}

	@Test
	public void parseRecurrenceMonthOfYearIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getMonthOfYear()).isNull();
	}

	@Test
	public void parseRecurrenceOccurrences() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
					"<Recurrence_Occurrences>15</Recurrence_Occurrences>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getOccurrences()).isEqualTo(15);
	}

	@Test
	public void parseRecurrenceOccurrencesIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getOccurrences()).isNull();
	}

	@Test
	public void parseRecurrenceUntil() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
					"<Recurrence_Until>20120719T200830Z</Recurrence_Until>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getUntil()).isEqualTo(date("2012-07-19T20:08:30+00"));
	}

	@Test
	public void parseRecurrenceUntilIsNotRequired() throws Exception {
		Document doc = DOMUtils.parse(
			"<Recurrences>" +
				"<Recurrence>" +
					"<Recurrence_Type>0</Recurrence_Type>" +
					"<Recurrence_Interval>2</Recurrence_Interval>" +
				"</Recurrence>" +
			"</Recurrences>");

		MSMeetingRequestRecurrence recurrence = Iterables.getOnlyElement(decoder.recurrences(doc.getDocumentElement()));
		
		assertThat(recurrence.getUntil()).isNull();
	}
	
	private ASTimeZone expectTimeZone() {
		return expectTimeZone(TimeZone.getTimeZone("UTC"), 
				"xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQ" +
				"BlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAA" +
				"AFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAA" +
				"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==");
	}

	private ASTimeZone expectTimeZone(TimeZone expectedTimeZone, String utc) {
		ASTimeZone asTimeZone = createMock(ASTimeZone.class);
		expect(base64AsTimeZoneDecoder.decode(aryEq(utc.getBytes(Charsets.UTF_8))))
			.andReturn(asTimeZone);
		expect(asTimeZoneConverter.convert(asTimeZone))
			.andReturn(expectedTimeZone);
		replay(base64AsTimeZoneDecoder, asTimeZoneConverter, asTimeZone);
		return asTimeZone;
	}
	
	private void verifyTimeZone() { 
		verify(base64AsTimeZoneDecoder, asTimeZoneConverter);
	}
}
