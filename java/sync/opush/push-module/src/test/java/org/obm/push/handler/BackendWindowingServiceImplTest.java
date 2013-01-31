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
package org.obm.push.handler;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.backend.BackendWindowingService.BackendChangesProvider;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;

import com.google.common.collect.ImmutableList;


public class BackendWindowingServiceImplTest {

	private IMocksControl mocks;
	private ResponseWindowingService responseWindowingService;

	private BackendWindowingServiceImpl testee;
	
	private User user;
	private Device device;
	private Credentials credentials;
	private UserDataRequest udr;
	
	@Before
	public void setUp() {
		user = Factory.create().createUser("test@test", "test@domain", "displayName");
		device = new Device.Factory().create(null, "iPhone", "iOs 5", new DeviceId("my phone"), null);
		credentials = new Credentials(user, "password");
		udr = new UserDataRequest(credentials, "noCommand", device);
		
		mocks = createControl();
		responseWindowingService = mocks.createMock(ResponseWindowingService.class);
		
		testee = new BackendWindowingServiceImpl(responseWindowingService);
	}

	@Test(expected=NullPointerException.class)
	public void testRequireUserDataRequest() {
		int collectionId = 15;
		UserDataRequest userDataRequest = null;
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		SyncClientCommands clientCommands = SyncClientCommands.empty();
		BackendChangesProvider backendChangesProvider = mocks.createMock(BackendChangesProvider.class);

		testee.windowedChanges(userDataRequest, syncCollection, clientCommands, backendChangesProvider);
	}

	@Test(expected=NullPointerException.class)
	public void testRequireSyncCollection() {
		SyncCollection syncCollection = null;
		SyncClientCommands clientCommands = SyncClientCommands.empty();
		BackendChangesProvider backendChangesProvider = mocks.createMock(BackendChangesProvider.class);

		testee.windowedChanges(udr, syncCollection, clientCommands, backendChangesProvider);
	}

	@Test(expected=NullPointerException.class)
	public void testRequireClientCommands() {
		int collectionId = 15;
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		SyncClientCommands clientCommands = null;
		BackendChangesProvider backendChangesProvider = mocks.createMock(BackendChangesProvider.class);

		testee.windowedChanges(udr, syncCollection, clientCommands, backendChangesProvider);
	}

	@Test(expected=NullPointerException.class)
	public void testRequireBackendChangesProvider() {
		int collectionId = 15;
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		SyncClientCommands clientCommands = SyncClientCommands.empty();
		BackendChangesProvider backendChangesProvider = null;

		testee.windowedChanges(udr, syncCollection, clientCommands, backendChangesProvider);
	}

	@Test
	public void testWindowedChangesWhenNoPendingResponseFittingWindowSize() {
		int collectionId = 15;
		int windowSize = 10;
		SyncKey allocatedSyncKey = new SyncKey("456");
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		syncCollection.setWindowSize(windowSize);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setItemSyncState(ItemSyncState.builder()
				.syncDate(date("2012-05-04T11:22:53"))
				.syncKey(new SyncKey("123"))
				.id(5)
				.build()); 
		SyncClientCommands clientCommands = SyncClientCommands.empty();

		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(false);
		
		List<ItemChange> changes = ImmutableList.of(new ItemChange("1"), new ItemChange("2"));
		List<ItemDeletion> deletions = ImmutableList.of(ItemDeletion.builder().serverId("3").build());
		DataDelta backendDataDelta = DataDelta.builder()
				.changes(changes)
				.deletions(deletions)
				.syncDate(date("2012-05-04T12:22:53"))
				.syncKey(allocatedSyncKey)
				.build();
		
		BackendChangesProvider backendChangesProvider = mocks.createMock(BackendChangesProvider.class);
		expect(backendChangesProvider.getAllChanges()).andReturn(backendDataDelta);
		expect(responseWindowingService.windowChanges(syncCollection, backendDataDelta, udr, clientCommands)).andReturn(changes);
		expect(responseWindowingService.windowDeletions(syncCollection, backendDataDelta, udr, clientCommands)).andReturn(deletions);
		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(false);
		
		mocks.replay();
		DataDelta dataDelta = testee.windowedChanges(udr, syncCollection, clientCommands, backendChangesProvider);
		mocks.verify();
		
		assertThat(dataDelta.getSyncDate()).isEqualTo(date("2012-05-04T12:22:53"));
		assertThat(dataDelta.getSyncKey()).isEqualTo(allocatedSyncKey);
		assertThat(dataDelta.getChanges()).containsOnly(new ItemChange("1"), new ItemChange("2"));
		assertThat(dataDelta.getDeletions()).containsOnly(ItemDeletion.builder().serverId("3").build());
		assertThat(dataDelta.hasMoreAvailable()).isFalse();
	}

	@Test
	public void testWindowedChangesWhenNoPendingResponseNotFittingWindowSize() {
		int collectionId = 15;
		int windowSize = 1;
		SyncKey allocatedSyncKey = new SyncKey("456");
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		syncCollection.setWindowSize(windowSize);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setItemSyncState(ItemSyncState.builder()
				.syncDate(date("2012-05-04T11:22:53"))
				.syncKey(new SyncKey("123"))
				.id(5)
				.build()); 
		SyncClientCommands clientCommands = SyncClientCommands.empty();

		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(false);
		
		List<ItemChange> changes = ImmutableList.of(new ItemChange("1"), new ItemChange("2"));
		List<ItemDeletion> deletions = ImmutableList.of(ItemDeletion.builder().serverId("3").build());
		DataDelta backendDataDelta = DataDelta.builder()
				.changes(changes)
				.deletions(deletions)
				.syncDate(date("2012-05-04T12:22:53"))
				.syncKey(allocatedSyncKey)
				.build();
		
		BackendChangesProvider backendChangesProvider = mocks.createMock(BackendChangesProvider.class);
		expect(backendChangesProvider.getAllChanges()).andReturn(backendDataDelta);
		expect(responseWindowingService.windowChanges(syncCollection, backendDataDelta, udr, clientCommands))
			.andReturn(ImmutableList.of(new ItemChange("1")));
		expect(responseWindowingService.windowDeletions(syncCollection, backendDataDelta, udr, clientCommands))
			.andReturn(ImmutableList.<ItemDeletion>of());
		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(true);
	
		mocks.replay();
		DataDelta dataDelta = testee.windowedChanges(udr, syncCollection, clientCommands, backendChangesProvider);
		mocks.verify();
		
		assertThat(dataDelta.getSyncDate()).isEqualTo(date("2012-05-04T12:22:53"));
		assertThat(dataDelta.getSyncKey()).isEqualTo(allocatedSyncKey);
		assertThat(dataDelta.getChanges()).containsOnly(new ItemChange("1"));
		assertThat(dataDelta.getDeletions()).isEmpty();
		assertThat(dataDelta.hasMoreAvailable()).isTrue();
	}
	
	@Test
	public void testWindowedChangesWhenPendingResponseFittingWindowSize() {
		int collectionId = 15;
		int windowSize = 10;
		SyncKey syncKey = new SyncKey("123");
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(date("2012-05-04T11:22:53"))
				.syncKey(syncKey)
				.id(5)
				.build();
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		syncCollection.setWindowSize(windowSize);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setItemSyncState(syncState);
		syncCollection.setSyncKey(syncKey);
		SyncClientCommands clientCommands = SyncClientCommands.empty();

		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(true);
		
		DataDelta continueWindowingDelta = DataDelta.newEmptyDelta(syncState.getSyncDate(), syncKey);
		BackendChangesProvider backendChangesProvider = mocks.createMock(BackendChangesProvider.class);
		
		expect(responseWindowingService.windowChanges(syncCollection, continueWindowingDelta, udr, clientCommands))
			.andReturn(ImmutableList.of(new ItemChange("123")));
		expect(responseWindowingService.windowDeletions(syncCollection, continueWindowingDelta, udr, clientCommands))
			.andReturn(ImmutableList.of(ItemDeletion.builder().serverId("456").build()));
		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(false);
		
		mocks.replay();
		DataDelta dataDelta = testee.windowedChanges(udr, syncCollection, clientCommands, backendChangesProvider);
		mocks.verify();
		
		assertThat(dataDelta.getSyncDate()).isEqualTo(syncState.getSyncDate());
		assertThat(dataDelta.getSyncKey()).isEqualTo(syncKey);
		assertThat(dataDelta.getChanges()).containsOnly(new ItemChange("123"));
		assertThat(dataDelta.getDeletions()).containsOnly(ItemDeletion.builder().serverId("456").build());
		assertThat(dataDelta.hasMoreAvailable()).isFalse();
	}
	
	@Test
	public void testWindowedChangesWhenPendingResponseNotFittingWindowSize() {
		int collectionId = 15;
		int windowSize = 1;
		SyncKey syncKey = new SyncKey("123");
		ItemSyncState syncState = ItemSyncState.builder()
				.syncDate(date("2012-05-04T11:22:53"))
				.syncKey(syncKey)
				.id(5)
				.build();
		SyncCollection syncCollection = new SyncCollection(collectionId, "obm:\\\\test@test\\email\\INBOX");
		syncCollection.setWindowSize(windowSize);
		syncCollection.setDataType(PIMDataType.EMAIL);
		syncCollection.setItemSyncState(syncState);
		syncCollection.setSyncKey(syncKey);
		SyncClientCommands clientCommands = SyncClientCommands.empty();

		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(true);
		
		DataDelta continueWindowingDelta = DataDelta.newEmptyDelta(syncState.getSyncDate(), syncKey);
		BackendChangesProvider backendChangesProvider = mocks.createMock(BackendChangesProvider.class);
		
		expect(responseWindowingService.windowChanges(syncCollection, continueWindowingDelta, udr, clientCommands))
			.andReturn(ImmutableList.of(new ItemChange("123")));
		expect(responseWindowingService.windowDeletions(syncCollection, continueWindowingDelta, udr, clientCommands))
			.andReturn(ImmutableList.<ItemDeletion>of());
		expect(responseWindowingService.hasPendingResponse(credentials, device, collectionId)).andReturn(true);
		
		mocks.replay();
		DataDelta dataDelta = testee.windowedChanges(udr, syncCollection, clientCommands, backendChangesProvider);
		mocks.verify();
		
		assertThat(dataDelta.getSyncDate()).isEqualTo(syncState.getSyncDate());
		assertThat(dataDelta.getSyncKey()).isEqualTo(syncKey);
		assertThat(dataDelta.getChanges()).containsOnly(new ItemChange("123"));
		assertThat(dataDelta.getDeletions()).isEmpty();
		assertThat(dataDelta.hasMoreAvailable()).isTrue();
	}
}
