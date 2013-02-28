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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.obm.DateUtils;
import org.obm.push.OpushUser;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.store.UnsynchronizedItemDao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class ResponseWindowingTest {

	@Test(expected=NullPointerException.class)
	public void processWindowSizeDeltaIsNull() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(syncCollection(5), null, user.userDataRequest, SyncClientCommands.empty());
	}
	
	@Test(expected=NullPointerException.class)
	public void processWindowSizeSyncCollectionIsNull() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(null, deltas(2), user.userDataRequest, SyncClientCommands.empty());
	}
	
	@Test(expected=NullPointerException.class)
	public void processWindowSizeUserDataRequestIsNull() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(syncCollection(5), deltas(2), null, SyncClientCommands.empty());
	}

	@Test(expected=NullPointerException.class)
	public void processWindowSizeProcessedClientIdsIsNull() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(syncCollection(5), deltas(2), user.userDataRequest, null);
	}

	
	@Test
	public void processWindowSizeChangesFitTheWindow() {
		OpushUser user = OpushUser.create("usera@domain", "pw");

		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(2);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(syncCollection(5), deltas, user.userDataRequest, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
				
		assertThat(actual).isEqualTo(deltas.getChanges());
	}

	@Test
	public void processWindowSizeChangesDoesntFit() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		DataDelta inputDeltas = deltas(5);
		
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		unsynchronizedItemDao.storeItemsToAdd(user.credentials, user.device, 1, deltasWithOffset(3, 2).getChanges());
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(Sets.newLinkedHashSet(deltasWithOffset(3, 2).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		unsynchronizedItemDao.storeItemsToAdd(user.credentials, user.device, 1, deltasWithOffset(1, 4).getChanges());
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(Sets.newLinkedHashSet(deltasWithOffset(1, 4).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> firstCall = 
				responseWindowingProcessor.windowChanges(syncCollection(2), inputDeltas, user.userDataRequest, SyncClientCommands.empty());
		List<ItemChange> secondCall = 
				responseWindowingProcessor.windowChanges(syncCollection(2), emptyDelta(), user.userDataRequest, SyncClientCommands.empty());
		List<ItemChange> thirdCall = 
				responseWindowingProcessor.windowChanges(syncCollection(2), emptyDelta(), user.userDataRequest, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
		
		assertThat(firstCall).isEqualTo(deltas(2).getChanges());
		assertThat(secondCall).isEqualTo(deltasWithOffset(2, 2).getChanges());
		assertThat(thirdCall).isEqualTo(deltasWithOffset(1, 4).getChanges());
	}

	@Test
	public void processNoNewDataButUnsynchronized() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(Sets.newLinkedHashSet(deltas(3).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(syncCollection(5), emptyDelta(), user.userDataRequest, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltas(3).getChanges());
	}

	@Test
	public void processNewDataAndUnsynchronized() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(Sets.newLinkedHashSet(deltas(3).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(syncCollection(5), deltasWithOffset(2, 3), user.userDataRequest, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltas(5).getChanges());
	}

	@Test
	public void processNewChangesAskedByClientAndUnsynchronized() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(Sets.newLinkedHashSet(deltas(3).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		unsynchronizedItemDao.storeItemsToAdd(user.credentials, user.device, 1, deltas(3).getChanges());
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(
						syncCollection(2), deltasWithOffset(2, 3), 
						user.userDataRequest, clientCommands(deltasWithOffset(2, 3)));
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltasWithOffset(2, 3).getChanges());
	}

	@Test
	public void processNewChangesAskedByClientGreaterThanWindow() {
		OpushUser user = OpushUser.create("usera@domain", "pw");
		
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(Sets.<ItemChange>newLinkedHashSet());
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(
						syncCollection(2), deltas(5),
						user.userDataRequest, clientCommands(deltas(5)));
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltas(5).getChanges());
	}
	
	@Test
	public void windowDeletionsFitTheWindow() {
		OpushUser user = OpushUser.create("usera@domain", "pw");

		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToRemove(user.credentials, user.device, 1)).andReturn(ImmutableSet.<ItemDeletion>of());
		unsynchronizedItemDao.clearItemsToRemove(user.credentials, user.device, 1);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deletions(2);
		List<ItemDeletion> actual = 
				responseWindowingProcessor.windowDeletions(syncCollection(5), deltas, user.userDataRequest, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
				
		assertThat(actual).isEqualTo(deltas.getDeletions());
	}

	@Test
	public void processWindowSizeDeletionsAndChangesFitTheWindows() {
		OpushUser user = OpushUser.create("usera@domain", "pw");

		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		expect(unsynchronizedItemDao.listItemsToRemove(user.credentials, user.device, 1)).andReturn(ImmutableSet.<ItemDeletion>of());
		unsynchronizedItemDao.clearItemsToRemove(user.credentials, user.device, 1);
		expect(unsynchronizedItemDao.hasAnyItemsFor(user.credentials, user.device, 1)).andReturn(false);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(2);
		DataDelta windowedResponse = responseWindowingProcessor.windowedResponse(user.userDataRequest,
				syncCollection(5), deltas, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
				
		assertThat(windowedResponse.getChanges()).isEqualTo(deltas.getChanges());
		assertThat(windowedResponse.getDeletions()).isEqualTo(deltas.getDeletions());
		assertThat(windowedResponse.getSyncDate()).isEqualTo(deltas.getSyncDate());
		assertThat(windowedResponse.getSyncKey()).isEqualTo(deltas.getSyncKey());
		assertThat(windowedResponse.hasMoreAvailable()).isFalse();
	}

	@Test
	public void processWindowSizeDeletionsAndChangesDoNotFitTheWindows() {
		OpushUser user = OpushUser.create("usera@domain", "pw");

		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		expect(unsynchronizedItemDao.listItemsToRemove(user.credentials, user.device, 1)).andReturn(ImmutableSet.<ItemDeletion>of());
		unsynchronizedItemDao.clearItemsToRemove(user.credentials, user.device, 1);
		unsynchronizedItemDao.storeItemsToAdd(user.credentials, user.device, 1, deltasWithOffset(3, 2).getChanges());
		expect(unsynchronizedItemDao.hasAnyItemsFor(user.credentials, user.device, 1)).andReturn(true);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(5);
		DataDelta windowedResponse = responseWindowingProcessor.windowedResponse(user.userDataRequest,
				syncCollection(2), deltas, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
				
		assertThat(windowedResponse.getChanges()).isEqualTo(deltas(2).getChanges());
		assertThat(windowedResponse.getDeletions()).isEqualTo(deltas.getDeletions());
		assertThat(windowedResponse.getSyncDate()).isEqualTo(deltas.getSyncDate());
		assertThat(windowedResponse.getSyncKey()).isEqualTo(deltas.getSyncKey());
		assertThat(windowedResponse.hasMoreAvailable()).isTrue();
	}

	@Test(expected=IllegalStateException.class)
	public void windowChangesWithDuplicates() {
		OpushUser user = OpushUser.create("usera@domain", "pw");

		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, 1)).andReturn(deltas(2).getChanges());
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, 1);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(2);
		try {
			responseWindowingProcessor.windowChanges(syncCollection(2), deltas, user.userDataRequest, SyncClientCommands.empty());
		} catch (IllegalStateException e) {
			verify(unsynchronizedItemDao);
			throw e;
		}
	}
	
	@Test
	public void windowDeletionsWithDuplicates() {
		OpushUser user = OpushUser.create("usera@domain", "pw");

		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ItemDeletion duplicateEntry = ItemDeletion.builder().serverId("12:23").build();
		expect(unsynchronizedItemDao.listItemsToRemove(user.credentials, user.device, 1))
			.andReturn(
					ImmutableSet.of(
							ItemDeletion.builder().serverId("12:22").build(), 
							duplicateEntry));
		unsynchronizedItemDao.clearItemsToRemove(user.credentials, user.device, 1);
		unsynchronizedItemDao.storeItemsToRemove(user.credentials, user.device, 1, 
				ImmutableList.of(ItemDeletion.builder().serverId("12:24").build()));
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = DataDelta.builder().deletions(
				ImmutableList.of(
						duplicateEntry,
						ItemDeletion.builder().serverId("12:24").build()))
				.syncDate(DateUtils.date("2012-01-01"))
				.syncKey(new SyncKey("1234"))
				.build();
		List<ItemDeletion> actual = 
				responseWindowingProcessor.windowDeletions(syncCollection(2), deltas, user.userDataRequest, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
				
		assertThat(actual).containsExactly(
				ItemDeletion.builder().serverId("12:22").build(),
				duplicateEntry);
	}
	
	private SyncClientCommands clientCommands(DataDelta deltasWithOffset) {
		SyncClientCommands.Builder builder = SyncClientCommands.builder();
		for (ItemChange change: deltasWithOffset.getChanges()) {
			builder.putChange(new SyncClientCommands.Update(change.getServerId()));
		}
		return builder.build();
	}

	private DataDelta emptyDelta() {
		return DataDelta.newEmptyDelta(DateUtils.date("2012-01-01"), new SyncKey("1324"));
	}

	private DataDelta deletions(int nbDeletions) {
		return deletionsWithOffset(nbDeletions, 0);
	}
	
	private DataDelta deletionsWithOffset(int nbDeletions, int offset) {
		return DataDelta.builder()
				.changes(buildItemChangeList(0, "addServerId", 0)) 
				.deletions(buildItemDeletions(nbDeletions, "delServerId", offset)) 
				.syncDate(DateUtils.date("2012-01-01"))
				.syncKey(new SyncKey("1324"))
				.build();
	}

	private DataDelta deltas(int nbAdditions) {
		return deltasWithOffset(nbAdditions, 0);
	}
	
	private DataDelta deltasWithOffset(int nbAdditions, int offset) {
		return DataDelta.builder()
				.changes(buildItemChangeList(nbAdditions, "addServerId", offset)) 
				.deletions(buildItemDeletions(0, "delServerId", 0))
				.syncDate(DateUtils.date("2012-01-01"))
				.syncKey(new SyncKey("1324"))
				.build();
	}
	
	private ArrayList<ItemChange> buildItemChangeList(int nbChanges, String serverIdPrefix, int offset) {
		ArrayList<ItemChange> changes = Lists.newArrayList();
		for (int i = 0; i < nbChanges; ++i) {
			changes.add(
					new ItemChangeBuilder()
						.serverId(serverIdPrefix + (i + offset))
						.build()
				);
		}
		return changes;
	}

	private List<ItemDeletion> buildItemDeletions(int nbChanges, String serverIdPrefix, int offset) {
		List<ItemDeletion> changes = Lists.newArrayList();
		for (int i = 0; i < nbChanges; ++i) {
			changes.add(ItemDeletion.builder().serverId(serverIdPrefix + (i + offset)).build());
		}
		return changes;
	}
	
	private SyncCollection syncCollection(int windowSize) {
		SyncCollection syncCollection = new SyncCollection(1, "path");
		syncCollection.setWindowSize(windowSize);
		return syncCollection;
	}
	
}
