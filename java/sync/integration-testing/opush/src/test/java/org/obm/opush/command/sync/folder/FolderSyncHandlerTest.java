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
package org.obm.opush.command.sync.folder;

import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChangesForMailboxes;
import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChangesOnlyInbox;
import static org.obm.opush.IntegrationPushTestUtils.mockNextGeneratedSyncKey;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectCreateFolderMappingState;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Arrays;
import java.util.List;

import org.easymock.IMocksControl;
import org.fest.util.Files;
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
import org.obm.opush.env.Configuration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.FolderType;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.hierarchy.CollectionDeletion;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.exception.DaoException;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class FolderSyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(FolderSyncHandlerTestModule.class);

	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;
	
	private List<OpushUser> userAsList;
	private OpushUser user;

	@Before
	public void init() {
		user = singleUserFixture.jaures;
		userAsList = Arrays.asList(user);
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testInitialFolderSyncContainsINBOX() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey newGeneratedSyncKey = new SyncKey("d58ea559-d1b8-4091-8ba5-860e6fa54875");
		FolderSyncState newMappingSyncState = FolderSyncState.builder().syncKey(newGeneratedSyncKey).build();
		
		mockUsersAccess(classToInstanceMap, userAsList);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockNextGeneratedSyncKey(classToInstanceMap, newGeneratedSyncKey);
		expectCollectionDaoAllocateFolderSyncState(classToInstanceMap.get(CollectionDao.class), newGeneratedSyncKey, newMappingSyncState);
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));

//		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
//		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers, Collections.<Integer>emptySet());
//		mockCollectionDao(collectionDao, initialSyncKey, serverId);

		
		mocksControl.replay();
		
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		
		mocksControl.verify();
		
		assertThat(folderSyncResponse.getNewSyncKey()).isEqualTo(newGeneratedSyncKey);
		assertThat(folderSyncResponse.getStatus()).isEqualTo(FolderSyncStatus.OK);
		assertThat(folderSyncResponse.getCount()).isEqualTo(1);
		assertThat(folderSyncResponse.getCollectionsAddedAndUpdated()).hasSize(1);
		CollectionChange inbox = Iterables.getOnlyElement(folderSyncResponse.getCollectionsAddedAndUpdated());
		assertThat(inbox.getDisplayName()).isEqualTo("INBOX");
		assertThat(inbox.getFolderType()).isEqualTo(FolderType.DEFAULT_INBOX_FOLDER);
	}

	@Test
	public void testFolderSyncHasNoChange() throws Exception {
		SyncKey newSyncKey = new SyncKey("12341234-1234-1234-1234-123456123456");

		SyncKey newGeneratedSyncKey = new SyncKey("d58ea559-d1b8-4091-8ba5-860e6fa54875");
		int newSyncStateId = 1156;
		FolderSyncState newSyncState = newFolderSyncState(newGeneratedSyncKey, newSyncStateId);
		
		mockUsersAccess(classToInstanceMap, userAsList);
		mockHierarchyChangesForMailboxes(classToInstanceMap, buildHierarchyItemsChangeEmpty());
		mockNextGeneratedSyncKey(classToInstanceMap, newGeneratedSyncKey);
		expectCollectionDaoFindFolderSyncState(classToInstanceMap.get(CollectionDao.class), newSyncKey, newSyncState);
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));

		mocksControl.replay();
		
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(newSyncKey);

		mocksControl.verify();

		assertThat(folderSyncResponse.getNewSyncKey()).isEqualTo(newGeneratedSyncKey);
		assertThat(folderSyncResponse.getStatus()).isEqualTo(FolderSyncStatus.OK);
		assertThat(folderSyncResponse.getCount()).isEqualTo(0);
		assertThat(folderSyncResponse.getCollectionsAddedAndUpdated()).isEmpty();
	}
	
	@Test
	public void testFolderSyncHasChanges() throws Exception {
		SyncKey newSyncKey = new SyncKey("12341234-1234-1234-1234-123456123456");

		SyncKey newGeneratedSyncKey = new SyncKey("d58ea559-d1b8-4091-8ba5-860e6fa54875");
		int newSyncStateId = 1156;
		FolderSyncState newSyncState = newFolderSyncState(newGeneratedSyncKey, newSyncStateId);
		
		String collectionId = "4";
		String parentId = "23";
		
		org.obm.push.bean.FolderType itemChangeType = org.obm.push.bean.FolderType.USER_CREATED_EMAIL_FOLDER;
		HierarchyCollectionChanges mailboxChanges = HierarchyCollectionChanges.builder()
			.changes(Lists.newArrayList(
					CollectionChange.builder()
						.collectionId(collectionId)
						.parentCollectionId(parentId)
						.displayName("aNewImapFolder")
						.folderType(itemChangeType)
						.isNew(true)
						.build()))
			.build();
//		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
//		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers, Collections.<Integer>emptySet());
//		mockCollectionDao(collectionDao, syncKey, serverId);
		
		mockUsersAccess(classToInstanceMap, userAsList);
		mockHierarchyChangesForMailboxes(classToInstanceMap, mailboxChanges);
		mockNextGeneratedSyncKey(classToInstanceMap, newGeneratedSyncKey);
		expectCollectionDaoFindFolderSyncState(classToInstanceMap.get(CollectionDao.class), newSyncKey, newSyncState);
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		
		mocksControl.replay();
		
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(newSyncKey);

		mocksControl.verify();

		assertThat(folderSyncResponse.getNewSyncKey()).isEqualTo(newGeneratedSyncKey);
		assertThat(folderSyncResponse.getStatus()).isEqualTo(FolderSyncStatus.OK);
		assertThat(folderSyncResponse.getCount()).isEqualTo(1);
		assertThat(folderSyncResponse.getCollectionsAddedAndUpdated()).hasSize(1);
		CollectionChange inbox = Iterables.getOnlyElement(folderSyncResponse.getCollectionsAddedAndUpdated());
		assertThat(inbox.getDisplayName()).isEqualTo("aNewImapFolder");
		assertThat(inbox.getFolderType()).isEqualTo(FolderType.USER_CREATED_EMAIL_FOLDER);
	}

	@Test
	public void testFolderSyncHasDeletions() throws Exception {
		SyncKey newSyncKey = new SyncKey("12341234-1234-1234-1234-123456123456");

		SyncKey newGeneratedSyncKey = new SyncKey("d58ea559-d1b8-4091-8ba5-860e6fa54875");
		int newSyncStateId = 1156;
		FolderSyncState newSyncState = newFolderSyncState(newGeneratedSyncKey, newSyncStateId);

		String collectionId = "4";
		
		HierarchyCollectionChanges mailboxChanges = HierarchyCollectionChanges.builder()
			.deletions(Lists.newArrayList(
					CollectionDeletion.builder().collectionId(collectionId).build()))
			.build();
		
		mockUsersAccess(classToInstanceMap, userAsList);
		mockHierarchyChangesForMailboxes(classToInstanceMap, mailboxChanges);
		mockNextGeneratedSyncKey(classToInstanceMap, newGeneratedSyncKey);
		expectCollectionDaoFindFolderSyncState(classToInstanceMap.get(CollectionDao.class), newSyncKey, newSyncState);
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(newSyncKey);

		mocksControl.verify();

		assertThat(folderSyncResponse.getNewSyncKey()).isEqualTo(newGeneratedSyncKey);
		assertThat(folderSyncResponse.getStatus()).isEqualTo(FolderSyncStatus.OK);
		assertThat(folderSyncResponse.getCount()).isEqualTo(1);
		assertThat(folderSyncResponse.getCollectionsDeleted()).hasSize(1);
		CollectionDeletion inbox = Iterables.getOnlyElement(folderSyncResponse.getCollectionsDeleted());
		assertThat(inbox.getCollectionId()).isEqualTo(collectionId);
	}

	private HierarchyCollectionChanges buildHierarchyItemsChangeEmpty() {
		return HierarchyCollectionChanges.builder().build();
	}

	private void expectCollectionDaoAllocateFolderSyncState(CollectionDao collectionDao, SyncKey syncKey, FolderSyncState newSyncState) 
			throws DaoException {
	
		expect(collectionDao.allocateNewFolderSyncState(user.device, syncKey)).andReturn(newSyncState);
	}

	private void expectCollectionDaoFindFolderSyncState(CollectionDao collectionDao,
			SyncKey newSyncKey, FolderSyncState newSyncState) throws DaoException {
		
		expect(collectionDao.findFolderStateForKey(newSyncKey)).andReturn(FolderSyncState.builder().syncKey(newSyncKey).build());
		expectCollectionDaoAllocateFolderSyncState(collectionDao, newSyncState.getSyncKey(), newSyncState);
	}

	private FolderSyncState newFolderSyncState(SyncKey newGeneratedSyncKey, int newSyncStateId) {
		return FolderSyncState.builder()
				.id(newSyncStateId)
				.syncKey(newGeneratedSyncKey)
				.build();
	}

}
