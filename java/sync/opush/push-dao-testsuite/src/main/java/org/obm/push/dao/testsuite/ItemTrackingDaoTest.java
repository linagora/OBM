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
import static org.obm.DateUtils.dateUTC;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceRunner;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

@RunWith(GuiceRunner.class)
public abstract class ItemTrackingDaoTest {

	@Inject protected CollectionDao collectionDao;
	@Inject protected ItemTrackingDao itemTrackingDao;
	
	private Device device;

	@Before
	public void setUp() {
		device = new Device(1, "devType", new DeviceId("devId"), new Properties(), ProtocolVersion.V121);
	}
	
	@Test(expected=DaoException.class)
	public void testMarkAsSyncedWithNonExistingState() {
		ItemSyncState nonExistingState = ItemSyncState.builder()
				.id(25)
				.syncDate(dateUTC("2005-10-15T11:15:10Z"))
				.syncKey(new SyncKey("123"))
				.build();
		itemTrackingDao.markAsSynced(nonExistingState, ImmutableSet.of(new ServerId("9:1")));
	}
	
	@Test
	public void testMarkAsSynced() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsSynced(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:1"))).isTrue();
	}
	
	@Test
	public void testServerIdSyncedWithSyncedAndWrongServerId() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsSynced(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:99"))).isFalse();
	}
	
	@Test
	public void testServerIdSyncedWithSyncedAndWrongState() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsSynced(state, ImmutableSet.of(new ServerId("9:1")));

		ItemSyncState otherState = ItemSyncState.builder()
				.id(25)
				.syncDate(dateUTC("2005-10-15T11:15:10Z"))
				.syncKey(new SyncKey("123"))
				.build();
		
		assertThat(itemTrackingDao.isServerIdSynced(otherState, new ServerId("9:1"))).isFalse();
	}
	
	@Test
	public void testMarkAsSyncedTwice() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsSynced(state, ImmutableSet.of(new ServerId("9:1")));
		itemTrackingDao.markAsSynced(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:1"))).isTrue();
	}
	
	@Test(expected=DaoException.class)
	public void testMarkAsDeletedWithNonExistingState() {
		ItemSyncState nonExistingState = ItemSyncState.builder()
				.id(25)
				.syncDate(dateUTC("2005-10-15T11:15:10Z"))
				.syncKey(new SyncKey("123"))
				.build();
		itemTrackingDao.markAsDeleted(nonExistingState, ImmutableSet.of(new ServerId("9:1")));
	}
	
	@Test
	public void testMarkAsDeleted() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsDeleted(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:1"))).isFalse();
	}
	
	@Test
	public void testServerIdSyncedWithDeletedAndWrongServerId() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsDeleted(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:99"))).isFalse();
	}
	
	@Test
	public void testServerIdSyncedWithDeletedAndWrongState() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsDeleted(state, ImmutableSet.of(new ServerId("9:1")));

		ItemSyncState otherState = ItemSyncState.builder()
				.id(25)
				.syncDate(dateUTC("2005-10-15T11:15:10Z"))
				.syncKey(new SyncKey("123"))
				.build();
		assertThat(itemTrackingDao.isServerIdSynced(otherState, new ServerId("9:1"))).isFalse();
	}
	
	@Test
	public void testMarkAsDeletedTwice() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsDeleted(state, ImmutableSet.of(new ServerId("9:1")));
		itemTrackingDao.markAsDeleted(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:1"))).isFalse();
	}
	
	@Test
	public void testMarkAsSyncedThenAsDeletedWithSameStateReturnsSynced() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsSynced(state, ImmutableSet.of(new ServerId("9:1")));
		itemTrackingDao.markAsDeleted(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:1"))).isTrue();
	}
	
	@Test
	public void testMarkAsSyncedThenAsDeletedWithOtherStateDependsOnLastSyncDate() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState stateSynced = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-17T11:15:10Z"));
		itemTrackingDao.markAsSynced(stateSynced, ImmutableSet.of(new ServerId("9:1")));
		ItemSyncState stateDeleted = collectionDao.updateState(device, colId, new SyncKey("456"), dateUTC("2005-10-15T11:15:10Z"));
		itemTrackingDao.markAsDeleted(stateDeleted, ImmutableSet.of(new ServerId("9:1")));
		ItemSyncState stateDeleted2 = collectionDao.updateState(device, colId, new SyncKey("789"), dateUTC("2005-10-19T11:15:10Z"));
		itemTrackingDao.markAsDeleted(stateDeleted2, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(stateSynced, new ServerId("9:1"))).isTrue();
		assertThat(itemTrackingDao.isServerIdSynced(stateDeleted, new ServerId("9:1"))).isFalse();
		assertThat(itemTrackingDao.isServerIdSynced(stateDeleted2, new ServerId("9:1"))).isFalse();
	}
	
	@Test
	public void testMarkAsDeletedThenAsSyncedWithSameStateReturnsDeleted() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState state = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-15T11:15:10Z"));
		
		itemTrackingDao.markAsDeleted(state, ImmutableSet.of(new ServerId("9:1")));
		itemTrackingDao.markAsSynced(state, ImmutableSet.of(new ServerId("9:1")));
		
		assertThat(itemTrackingDao.isServerIdSynced(state, new ServerId("9:1"))).isFalse();
	}
	
	@Test
	public void testMarkAsDeletedThenAsSyncedWithOtherStateDependsOnLastSyncDate() {
		int colId = collectionDao.addCollectionMapping(device, "collection");
		ItemSyncState stateDeleted = collectionDao.updateState(device, colId, new SyncKey("123"), dateUTC("2005-10-17T11:15:10Z"));
		itemTrackingDao.markAsDeleted(stateDeleted, ImmutableSet.of(new ServerId("9:1")));
		ItemSyncState stateSynced = collectionDao.updateState(device, colId, new SyncKey("456"), dateUTC("2005-10-15T11:15:10Z"));
		itemTrackingDao.markAsSynced(stateSynced, ImmutableSet.of(new ServerId("9:1")));
		ItemSyncState stateSynced2 = collectionDao.updateState(device, colId, new SyncKey("789"), dateUTC("2005-10-19T11:15:10Z"));
		itemTrackingDao.markAsSynced(stateSynced2, ImmutableSet.of(new ServerId("9:1")));

		assertThat(itemTrackingDao.isServerIdSynced(stateDeleted, new ServerId("9:1"))).isFalse();
		assertThat(itemTrackingDao.isServerIdSynced(stateSynced, new ServerId("9:1"))).isTrue();
		assertThat(itemTrackingDao.isServerIdSynced(stateSynced2, new ServerId("9:1"))).isTrue()
		;
	}
}
