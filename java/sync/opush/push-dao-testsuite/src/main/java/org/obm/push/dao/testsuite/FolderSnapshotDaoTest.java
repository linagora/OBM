/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSnapshotDao;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
public abstract class FolderSnapshotDaoTest {

	@Inject protected CollectionDao collectionDao;
	@Inject protected FolderSnapshotDao folderDao;
	
	private Device device;

	@Before
	public void setUp() {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test(expected=DaoException.class)
	public void testCreateFolderSnapshotWhenNonExistingFolderState() {
		int nonExistingFolderStateId = 1337;
		int colId = collectionDao.addCollectionMapping(device, "collection");
		folderDao.createFolderSnapshot(nonExistingFolderStateId, ImmutableList.of(colId));
	}

	@Test(expected=DaoException.class)
	public void testCreateFolderSnapshotWhenNonExistingCollectionMapping() {
		int nonExistingCollectionMapping = 1337;
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));
		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(nonExistingCollectionMapping));
	}

	@Test
	public void testCreateAndGetFolderSnapshotWhenEmptyCollectionList() {
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));
		
		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.<Integer>of());
		
		assertThat(folderDao.getFolderSnapshot(folderState.getId())).isEmpty();
		assertThat(folderDao.getFolderSnapshot(folderState.getSyncKey().getSyncKey())).isEmpty();
	}

	@Test
	public void testCreateAndGetFolderSnapshot() {
		int colId1 = collectionDao.addCollectionMapping(device, "collection1");
		int colId2 = collectionDao.addCollectionMapping(device, "collection2");
		int colId3 = collectionDao.addCollectionMapping(device, "collection3");
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));
		
		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(colId1, colId2, colId3));
		
		assertThat(folderDao.getFolderSnapshot(folderState.getId())).containsOnly(colId1, colId2, colId3);
		assertThat(folderDao.getFolderSnapshot(folderState.getSyncKey().getSyncKey())).containsOnly(colId1, colId2, colId3);
	}

	@Test
	public void testGetFolderSnapshotWrongIdAndSyncKey() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));
		
		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(colId));
		
		assertThat(folderDao.getFolderSnapshot(1337)).isEmpty();
		assertThat(folderDao.getFolderSnapshot("456789")).isEmpty();
	}
	
	@Test
	public void testGetFolderSyncStateId() {
		int colId1 = collectionDao.addCollectionMapping(device, "collection1");
		int colId2 = collectionDao.addCollectionMapping(device, "collection2");
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));

		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(colId1, colId2));
		
		assertThat(folderDao.getFolderSyncStateId(colId1, device)).isEqualTo(folderState.getId());
		assertThat(folderDao.getFolderSyncStateId(colId2, device)).isEqualTo(folderState.getId());
	}
	
	@Test
	public void testGetFolderSyncStateIdWhenWrongCollectionId() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));

		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(colId));
		
		assertThat(folderDao.getFolderSyncStateId(1337, device)).isNull();
	}
	
	@Test
	public void testGetFolderSyncStateWhenWrongDevice() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));

		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(colId));

		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		assertThat(folderDao.getFolderSyncStateId(colId, otherDevice)).isNull();
	}
	
	@Test
	public void testGetFolderSyncKey() {
		int colId1 = collectionDao.addCollectionMapping(device, "collection1");
		int colId2 = collectionDao.addCollectionMapping(device, "collection2");
		FolderSyncState folderState1 = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));
		FolderSyncState folderState2 = collectionDao.allocateNewFolderSyncState(device, new SyncKey("456"));

		folderDao.createFolderSnapshot(folderState1.getId(), ImmutableList.of(colId1));
		folderDao.createFolderSnapshot(folderState2.getId(), ImmutableList.of(colId2));

		assertThat(folderDao.getFolderSyncKey(colId1, device)).isEqualTo("132");
		assertThat(folderDao.getFolderSyncKey("collection1", device)).isEqualTo("132");
		assertThat(folderDao.getFolderSyncKey(colId2, device)).isEqualTo("456");
		assertThat(folderDao.getFolderSyncKey("collection2", device)).isEqualTo("456");
	}
	
	@Test
	public void testGetFolderSyncKeyWhenWrongDevice() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));

		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(colId));

		Device otherDevice = new Device(6, "otherType", new DeviceId("otherId"), new Properties(), ProtocolVersion.V121);
		assertThat(folderDao.getFolderSyncKey(colId, otherDevice)).isNull();
		assertThat(folderDao.getFolderSyncKey("collection", otherDevice)).isNull();
	}
	
	@Test
	public void testGetFolderSyncKeyWhenWrongIds() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		FolderSyncState folderState = collectionDao.allocateNewFolderSyncState(device, new SyncKey("132"));

		folderDao.createFolderSnapshot(folderState.getId(), ImmutableList.of(colId));

		assertThat(folderDao.getFolderSyncKey(1337, device)).isNull();
		assertThat(folderDao.getFolderSyncKey("wrong collection", device)).isNull();
	}
}
