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
package org.obm.opush.command.sync;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChangesOnlyInbox;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectAllocateFolderState;
import static org.obm.opush.IntegrationTestUtils.expectContentExporterFetching;
import static org.obm.opush.IntegrationTestUtils.expectContinuationTransactionLifecycle;
import static org.obm.opush.IntegrationTestUtils.expectCreateFolderMappingState;
import static org.obm.opush.IntegrationTestUtils.replayMocks;
import static org.obm.opush.IntegrationTestUtils.verifyMocks;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailSyncClasses;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.IntegrationTestUtils;
import org.obm.opush.IntegrationUserAccessUtils;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.ContinuationService;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.Device;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemChangesBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.SerializableInputStream;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.Add;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Delete;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.SyncResponse;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(SyncHandlerTestModule.class);

	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;

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
	public void testSyncDefaultMailFolderUnchange() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("1");
		int syncEmailCollectionId = 4;
		DataDelta delta = DataDelta.builder().syncDate(new Date()).build();
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId(), FilterType.THREE_DAYS_BACK);

		assertThat(syncEmailResponse).isNotNull();
		Collection inboxCollection = syncEmailResponse.getCollection(String.valueOf(inbox.getServerId()));
		assertThat(inboxCollection).isNotNull();
		assertThat(inboxCollection.getAdds()).isEmpty();
		assertThat(inboxCollection.getDeletes()).isEmpty();
	}
	
	@Test
	public void testSyncOneInboxMail() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("13424");
		int syncEmailCollectionId = 432;

		DataDelta delta = DataDelta.builder()
			.changes(new ItemChangesBuilder()
					.addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":0")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText)))
					.build())
			.syncDate(new Date())
			.build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId(), FilterType.THREE_DAYS_BACK);
		
		assertThat(syncEmailResponse).isNotNull();
		Collection inboxCollection = syncEmailResponse.getCollection(String.valueOf(inbox.getServerId()));
		assertThat(inboxCollection).isNotNull();
		assertThat(inboxCollection.getAdds()).containsOnly(new Add(syncEmailCollectionId + ":" + 0));
		assertThat(inboxCollection.getDeletes()).isEmpty();
	}

	@Test
	public void testSyncTwoInboxMails() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("13424");
		int syncEmailCollectionId = 432;
		
		DataDelta delta = DataDelta.builder()
			.changes(new ItemChangesBuilder()
					.addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":0")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText)))
					.addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":1")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText)))
					.build())
			.syncDate(new Date())
			.build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId(), FilterType.THREE_DAYS_BACK);

		assertThat(syncEmailResponse).isNotNull();
		Collection inboxCollection = syncEmailResponse.getCollection(String.valueOf(inbox.getServerId()));
		assertThat(inboxCollection).isNotNull();
		assertThat(inboxCollection.getAdds()).containsOnly(
				new Add(syncEmailCollectionId + ":" + 0),
				new Add(syncEmailCollectionId + ":" + 1));
		assertThat(inboxCollection.getDeletes()).isEmpty();
	}

	@Test
	public void testSyncOneInboxDeletedMail() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("13424");
		int syncEmailCollectionId = 432;
		
		DataDelta delta = DataDelta.builder()
			.deletions(ImmutableList.of(
					ItemDeletion.builder().serverId(syncEmailCollectionId + ":0").build()))
			.syncDate(new Date())
			.build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId(), FilterType.THREE_DAYS_BACK);
		
		assertThat(syncEmailResponse).isNotNull();
		Collection inboxCollection = syncEmailResponse.getCollection(String.valueOf(inbox.getServerId()));
		assertThat(inboxCollection).isNotNull();
		assertThat(inboxCollection.getAdds()).isEmpty();
		assertThat(inboxCollection.getDeletes()).containsOnly(new Delete(syncEmailCollectionId + ":" + 0));
	}

	@Test
	public void testSyncInboxOneNewOneDeletedMail() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("13424");
		int syncEmailCollectionId = 432;
		DataDelta delta = DataDelta.builder()
			.changes(new ItemChangesBuilder()
					.addItemChange(
						new ItemChangeBuilder().serverId(syncEmailCollectionId + ":123")
							.withApplicationData(applicationData("text", MSEmailBodyType.PlainText)))
					.build())
			.deletions(ImmutableList.of(
					ItemDeletion.builder().serverId(syncEmailCollectionId + ":122").build()))
			.syncDate(new Date())
			.build();

		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getServerId(), FilterType.THREE_DAYS_BACK);

		assertThat(syncEmailResponse).isNotNull();
		Collection inboxCollection = syncEmailResponse.getCollection(String.valueOf(inbox.getServerId()));
		assertThat(inboxCollection).isNotNull();
		assertThat(inboxCollection.getAdds()).containsOnly(new Add(syncEmailCollectionId + ":123"));
		assertThat(inboxCollection.getDeletes()).containsOnly(new Delete(syncEmailCollectionId + ":122"));
	}

	@Test
	public void testSyncInboxFetchIdsNotEmpty() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("13424");
		int syncEmailCollectionId = 432;
		String serverId = syncEmailCollectionId + ":123";
		List<ItemChange> itemChanges = new ItemChangesBuilder()
				.addItemChange(
					new ItemChangeBuilder().serverId(serverId)
						.withApplicationData(applicationData("text", MSEmailBodyType.PlainText)))
				.build();
		DataDelta delta = DataDelta.builder()
			.changes(itemChanges)
			.deletions(ImmutableList.of(
					ItemDeletion.builder().serverId(syncEmailCollectionId + ":122").build()))
			.syncDate(new Date())
			.build();

		UserDataRequest userDataRequest = new UserDataRequest(singleUserFixture.jaures.credentials, 
				"Sync", 
				singleUserFixture.jaures.device, 
				new BigDecimal(12.1).setScale(1, BigDecimal.ROUND_HALF_UP));
		
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), userDataRequest, 0);
		expectContentExporterFetching(classToInstanceMap.get(IContentsExporter.class), userDataRequest, itemChanges);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, ImmutableList.<Integer> of(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		Folder inbox = folderSyncResponse.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncEmailResponse = opClient.syncEmailWithFetch(syncEmailSyncKey, inbox.getServerId(), serverId);

		assertThat(syncEmailResponse).isNotNull();
		Collection inboxCollection = syncEmailResponse.getCollection(String.valueOf(inbox.getServerId()));
		assertThat(inboxCollection).isNotNull();
		assertThat(inboxCollection.getAdds()).isEmpty();
		assertThat(inboxCollection.getDeletes()).isEmpty();
	}
	
	@Test
	public void testSyncWithUnknownSyncKeyReturnsInvalidSyncKeyStatus() throws Exception {
		int collectionId= 1;
		String collectionIdAsString = String.valueOf(collectionId);
		
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey secondSyncKey = new SyncKey("456");
		Date initialUpdateStateDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstItemSyncState = ItemSyncState.builder().syncKey(initialSyncKey).syncDate(initialUpdateStateDate).build();
		
		IntegrationUserAccessUtils.mockUsersAccess(classToInstanceMap, fakeTestUsers);
		EmailSyncTestUtils.mockEmailSyncedCollectionDao(classToInstanceMap.get(SyncedCollectionDao.class));
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class),
				singleUserFixture.jaures.userDataRequest, 0);
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		IntegrationTestUtils.expectUserCollectionsNeverChange(collectionDao, fakeTestUsers);
		EmailSyncTestUtils.mockEmailUnsynchronizedItemDao(classToInstanceMap.get(UnsynchronizedItemDao.class));
		expect(collectionDao.findItemStateForKey(initialSyncKey)).andReturn(null);
		expect(collectionDao.findItemStateForKey(secondSyncKey)).andReturn(null).times(2);
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(), anyObject(SyncKey.class), anyObject(Date.class)))
			.andReturn(firstItemSyncState)
			.anyTimes();
		collectionDao.resetCollection(singleUserFixture.jaures.device, collectionId);
		expectLastCall();
		
		replayMocks(classToInstanceMap);
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		opClient.syncEmail(initialSyncKey, collectionIdAsString, FilterType.THREE_DAYS_BACK);
		SyncResponse syncResponse = opClient.syncEmail(secondSyncKey, collectionIdAsString, FilterType.THREE_DAYS_BACK);
		verifyMocks(classToInstanceMap);
		
		assertThat(syncResponse.getCollection(collectionIdAsString).getStatus()).isEqualTo(SyncStatus.INVALID_SYNC_KEY);
	}
	
	private FolderSyncState newSyncState(SyncKey syncEmailSyncKey) {
		return FolderSyncState.builder()
				.syncKey(syncEmailSyncKey)
				.build();
	}
	
	private MSEmail applicationData(String message, MSEmailBodyType emailBodyType) {
		return MSEmail.builder()
			.uid(1l)
			.header(MSEmailHeader.builder().build())
			.body(new MSEmailBody(new SerializableInputStream(
					new ByteArrayInputStream(message.getBytes())), emailBodyType, 0, Charsets.UTF_8, false)).build();
	}

	@Test
	public void testSyncOnUnexistingCollection() throws Exception {
		SyncKey syncEmailSyncKey = new SyncKey("1");
		java.util.Collection<Integer> existingCollections = Collections.emptySet();
		int syncEmailUnexistingCollectionId = 15105;
		DataDelta delta = DataDelta.builder().syncDate(new Date()).build();
		expectContinuationTransactionLifecycle(classToInstanceMap.get(ContinuationService.class), singleUserFixture.jaures.userDataRequest, 0);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, existingCollections, delta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, syncEmailUnexistingCollectionId, FilterType.THREE_DAYS_BACK);

		org.obm.sync.push.client.Collection unexistingCollection = syncEmailResponse.getCollection(syncEmailUnexistingCollectionId);
		Assertions.assertThat(unexistingCollection.getStatus()).isEqualTo(SyncStatus.OBJECT_NOT_FOUND);
	}
}
