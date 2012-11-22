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
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequest;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestCategory;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestInstanceType;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrence;
import org.obm.push.bean.msmeetingrequest.MSMeetingRequestRecurrenceType;
import org.obm.push.utils.SerializableInputStream;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
		syncCollection.addChange(new SyncCollectionChange("serverId", "clientId", "modType", new MSContact(), PIMDataType.CALENDAR));
		syncCollection.setItemSyncState(new ItemSyncState("key", new Date()));
		objectOutputStream.writeObject(syncCollection);
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
			.uid(1l)
			.header(MSEmailHeader.builder().build())
			.body(new org.obm.push.bean.ms.MSEmailBody(new SerializableInputStream(
					new ByteArrayInputStream("message".getBytes())), MSEmailBodyType.PlainText, 0, Charsets.UTF_8, false))
			.meetingRequest(
					new MSMeetingRequest.MsMeetingRequestBuilder()
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
	public void testMSEvent() throws IOException {
		MSEvent msEvent = new MSEvent();
		msEvent.addAttendee(new MSAttendee());
		msEvent.setRecurrence(new MSRecurrence());
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
		Device obj = new Device(1, "toto", "toto", new Properties());
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
}
