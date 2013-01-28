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
package org.obm.opush.command;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectSyncState;
import static org.obm.opush.IntegrationTestUtils.expectUserCollectionsNeverChange;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockCollectionDaoForEmailSync;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailSyncedCollectionDao;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailUnsynchronizedItemDao;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockItemTrackingDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.command.sync.EmailSyncTestUtils;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.GetItemEstimateStatus;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.beans.GetItemEstimateSingleFolderResponse;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class GetItemEstimateHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(GetIemEstimateTestModule.class);

	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject IMocksControl mocksControl;

	private List<OpushUser> fakeTestUsers;

	@Before
	public void init() {
		fakeTestUsers = Arrays.asList(singleUserFixture.jaures);
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testGetItemEstimateWithValidCollectionAndSyncKey() throws Exception {
		SyncKey syncKey = new SyncKey("1");
		ItemSyncState expectedSyncState = ItemSyncState.builder().syncDate(DateUtils.getCurrentDate()).syncKey(syncKey).build();
		int collectionId = 15105;
		Set<Integer> existingCollections = Sets.newHashSet(collectionId);
		mockAccessAndStateThenStart(existingCollections, syncKey, expectedSyncState);
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		
		GetItemEstimateSingleFolderResponse response =
				opClient.getItemEstimateOnMailFolder(syncKey, collectionId);

		assertThat(response.getStatus()).isEqualTo(GetItemEstimateStatus.OK);
		assertThat(response.getCollectionId()).isEqualTo(collectionId);
		assertThat(response.getEstimate()).isEqualTo(0);
	}

	@Test
	public void testGetItemEstimateWithUnexistingCollection() throws Exception {
		SyncKey syncKey = new SyncKey("1");
		ItemSyncState expectedSyncState = ItemSyncState.builder().syncDate(DateUtils.getCurrentDate()).syncKey(syncKey).build();
		int unexistingCollectionId = 15105;
		Set<Integer> existingCollections = Collections.<Integer>emptySet();
		mockAccessAndStateThenStart(existingCollections, syncKey, expectedSyncState);
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		
		GetItemEstimateSingleFolderResponse response =
				opClient.getItemEstimateOnMailFolder(syncKey, unexistingCollectionId);

		assertThat(response.getStatus()).isEqualTo(GetItemEstimateStatus.INVALID_COLLECTION);
		assertThat(response.getCollectionId()).isNull();
		assertThat(response.getEstimate()).isNull();
	}

	@Test
	public void testGetItemEstimateWithInvalidSyncKey() throws Exception {
		SyncKey invalidSyncKey = new SyncKey("1");
		ItemSyncState expectedSyncState = null;
		int collectionId = 15105;
		Set<Integer> existingCollections = Sets.newHashSet(collectionId);
		mockAccessAndStateThenStart(existingCollections, invalidSyncKey, expectedSyncState);
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		
		GetItemEstimateSingleFolderResponse response =
				opClient.getItemEstimateOnMailFolder(invalidSyncKey, collectionId);

		assertThat(response.getStatus()).isEqualTo(GetItemEstimateStatus.INVALID_SYNC_KEY);
		assertThat(response.getCollectionId()).isEqualTo(collectionId);
		assertThat(response.getEstimate()).isNull();
	}

	private void mockAccessAndStateThenStart(Set<Integer> existingCollections, SyncKey syncKey, ItemSyncState syncState)
			throws Exception {
		expectSyncState(classToInstanceMap.get(StateMachine.class), syncKey, syncState);

		DataDelta delta = DataDelta.builder().syncDate(new Date()).syncKey(syncKey).build();
		EmailSyncTestUtils.mockEmailSyncClasses(syncKey, existingCollections, delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();
	}

	@Test
	public void testGetItemEstimateWithHierarchyChangedException() throws Exception {
		SyncKey syncKey = new SyncKey("1");
		ItemSyncState syncState = ItemSyncState.builder().syncDate(DateUtils.getCurrentDate()).syncKey(syncKey).build();
		int collectionId = 15105;
		Set<Integer> syncEmailCollectionsIds = Sets.newHashSet(collectionId);
		
		expectSyncState(classToInstanceMap.get(StateMachine.class), syncKey, syncState);

		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		
		mockEmailSyncWithHierarchyChangedException(syncKey, syncEmailCollectionsIds);
		
		mocksControl.replay();
		opushServer.start();
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		
		GetItemEstimateSingleFolderResponse response =
				opClient.getItemEstimateOnMailFolder(syncKey, collectionId);

		assertThat(response.getStatus()).isEqualTo(GetItemEstimateStatus.INVALID_COLLECTION);
		assertThat(response.getCollectionId()).isNull();
		assertThat(response.getEstimate()).isNull();
	}

	private void mockEmailSyncWithHierarchyChangedException(SyncKey syncKey, Set<Integer> syncEmailCollectionsIds)
			throws DaoException, ConversionException, FilterTypeChangedException {
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		mockEmailSyncedCollectionDao(syncedCollectionDao);
		
		UnsynchronizedItemDao unsynchronizedItemDao = classToInstanceMap.get(UnsynchronizedItemDao.class);
		mockEmailUnsynchronizedItemDao(unsynchronizedItemDao);

		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers, syncEmailCollectionsIds);
		mockCollectionDaoForEmailSync(collectionDao, syncKey, syncEmailCollectionsIds);
		
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		mockItemTrackingDao(itemTrackingDao);
		
		IContentsExporter contentsExporterBackend = classToInstanceMap.get(IContentsExporter.class);
		expect(contentsExporterBackend.getItemEstimateSize(
				anyObject(UserDataRequest.class), 
				anyObject(ItemSyncState.class),
				anyObject(SyncCollection.class)))
			.andThrow(new HierarchyChangedException(new NotAllowedException("Not allowed")));
	}
}
