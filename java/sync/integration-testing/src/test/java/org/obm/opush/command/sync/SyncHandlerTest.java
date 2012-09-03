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
import static org.obm.DateUtils.date;
import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChangesOnlyInbox;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectAllocateFolderState;
import static org.obm.opush.IntegrationTestUtils.expectContentExporterFetching;
import static org.obm.opush.IntegrationTestUtils.expectCreateFolderMappingState;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasAddItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasDeleteItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasNoChange;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailSyncClasses;
import static org.obm.push.bean.FilterType.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.easymock.IMocksControl;
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
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.MSEmailHeader;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemChangesBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.SerializableInputStream;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.beans.Add;
import org.obm.sync.push.client.beans.Collection;
import org.obm.sync.push.client.beans.Delete;
import org.obm.sync.push.client.beans.SyncResponse;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(SyncHandlerTestModule.class);

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
	public void testSyncDefaultMailFolderUnchange() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("1");
		int syncEmailCollectionId = 4;
		DataDelta delta = DataDelta.builder().syncDate(new Date()).build();
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);

		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getCollectionId(), THREE_DAYS_BACK, 150);

		checkMailFolderHasNoChange(syncEmailResponse, inbox.getCollectionId());
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
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		
		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getCollectionId(), THREE_DAYS_BACK, 150);

		checkMailFolderHasAddItems(syncEmailResponse, inbox.getCollectionId(),
				new Add(syncEmailCollectionId + ":" + 0));
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
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		
		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getCollectionId(), THREE_DAYS_BACK, 150);

		checkMailFolderHasAddItems(syncEmailResponse, inbox.getCollectionId(),
				new Add(syncEmailCollectionId + ":" + 0),
				new Add(syncEmailCollectionId + ":" + 1));
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
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		
		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getCollectionId(), THREE_DAYS_BACK, 150);

		checkMailFolderHasDeleteItems(syncEmailResponse, inbox.getCollectionId(),
				new Delete(syncEmailCollectionId + ":" + 0));
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
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);

		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getCollectionId(), THREE_DAYS_BACK, 150);

		checkMailFolderHasItems(syncEmailResponse, inbox.getCollectionId(), 
				ImmutableSet.of(new Add(syncEmailCollectionId + ":123")),
				ImmutableSet.of(new Delete(syncEmailCollectionId + ":122")));
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
				singleUserFixture.jaures.device);
		
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		expectContentExporterFetching(classToInstanceMap.get(IContentsExporter.class), userDataRequest, itemChanges);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, ImmutableList.<Integer> of(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		
		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmailWithFetch(syncEmailSyncKey, inbox.getCollectionId(), serverId);

		checkMailFolderHasNoChange(syncEmailResponse, inbox.getCollectionId());
	}
	
	@Test
	public void testSyncWithUnknownSyncKeyReturnsInvalidSyncKeyStatus() throws Exception {
		int collectionId= 1;
		String collectionIdAsString = String.valueOf(collectionId);
		String collectionPath = IntegrationTestUtils.buildEmailInboxCollectionPath(singleUserFixture.jaures); 
		
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey secondSyncKey = new SyncKey("456");
		Date initialUpdateStateDate = DateUtils.getEpochPlusOneSecondCalendar().getTime();
		ItemSyncState firstItemSyncState = ItemSyncState.builder().syncKey(initialSyncKey).syncDate(initialUpdateStateDate).build();
		
		IntegrationUserAccessUtils.mockUsersAccess(classToInstanceMap, fakeTestUsers);
		EmailSyncTestUtils.mockEmailSyncedCollectionDao(classToInstanceMap.get(SyncedCollectionDao.class));
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expect(collectionDao.getCollectionPath(collectionId)).andReturn(collectionPath).once();
		expect(collectionDao.getCollectionPath(collectionId)).andThrow(new CollectionNotFoundException());
		
		EmailSyncTestUtils.mockEmailUnsynchronizedItemDao(classToInstanceMap.get(UnsynchronizedItemDao.class));
		expect(collectionDao.findItemStateForKey(initialSyncKey)).andReturn(null);
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(), anyObject(SyncKey.class), anyObject(Date.class)))
			.andReturn(firstItemSyncState)
			.anyTimes();
		collectionDao.resetCollection(singleUserFixture.jaures.device, collectionId);
		expectLastCall();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		opClient.syncEmail(initialSyncKey, collectionIdAsString, THREE_DAYS_BACK, 100);
		SyncResponse syncResponse = opClient.syncEmail(secondSyncKey, collectionIdAsString, THREE_DAYS_BACK, 100);
		mocksControl.verify();
		
		assertThat(syncResponse.getCollection(collectionIdAsString).getStatus()).isEqualTo(SyncStatus.OBJECT_NOT_FOUND);
	}

	@Test
	public void testSyncWithoutOptionsAndNoOptionsInCacheTakeThePreviousOne() throws Exception {
		OpushUser user = singleUserFixture.jaures;
		int collectionId = 1;
		String collectionPath = IntegrationTestUtils.buildEmailInboxCollectionPath(user);
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey secondSyncKey = new SyncKey("13424");

		SyncCollectionOptions toStoreOptions = new SyncCollectionOptions();
		toStoreOptions.setFilterType(THREE_DAYS_BACK);
		toStoreOptions.setConflict(1);
		SyncCollection firstToStoreCollection = new SyncCollection();
		firstToStoreCollection.setCollectionId(collectionId);
		firstToStoreCollection.setCollectionPath(collectionPath);
		firstToStoreCollection.setDataType(PIMDataType.EMAIL);
		firstToStoreCollection.setOptions(toStoreOptions);
		firstToStoreCollection.setWindowSize(25);
		firstToStoreCollection.setSyncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY);

		SyncCollection secondToStoreCollection = new SyncCollection();
		secondToStoreCollection.setCollectionId(collectionId);
		secondToStoreCollection.setCollectionPath(collectionPath);
		secondToStoreCollection.setDataType(PIMDataType.EMAIL);
		secondToStoreCollection.setOptions(toStoreOptions);
		secondToStoreCollection.setSyncKey(secondSyncKey);
		ItemSyncState secondRequestSyncState = ItemSyncState.builder()
				.id(4)
				.syncKey(secondSyncKey)
				.syncDate(date("2012-10-10T16:22:53"))
				.build();

		IntegrationUserAccessUtils.mockUsersAccess(classToInstanceMap, fakeTestUsers);
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(secondSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		UnsynchronizedItemDao unsynchronizedItemDao = classToInstanceMap.get(UnsynchronizedItemDao.class);
		expectUnsynchronizedItemToNeverExceedWindowSize(unsynchronizedItemDao, user, collectionId);
		IContentsExporter contentsExporter = classToInstanceMap.get(IContentsExporter.class);
		expect(contentsExporter.getChanged(
				anyObject(UserDataRequest.class), anyObject(SyncCollection.class), anyObject(SyncKey.class)))
			.andReturn(DataDelta.newEmptyDelta(secondRequestSyncState.getSyncDate()));
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expect(collectionDao.getCollectionPath(collectionId)).andReturn(collectionPath).anyTimes();
		expect(collectionDao.findItemStateForKey(initialSyncKey)).andReturn(null);
		expect(collectionDao.findItemStateForKey(secondSyncKey)).andReturn(secondRequestSyncState).times(2);
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(),
				anyObject(SyncKey.class), anyObject(Date.class))).andReturn(secondRequestSyncState).times(2);
		collectionDao.resetCollection(user.device, collectionId);
		expectLastCall();
		
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		expect(syncedCollectionDao.get(user.credentials, user.device, collectionId)).andReturn(null);
		syncedCollectionDao.put(user.credentials, user.device, firstToStoreCollection);
		expectLastCall();
		expect(syncedCollectionDao.get(user.credentials, user.device, collectionId)).andReturn(null);
		syncedCollectionDao.put(user.credentials, user.device, secondToStoreCollection);
		expectLastCall();
		
		mocksControl.replay();
		opushServer.start();
		OPClient opClient = buildWBXMLOpushClient(user, opushServer.getPort());
		
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		
		opClient.syncEmail(initialSyncKey, inbox.getCollectionId(), toStoreOptions.getFilterType(), 25);
		SyncResponse syncWithoutOptions = opClient.syncWithoutOptions(secondSyncKey, inbox.getCollectionId());
		mocksControl.verify();

		checkMailFolderHasNoChange(syncWithoutOptions, inbox.getCollectionId());
	}

	private void expectUnsynchronizedItemToNeverExceedWindowSize(
			UnsynchronizedItemDao unsynchronizedItemDao, OpushUser user, int collectionId) {
		
		expect(unsynchronizedItemDao.listItemsToAdd(user.credentials, user.device, collectionId))
				.andReturn(ImmutableList.<ItemChange>of()).anyTimes();
		expect(unsynchronizedItemDao.listItemsToRemove(user.credentials, user.device, collectionId))
				.andReturn(ImmutableList.<ItemDeletion>of()).anyTimes();
		unsynchronizedItemDao.clearItemsToAdd(user.credentials, user.device, collectionId);
		expectLastCall().anyTimes();
		unsynchronizedItemDao.clearItemsToRemove(user.credentials, user.device, collectionId);
		expectLastCall().anyTimes();
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
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, existingCollections, delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, syncEmailUnexistingCollectionId, THREE_DAYS_BACK, 25);

		Collection unexistingCollection = syncEmailResponse.getCollection(syncEmailUnexistingCollectionId);
		Assertions.assertThat(unexistingCollection.getStatus()).isEqualTo(SyncStatus.OBJECT_NOT_FOUND);
	}

	private CollectionChange lookupInbox(Iterable<CollectionChange> items) {
		return FluentIterable
				.from(items)
				.firstMatch(new Predicate<CollectionChange>() {
		
					@Override
					public boolean apply(CollectionChange input) {
						return input.getFolderType() == org.obm.push.bean.FolderType.DEFAULT_INBOX_FOLDER;
					}
				}).get();
	}
}
