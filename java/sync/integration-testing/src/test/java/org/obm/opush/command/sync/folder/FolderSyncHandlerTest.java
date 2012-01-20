package org.obm.opush.command.sync.folder;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationPushTestUtils.mockHierarchyChanges;
import static org.obm.opush.IntegrationTestUtils.buildWBXMLOpushClient;
import static org.obm.opush.IntegrationTestUtils.expectUserCollectionsNeverChange;
import static org.obm.opush.IntegrationTestUtils.replayMocks;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.PortNumber;
import org.obm.opush.SingleUserFixture;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.bean.Device;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.push.client.FolderHierarchy;
import org.obm.sync.push.client.FolderStatus;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.OPClient;

import com.google.inject.Inject;

public class FolderSyncHandlerTest {

	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(FolderSyncHandlerTestModule.class);

	@Inject @PortNumber int port;
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
	public void testInitialFolderSync() throws Exception {
		String initialSyncKey = "0";
		int serverId = 4;
				
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockHierarchyChanges(classToInstanceMap);
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers);
		mockCollectionDao(collectionDao, initialSyncKey, serverId);
		
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		mockItemTrackingDao(itemTrackingDao);
		
		replayMocks(classToInstanceMap);
		
		opushServer.start();
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(initialSyncKey);
		
		Assertions.assertThat(folderSyncResponse.getStatus()).isEqualTo(1);
		checkRegularFoldersAreSynchronized(folderSyncResponse, FolderStatus.ADD);
	}
	
	@Test
	public void testFolderSyncUnchange() throws Exception {
		String syncKey = "d58ea559-d1b8-4091-8ba5-860e6fa54875";
		int serverId = 4;
		
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockHierarchyChanges(classToInstanceMap);
		
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers);
		mockCollectionDao(collectionDao, syncKey, serverId);
		
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		mockItemTrackingDao(itemTrackingDao);		
		
		replayMocks(classToInstanceMap);
		
		opushServer.start();
		
		OPClient opClient = buildWBXMLOpushClient(singleUserFixture.jaures, port);
		FolderSyncResponse folderSyncResponse = opClient.folderSync(syncKey);
		
		Assertions.assertThat(folderSyncResponse.getStatus()).isEqualTo(1);
		Assertions.assertThat(folderSyncResponse.getCount()).isEqualTo(0);
		Assertions.assertThat(folderSyncResponse.getFolders()).isEmpty();
	}
	
	private void checkRegularFoldersAreSynchronized(FolderSyncResponse folderSyncResponse, FolderStatus folderStatus) {
		FolderHierarchy folderHierarchy = folderSyncResponse.getFolders();
		Assertions.assertThat(folderHierarchy).isNotNull();
		Assertions.assertThat(folderHierarchy.keySet())
			.contains(FolderType.DEFAULT_INBOX_FOLDER,
					FolderType.DEFAULT_DRAFTS_FOLDERS,
					FolderType.DEFAULT_SENT_EMAIL_FOLDER,
					FolderType.DEFAULT_DELETED_ITEMS_FOLDERS);
		checkStatus(folderHierarchy, FolderType.DEFAULT_INBOX_FOLDER, folderStatus);
		checkStatus(folderHierarchy, FolderType.DEFAULT_DRAFTS_FOLDERS, folderStatus);
		checkStatus(folderHierarchy, FolderType.DEFAULT_SENT_EMAIL_FOLDER, folderStatus);
		checkStatus(folderHierarchy, FolderType.DEFAULT_DELETED_ITEMS_FOLDERS, folderStatus);
	}

	private void checkStatus(FolderHierarchy folderHierarchy, FolderType folderType, FolderStatus folderStatus) {
		Assertions.assertThat(folderHierarchy.get(folderType).getStatus()).isEqualTo(folderStatus);
	}
	
	private void mockItemTrackingDao(ItemTrackingDao itemTrackingDao) throws DaoException {
		itemTrackingDao.markAsSynced(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		itemTrackingDao.markAsDeleted(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		expect(itemTrackingDao.isServerIdSynced(anyObject(SyncState.class), anyObject(ServerId.class))).andReturn(false).anyTimes();
	}

	private void mockCollectionDao(CollectionDao collectionDao, String syncKey, int serverId) throws DaoException {
		expect(collectionDao.getCollectionMapping(anyObject(Device.class), anyObject(String.class)))
				.andReturn(serverId).anyTimes();
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(), anyObject(SyncState.class)))
				.andReturn((int)(Math.random()*10000)).anyTimes();
		SyncState state = new SyncState(syncKey);
		expect(collectionDao.findStateForKey(syncKey)).andReturn(state).anyTimes();
	}

}
