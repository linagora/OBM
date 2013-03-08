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
package org.obm.push.bean;

import static org.obm.DateUtils.date;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestCategory;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceType;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.testing.SerializableTester;

@RunWith(SlowFilterRunner.class)
public class SerializableTest {

	private ObjectOutputStream objectOutputStream;


	@Before
	public void buildOutputStream() throws IOException {
		objectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream());	
	}

	@Test
	public void testSyncCollectionOptions() throws IOException {
		SyncCollectionOptions obj = new SyncCollectionOptions();
		obj.setBodyPreferences(ImmutableList.of(BodyPreference.builder().build()));
		objectOutputStream.writeObject(obj);
	}
	
	@Test
	public void testSyncCollection() throws IOException {
		SyncCollection syncCollection = new SyncCollection();
		syncCollection.addChange(new SyncCollectionChange("serverId", "clientId", SyncCommand.ADD, new MSContact(), PIMDataType.CALENDAR));
		syncCollection.setItemSyncState(ItemSyncState.builder()
				.syncDate(new Date())
				.syncKey(new SyncKey("key"))
				.build());
		objectOutputStream.writeObject(syncCollection);
	}
	
	@Test
	public void testMSContact() throws Exception {
		MSContact contact = new MSContact();
		contact.setAssistantName("AssistantName");
		contact.setAssistantPhoneNumber("AssistantTelephoneNumber");
		contact.setAssistnamePhoneNumber("AssistnameTelephoneNumber");
		contact.setBusiness2PhoneNumber("Business2TelephoneNumber");
		contact.setBusinessPhoneNumber("BusinessTelephoneNumber");
		contact.setWebPage("Webpage");
		contact.setDepartment("Department");
		contact.setEmail1Address("Email1Address");
		contact.setEmail2Address("Email2Address");
		contact.setEmail3Address("Email3Address");
		contact.setBusinessFaxNumber("BusinessFaxNumber");
		contact.setFileAs("FileAs");
		contact.setFirstName("FirstName");
		contact.setMiddleName("MiddleName");
		contact.setHomeAddressCity("HomeAddressCity");
		contact.setHomeAddressCountry("HomeAddressCountry");
		contact.setHomeFaxNumber("HomeFaxNumber");
		contact.setHomePhoneNumber("HomeTelephoneNumber");
		contact.setHome2PhoneNumber("Home2TelephoneNumber");
		contact.setHomeAddressPostalCode("HomeAddressPostalCode");
		contact.setHomeAddressState("HomeAddressState");
		contact.setHomeAddressStreet("HomeAddressStreet");
		contact.setMobilePhoneNumber("MobileTelephoneNumber");
		contact.setSuffix("Suffix");
		contact.setCompanyName("CompanyName");
		contact.setOtherAddressCity("OtherAddressCity");
		contact.setOtherAddressCountry("OtherAddressCountry");
		contact.setCarPhoneNumber("CarTelephoneNumber");
		contact.setOtherAddressPostalCode("OtherAddressPostalCode");
		contact.setOtherAddressState("OtherAddressState");
		contact.setOtherAddressStreet("OtherAddressStreet");
		contact.setPagerNumber("PagerNumber");
		contact.setTitle("Title");
		contact.setBusinessPostalCode("BusinessAddressPostalCode");
		contact.setBusinessState("BusinessAddressState");
		contact.setBusinessStreet("BusinessAddressStreet");
		contact.setBusinessAddressCountry("BusinessAddressCountry");
		contact.setBusinessAddressCity("BusinessAddressCity");
		contact.setLastName("LastName");
		contact.setSpouse("Spouse");
		contact.setJobTitle("JobTitle");
		contact.setYomiFirstName("YomiFirstName");
		contact.setYomiLastName("YomiLastName");
		contact.setYomiCompanyName("YomiCompanyName");
		contact.setOfficeLocation("OfficeLocation");
		contact.setRadioPhoneNumber("RadioTelephoneNumber");
		contact.setPicture("Picture");
		contact.setAnniversary(date("2008-10-15T11:15:10Z"));
		contact.setBirthday(date("2007-10-15T11:15:10Z"));
		contact.setCategories(Lists.newArrayList("category"));
		contact.setChildren(Lists.newArrayList("children"));
		contact.setCustomerId("CustomerId");
		contact.setGovernmentId("GovernmentId");
		contact.setIMAddress("IMAddress");
		contact.setIMAddress2("IMAddress2");
		contact.setIMAddress3("IMAddress3");
		contact.setManagerName("ManagerName");
		contact.setCompanyMainPhone("CompanyMainPhone");
		contact.setAccountName("AccountName");
		contact.setNickName("NickName");
		contact.setMMS("MMS");
		contact.setData("Data");
		objectOutputStream.writeObject(contact);
	}
	
	@Test
	public void testMSEmail() throws IOException {
		MSEmail msEmail = new MSEmail();
		msEmail.setBody(new MSEmailBody());
		msEmail.setFrom(new MSAddress("toto", "toto@titi.com"));
		msEmail.setAttachements(ImmutableSet.of(new MSAttachement()));
		msEmail.setMimeData(new ByteArrayInputStream(new byte[0]));
		objectOutputStream.writeObject(msEmail);
	}

	@Test
	public void testNewMSEmail() {
		 org.obm.push.bean.ms.MSEmail msEmail = org.obm.push.bean.ms.MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(org.obm.push.bean.ms.MSEmailBody.builder()
					.mimeData(new SerializableInputStream(new ByteArrayInputStream("message".getBytes())))
					.bodyType(MSEmailBodyType.PlainText)
					.estimatedDataSize(0)
					.charset(Charsets.UTF_8)
					.truncated(false)
					.build())
			.meetingRequest(
					MSMeetingRequest.builder()
						.startTime(date("2012-02-03T11:22:33"))
						.endTime(date("2012-02-03T12:22:33"))
						.dtStamp(date("2012-02-02T11:22:33"))
						.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
						.msEventExtId(new MSEventExtId("ext-id-123-536"))
						.recurrences(Arrays.asList(
								MSMeetingRequestRecurrence.builder()
								.type(MSMeetingRequestRecurrenceType.DAILY)
								.interval(1)
								.build()))
						.recurrenceId(date("2012-02-02T11:22:33"))
						.categories(Arrays.asList(
								new MSMeetingRequestCategory("category")
								))
						.build())
			.build();
		 SerializableTester.reserializeAndAssert(msEmail);
	}

	@Test
	public void testUidMSEmail() {
		 org.obm.push.bean.ms.MSEmail msEmail = org.obm.push.bean.ms.MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(org.obm.push.bean.ms.MSEmailBody.builder()
					.mimeData(new SerializableInputStream(new ByteArrayInputStream("message".getBytes())))
					.bodyType(MSEmailBodyType.PlainText)
					.estimatedDataSize(0)
					.charset(Charsets.UTF_8)
					.truncated(false)
					.build())
			.meetingRequest(
					MSMeetingRequest.builder()
						.startTime(date("2012-02-03T11:22:33"))
						.endTime(date("2012-02-03T12:22:33"))
						.dtStamp(date("2012-02-02T11:22:33"))
						.instanceType(MSMeetingRequestInstanceType.MASTER_RECURRING)
						.msEventExtId(new MSEventExtId("ext-id-123-536"))
						.recurrences(Arrays.asList(
								MSMeetingRequestRecurrence.builder()
								.type(MSMeetingRequestRecurrenceType.DAILY)
								.interval(1)
								.build()))
						.recurrenceId(date("2012-02-02T11:22:33"))
						.categories(Arrays.asList(
								new MSMeetingRequestCategory("category")
								))
						.build())
			.build();
		 
		 SerializableTester.reserializeAndAssert(UidMSEmail.uidBuilder()
				 .uid(1)
				 .email(msEmail)
				 .build());
	}

	@Test
	public void testMSEvent() throws IOException {
		MSRecurrence msRecurrence = new MSRecurrence();
		msRecurrence.setType(RecurrenceType.DAILY);
		msRecurrence.setInterval(7);
		msRecurrence.setUntil(date("2004-12-11T11:15:10Z"));
		msRecurrence.setOccurrences(4);
		msRecurrence.setDayOfMonth(2);
		msRecurrence.setDayOfWeek(ImmutableSet.of(RecurrenceDayOfWeek.FRIDAY, RecurrenceDayOfWeek.SUNDAY));
		msRecurrence.setWeekOfMonth(4);
		msRecurrence.setMonthOfYear(2);
		msRecurrence.setDeadOccur(true);
		msRecurrence.setRegenerate(true);
		msRecurrence.setStart(date("2004-12-11T12:15:10Z"));
		
		MSEvent msEvent = new MSEventBuilder()
		.withStartTime(date("2004-12-11T11:15:10Z"))
		.withEndTime(date("2004-12-12T11:15:10Z"))
		.withDtStamp(date("2004-12-15T11:15:10Z"))
		.withSubject("Any Subject")
		.withRecurrence(msRecurrence)
		.withMeetingStatus(CalendarMeetingStatus.IS_A_MEETING)
		.withAllDayEvent(true)
		.withAttendees(ImmutableSet.of(MSAttendee.builder()
				.withEmail("email")
				.withName("name")
				.withStatus(AttendeeStatus.ACCEPT)
				.withType(AttendeeType.REQUIRED)
				.build()))
		.withBusyStatus(CalendarBusyStatus.BUSY)
		.withCategories(ImmutableList.of("business"))
		.withDescription("description")
		.withExceptions(ImmutableList.of(new MSEventExceptionBuilder()
				.withAllDayEvent(true)
				.withBusyStatus(CalendarBusyStatus.TENTATIVE)
				.withCategories(ImmutableList.of("business"))
				.withDeleted(false)
				.withDescription("description")
				.withDtStamp(date("2004-12-18T11:15:10Z"))
				.withEndTime(date("2004-11-15T11:15:10Z"))
				.withExceptionStartTime(date("2004-12-14T21:15:10Z"))
				.withLocation("location")
				.withMeetingStatus(CalendarMeetingStatus.MEETING_IS_CANCELED)
				.withReminder(46)
				.withSensitivity(CalendarSensitivity.CONFIDENTIAL)
				.withStartTime(date("2004-10-15T11:15:10Z"))
				.withSubject("subject")
				.build()))
		.withLocation("location")
		.withMeetingStatus(CalendarMeetingStatus.MEETING_RECEIVED)
		.withOrganizerEmail("organizerEmail")
		.withOrganizerName("organizerName")
		.withReminder(45)
		.withSensitivity(CalendarSensitivity.NORMAL)
		.withTimeZone(TimeZone.getTimeZone("GMT"))
		.withUid(new MSEventUid("456"))
		.build();
		objectOutputStream.writeObject(msEvent);
	}
	
	@Test
	public void testMSTask() throws IOException {
		MSTask msTask = new MSTask();
		msTask.setRecurrence(new MSRecurrence());
		objectOutputStream.writeObject(msTask);
	}
	
	@Test
	public void testDevice() throws IOException {
		Device obj = new Device(1, "toto", new DeviceId("toto"), new Properties(), new BigDecimal("12.1"));
		objectOutputStream.writeObject(obj);
	}

	@Test
	public void testCredentials() throws IOException {
		User user = Factory.create().createUser("login@titi", "email", "displayName");
		Credentials obj = new Credentials(user, "tata");
		objectOutputStream.writeObject(obj);
	}
	
	@Test
	public void testMSEventUid() throws IOException {
		MSEventUid msEventUid = new MSEventUid("totototo");
		objectOutputStream.writeObject(msEventUid);
	}
	
	@Test
	public void testDeviceId() throws IOException {
		DeviceId deviceId = new DeviceId("deviceId");
		objectOutputStream.writeObject(deviceId);
	}

	@Test
	public void testSyncKey() throws IOException {
		SyncKey syncKey = new SyncKey("syncKey");
		objectOutputStream.writeObject(syncKey);
	}
}
