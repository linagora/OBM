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
package org.obm.opush.command.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.obm.opush.IntegrationTestUtils.expectUserCollectionsNeverChange;
import static org.obm.opush.IntegrationUserAccessUtils.mockUsersAccess;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.hierarchy.CollectionChange;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.AuthFault;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

public class EmailSyncTestUtils {
	
	public static void checkMailFolderHasNoChange(SyncResponse response, String serverId) {
		SyncCollectionResponse collection = getCollectionWithId(response, serverId);
		assertThat(collection.getItemChanges()).isEmpty();
		assertThat(collection.getItemChangesDeletion()).isEmpty();
	}

	public static void checkMailFolderHasAddItems(SyncResponse response, String serverId, ItemChange... changes) {
		SyncCollectionResponse collection = getCollectionWithId(response, serverId);
		assertThat(collection.getItemChanges()).containsOnly(changes);
		assertThat(collection.getItemChangesDeletion()).isEmpty();
	}

	public static void checkMailFolderHasFetchItems(SyncResponse response, String serverId, ItemChange... changes) {
		SyncCollectionResponse collection = getCollectionWithId(response, serverId);
		List<ItemChange> itemChanges = collection.getItemFetchs();
		assertThat(itemChanges).containsOnly(changes);
		assertThat(collection.getItemChangesDeletion()).isEmpty();
	}

	public static void checkMailFolderHasDeleteItems(SyncResponse response, String serverId, ItemDeletion... deletes) {
		SyncCollectionResponse collection = getCollectionWithId(response, serverId);
		assertThat(collection.getItemChanges()).isEmpty();
		assertThat(collection.getItemChangesDeletion()).containsOnly(deletes);
	}

	public static void checkMailFolderHasItems(
			SyncResponse response, String serverId, Iterable<ItemChange> changes, Iterable<ItemDeletion> deletes) {
		SyncCollectionResponse collection = getCollectionWithId(response, serverId);
		assertThat(collection.getItemChanges()).containsOnly(Iterables.toArray(changes, ItemChange.class));
		assertThat(collection.getItemChangesDeletion()).containsOnly(Iterables.toArray(deletes, ItemDeletion.class));
	}


	public static void assertEqualsWithoutApplicationData(List<ItemChange> itemChanges, List<ItemChange> expectedChanges) {
		assertThat(itemChanges).hasSize(expectedChanges.size());
		
		for (ItemChange change : itemChanges) {
			for (ItemChange expected : expectedChanges) {
				if (change.getServerId().equals(expected.getServerId())) {
					assertThat(change.isNew()).isEqualTo(expected.isNew());
				}
			}
		}
	}
	
	public static SyncCollectionResponse getCollectionWithId(SyncResponse response, String serverId) {
		for (SyncCollectionResponse collection : response.getCollectionResponses()) {
			if (serverId.equals(String.valueOf(collection.getCollectionId()))) {
				return collection;
			}
		}
		return null;
	}

	public static CollectionChange lookupInbox(Iterable<CollectionChange> items) {
		return FluentIterable
				.from(items)
				.firstMatch(new Predicate<CollectionChange>() {
		
					@Override
					public boolean apply(CollectionChange input) {
						return input.getFolderType() == org.obm.push.bean.FolderType.DEFAULT_INBOX_FOLDER;
					}
				}).get();
	}

	public static void mockEmailSyncClasses(
			SyncKey syncEmailSyncKey, Collection<Integer> syncEmailCollectionsIds, DataDelta delta, 
			List<OpushUser> fakeTestUsers, ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, CollectionNotFoundException, ProcessingEmailException, UnexpectedObmSyncServerException, AuthFault,
			ConversionException, FilterTypeChangedException, HierarchyChangedException {
		
		mockUsersAccess(classToInstanceMap, fakeTestUsers);
		mockEmailSync(syncEmailSyncKey, syncEmailCollectionsIds, delta, fakeTestUsers, classToInstanceMap);
	}
	
	private static void mockEmailSync(SyncKey syncEmailSyncKey, Collection<Integer> syncEmailCollectionsIds, DataDelta delta,
			List<OpushUser> fakeTestUsers, ClassToInstanceAgregateView<Object> classToInstanceMap)
			throws DaoException, CollectionNotFoundException, ProcessingEmailException, UnexpectedObmSyncServerException,
			ConversionException, FilterTypeChangedException, HierarchyChangedException {
		
		IContentsExporter contentsExporterBackend = classToInstanceMap.get(IContentsExporter.class);
		mockContentsExporter(contentsExporterBackend, delta);

		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		expectUserCollectionsNeverChange(collectionDao, fakeTestUsers, syncEmailCollectionsIds);
		mockCollectionDaoForEmailSync(collectionDao, syncEmailSyncKey, syncEmailCollectionsIds);
		
		ItemTrackingDao itemTrackingDao = classToInstanceMap.get(ItemTrackingDao.class);
		mockItemTrackingDao(itemTrackingDao);
	}

	public static void mockItemTrackingDao(ItemTrackingDao itemTrackingDao) throws DaoException {
		itemTrackingDao.markAsSynced(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		itemTrackingDao.markAsDeleted(anyObject(ItemSyncState.class), anyObject(Set.class));
		expectLastCall().anyTimes();
		expect(itemTrackingDao.isServerIdSynced(anyObject(ItemSyncState.class), anyObject(ServerId.class))).andReturn(false).anyTimes();
	}

	public static void mockCollectionDaoForEmailSync(CollectionDao collectionDao, SyncKey syncEmailSyncKey,
			Collection<Integer> syncEmailCollectionsIds) throws DaoException {
		
		for (Integer syncEmailCollectionId : syncEmailCollectionsIds) {
			expect(collectionDao.getCollectionMapping(anyObject(Device.class), anyObject(String.class)))
				.andReturn(syncEmailCollectionId).anyTimes();
		}
		expect(collectionDao.updateState(anyObject(Device.class), anyInt(), anyObject(SyncKey.class), anyObject(Date.class)))
				.andReturn(ItemSyncState.builder()
						.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
						.syncKey(syncEmailSyncKey)
						.id((int)(Math.random()*10000))
						.build()).anyTimes();
		ItemSyncState state = ItemSyncState.builder()
				.syncDate(DateUtils.getEpochPlusOneSecondCalendar().getTime())
				.syncKey(syncEmailSyncKey)
				.build();
		expect(collectionDao.findItemStateForKey(syncEmailSyncKey)).andReturn(state).anyTimes();
	}

	private static void mockContentsExporter(IContentsExporter contentsExporter, DataDelta delta) 
			throws CollectionNotFoundException, ProcessingEmailException, DaoException, UnexpectedObmSyncServerException, ConversionException,
			FilterTypeChangedException, HierarchyChangedException {

		expect(contentsExporter.getChanged(
				anyObject(UserDataRequest.class),
				anyObject(ItemSyncState.class),
				anyObject(AnalysedSyncCollection.class),
				anyObject(SyncClientCommands.class),
				anyObject(SyncKey.class)))
				.andReturn(delta).once();
		
		expect(contentsExporter.getItemEstimateSize(
				anyObject(UserDataRequest.class), 
				anyObject(PIMDataType.class),
				anyObject(AnalysedSyncCollection.class),
				anyObject(ItemSyncState.class)))
			.andReturn(delta.getItemEstimateSize()).anyTimes();
	}

}
