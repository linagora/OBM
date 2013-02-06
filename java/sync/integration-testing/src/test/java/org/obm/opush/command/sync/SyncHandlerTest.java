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
import static org.obm.opush.IntegrationTestUtils.expectUserCollectionsNeverChange;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasAddItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasDeleteItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasItems;
import static org.obm.opush.command.sync.EmailSyncTestUtils.checkMailFolderHasNoChange;
import static org.obm.opush.command.sync.EmailSyncTestUtils.getCollectionWithId;
import static org.obm.opush.command.sync.EmailSyncTestUtils.lookupInbox;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockCollectionDaoForEmailSync;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailSyncClasses;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailSyncedCollectionDao;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockEmailUnsynchronizedItemDao;
import static org.obm.opush.command.sync.EmailSyncTestUtils.mockItemTrackingDao;
import static org.obm.push.bean.FilterType.THREE_DAYS_BACK;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.easymock.IMocksControl;
import org.fest.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
import org.obm.opush.env.Configuration;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.Device;
import org.obm.push.bean.FilterType;
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
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangeBuilder;
import org.obm.push.bean.change.item.ItemChangesBuilder;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.bean.ms.MSEmail;
import org.obm.push.bean.ms.MSEmailBody;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.FolderSyncStateBackendMappingDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.SerializableInputStream;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.OPClient;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class SyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(SyncHandlerTestModule.class);

	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;
	@Inject IMocksControl mocksControl;
	@Inject Configuration configuration;

	private List<OpushUser> fakeTestUsers;

	@Before
	public void init() {
		fakeTestUsers = Arrays.asList(singleUserFixture.jaures);
	}
	
	@After
	public void shutdown() throws Exception {
		opushServer.stop();
		Files.delete(configuration.dataDir);
	}

	@Test
	public void testSyncDefaultMailFolderUnchange() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("1");
		int syncEmailCollectionId = 4;
		DataDelta delta = DataDelta.builder().syncDate(new Date()).syncKey(syncEmailSyncKey).build();
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
	public void testSyncWithWaitReturnsServerError() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("1");
		int syncEmailCollectionId = 4;
		DataDelta delta = DataDelta.builder().syncDate(new Date()).syncKey(syncEmailSyncKey).build();
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId), delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmailWithWait(syncEmailSyncKey, inbox.getCollectionId(), THREE_DAYS_BACK, 150);

		assertThat(syncEmailResponse.getStatus()).isEqualTo(SyncStatus.SERVER_ERROR);
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
			.syncKey(syncEmailSyncKey)
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
				new ItemChangeBuilder().serverId(syncEmailCollectionId + ":" + 0).withNewFlag(true).build());
	}

	@Test
	public void testSyncTwoMailButOneDisappearing() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("13424");
		int syncEmailCollectionId = 432;

		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers, ImmutableList.of(syncEmailCollectionId));
		mockCollectionDaoForEmailSync(collectionDao, syncEmailSyncKey, ImmutableList.of(syncEmailCollectionId));
		
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		mockEmailSyncedCollectionDao(syncedCollectionDao);
		
		UnsynchronizedItemDao unsynchronizedItemDao = classToInstanceMap.get(UnsynchronizedItemDao.class);
		mockEmailUnsynchronizedItemDao(unsynchronizedItemDao);

		IContentsExporter contentsExporter = classToInstanceMap.get(IContentsExporter.class);
		expect(contentsExporter.getChanged(
				anyObject(UserDataRequest.class), 
				anyObject(SyncCollection.class),
				anyObject(SyncClientCommands.class),
				anyObject(SyncKey.class)))
				.andThrow(new ItemNotFoundException());
		
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		CollectionChange inbox = lookupInbox(folderSyncResponse.getCollectionsAddedAndUpdated());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, inbox.getCollectionId(), FilterType.THREE_DAYS_BACK, 100);
		
		assertThat(syncEmailResponse).isNotNull();
		assertThat(syncEmailResponse.getStatus()).isEqualTo(SyncStatus.NEED_RETRY);
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
			.syncKey(syncEmailSyncKey)
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
				new ItemChangeBuilder().serverId(syncEmailCollectionId + ":" + 0).withNewFlag(true).build(),
				new ItemChangeBuilder().serverId(syncEmailCollectionId + ":" + 1).withNewFlag(true).build()); 
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
			.syncKey(syncEmailSyncKey)
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
				ItemDeletion.builder().serverId(syncEmailCollectionId + ":" + 0).build());
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
			.syncKey(syncEmailSyncKey)
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
				ImmutableSet.of(new ItemChangeBuilder().serverId(syncEmailCollectionId + ":123").withNewFlag(true).build()),
				ImmutableSet.of(ItemDeletion.builder().serverId(syncEmailCollectionId + ":122").build()));
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
			.syncKey(syncEmailSyncKey)
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
		SyncResponse syncEmailResponse = opClient.syncWithCommand(
				syncEmailSyncKey, inbox.getCollectionId(), SyncCommand.FETCH, serverId);

		checkMailFolderHasAddItems(syncEmailResponse, inbox.getCollectionId(),
				new ItemChangeBuilder().serverId(syncEmailCollectionId + ":123").withNewFlag(false).build());
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

		SyncCollectionResponse inboxResponse = getCollectionWithId(syncResponse, collectionIdAsString);
		assertThat(inboxResponse.getSyncCollection().getStatus()).isEqualTo(SyncStatus.OBJECT_NOT_FOUND);
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
				anyObject(UserDataRequest.class),
				anyObject(SyncCollection.class),
				anyObject(SyncClientCommands.class),
				anyObject(SyncKey.class)))
			.andReturn(DataDelta.newEmptyDelta(secondRequestSyncState.getSyncDate(), secondRequestSyncState.getSyncKey()));
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expect(collectionDao.getCollectionPath(collectionId)).andReturn(collectionPath).anyTimes();
		expect(collectionDao.findItemStateForKey(initialSyncKey)).andReturn(null);
		expect(collectionDao.findItemStateForKey(secondSyncKey)).andReturn(secondRequestSyncState).times(2);
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(),
				anyObject(SyncKey.class), anyObject(Date.class))).andReturn(secondRequestSyncState).once();
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
	
	public void testPartialSyncWhenNoPreviousSendError13() throws Exception {
		SyncKey initialFolderSyncKey = new SyncKey("0");
		SyncKey nextFolderSyncKey = new SyncKey("1234");
		
		IntegrationUserAccessUtils.mockUsersAccess(classToInstanceMap, fakeTestUsers);
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(nextFolderSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		opClient.folderSync(initialFolderSyncKey);
		SyncResponse partialSyncResponse = opClient.partialSync();
		
		assertThat(partialSyncResponse.getStatus()).isEqualTo(SyncStatus.PARTIAL_REQUEST);
	}
	
	@Ignore("We don't support partial request yet")
	@Test
	public void testPartialSyncWhenValidPreviousSync() throws Exception {
		SyncKey initialFolderSyncKey = new SyncKey("0");
		SyncKey nextFolderSyncKey = new SyncKey("56789");

		SyncKey initialSyncKey = new SyncKey("1234");
		int syncEmailCollectionId = 12;
		DataDelta emptyDelta = DataDelta.builder().syncDate(new Date()).syncKey(initialSyncKey).build();
		
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(nextFolderSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(initialSyncKey, ImmutableSet.of(syncEmailCollectionId), emptyDelta, fakeTestUsers, classToInstanceMap);
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		opClient.folderSync(initialFolderSyncKey);
		opClient.syncEmail(initialSyncKey, syncEmailCollectionId, THREE_DAYS_BACK, 150);
		SyncResponse partialSyncResponse = opClient.partialSync();
		
		assertThat(partialSyncResponse.getStatus()).isEqualTo(SyncStatus.OK);
	}
	
	private MSEmail applicationData(String message, MSEmailBodyType emailBodyType) {
		return MSEmail.builder()
			.header(MSEmailHeader.builder().build())
			.body(MSEmailBody.builder()
					.mimeData(new SerializableInputStream(new ByteArrayInputStream(message.getBytes())))
					.bodyType(emailBodyType)
					.estimatedDataSize(0)
					.charset(Charsets.UTF_8)
					.truncated(false)
					.build())
			.build();
	}

	@Test
	public void testSyncOnUnexistingCollection() throws Exception {
		SyncKey syncEmailSyncKey = new SyncKey("1");
		java.util.Collection<Integer> existingCollections = Collections.emptySet();
		int syncEmailUnexistingCollectionId = 15105;
		DataDelta delta = DataDelta.builder().syncDate(new Date()).syncKey(syncEmailSyncKey).build();
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncEmailSyncKey, existingCollections, delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, syncEmailUnexistingCollectionId, THREE_DAYS_BACK, 25);

		SyncCollectionResponse mailboxResponse = getCollectionWithId(syncEmailResponse, String.valueOf(syncEmailUnexistingCollectionId));
		assertThat(mailboxResponse.getSyncCollection().getStatus()).isEqualTo(SyncStatus.OBJECT_NOT_FOUND);
	}

	@Test
	public void testSyncDataClassAtEmailButRecognizedAsCalendar() throws Exception {
		SyncKey syncKey = new SyncKey("1");
		int collectionId = 15105;
		List<Integer> existingCollections = ImmutableList.of(collectionId);
		DataDelta delta = DataDelta.builder().syncDate(new Date()).syncKey(syncKey).build();
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncKey, existingCollections, delta, fakeTestUsers, classToInstanceMap);
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		SyncResponse syncResponse = opClient.sync(syncKey, collectionId, PIMDataType.EMAIL);

		assertThat(syncResponse.getStatus()).isEqualTo(SyncStatus.SERVER_ERROR);
	}

	@Test
	public void testSyncOnHierarchyChangedException() throws Exception {
		SyncKey initialSyncKey = SyncKey.INITIAL_FOLDER_SYNC_KEY;
		SyncKey syncEmailSyncKey = new SyncKey("1");
		int syncEmailCollectionId = 4;
		expectAllocateFolderState(classToInstanceMap.get(CollectionDao.class), newSyncState(syncEmailSyncKey));
		expectCreateFolderMappingState(classToInstanceMap.get(FolderSyncStateBackendMappingDao.class));
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncWithHierarchyChangedException(syncEmailSyncKey, Sets.newHashSet(syncEmailCollectionId));
		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		opClient.folderSync(initialSyncKey);
		SyncResponse syncEmailResponse = opClient.syncEmail(syncEmailSyncKey, syncEmailCollectionId, FilterType.THREE_DAYS_BACK, 100);

		assertThat(syncEmailResponse).isNotNull();
		assertThat(syncEmailResponse.getStatus()).isEqualTo(SyncStatus.HIERARCHY_CHANGED);
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
		expect(contentsExporterBackend.getChanged(
				anyObject(UserDataRequest.class), 
				anyObject(SyncCollection.class),
				anyObject(SyncClientCommands.class),
				anyObject(SyncKey.class)))
				.andThrow(new HierarchyChangedException(new NotAllowedException("Not allowed")));

	}

	@Test
	public void testSyncWithAddCommandButWithoutApplicationDataGetsProtocolError() throws Exception {
		testSyncWithGivenCommandButWithoutApplicationDataGetsProtocolError(SyncCommand.ADD);
	}

	@Test
	public void testSyncWithChangeCommandButWithoutApplicationDataGetsProtocolError() throws Exception {
		testSyncWithGivenCommandButWithoutApplicationDataGetsProtocolError(SyncCommand.CHANGE);
	}

	private void testSyncWithGivenCommandButWithoutApplicationDataGetsProtocolError(SyncCommand command) throws Exception {
		SyncKey syncKey = new SyncKey("1");
		List<Integer> existingCollections = ImmutableList.of(15);
		DataDelta delta = DataDelta.builder().syncDate(new Date()).syncKey(syncKey).build();
		mockHierarchyChangesOnlyInbox(classToInstanceMap);
		mockEmailSyncClasses(syncKey, existingCollections, delta, fakeTestUsers, classToInstanceMap);

		mocksControl.replay();
		opushServer.start();

		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, opushServer.getPort());
		SyncResponse syncResponse = opClient.syncWithCommand(syncKey, "15", command, "15:51");

		assertThat(syncResponse.getStatus()).isEqualTo(SyncStatus.PROTOCOL_ERROR);
	}
}
