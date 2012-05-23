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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.SyncCollection;
import org.obm.push.store.UnsynchronizedItemDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ResponseWindowingProcessor {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private final UnsynchronizedItemDao unSynchronizedItemCache;
	
	@Inject
	@VisibleForTesting ResponseWindowingProcessor(UnsynchronizedItemDao unSynchronizedItemCache) {
		this.unSynchronizedItemCache = unSynchronizedItemCache;
	}
	
	@VisibleForTesting List<ItemChange> window(SyncCollection c, DataDelta delta, 
			BackendSession backendSession, Map<String, String> processedClientIds) {
	
		final Credentials credentials = backendSession.getCredentials();
		final Device device = backendSession.getDevice();
		
		List<ItemChange> changes = listItems(delta, credentials, device, c.getCollectionId());
		
		if (changesFitWindow(c.getWindowSize(), changes)) {
			return changes;
		} else {
			return handleOverflow(c, processedClientIds, credentials, device, changes);
		}
	}

	private List<ItemChange> handleOverflow(SyncCollection c, Map<String, String> processedClientIds, 
			Credentials credentials, Device device, List<ItemChange> changes) {
		
		c.setMoreAvailable(true);
		
		logWindowingInformation(c, changes);

		List<ItemChange> changesFromClient = changedFromClient(changes, processedClientIds);
		List<ItemChange> changesFromServer = changesFromServer(changes, changesFromClient);
		
		int numberOfChangesFromServerToInclude = Math.max(0, c.getWindowSize() - changesFromClient.size());
		storeOverflowingItems(credentials, device, c.getCollectionId(), changesFromServer, numberOfChangesFromServerToInclude);
		
		return Lists.newArrayList(
				Iterables.concat(
						changesFromClient, 
						Iterables.limit(changesFromServer, numberOfChangesFromServerToInclude)));
	}

	private void storeOverflowingItems(final Credentials credentials,
			final Device device, final Integer collectionId,
			List<ItemChange> changesFromServer,
			int numberOfChangesFromServerToInclude) {
		if (numberOfChangesFromServerToInclude < changesFromServer.size()) {
			List<ItemChange> itemsToStore = Lists.newArrayList(Iterables.skip(changesFromServer, numberOfChangesFromServerToInclude));
			unSynchronizedItemCache.storeItemsToAdd(credentials, device, collectionId, itemsToStore);
		}
	}

	private List<ItemChange> changesFromServer(List<ItemChange> changes, List<ItemChange> itemsChangedByClient) {
		List<ItemChange> changesFromServer = Lists.newArrayList(changes);
		changesFromServer.removeAll(itemsChangedByClient);
		return changesFromServer;
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

	private void logWindowingInformation(SyncCollection c, List<ItemChange> changes) {
		int overflow = changes.size() - c.getWindowSize();
		logger.info("Should send {} change(s)", changes.size());
		logger.info("WindowsSize value is {} , {} change(s) will not be sent", c.getWindowSize(), overflow);
	}

	private boolean changesFitWindow(final Integer windowSize,
			List<ItemChange> changes) {
		return changes.size() <= windowSize;
	}

	private List<ItemChange> listItems(DataDelta delta, 	Credentials credentials, Device device, Integer collectionId) {
		
		return Lists.newArrayList(
				Iterables.concat(
						popUnsynchronizedItems(credentials, device, collectionId),
						getNewItems(delta)));
	}

	private List<ItemChange> getNewItems(DataDelta delta) {
		if (delta != null) {
			return delta.getChanges();
		}
		return ImmutableList.<ItemChange>of();
	}

	private Set<ItemChange> popUnsynchronizedItems(
			final Credentials credentials, final Device device,
			final Integer collectionId) {
		Set<ItemChange> unsynchronizedItems = unSynchronizedItemCache.listItemsToAdd(credentials, device, collectionId);
		unSynchronizedItemCache.clearItemsToAdd(credentials, device,collectionId);
		return unsynchronizedItems;
	}

	
}
