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
package org.obm.push.handler;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.obm.push.backend.DataDelta;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.utils.collection.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ResponseWindowingService {

	private interface Store {
		Collection<ItemChange> listAndRemove();
		void store(List<ItemChange> itemsToStore);
	}
	
	private static class WindowLogic {
		private Logger logger = LoggerFactory.getLogger(getClass());
		private final Store store;
		
		WindowLogic(Store store) {
			this.store = store;
		}
		
		List<ItemChange> window(SyncCollection c, List<ItemChange> newChanges, Map<String, String> processedClientIds) {
		
			List<ItemChange> changes = listChanges(newChanges);
			
			if (changesFitWindow(c.getWindowSize(), changes)) {
				return changes;
			} else {
				return handleChangesOverflow(c, processedClientIds, changes);
			}
		}
		
		List<ItemChange> listChanges(List<ItemChange> newChanges) {
			
			return Lists.newArrayList(
					Iterables.concat(
							popUnsynchronizedChanges(),
							newChanges));
		}

		Collection<ItemChange> popUnsynchronizedChanges() {
			Collection<ItemChange> unsynchronizedItems = store.listAndRemove();
			return unsynchronizedItems;
		}

		boolean changesFitWindow(final Integer windowSize, List<ItemChange> changes) {
			return changes.size() <= windowSize;
		}
		
		private List<ItemChange> handleChangesOverflow(
				SyncCollection c, Map<String, String> processedClientIds, List<ItemChange> changes) {
			
			c.setMoreAvailable(true);
			
			logWindowingInformation(c, changes);

			List<ItemChange> changesFromClient = changedFromClient(changes, processedClientIds);
			List<ItemChange> changesFromServer = changesFromServer(changes, changesFromClient);
			
			int numberOfChangesFromServerToInclude = Math.max(0, c.getWindowSize() - changesFromClient.size());
			storeOverflowingChanges(changesFromServer, numberOfChangesFromServerToInclude);
			
			return Lists.newArrayList(
					Iterables.concat(
							changesFromClient, 
							Iterables.limit(changesFromServer, numberOfChangesFromServerToInclude)));
		}
		
		private void logWindowingInformation(SyncCollection c, List<ItemChange> changes) {
			int overflow = changes.size() - c.getWindowSize();
			logger.info("Should send {} change(s)", changes.size());
			logger.info("WindowsSize value is {} , {} change(s) will not be sent", c.getWindowSize(), overflow);
		}

		private List<ItemChange> changesFromServer(List<ItemChange> changes, List<ItemChange> itemsChangedByClient) {
			return Lists.newArrayList(
					Sets.difference(changes, itemsChangedByClient, new Comparator<ItemChange>() {
						@Override
						public int compare(ItemChange o1, ItemChange o2) {
							return o1.getServerId().compareTo(o2.getServerId());
						}
					})
				);
		}

		private List<ItemChange> changedFromClient(List<ItemChange> changes, Map<String, String> processedClientIds) {
			List<ItemChange> itemsChangedByClient = Lists.newArrayList();
			for (ItemChange change: changes) {
				if (processedClientIds.containsKey(change.getServerId())) {
					itemsChangedByClient.add(change);
				}
				if (processedClientIds.size() == itemsChangedByClient.size()) {
					break;
				}
			}
			
			return itemsChangedByClient;
		}
		
		private void storeOverflowingChanges(List<ItemChange> changesFromServer, int numberOfChangesFromServerToInclude) {
			if (numberOfChangesFromServerToInclude < changesFromServer.size()) {
				List<ItemChange> itemsToStore = Lists.newArrayList(Iterables.skip(changesFromServer, numberOfChangesFromServerToInclude));
				store.store(itemsToStore);
			}
		}

	}
	
	private final UnsynchronizedItemDao unSynchronizedItemCache;
	
	@Inject
	@VisibleForTesting ResponseWindowingService(UnsynchronizedItemDao unSynchronizedItemCache) {
		this.unSynchronizedItemCache = unSynchronizedItemCache;
	}
	
	public List<ItemChange> windowChanges(SyncCollection c, DataDelta delta,
			UserDataRequest userDataRequest, Map<String, String> processedClientIds) {
		final Credentials credentials = userDataRequest.getCredentials();
		final Device device = userDataRequest.getDevice();
		final Integer collectionId = c.getCollectionId();
		
		return new WindowLogic(new Store() {
			
			@Override
			public void store(List<ItemChange> itemsToStore) {
				unSynchronizedItemCache.storeItemsToAdd(credentials, device, collectionId, itemsToStore);
			}

			@Override
			public Collection<ItemChange> listAndRemove() {
				Collection<ItemChange> items = unSynchronizedItemCache.listItemsToAdd(credentials, device, collectionId);
				unSynchronizedItemCache.clearItemsToAdd(credentials, device, collectionId);
				return items;
			}
			
		}).window(c, delta.getChanges(), processedClientIds);
	}

	public List<ItemChange> windowDeletions(final SyncCollection c, DataDelta delta, final UserDataRequest userDataRequest, Map<String, String> processedClientIds) {
		final Credentials credentials = userDataRequest.getCredentials();
		final Device device = userDataRequest.getDevice();
		final Integer collectionId = c.getCollectionId();
		
		return new WindowLogic(new Store() {
			
			@Override
			public void store(List<ItemChange> itemsToStore) {
				unSynchronizedItemCache.storeItemsToRemove(credentials, device, collectionId, itemsToStore);
			}
			
			@Override
			public Collection<ItemChange> listAndRemove() {
				Collection<ItemChange> items = unSynchronizedItemCache.listItemsToRemove(credentials, device, collectionId);
				unSynchronizedItemCache.clearItemsToRemove(credentials, device, collectionId);
				return items;
			}
			
		}).window(c, delta.getDeletions(), processedClientIds);
	}

	
}
