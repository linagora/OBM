package org.obm.push.data;

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
import org.obm.push.bean.SyncCollection;
import org.obm.push.protocol.data.CalendarEncoder;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.w3c.dom.Document;

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
		event.setExtId(new EventExtId("FAC000123D"));
		return event;
	}

	private BackendSession getFakeBackendSession() {
		BackendSession bs = new BackendSession(new Credentials("adrien@test.tlse.lngr", "test"),
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
