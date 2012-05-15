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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
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
	
	@VisibleForTesting List<ItemChange> processWindowSize(SyncCollection c, DataDelta delta, 
			BackendSession backendSession, Map<String, String> processedClientIds) {
	
		final Credentials credentials = backendSession.getCredentials();
		final Device device = backendSession.getDevice();
		final Integer collectionId = c.getCollectionId();
		final Integer windowSize = c.getWindowSize();	
		
		List<ItemChange> changes = listItems(delta, credentials, device, collectionId);
		
		if (changesFitWindow(windowSize, changes)) {
			return changes;
		}
		
		logger.info("should send {} change(s)", changes.size());
		int changeItem = changes.size() - c.getWindowSize();
		logger.info("WindowsSize value is {} , {} changes will not be sent", 
				new Object[]{ c.getWindowSize(), (changeItem < 0 ? 0 : changeItem) });

		final Set<ItemChange> changeByMobile = new HashSet<ItemChange>();
		// Find changes ask by the device
		for (Iterator<ItemChange> it = changes.iterator(); it.hasNext();) {
			ItemChange ic = it.next();
			if (processedClientIds.containsKey(ic.getServerId())) {
				changeByMobile.add(ic);
				it.remove();
			}
			if (processedClientIds.size() == changeByMobile.size()) {
				break;
			}
		}

		LinkedList<ItemChange> toKeepForLaterSync = Lists.newLinkedList();
		
		int changedSize = changes.size();
		for (int i = windowSize; i < changedSize; i++) {
			ItemChange ic = changes.get(changes.size() - 1);
			toKeepForLaterSync.addFirst(ic);
			changes.remove(ic);
		}

		unSynchronizedItemCache.storeItemsToAdd(credentials, device, collectionId, toKeepForLaterSync);
		changes.addAll(changeByMobile);
		c.setMoreAvailable(true);
		
		return changes;
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
