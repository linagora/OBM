package org.obm.opush.command.sync;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationTestUtils.expectUserCollectionsNeverChange;
import static org.obm.opush.IntegrationTestUtils.replayMocks;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.fest.assertions.Assertions;
import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.IContentsExporter;
import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.push.client.Add;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Delete;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.SyncResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

public class EmailSyncTestUtils {
	
	public static void checkSyncDefaultMailFolderHasNoChange(Folder inbox, SyncResponse syncEmailResponse) {
		checkSyncDefaultMailFolderHasItems(inbox, syncEmailResponse, ImmutableList.<Add>of(), ImmutableList.<Delete>of());
	}

	public static void checkSyncDefaultMailFolderHasAddItems(Folder inbox, SyncResponse response, Add... changes) {
		checkSyncDefaultMailFolderHasItems(inbox, response, Arrays.asList(changes), ImmutableList.<Delete>of());
	}

	public static void checkSyncDefaultMailFolderHasDeleteItems(Folder inbox, SyncResponse response, Delete... deletes) {
		checkSyncDefaultMailFolderHasItems(inbox, response, ImmutableList.<Add>of(), Arrays.asList(deletes));
	}

	
	public static void checkSyncDefaultMailFolderHasItems(
			Folder inbox, SyncResponse response, Iterable<Add> adds, Iterable<Delete> deletes) {
		Assertions.assertThat(response).isNotNull();
		Collection inboxCollection = response.getCollection(String.valueOf(inbox.getServerId()));
		Assertions.assertThat(inboxCollection).isNotNull();
		Assertions.assertThat(inboxCollection.getAdds()).containsOnly(Iterables.toArray(adds, Object.class));
		Assertions.assertThat(inboxCollection.getDeletes()).containsOnly(Iterables.toArray(deletes, Object.class));
	}

	public static void mockEmailSyncClasses(
			String syncEmailSyncKey, int syncEmailCollectionId, DataDelta delta, 
			List<OpushUser> fakeTestUsers, ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, CollectionNotFoundException, ProcessingEmailException, UnknownObmSyncServerException, AuthFault {
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockEmailSync(syncEmailSyncKey, syncEmailCollectionId, delta, fakeTestUsers, classToInstanceMap);
		replayMocks(classToInstanceMap);
	}
	
	private static void mockEmailSync(String syncEmailSyncKey, int syncEmailCollectionId, DataDelta delta,
			List<OpushUser> fakeTestUsers, ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, CollectionNotFoundException, ProcessingEmailException, UnknownObmSyncServerException {
		SyncedCollectionDao syncedCollectionDao = classToInstanceMap.get(SyncedCollectionDao.class);
		mockEmailSyncedCollectionDao(syncedCollectionDao);
		
		UnsynchronizedItemDao unsynchronizedItemDao = classToInstanceMap.get(UnsynchronizedItemDao.class);
		mockEmailUnsynchronizedItemDao(unsynchronizedItemDao);

		IContentsExporter contentsExporterBackend = classToInstanceMap.get(IContentsExporter.class);
		mockContentsExporter(contentsExporterBackend, delta);

		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers);
		mockCollectionDaoForEmailSync(collectionDao, syncEmailSyncKey, syncEmailCollectionId);
		
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		mockItemTrackingDao(itemTrackingDao);
	}
	
	private static void mockEmailUnsynchronizedItemDao(UnsynchronizedItemDao unsynchronizedItemDao) {
		expect(unsynchronizedItemDao.listItemsToAdd(
				anyObject(Credentials.class), 
				anyObject(Device.class),
				anyInt())).andReturn(ImmutableSet.<ItemChange>of()).anyTimes();
		unsynchronizedItemDao.clearItemsToAdd(
				anyObject(Credentials.class), 
				anyObject(Device.class),
				anyInt());
		expectLastCall().anyTimes();
		expect(unsynchronizedItemDao.listItemsToRemove(
				anyObject(Credentials.class), 
				anyObject(Device.class),
				anyInt())).andReturn(null).anyTimes();
		unsynchronizedItemDao.storeItemsToRemove(
				anyObject(Credentials.class), 
				anyObject(Device.class),
				anyInt(),
				anyObject(List.class));
		expectLastCall().anyTimes();
	}

	private static void mockEmailSyncedCollectionDao(SyncedCollectionDao syncedCollectionDao) {
		expect(syncedCollectionDao.get(
				anyObject(Credentials.class), 
				anyObject(Device.class),
				anyInt())).andReturn(null).anyTimes();
		syncedCollectionDao.put(
				anyObject(Credentials.class), 
				anyObject(Device.class),
				anyObject(Set.class));
		expectLastCall().once().anyTimes();
	}
	

	private static void mockItemTrackingDao(ItemTrackingDao itemTrackingDao) throws DaoException {
		itemTrackingDao.markAsSynced(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		itemTrackingDao.markAsDeleted(anyObject(SyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		expect(itemTrackingDao.isServerIdSynced(anyObject(SyncState.class), anyObject(ServerId.class))).andReturn(false).anyTimes();
	}

	private static void mockCollectionDaoForEmailSync(CollectionDao collectionDao, String syncEmailSyncKey, int syncEmailCollectionId) throws DaoException {
		expect(collectionDao.getCollectionMapping(anyObject(Device.class), anyObject(String.class)))
				.andReturn(syncEmailCollectionId).anyTimes();
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(), anyObject(SyncState.class)))
				.andReturn((int)(Math.random()*10000)).anyTimes();
		SyncState state = new SyncState(syncEmailSyncKey);
		expect(collectionDao.findStateForKey(syncEmailSyncKey)).andReturn(state).anyTimes();
	}

	private static void mockContentsExporter(IContentsExporter contentsExporter, DataDelta delta) 
			throws CollectionNotFoundException, ProcessingEmailException, DaoException, UnknownObmSyncServerException {

		expect(contentsExporter.getChanged(
				anyObject(BackendSession.class), 
				anyObject(SyncState.class),
				anyInt(),
				anyObject(FilterType.class),
				anyObject(PIMDataType.class)))
			.andReturn(delta).once();
	}

}
