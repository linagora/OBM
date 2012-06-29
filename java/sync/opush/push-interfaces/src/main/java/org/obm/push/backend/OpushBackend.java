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
package org.obm.push.backend;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.push.backend.CollectionPath.Builder;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncState;
import org.obm.push.bean.HierarchyItemsChanges;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.service.impl.MappingService;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Provider;

public abstract class OpushBackend {

	protected final MappingService mappingService;
	protected final Provider<Builder> collectionPathBuilderProvider;

	protected OpushBackend(MappingService mappingService, Provider<Builder> collectionPathBuilderProvider) {
		this.mappingService = mappingService;
		this.collectionPathBuilderProvider = collectionPathBuilderProvider;
	}
	
	protected void snapshotHierarchy(UserDataRequest udr, Iterable<CollectionPath> collections,
			FolderSyncState outgoingSyncState) throws DaoException {

		Set<Integer> collectionIds = Sets.newHashSet();
		for (CollectionPath collection: collections) {
			collectionIds.add(getOrCreateCollectionId(udr.getDevice(), collection));
		}
		if (!collectionIds.isEmpty()) {
			mappingService.snapshotCollections(outgoingSyncState, collectionIds);
		}
	}
	
	private int getOrCreateCollectionId(Device device, CollectionPath collection)
			throws DaoException {
		
		try {
			return mappingService.getCollectionIdFor(device, collection.collectionPath());
		} catch (CollectionNotFoundException e) {
			return mappingService.createCollectionMapping(device, collection.collectionPath());
		}
	}

	protected CollectionPath collectionPathForItem(UserDataRequest udr, PIMDataType pimType, ItemChange itemChange) {
		return collectionPathBuilderProvider.get()
				.userDataRequest(udr)
				.pimType(pimType)
				.displayName(itemChange.getDisplayName())
				.build();
	}

	protected ImmutableSet<CollectionPath> lastKnownCollectionPath(UserDataRequest udr,
			FolderSyncState lastKnownState, final PIMDataType filterPimDataType) throws DaoException {
		
		return FluentIterable
				.from(mappingService.listCollections(udr, lastKnownState))
				.filter(
						new Predicate<CollectionPath>() {
							@Override
							public boolean apply(CollectionPath collectionPath) {
								return collectionPath.pimType() == filterPimDataType;
							}
						})
				.toImmutableSet();
	}

	protected HierarchyItemsChanges buildHierarchyItemsChanges(UserDataRequest udr,
			Iterable<CollectionPath> changedCollections, Iterable<CollectionPath> deletedCollections)
					throws DaoException, CollectionNotFoundException {
		
		return new HierarchyItemsChanges.Builder()
			.changes(itemChangesTaggedNew(udr, changedCollections))
			.deletions(itemChangesTaggedDeleted(udr, deletedCollections))
			.lastSync(new Date())
			.build();
	}


	private List<ItemChange> itemChangesTaggedNew(UserDataRequest udr, Iterable<CollectionPath> changedCollections)
			throws DaoException, CollectionNotFoundException {
		return itemChanges(udr, changedCollections, true);
	}

	private List<ItemChange> itemChangesTaggedDeleted(UserDataRequest udr, Iterable<CollectionPath> deletedCollections)
			throws DaoException, CollectionNotFoundException {
		return itemChanges(udr, deletedCollections, false);
	}
	
	private List<ItemChange> itemChanges(UserDataRequest udr, Iterable<CollectionPath> collectionPaths, boolean isNew)
			throws DaoException, CollectionNotFoundException {

		List<ItemChange> changes = Lists.newArrayList();
		for (CollectionPath collectionPath: collectionPaths) {
			ItemChange itemChange = createItemChange(udr, collectionPath);
			itemChange.setNew(isNew);
			changes.add(itemChange);
		}
		return changes;
	}

	protected abstract ItemChange createItemChange(UserDataRequest udr, CollectionPath collectionPath)
			throws DaoException, CollectionNotFoundException;
}
