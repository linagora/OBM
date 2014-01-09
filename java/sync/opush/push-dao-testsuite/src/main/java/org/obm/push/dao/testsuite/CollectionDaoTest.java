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
package org.obm.push.dao.testsuite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.obm.DateUtils.dateUTC;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSnapshotDao;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
public abstract class CollectionDaoTest {

	@Inject CollectionDao collectionDao;
	@Inject FolderSnapshotDao folderSnapshotDao;
	
	protected Device device;
	
	@Before
	public void setUp() {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test(expected=DaoException.class)
	public void testAllocateFolderSyncStateUniqueSyncKey() throws Exception {
		collectionDao.allocateNewFolderSyncState(device, new SyncKey("123"));
		collectionDao.allocateNewFolderSyncState(device, new SyncKey("123"));
	}

	@Test(expected=DaoException.class)
	public void testAllocateFolderSyncStateUniqueSyncKeyButOtherDevice() throws Exception {
		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		collectionDao.allocateNewFolderSyncState(device, new SyncKey("123"));
		collectionDao.allocateNewFolderSyncState(otherDevice, new SyncKey("123"));
	}
	
	@Test
	public void testGetUserCollectionsWhenEmpty() throws Exception {
		SyncKey sk = new SyncKey("123");
		List<String> result = collectionDao.getUserCollections(FolderSyncState.builder().syncKey(sk).build());
		assertThat(result).isEmpty();
	}
	
	@Test
	public void testGetUserCollectionsForAnotherSyncKey() throws Exception {
		SyncKey otherSk = new SyncKey("123");
		FolderSyncState newFolderSyncState = collectionDao.allocateNewFolderSyncState(device, otherSk);
		folderSnapshotDao.createFolderSnapshot(newFolderSyncState.getId(), ImmutableList.of(
				collectionDao.addCollectionMapping(device, "The collection")));

		SyncKey sk = new SyncKey("456");
		FolderSyncState otherFolderSyncState = collectionDao.allocateNewFolderSyncState(device, sk);
		folderSnapshotDao.createFolderSnapshot(otherFolderSyncState.getId(), ImmutableList.of(
				collectionDao.addCollectionMapping(device, "The right collection")));
		
		List<String> result = collectionDao.getUserCollections(FolderSyncState.builder().syncKey(sk).build());
		assertThat(result).containsOnly("The right collection");
	}
	
	@Test
	public void testGetUserCollectionsWhenMany() throws Exception {
		SyncKey sk = new SyncKey("123");
		FolderSyncState newFolderSyncState = collectionDao.allocateNewFolderSyncState(device, sk);
		folderSnapshotDao.createFolderSnapshot(newFolderSyncState.getId(), ImmutableList.of(
				collectionDao.addCollectionMapping(device, "The collection"),
				collectionDao.addCollectionMapping(device, "The right collection"),
				collectionDao.addCollectionMapping(device, "The new collection")));
		
		List<String> result = collectionDao.getUserCollections(FolderSyncState.builder().syncKey(sk).build());
		assertThat(result).containsOnly("The collection", "The right collection", "The new collection");
	}
	
	@Test
	public void testGetCollectionPath() throws Exception {
		int col1 = collectionDao.addCollectionMapping(device, "The collection");
		int col2 = collectionDao.addCollectionMapping(device, "The right collection");
		int col3 = collectionDao.addCollectionMapping(device, "The new collection");
		
		assertThat(collectionDao.getCollectionPath(col1)).isEqualTo("The collection");
		assertThat(collectionDao.getCollectionPath(col2)).isEqualTo("The right collection");
		assertThat(collectionDao.getCollectionPath(col3)).isEqualTo("The new collection");
	}
	
	@Test(expected=CollectionNotFoundException.class)
	public void testGetCollectionPathWhichDoesNotExist() throws Exception {
		collectionDao.getCollectionPath(1337);
	}
	
	@Test
	public void testGetCollectionMapping() throws Exception {
		int col1 = collectionDao.addCollectionMapping(device, "The collection");
		int col2 = collectionDao.addCollectionMapping(device, "The right collection");
		int col3 = collectionDao.addCollectionMapping(device, "The new collection");
		
		assertThat(collectionDao.getCollectionMapping(device, "The collection")).isEqualTo(col1);
		assertThat(collectionDao.getCollectionMapping(device, "The right collection")).isEqualTo(col2);
		assertThat(collectionDao.getCollectionMapping(device, "The new collection")).isEqualTo(col3);
	}
	
	@Test
	public void testGetCollectionMappingWithWrongCollection() throws Exception {
		collectionDao.addCollectionMapping(device, "The collection");
		assertThat(collectionDao.getCollectionMapping(device, "Non existing collection")).isNull();
	}
	
	@Test
	public void testGetCollectionMappingWithWrongDevice() throws Exception {
		collectionDao.addCollectionMapping(device, "The collection");
		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		assertThat(collectionDao.getCollectionMapping(otherDevice, "The collection")).isNull();
	}
	
	@Test(expected=DaoException.class)
	public void testUpdateStateOnNonExistingCollection() throws Exception {
		collectionDao.updateState(device, 1337, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
	}
	
	@Test(expected=DaoException.class)
	public void testUpdateStateOnDifferentDeviceButSameSyncKey() throws Exception {
		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		int col1 = collectionDao.addCollectionMapping(device, "The collection");
		int col2 = collectionDao.addCollectionMapping(otherDevice, "The collection 2");
		
		collectionDao.updateState(device, col1, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
		collectionDao.updateState(otherDevice, col2, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
	}
	
	@Test
	public void testUpdateState() throws Exception {
		int colId = collectionDao.addCollectionMapping(device, "The collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
		
		assertThat(state.getId()).isNotZero().isPositive();
		assertThat(state.getSyncDate()).isEqualTo(dateUTC("2012-05-04T11:39:37"));
		assertThat(state.getSyncKey()).isEqualTo(new SyncKey("123"));
	}
	
	@Test
	public void testResetCollectionOnNotExistingCollectionProducesNoException() throws Exception {
		collectionDao.resetCollection(device, 1337);
	}
	
	@Test
	public void testResetCollection() throws Exception {
		int col1 = collectionDao.addCollectionMapping(device, "The collection");
		int col2 = collectionDao.addCollectionMapping(device, "The right collection");
		int col3 = collectionDao.addCollectionMapping(device, "The new collection");
		ItemSyncState st1 = collectionDao.updateState(device, col1, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
		ItemSyncState st2 = collectionDao.updateState(device, col2, new SyncKey("456"), dateUTC("2012-05-05T11:39:37"));
		collectionDao.updateState(device, col3, new SyncKey("789"), dateUTC("2012-05-06T11:39:37"));
		
		collectionDao.resetCollection(device, col3);
		
		assertThat(collectionDao.lastKnownState(device, col1)).isEqualTo(st1);
		assertThat(collectionDao.lastKnownState(device, col2)).isEqualTo(st2);
		assertThat(collectionDao.lastKnownState(device, col3)).isNull();
	}
	
	@Test
	public void testFindItemStateForKeyWhenNonExistingKey() throws Exception {
		assertThat(collectionDao.findItemStateForKey(new SyncKey("456"))).isNull();
	}
	
	@Test
	public void testFindItemStateForKey() throws Exception {
		int col = collectionDao.addCollectionMapping(device, "The collection");
		ItemSyncState st = collectionDao.updateState(device, col, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));

		assertThat(collectionDao.findItemStateForKey(new SyncKey("123"))).isEqualTo(st);
	}
	
	@Test
	public void testFindFolderStateForKeyWhenNonExistingKey() throws Exception {
		assertThat(collectionDao.findFolderStateForKey(new SyncKey("123"))).isNull();
	}
	
	@Test
	public void testFindFolderStateForKey() throws Exception {
		FolderSyncState state = collectionDao.allocateNewFolderSyncState(device, new SyncKey("123"));
		
		assertThat(collectionDao.findFolderStateForKey(new SyncKey("123"))).isEqualTo(state);
	}
	
	@Test
	public void testLastKnownState() throws Exception {
		int colId = collectionDao.addCollectionMapping(device, "The collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
		
		assertThat(collectionDao.lastKnownState(device, colId)).isEqualTo(state);
	}
	
	@Test
	public void testLastKnownStateWithOtherDevice() throws Exception {
		int colId = collectionDao.addCollectionMapping(device, "The collection");
		collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
		
		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		assertThat(collectionDao.lastKnownState(otherDevice, colId)).isNull();
	}
	
	@Test
	public void testLastKnownStateWithOtherCollectionId() throws Exception {
		int colId = collectionDao.addCollectionMapping(device, "The collection");
		collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
		
		assertThat(collectionDao.lastKnownState(device, 1337)).isNull();
	}
	
	@Test
	public void testLastKnownStateAfterManyUpdate() throws Exception {
		int colId = collectionDao.addCollectionMapping(device, "The collection");
		ItemSyncState st1 = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2012-05-04T11:39:37"));
		ItemSyncState st2 = collectionDao.updateState(device, colId, new SyncKey("456"), dateUTC("2012-05-05T11:39:37"));
		ItemSyncState st3 = collectionDao.updateState(device, colId, new SyncKey("789"), dateUTC("2012-05-06T11:39:37"));
		
		assertThat(collectionDao.lastKnownState(device, colId))
			.isNotEqualTo(st1)
			.isNotEqualTo(st2)
			.isEqualTo(st3);
	}
}