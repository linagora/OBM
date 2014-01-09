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

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.FactoryConfigurationError;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.bean.MSEventExtId;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest.Builder;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestIntDBusyStatus;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestSensitivity;
import org.obm.push.utils.IntEncoder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class MSMeetingRequestSerializerTest {

	private SimpleDateFormat protocolDateFormat;
	private SimpleDateFormat protocolCalendarDateFormat;
	private SerializingTest serializingTest;

	@Before
	public void setUp() {
		protocolDateFormat = new SimpleDateFormat(MSEmailEncoder.UTC_DATE_PATTERN);
		protocolCalendarDateFormat = new SimpleDateFormat(MSEmailEncoder.UTC_DATE_NO_PUNCTUATION_PATTERN);
		serializingTest = new SerializingTest();
	}
	
	@Test
	public void testConvertMSEventUidToGlobalObjId() {
		/*
		 * Bytes 1-16:  <04><00><00><00><82><00><E0><00><74><C5><B7><10><1A><82><E0><08>
		 * Bytes 17-20: <00><00><00><00>
		 * Bytes 21-36: <00><00><00><00><00><00><00><00><00><00><00><00><00><00><00><00>
		 * Bytes 37-40: <33><00><00><00>
		 * Bytes 41-52: vCal-Uid<01><00><00><00>
		 * Bytes 53-91: {81412D3C-2A24-4E9D-B20E-11F7BBE92799}<00>
		 */
		byte[] expectedBytes = MSMeetingRequestSerializer.buildByteSequence(
				0x04, 0x00, 0x00, 0x00, 0x82, 0x00, 0xE0, 0x00, 0x74, 0xC5, 0xB7, 0x10, 
				0x1A, 0x82, 0xE0, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	0x00, 0x00,
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				0x33, 0x00, 0x00, 0x00, 0x76, 0x43, 0x61, 0x6C, 0x2D, 0x55, 0x69, 0x64,
				0x01, 0x00, 0x00, 0x00, 0x7B, 0x38, 0x31, 0x34, 0x31, 0x32, 0x44, 0x33,
				0x43, 0x2D, 0x32, 0x41, 0x32, 0x34, 0x2D, 0x34, 0x45, 0x39, 0x44, 0x2D,
				0x42, 0x32, 0x30, 0x45, 0x2D, 0x31, 0x31, 0x46, 0x37, 0x42, 0x42, 0x45,
				0x39, 0x32, 0x37, 0x39, 0x39, 0x7D, 0x00);
		String expected = Base64.encodeBase64String(expectedBytes);
		String actual = MSMeetingRequestSerializer.msEventUidToGlobalObjId(
				new MSEventUid("{81412D3C-2A24-4E9D-B20E-11F7BBE92799}"), new IntEncoder());
		assertThat(actual).isEqualTo(expected);
	}
	
	@Test(expected=NullPointerException.class)
	public void testMeetingRequestNull() {
		Element encodedDocument = encode(null);
		
		assertThat(tag(encodedDocument, ASEmail.MEETING_REQUEST)).isNull();
	}

	@Test
	public void testMeetingRequestStartTime() {
		Date startTime = DateUtils.date("1970-01-01T12:00:00");
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.startTime(startTime)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.START_TIME)).isEqualTo(date(startTime));
	}

	@Test
	public void testMeetingRequestEndTime() {
		Date endTime = DateUtils.date("1970-01-01T12:00:00");
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.endTime(endTime)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.END_TIME)).isEqualTo(date(endTime));
	}

	@Test
	public void testMeetingRequestDTStamp() {
		Date dtStamp = DateUtils.date("1970-01-01T12:00:00");
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.dtStamp(dtStamp)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.DTSTAMP)).isEqualTo(date(dtStamp));
	}

	@Test
	public void testMeetingRequestInstanceTypeSingle() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.instanceType(MSMeetingRequestInstanceType.SINGLE)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INSTANCE_TYPE))
				.isEqualTo(MSMeetingRequestInstanceType.SINGLE.specificationValue());
	}

	@Test
	public void testMeetingRequestInstanceTypeSingleRecurring() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.instanceType(MSMeetingRequestInstanceType.SINGLE_INSTANCE_RECURRING)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INSTANCE_TYPE))
				.isEqualTo(MSMeetingRequestInstanceType.SINGLE_INSTANCE_RECURRING.specificationValue());
	}

	@Test
	public void testMeetingRequestInstanceTypeMaster() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INSTANCE_TYPE))
				.isEqualTo(MSMeetingRequestInstanceType.MASTER_RECURRING.specificationValue());
	}

	@Test
	public void testMeetingRequestInstanceTypeExceptionToRecurring() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.instanceType(MSMeetingRequestInstanceType.EXCEPTION_TO_RECURRING)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INSTANCE_TYPE))
				.isEqualTo(MSMeetingRequestInstanceType.EXCEPTION_TO_RECURRING.specificationValue());
	}

	@Test
	public void testMeetingRequestAllDayTrue() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.allDayEvent(true)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.ALL_DAY_EVENT)).isEqualTo("1");
	}

	@Test
	public void testMeetingRequestAllDayFalse() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.allDayEvent(false)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.ALL_DAY_EVENT)).isEqualTo("0");
	}

	@Test
	public void testMeetingRequestCategoriesNull() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.categories(null)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tag(encodedDocument, ASEmail.CATEGORIES)).isNull();
	}

	@Test
	public void testMeetingRequestGlobalObjId() {
		MSEventUid globalObjIdValue = new MSEventUid("a globalObjIdValue");
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.msEventUid(globalObjIdValue)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.GLOBAL_OBJ_ID)).isEqualTo(
				MSMeetingRequestSerializer.msEventUidToGlobalObjId(globalObjIdValue, new IntEncoder()));
	}
	
	@Test
	public void testMeetingRequestBusyStatusFree() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.intDBusyStatus(MSMeetingRequestIntDBusyStatus.FREE)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INT_DB_BUSY_STATUS))
				.isEqualTo(MSMeetingRequestIntDBusyStatus.FREE.specificationValue());
	}
	
	@Test
	public void testMeetingRequestBusyStatusBusy() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.intDBusyStatus(MSMeetingRequestIntDBusyStatus.BUSY)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INT_DB_BUSY_STATUS))
				.isEqualTo(MSMeetingRequestIntDBusyStatus.BUSY.specificationValue());
	}
	
	@Test
	public void testMeetingRequestBusyStatusOutOfOffice() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.intDBusyStatus(MSMeetingRequestIntDBusyStatus.OUT_OF_OFFICE)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INT_DB_BUSY_STATUS))
				.isEqualTo(MSMeetingRequestIntDBusyStatus.OUT_OF_OFFICE.specificationValue());
	}
	
	@Test
	public void testMeetingRequestBusyStatusTentative() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.intDBusyStatus(MSMeetingRequestIntDBusyStatus.TENTATIVE)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.INT_DB_BUSY_STATUS))
				.isEqualTo(MSMeetingRequestIntDBusyStatus.TENTATIVE.specificationValue());
	}

	@Test
	public void testMeetingRequestLocationNull() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.location(null)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tag(encodedDocument, ASEmail.LOCATION)).isNull();
	}

	@Test
	public void testMeetingRequestLocation() {
		String location = "a location value";
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.location(location)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.LOCATION)).isEqualTo(location);
	}

	@Test
	public void testMeetingRequestOrganizerNull() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.organizer(null)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tag(encodedDocument, ASEmail.ORGANIZER)).isNull();
	}

	@Test
	public void testMeetingRequestOrganizer() {
		String organizer = "organizer@domain";
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.organizer(organizer)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.ORGANIZER)).isEqualTo(organizer);
	}

	@Test
	public void testMeetingRequestRecurrenceIdNull() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.recurrenceId(null)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tag(encodedDocument, ASEmail.RECURRENCE_ID)).isNull();
	}

	@Test
	public void testMeetingRequestRecurrenceId() {
		Date recurrenceId = DateUtils.date("1970-01-01T10:00:00");
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.recurrenceId(recurrenceId)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		String expectedFormat = protocolCalendarDateFormat.format(recurrenceId);
		assertThat(tagValue(encodedDocument, ASEmail.RECURRENCE_ID)).isEqualTo(expectedFormat);
	}

	@Test
	public void testMeetingRequestReminderNull() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.reminder(null)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tag(encodedDocument, ASEmail.REMINDER)).isNull();
	}

	@Test
	public void testMeetingRequestReminder() {
		Long reminder = 123l;
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.reminder(reminder)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.REMINDER)).isEqualTo(String.valueOf(reminder));
	}

	@Test
	public void testMeetingReponseRequestedTrue() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.responseRequested(true)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.RESPONSE_REQUESTED)).isEqualTo("1");
	}
	
	@Test
	public void testMeetingReponseRequestedFalse() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.responseRequested(false)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tag(encodedDocument, ASEmail.RESPONSE_REQUESTED)).isNull();
	}
	
	@Test
	public void testMeetingReponseSensitivityConfidential() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.sensitivity(MSMeetingRequestSensitivity.CONFIDENTIAL)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.SENSITIVITY))
				.isEqualTo(MSMeetingRequestSensitivity.CONFIDENTIAL.specificationValue());
	}
	
	@Test
	public void testMeetingReponseSensitivityNormal() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.sensitivity(MSMeetingRequestSensitivity.NORMAL)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.SENSITIVITY))
				.isEqualTo(MSMeetingRequestSensitivity.NORMAL.specificationValue());
	}

	@Test
	public void testMeetingReponseSensitivityPersonal() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.sensitivity(MSMeetingRequestSensitivity.PERSONAL)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.SENSITIVITY))
				.isEqualTo(MSMeetingRequestSensitivity.PERSONAL.specificationValue());
	}
	
	@Test
	public void testMeetingReponseSensitivityPrivate() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.sensitivity(MSMeetingRequestSensitivity.PRIVATE)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.SENSITIVITY))
				.isEqualTo(MSMeetingRequestSensitivity.PRIVATE.specificationValue());
	}
	
	@Test
	public void testMeetingReponseTimeZoneNull() {
		MSMeetingRequest meetingRequest = initializedRequiredFieldsMeetingRequestBuilder()
				.timeZone(null)
				.build();
		
		Element encodedDocument = encode(meetingRequest);
		
		assertThat(tagValue(encodedDocument, ASEmail.TIME_ZONE))
				.isEqualTo(MSEmailEncoder.DEFAULT_TIME_ZONE);
	}
	
	private String date(Date date) {
		return protocolDateFormat.format(date);
	}

	private Builder initializedRequiredFieldsMeetingRequestBuilder() {
		return MSMeetingRequest.builder()
				.dtStamp(DateUtils.date("1970-01-01T10:00:00"))
				.startTime(DateUtils.date("1970-01-01T12:00:00"))
				.endTime(DateUtils.date("1970-01-01T15:00:00"))
				.instanceType(MSMeetingRequestInstanceType.SINGLE)
				.msEventExtId(new MSEventExtId("anyExtId"))
				.msEventUid(new MSEventUid("81412D3C-2A24-4E9D-B20E-11F7BBE92799"));
	}

	private Element encode(MSMeetingRequest meetingRequest) throws FactoryConfigurationError {
		return encode(meetingRequest, MSEmailEncoder.DEFAULT_TIME_ZONE);
	}
	
	private Element encode(MSMeetingRequest meetingRequest, String timeZone) throws FactoryConfigurationError {
		Element parentElement = createRootDocument();
		MSMeetingRequestSerializer msMeetingRequestSerializer = new MSMeetingRequestSerializer(
				new IntEncoder(), parentElement, meetingRequest);
		msMeetingRequestSerializer.serializeMSMeetingRequest(timeZone);
		return parentElement;
	}

	private Node tag(Element element, ASEmail asemail) {
		return serializingTest.tag(element, asemail);
	}

	private String tagValue(Element element, ASEmail asemail) {
		return serializingTest.tagValue(element, asemail);
	}

	private Element createRootDocument() {
		return serializingTest.createRootDocument();
	}
}
