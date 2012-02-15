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

package org.obm.push.protocol.data;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.DateUtils;
import org.obm.sync.calendar.EventObmId;
import org.w3c.dom.Document;

import fr.aliacom.obm.common.domain.ObmDomain;

public class CalendarEncoderTest {
	private CalendarEncoder encoder;

	@Before
	public void prepareEventConverter() {
		encoder = new CalendarEncoder();
	}

	private MSEvent getFakeMSEvent() {
		MSEvent event = new MSEvent();
		event.setObmId(new EventObmId(10));
		event.setSensitivity(CalendarSensitivity.NORMAL);
		event.setBusyStatus(CalendarBusyStatus.FREE);
		event.setAllDayEvent(false);
		event.setUid(new MSEventUid("FAC000123D"));
		event.setDtStamp(DateUtils.getEpochCalendar().getTime());
		return event;
	}

	private BackendSession getFakeBackendSession() {
		User user = Factory.create().createUser("adrien@test.tlse.lngr", "email@test.tlse.lngr", "Adrien");
		BackendSession bs = new BackendSession(new Credentials(user, "test", new ObmDomain()),
				"Sync", getFakeDevice(), new BigDecimal("12.5"));
		return bs;
	}

	private SyncCollection getFakeSyncCollection() {
		SyncCollection col = new SyncCollection();
		return col;
	}

	@Test
	public void testEncodeEmptyAttendees() throws Exception {
		MSEvent event = getFakeMSEvent();
		Document doc = DOMUtils.createDoc("test", "ApplicationData");
		encoder.encode(getFakeBackendSession(), doc.getDocumentElement(),
				event, getFakeSyncCollection(), true);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DOMUtils.serialise(doc, outputStream);
		String actual = new String(outputStream.toByteArray());
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==</Calendar:TimeZone>");
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
		Assert.assertEquals(expected.toString(), actual);
	}

	@Test
	public void testEncodeTwoAttendees() throws Exception {
		MSEvent event = getFakeMSEvent();
		appendAttendee(event, "adrien@test.tlse.lng", "Adrien Poupard",
				AttendeeStatus.ACCEPT, AttendeeType.REQUIRED);
		appendAttendee(event, "adrien@test.tlse.lng", "Adrien Poupard",
				AttendeeStatus.NOT_RESPONDED, AttendeeType.REQUIRED);

		Document doc = DOMUtils.createDoc("test", "ApplicationData");
		encoder.encode(getFakeBackendSession(), doc.getDocumentElement(),
				event, getFakeSyncCollection(), true);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		DOMUtils.serialise(doc, outputStream);
		String actual = new String(outputStream.toByteArray());
		
		StringBuilder expected = new StringBuilder();
		expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		expected.append("<ApplicationData xmlns=\"test\">");
		expected.append("<Calendar:TimeZone>xP///1IAbwBtAGEAbgBjAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAoAAAAFAAMAAAAAAAAAAAAAAFIAbwBtAGEAbgBjAGUAIABEAGEAeQBsAGkAZwBoAHQAIABUAGkAbQBlAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAxP///w==</Calendar:TimeZone>");
		expected.append("<Calendar:DTStamp>19700101T000000Z</Calendar:DTStamp>");
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
		Assert.assertEquals(expected.toString(), actual);
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
		return new Device(1, "devType", "devId", new Properties());
	}
}
