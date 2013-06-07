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
package org.obm.push.mail.bean;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.mail.bean.Email;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.mail.bean.Snapshot.Builder;
import org.obm.push.utils.DateUtils;

import com.google.common.collect.ImmutableList;

public class SnapshotTest {
	
	@Test (expected=IllegalArgumentException.class)
	public void testNullDeviceId() {
		Snapshot.builder()
			.filterType(FilterType.ONE_DAY_BACK)
			.syncKey(new SyncKey("syncKey"))
			.collectionId(1)
			.build();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testNullFilterType() {
		Snapshot.builder()
			.deviceId(new DeviceId("deviceId"))
			.syncKey(new SyncKey("syncKey"))
			.collectionId(1)
			.build();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testNullCollectionId() {
		Snapshot.builder()
			.deviceId(new DeviceId("deviceId"))
			.filterType(FilterType.ONE_DAY_BACK)
			.syncKey(new SyncKey("syncKey"))
			.build();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testNullSyncKey() {
		Snapshot.builder()
			.deviceId(new DeviceId("deviceId"))
			.filterType(FilterType.ONE_DAY_BACK)
			.collectionId(1)
			.build();
	}
	
	@Test
	public void testBuilder() {
		FilterType filterType = FilterType.ONE_DAY_BACK;
		DeviceId deviceId = new DeviceId("deviceId");
		SyncKey synckey = new SyncKey("syncKey");
		Integer collectionId = 1;
		long uidNext = 2;
		
		long emailUID = 3;
		Email email = Email.builder()
				.uid(emailUID)
				.read(false)
				.date(DateUtils.getCurrentDate())
				.build();
		long emailUID2 = 4;
		Email email2 = Email.builder()
				.uid(emailUID2)
				.read(true)
				.date(DateUtils.getCurrentDate())
				.build();
		
		Snapshot snapshot = Snapshot.builder()
			.deviceId(deviceId)
			.filterType(filterType)
			.syncKey(synckey)
			.collectionId(collectionId)
			.uidNext(uidNext)
			.addEmail(email)
			.addEmail(email2)
			.build();
		
		assertThat(snapshot.getDeviceId()).isEqualTo(deviceId);
		assertThat(snapshot.getFilterType()).isEqualTo(filterType);
		assertThat(snapshot.getCollectionId()).isEqualTo(collectionId);
		assertThat(snapshot.getUidNext()).isEqualTo(uidNext);
		assertThat(snapshot.getEmails()).containsExactly(email, email2);
		assertThat(snapshot.getMessageSet().asDiscreteValues()).containsOnly(emailUID, emailUID2);
	}
	
	@Test
	public void testContainsAllEmptyArgument() {
		
		Snapshot snapshot = defaultSnapshotBuilder()
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.build();
		assertThat(snapshot.containsAllIds(ImmutableList.<String>of())).isTrue();
	}

	@Test(expected=NullPointerException.class)
	public void testContainsAllNullArgument() {
		
		Snapshot snapshot = defaultSnapshotBuilder()
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.build();
		assertThat(snapshot.containsAllIds(null)).isTrue();
	}
	
	@Test(expected=InvalidServerId.class)
	public void testContainsAllInvalidArgument() {
		
		Snapshot snapshot = defaultSnapshotBuilder()
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.build();
		snapshot.containsAllIds(ImmutableList.<String>of("evil value"));
	}
	
	@Test
	public void testContainsAllMatchElement() {
		
		Snapshot snapshot = defaultSnapshotBuilder()
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.build();
		assertThat(snapshot.containsAllIds(ImmutableList.of("1:1"))).isTrue();
	}
	
	@Test
	public void testContainsAllMatchElement2() {
		Snapshot snapshot = defaultSnapshotBuilder()
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.addEmail(Email.builder()
						.uid(2)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.addEmail(Email.builder()
						.uid(3)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.addEmail(Email.builder()
						.uid(4)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.addEmail(Email.builder()
						.uid(223)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.build();
		assertThat(snapshot.containsAllIds(ImmutableList.of("1:1", "1:2", "1:3", "1:4", "1:223"))).isTrue();
	}
	
	@Test
	public void testContainsAllDontMatchElement() {
		
		Snapshot snapshot = defaultSnapshotBuilder()
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.build();
		assertThat(snapshot.containsAllIds(ImmutableList.of("1:2"))).isFalse();
	}
	
	@Test
	public void testContainsAllDontMatchElement2() {
		Snapshot snapshot = defaultSnapshotBuilder()
				.addEmail(Email.builder()
						.uid(1)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.addEmail(Email.builder()
						.uid(2)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.addEmail(Email.builder()
						.uid(3)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.addEmail(Email.builder()
						.uid(4)
						.read(true)
						.date(DateUtils.getCurrentDate())
						.build())
				.build();
		assertThat(snapshot.containsAllIds(ImmutableList.of("1:1", "1:2", "1:3", "1:4", "1:5"))).isFalse();
	}
	
	private Builder defaultSnapshotBuilder() {
		return Snapshot.builder()
				.deviceId(new DeviceId("deviceId"))
				.filterType(FilterType.ONE_DAY_BACK)
				.syncKey(new SyncKey("syncKey"))
				.collectionId(1)
				.uidNext(2);
	}
}
