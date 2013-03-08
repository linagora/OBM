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
package org.obm.push;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarMeetingStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSAddress;
import org.obm.push.bean.MSAttachement;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.MSEventBuilder;
import org.obm.push.bean.MSEventExceptionBuilder;
import org.obm.push.bean.MSEventExtId;
import org.obm.push.bean.MSEventUid;
import org.obm.push.bean.MSRecurrence;
import org.obm.push.bean.MSTask;
import org.obm.push.bean.MethodAttachment;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncKeysKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.User;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailMetadata;
import org.obm.push.bean.ms.UidMSEmail;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestCategory;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceType;
import org.obm.push.mail.EmailChanges;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.bean.SnapshotKey;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl;
import org.obm.push.store.ehcache.SyncedCollectionDaoEhcacheImpl;
import org.obm.push.store.ehcache.UnsynchronizedItemDaoEhcacheImpl;
import org.obm.push.store.ehcache.UnsynchronizedItemType;
import org.obm.push.store.ehcache.WindowingDaoEhcacheImpl;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**	Full list of serialized beans on QA branch at 8 March 2013:
 
	"org.obm.push.mail.EmailChanges", 
	"org.obm.push.mail.bean.WindowingIndexKey", 
	"org.obm.push.mail.bean.Email", 
	"org.obm.push.mail.bean.SnapshotKey", 
	"org.obm.push.mail.bean.Snapshot", 
	"org.obm.push.mail.bean.MessageSet", 
	"org.obm.push.bean.User",
	"org.obm.push.bean.Device", 
	"org.obm.push.bean.DeviceId", 
	"org.obm.push.bean.SyncKey", 
	"org.obm.push.bean.SyncKeysKey", 
	"org.obm.push.bean.Credentials", 
	"org.obm.push.bean.SyncCollection", 
	"org.obm.push.bean.ItemSyncState", 
	"org.obm.push.bean.SyncCollectionChange", 
	"org.obm.push.bean.MSContact", 
	"org.obm.push.bean.ms.MSEmail",         
	"org.obm.push.bean.ms.MSEmailBody", 
	"org.obm.push.bean.MSEmailHeader", 
	"org.obm.push.bean.MSAttachement", 
	"org.obm.push.bean.MSAddress", 
	"org.obm.push.bean.msmeetingrequest.MSMeetingRequest", 
	"org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence", 
	"org.obm.push.bean.msmeetingrequest.MSMeetingRequestCategory", 
	"org.obm.push.bean.MSEventExtId", 
	"org.obm.push.utils.SerializableInputStream", 
	"org.obm.push.bean.ms.UidMSEmail", 
	"org.obm.push.bean.ms.MSEmailMetadata", 
	"org.obm.push.bean.MSEvent", 
	"org.obm.push.bean.MSAttendee", 
	"org.obm.push.bean.MSEventUid", 
	"org.obm.push.bean.MSRecurrence", 
	"org.obm.push.bean.MSEventException", 
	"org.obm.push.bean.MSTask", 
	"org.obm.push.bean.SyncCollectionOptions", 
	"org.obm.push.bean.BodyPreference", 
	"org.obm.push.bean.change.item.ItemDeletion", 
	"org.obm.push.bean.change.item.ItemChange",
	"org.obm.push.store.ehcache.WindowingDaoEhcacheImpl$ChunkKey", 
	"org.obm.push.store.ehcache.WindowingDaoEhcacheImpl$WindowingIndex", 
	"org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl$Key", 
	"org.obm.push.store.ehcache.SyncedCollectionDaoEhcacheImpl$Key", 
	"org.obm.push.store.ehcache.UnsynchronizedItemDaoEhcacheImpl$Key"
 */
public class SerializableCompatibilityTest {

	private ImmutableMap<String, Object> serializedClasses;
	
	@Before
	public void setUp() {
		User user = User.Factory.create().createUser("userId@domain", "email@domain", "displayName");
		DeviceId deviceId = new DeviceId("device id");
		Device device = new Device.Factory().create(156, "dev", "userAgent", deviceId, new BigDecimal("12.1"));
		Credentials credentials = new Credentials(user, "pass");

		SyncKey syncKey = new SyncKey("1234");
		Email email = Email.builder().uid(1).read(true).date(date("2004-12-13T21:39:45Z")).build();
		WindowingIndexKey windowingIndexKey = new WindowingIndexKey(user, deviceId, 8);
		
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
		
		MSAttachement msAttachement = new MSAttachement();
		msAttachement.setDisplayName("displayName");
		msAttachement.setEstimatedDataSize(156);
		msAttachement.setFileReference("file reference");
		msAttachement.setMethod(MethodAttachment.EmbeddedMessage);
		
		MSEmail msEmail = MSEmail.builder()
				.answered(true)
				.attachements(ImmutableSet.of(msAttachement))
				.header(MSEmailHeader.builder()
						.subject("a subject")
						.from(new MSAddress("from@domain.org"))
						.to(new MSAddress("to@domain.org"))
						.cc(new MSAddress("cc@domain.org"))
						.replyTo(new MSAddress("replyto@domain.org"))
						.date(date("2008-02-03T20:37:05Z"))
						.build())
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
		
		MSTask task = new MSTask();
		task.setCategories(ImmutableList.of("category"));
		task.setComplete(true);
		task.setDateCompleted(date("2012-02-12T11:22:33"));
		task.setDescription("description");
		task.setDueDate(date("2012-02-02T11:27:33"));
		task.setImportance(2);
		task.setRecurrence(msRecurrence);
		task.setReminderSet(true);
		task.setReminderTime(date("2012-02-02T17:22:33"));
		task.setSensitivity(CalendarSensitivity.PRIVATE);
		task.setStartDate(date("2012-02-02T07:22:33"));
		task.setSubject("subject");
		task.setUtcDueDate(date("2012-02-03T11:24:33"));
		task.setUtcStartDate(date("2012-02-04T11:22:33"));
		
		ItemSyncState itemSyncState = ItemSyncState.builder()
				.id(25)
				.syncDate(date("2005-10-15T11:15:10Z"))
				.syncFiltred(false)
				.syncKey(syncKey)
				.build();
		
		BodyPreference bodyPreference = BodyPreference.builder()
				.allOrNone(true)
				.bodyType(MSEmailBodyType.MIME)
				.truncationSize(5).build();
		SyncCollectionOptions options = new SyncCollectionOptions(ImmutableList.of(bodyPreference));
		options.setConflict(5);
		options.setDeletesAsMoves(false);
		options.setFilterType(FilterType.ONE_DAY_BACK);
		options.setMimeSupport(6);
		options.setMimeTruncation(400);
		options.setTruncation(420);
		
		SyncCollection syncCollection = new SyncCollection(15, "path");
		syncCollection.addChange(new SyncCollectionChange("serverId", "clientId", SyncCommand.ADD, msEvent, PIMDataType.CALENDAR));
		syncCollection.setFetchIds(ImmutableList.of("456"));
		syncCollection.setItemSyncState(itemSyncState);
		syncCollection.setMoreAvailable(false);
		syncCollection.setOptions(options);
		syncCollection.setStatus(SyncStatus.SERVER_ERROR);
		syncCollection.setSyncKey(syncKey);
		syncCollection.setWindowSize(24);
		
		serializedClasses = ImmutableMap.<String, Object>builder()
				.put("org.obm.push.mail.EmailChanges", 
					EmailChanges.builder()
						.changes(ImmutableSet.of(
								email))
						.deletions(ImmutableSet.of(
								Email.builder().uid(2).read(true).date(date("2005-10-13T21:39:45Z")).build()))
						.additions(ImmutableSet.of(
								Email.builder().uid(3).read(true).date(date("2006-08-13T21:39:45Z")).build()))
						.build())
				.put("org.obm.push.mail.bean.WindowingIndexKey", new WindowingIndexKey(user, deviceId, 15))
				.put("org.obm.push.mail.bean.SnapshotKey", SnapshotKey.builder()
						.collectionId(15)
						.deviceId(deviceId)
						.syncKey(syncKey)
						.build())
				.put("org.obm.push.mail.bean.Snapshot", buildCompatibleSnapshot(deviceId, syncKey, email))
				.put("org.obm.push.bean.Device", device)
				.put("org.obm.push.bean.SyncKeysKey",
						SyncKeysKey.builder().collectionId(456).deviceId(deviceId).build())
				.put("org.obm.push.bean.Credentials", credentials)
				.put("org.obm.push.bean.SyncCollection", syncCollection)
				.put("org.obm.push.bean.MSContact", contact)
				.put("org.obm.push.bean.ms.MSEmail", msEmail) 
				.put("org.obm.push.bean.ms.UidMSEmail", UidMSEmail.uidBuilder().email(msEmail).uid(1456l).build()) 
				.put("org.obm.push.bean.ms.MSEmailMetadata", new MSEmailMetadata(true))
				.put("org.obm.push.bean.MSEvent", msEvent)
				.put("org.obm.push.bean.MSTask", task) 
				.put("org.obm.push.bean.SyncCollectionOptions", options)
				.put("org.obm.push.bean.change.item.ItemDeletion", ItemDeletion.builder().serverId("156").build()) 
				.put("org.obm.push.bean.change.item.ItemChange", new ItemChangeBuilder()
						.serverId(":33")
						.withNewFlag(true)
						.withApplicationData(task)
						.build())
				.put("org.obm.push.store.ehcache.WindowingDaoEhcacheImpl$ChunkKey", WindowingDaoEhcacheImpl.chunkKey(windowingIndexKey, 514))
				.put("org.obm.push.store.ehcache.WindowingDaoEhcacheImpl$WindowingIndex", WindowingDaoEhcacheImpl.windowingIndex(45, syncKey)) 
				.put("org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl$Key", new MonitoredCollectionDaoEhcacheImpl.Key(credentials, device)) 
				.put("org.obm.push.store.ehcache.SyncedCollectionDaoEhcacheImpl$Key", SyncedCollectionDaoEhcacheImpl.key(credentials, device, 5))
				.put("org.obm.push.store.ehcache.UnsynchronizedItemDaoEhcacheImpl$Key", UnsynchronizedItemDaoEhcacheImpl.key(credentials, device, 8, UnsynchronizedItemType.ADD))
				.build();
		
	}

	/**
	 * When the Snapshot reference file has been generated, the generateMessageSet method 
	 * extended the MessageSet to the given uidNext. This behavior has been removed by OBMFULL-4748.
	 * This hack is used to say, trust me they are equals !
	 */
	private Snapshot buildCompatibleSnapshot(DeviceId deviceId, SyncKey syncKey, final Email email) {
		final long uidNext = 45l;
		return new Snapshot(deviceId, FilterType.ALL_ITEMS, syncKey, 15, uidNext, ImmutableList.of(email)) {
			@Override
			protected MessageSet generateMessageSet() {
				return MessageSet.builder()
						.add(email.getUid())
						.add(uidNext)
						.build();
			}
		};
	}

	@Ignore("This test is only used to serialize beans into files")
	@Test
	public void generateSerializationFiles() throws Exception {
		for (Map.Entry<String, Object> classAndValue: serializedClasses.entrySet()) {
			
			File file = fileForClass(classAndValue.getKey());
			file.delete();
			
			FileOutputStream fileStream = new FileOutputStream(file);
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileStream);
			objectOutputStream.writeObject(classAndValue.getValue());
			objectOutputStream.flush();
			objectOutputStream.close();
			fileStream.flush();
			fileStream.close();
		}
	}

	@Test
	public void serializedFilesAreCompatibleAndEqualsToBeans() throws Exception {
		for (Map.Entry<String, Object> classAndValue: serializedClasses.entrySet()) {
			String serializedClassFileName = fileForClass(classAndValue.getKey()).getName();
			InputStream serializedClassStream = ClassLoader.getSystemResourceAsStream(serializedClassFileName);
			ObjectInputStream objectInputStream = new ObjectInputStream(serializedClassStream);
			
			assertThat(objectInputStream.readObject())
				.as("Type " + classAndValue.getKey())
				.isEqualTo(classAndValue.getValue());
		}
	}
	
	private File fileForClass(String className) {
		return new File(className + ".serialized");
	}
	
}
