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
package org.obm.push.handler;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.obm.push.backend.DataDelta;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ASItem;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.collection.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ResponseWindowingService {

	private interface Store<T extends ASItem> {
		Collection<T> listAndRemove();
		void store(List<T> itemsToStore);
	}
	
	private static interface ChangesMergePolicy<T extends ASItem> {
		List<T> merge(Collection<T> lhs, List<T> rhs);
	}
	
	private static class WindowLogic<T extends ASItem> {
		private final Logger logger = LoggerFactory.getLogger(getClass());
		private final Store<T> store;
		private final ChangesMergePolicy<T> changesMergePolicy;
		
		WindowLogic(Store<T> store, ChangesMergePolicy<T> changesMergePolicy) {
			this.store = store;
			this.changesMergePolicy = changesMergePolicy;
		}
		
		List<T> window(AnalysedSyncCollection c, List<T> newChanges, SyncClientCommands clientCommands) {
		
			List<T> changes = listChanges(newChanges);
			
			if (changesFitWindow(c.getWindowSize(), changes)) {
				return changes;
			} else {
				return handleChangesOverflow(c, clientCommands, changes);
			}
		}
		
		protected List<T> listChanges(List<T> newChanges) {
			return changesMergePolicy.merge(popUnsynchronizedChanges(), newChanges);
		}

		Collection<T> popUnsynchronizedChanges() {
			Collection<T> unsynchronizedItems = store.listAndRemove();
			return unsynchronizedItems;
		}

		boolean changesFitWindow(final Integer windowSize, List<T> changes) {
			return changes.size() <= windowSize;
		}
		
		private List<T> handleChangesOverflow(
				AnalysedSyncCollection c, SyncClientCommands clientCommands, List<T> changes) {
			
			logWindowingInformation(c, changes);

			List<T> changesFromClient = changedFromClient(changes, clientCommands);
			List<T> changesFromServer = changesFromServer(changes, changesFromClient);
			
			int numberOfChangesFromServerToInclude = Math.max(0, c.getWindowSize() - changesFromClient.size());
			storeOverflowingChanges(changesFromServer, numberOfChangesFromServerToInclude);
			
			return Lists.newArrayList(
					Iterables.concat(
							changesFromClient, 
							Iterables.limit(changesFromServer, numberOfChangesFromServerToInclude)));
		}
		
		private void logWindowingInformation(AnalysedSyncCollection c, List<T> changes) {
			int overflow = changes.size() - c.getWindowSize();
			logger.info("Should send {} change(s)", changes.size());
			logger.info("WindowsSize value is {} , {} change(s) will not be sent", c.getWindowSize(), overflow);
		}

		private List<T> changesFromServer(List<T> changes, List<T> itemsChangedByClient) {
			return Lists.newArrayList(
					Sets.difference(changes, itemsChangedByClient, new Comparator<T>() {
						@Override
						public int compare(T o1, T o2) {
							return o1.getServerId().compareTo(o2.getServerId());
						}
					})
				);
		}

		private List<T> changedFromClient(List<T> changes, SyncClientCommands clientCommands) {
			List<T> itemsChangedByClient = Lists.newArrayList();
			for (T change: changes) {
				if (clientCommands.hasCommandWithServerId(change.getServerId())) {
					itemsChangedByClient.add(change);
				}
				if (clientCommands.sumOfCommands() == itemsChangedByClient.size()) {
					break;
				}
			}
			
			return itemsChangedByClient;
		}
		
		private void storeOverflowingChanges(List<T> changesFromServer, int numberOfChangesFromServerToInclude) {
			if (numberOfChangesFromServerToInclude < changesFromServer.size()) {
				List<T> itemsToStore = Lists.newArrayList(Iterables.skip(changesFromServer, numberOfChangesFromServerToInclude));
				store.store(itemsToStore);
			}
		}

	}
	
	private final UnsynchronizedItemDao unSynchronizedItemCache;
	
	@Inject
	@VisibleForTesting ResponseWindowingService(UnsynchronizedItemDao unSynchronizedItemCache) {
		this.unSynchronizedItemCache = unSynchronizedItemCache;
	}
	
	public DataDelta windowedResponse(AnalysedSyncCollection c, DataDelta delta, SyncClientCommands clientCommands, SyncKey newSyncKey) {
		return DataDelta.builder()
				.changes(windowChanges(c, newSyncKey, delta, clientCommands))
				.deletions(windowDeletions(c, newSyncKey, delta, clientCommands))
				.syncDate(delta.getSyncDate())
				.syncKey(delta.getSyncKey())
				.moreAvailable(hasPendingResponse(c.getSyncKey()))
				.build();
	}
	
	@VisibleForTesting List<ItemChange> windowChanges(AnalysedSyncCollection c, final SyncKey newSyncKey, DataDelta delta,
			SyncClientCommands clientCommands) {
		Preconditions.checkNotNull(delta);
		Preconditions.checkNotNull(c);
		Preconditions.checkNotNull(clientCommands);
		
		final SyncKey syncKey = c.getSyncKey();
		
		return new WindowLogic<ItemChange>(new Store<ItemChange>() {
			
			@Override
			public void store(List<ItemChange> itemsToStore) {
				unSynchronizedItemCache.storeItemsToAdd(newSyncKey, itemsToStore);
			}

			@Override
			public Collection<ItemChange> listAndRemove() {
				Collection<ItemChange> items = unSynchronizedItemCache.listItemsToAdd(syncKey);
				unSynchronizedItemCache.clearItemsToAdd(syncKey);
				return items;
			}
			
		}, new ChangesMergePolicy<ItemChange>() {
			@Override
			public List<ItemChange> merge(Collection<ItemChange> lhs, List<ItemChange> rhs) {
				return Lists.newArrayList(Iterables.concat(lhs, rhs));
			}
		}).window(c, delta.getChanges(), clientCommands);
	}

	@VisibleForTesting List<ItemDeletion> windowDeletions(final AnalysedSyncCollection c, final SyncKey newSyncKey, DataDelta delta, SyncClientCommands clientCommands) {
		final SyncKey syncKey = c.getSyncKey();
		
		return new WindowLogic<ItemDeletion>(new Store<ItemDeletion>() {
			
			@Override
			public void store(List<ItemDeletion> itemsToStore) {
				unSynchronizedItemCache.storeItemsToRemove(newSyncKey, itemsToStore);
			}
			
			@Override
			public Collection<ItemDeletion> listAndRemove() {
				Collection<ItemDeletion> items = unSynchronizedItemCache.listItemsToRemove(syncKey);
				unSynchronizedItemCache.clearItemsToRemove(syncKey);
				return items;
			}
			
		}, new ChangesMergePolicy<ItemDeletion>() {
			@Override
			public List<ItemDeletion> merge(Collection<ItemDeletion> lhs, List<ItemDeletion> rhs) {
				return Lists.newArrayList(
						ImmutableSet.copyOf(Iterables.concat(lhs, rhs)));
			}
		}).window(c, delta.getDeletions(), clientCommands);
	}

	public boolean hasPendingResponse(SyncKey syncKey) {
		return unSynchronizedItemCache.hasAnyItemsFor(syncKey);
	}

	
}
