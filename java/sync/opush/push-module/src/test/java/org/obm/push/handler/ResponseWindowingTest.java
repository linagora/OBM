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
package org.obm.push.handler;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.store.UnsynchronizedItemDao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(SlowFilterRunner.class)
public class ResponseWindowingTest {

	@Test(expected=NullPointerException.class)
	public void processWindowSizeDeltaIsNull() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(syncCollection(5, new SyncKey("123")), new SyncKey("456"), null, SyncClientCommands.empty());
	}
	
	@Test(expected=NullPointerException.class)
	public void processWindowSizeSyncCollectionIsNull() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(null, new SyncKey("456"), deltas(2), SyncClientCommands.empty());
	}
	
	@Test(expected=NullPointerException.class)
	public void processWindowSizeUserDataRequestIsNull() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(syncCollection(5, new SyncKey("123")), new SyncKey("456"), deltas(2), null);
	}

	@Test(expected=NullPointerException.class)
	public void processWindowSizeProcessedClientIdsIsNull() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		responseWindowingProcessor.windowChanges(syncCollection(5, new SyncKey("123")), new SyncKey("456"), deltas(2), null);
	}

	
	@Test
	public void processWindowSizeChangesFitTheWindow() {
		SyncKey syncKey = new SyncKey("123");
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(2);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(syncCollection(5, syncKey), new SyncKey("456"), deltas, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
				
		assertThat(actual).isEqualTo(deltas.getChanges());
	}

	@Test
	public void processWindowSizeChangesDoesntFit() {
		DataDelta inputDeltas = deltas(5);
		
		SyncKey syncKey = new SyncKey("123");
		SyncKey syncKey2 = new SyncKey("456");
		SyncKey syncKey3 = new SyncKey("789");
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		unsynchronizedItemDao.storeItemsToAdd(syncKey2, deltasWithOffset(3, 2).getChanges());
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey2)).andReturn(Sets.newLinkedHashSet(deltasWithOffset(3, 2).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(syncKey2);
		unsynchronizedItemDao.storeItemsToAdd(syncKey3, deltasWithOffset(1, 4).getChanges());
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey3 )).andReturn(Sets.newLinkedHashSet(deltasWithOffset(1, 4).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(syncKey3);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> firstCall = 
				responseWindowingProcessor.windowChanges(syncCollection(2, syncKey), syncKey2, inputDeltas, SyncClientCommands.empty());
		List<ItemChange> secondCall = 
				responseWindowingProcessor.windowChanges(syncCollection(2, syncKey2), syncKey3, emptyDelta(), SyncClientCommands.empty());
		List<ItemChange> thirdCall = 
				responseWindowingProcessor.windowChanges(syncCollection(2, syncKey3), new SyncKey("901"), emptyDelta(), SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
		
		assertThat(firstCall).isEqualTo(deltas(2).getChanges());
		assertThat(secondCall).isEqualTo(deltasWithOffset(2, 2).getChanges());
		assertThat(thirdCall).isEqualTo(deltasWithOffset(1, 4).getChanges());
	}

	@Test
	public void processNoNewDataButUnsynchronized() {
		SyncKey syncKey = new SyncKey("123");
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(Sets.newLinkedHashSet(deltas(3).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(syncCollection(5, syncKey), new SyncKey("456"), emptyDelta(), SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltas(3).getChanges());
	}

	@Test
	public void processNewDataAndUnsynchronized() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		SyncKey syncKey = new SyncKey("123");
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(Sets.newLinkedHashSet(deltas(3).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(syncCollection(5, syncKey), new SyncKey("456"), deltasWithOffset(2, 3), SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltas(5).getChanges());
	}

	@Test
	public void processNewChangesAskedByClientAndUnsynchronized() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		SyncKey syncKey = new SyncKey("123");
		SyncKey newSyncKey = new SyncKey("456");
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(Sets.newLinkedHashSet(deltas(3).getChanges()));
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		unsynchronizedItemDao.storeItemsToAdd(newSyncKey, deltas(3).getChanges());
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(
						syncCollection(2, syncKey), newSyncKey, deltasWithOffset(2, 3), 
						clientCommands(deltasWithOffset(2, 3)));
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltasWithOffset(2, 3).getChanges());
	}

	@Test
	public void processNewChangesAskedByClientGreaterThanWindow() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		SyncKey syncKey = new SyncKey("132");
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(Sets.<ItemChange>newLinkedHashSet());
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		List<ItemChange> actual = 
				responseWindowingProcessor.windowChanges(
						syncCollection(2, syncKey), new SyncKey("456"), deltas(5),
						clientCommands(deltas(5)));
		
		verify(unsynchronizedItemDao);
		
		assertThat(actual).isEqualTo(deltas(5).getChanges());
	}
	
	@Test
	public void windowDeletionsFitTheWindow() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		SyncKey syncKey = new SyncKey("123");
		expect(unsynchronizedItemDao.listItemsToRemove(syncKey)).andReturn(ImmutableSet.<ItemDeletion>of());
		unsynchronizedItemDao.clearItemsToRemove(syncKey);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deletions(2);
		List<ItemDeletion> actual = 
				responseWindowingProcessor.windowDeletions(syncCollection(5, syncKey), new SyncKey("456"), deltas, SyncClientCommands.empty());
		
		verify(unsynchronizedItemDao);
				
		assertThat(actual).isEqualTo(deltas.getDeletions());
	}

	@Test
	public void processWindowSizeDeletionsAndChangesFitTheWindows() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		SyncKey syncKey = new SyncKey("123");
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		expect(unsynchronizedItemDao.listItemsToRemove(syncKey)).andReturn(ImmutableSet.<ItemDeletion>of());
		unsynchronizedItemDao.clearItemsToRemove(syncKey);
		expect(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).andReturn(false);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(2);
		DataDelta windowedResponse = responseWindowingProcessor.windowedResponse(syncCollection(5, syncKey), deltas, SyncClientCommands.empty(), new SyncKey("456"));
		
		verify(unsynchronizedItemDao);
				
		assertThat(windowedResponse.getChanges()).isEqualTo(deltas.getChanges());
		assertThat(windowedResponse.getDeletions()).isEqualTo(deltas.getDeletions());
		assertThat(windowedResponse.getSyncDate()).isEqualTo(deltas.getSyncDate());
		assertThat(windowedResponse.getSyncKey()).isEqualTo(deltas.getSyncKey());
		assertThat(windowedResponse.hasMoreAvailable()).isFalse();
	}

	@Test
	public void processWindowSizeDeletionsAndChangesDoNotFitTheWindows() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		SyncKey syncKey = new SyncKey("123");
		SyncKey newSyncKey = new SyncKey("456");
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(ImmutableSet.<ItemChange>of());
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		expect(unsynchronizedItemDao.listItemsToRemove(syncKey)).andReturn(ImmutableSet.<ItemDeletion>of());
		unsynchronizedItemDao.clearItemsToRemove(syncKey);
		unsynchronizedItemDao.storeItemsToAdd(newSyncKey, deltasWithOffset(3, 2).getChanges());
		expect(unsynchronizedItemDao.hasAnyItemsFor(syncKey)).andReturn(true);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(5);
		DataDelta windowedResponse = responseWindowingProcessor.windowedResponse(syncCollection(2, syncKey), deltas, SyncClientCommands.empty(), newSyncKey);
		
		verify(unsynchronizedItemDao);
				
		assertThat(windowedResponse.getChanges()).isEqualTo(deltas(2).getChanges());
		assertThat(windowedResponse.getDeletions()).isEqualTo(deltas.getDeletions());
		assertThat(windowedResponse.getSyncDate()).isEqualTo(deltas.getSyncDate());
		assertThat(windowedResponse.getSyncKey()).isEqualTo(deltas.getSyncKey());
		assertThat(windowedResponse.hasMoreAvailable()).isTrue();
	}

	@Test(expected=IllegalStateException.class)
	public void windowChangesWithDuplicates() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		SyncKey syncKey = new SyncKey("132");
		expect(unsynchronizedItemDao.listItemsToAdd(syncKey)).andReturn(deltas(2).getChanges());
		unsynchronizedItemDao.clearItemsToAdd(syncKey);
		replay(unsynchronizedItemDao);
		
		ResponseWindowingService responseWindowingProcessor = new ResponseWindowingService(unsynchronizedItemDao);
		
		DataDelta deltas = deltas(2);
		try {
			responseWindowingProcessor.windowChanges(syncCollection(2, syncKey), new SyncKey("456"), deltas, SyncClientCommands.empty());
		} catch (IllegalStateException e) {
			verify(unsynchronizedItemDao);
			throw e;
		}
	}
	
	@Test
	public void windowDeletionsWithDuplicates() {
		UnsynchronizedItemDao unsynchronizedItemDao = createMock(UnsynchronizedItemDao.class);
		ItemDeletion duplicateEntry = ItemDeletion.builder().serverId("12:23").build();
		SyncKey syncKey = new SyncKey("123");
		SyncKey newSyncKey = new SyncKey("456");
		expect(unsynchronizedItemDao.listItemsToRemove(syncKey))
			.andReturn(
					ImmutableSet.of(
							ItemDeletion.builder().serverId("12:22").build(), 
							duplicateEntry));
		unsynchronizedItemDao.clearItemsToRemove(syncKey);
		unsynchronizedItemDao.storeItemsToRemove(newSyncKey, ImmutableList.of(ItemDeletion.builder().serverId("12:24").build()));
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
				responseWindowingProcessor.windowDeletions(syncCollection(2, syncKey), newSyncKey, deltas, SyncClientCommands.empty());
		
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
	
	private AnalysedSyncCollection syncCollection(int windowSize, SyncKey syncKey) {
		return AnalysedSyncCollection.builder()
				.collectionId(1)
				.syncKey(syncKey)
				.windowSize(windowSize)
				.build();
	}
	
}
