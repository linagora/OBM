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
package org.obm.push.state;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StateMachine {

	private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);
	
	private final CollectionDao collectionDao;
	private final ItemTrackingDao itemTrackingDao;

	@Inject
	private StateMachine(CollectionDao collectionDao, ItemTrackingDao itemTrackingDao) {
		this.collectionDao = collectionDao;
		this.itemTrackingDao = itemTrackingDao;
	}

	public SyncState lastKnownState(Device device, Integer collectionId) throws DaoException {
		return collectionDao.lastKnownState(device, collectionId);
	}
	
	public SyncState getSyncState(String syncKey) throws DaoException {
		return collectionDao.findStateForKey(syncKey);
	}

	public String allocateNewSyncKey(BackendSession bs, Integer collectionId, Date lastSync, 
		Collection<ItemChange> changes, Collection<ItemChange> deletedItems) throws DaoException, InvalidServerId {
		
		String newSk = UUID.randomUUID().toString();
		SyncState newState = new SyncState(newSk, lastSync);
		int syncStateId = collectionDao.updateState(bs.getDevice(), collectionId, newState);
		newState.setId(syncStateId);
		
		if (changes != null && !changes.isEmpty()) {
			itemTrackingDao.markAsSynced(newState, listNewItems(changes));
		}
		if (deletedItems != null && !deletedItems.isEmpty()) {
			itemTrackingDao.markAsDeleted(newState, itemChangesAsServerIdSet(deletedItems));
		}
		
		log(bs, newState);
		return newSk;
	}

	private Set<ServerId> itemChangesAsServerIdSet(Iterable<ItemChange> changes) throws InvalidServerId {
		Set<ServerId> ids = Sets.newHashSet();
		for (ItemChange change: changes) {
			addServerItemId(ids, change);
		}
		return ids;
	}

	private Set<ServerId> listNewItems(Collection<ItemChange> changes) throws InvalidServerId {
		HashSet<ServerId> serverIds = Sets.newHashSet();
		for (ItemChange change: changes) {
			if (change.isNew()) {
				addServerItemId(serverIds, change);
			}
		}
		return serverIds;
	}

	private void addServerItemId(Set<ServerId> serverIds, ItemChange change) throws InvalidServerId {
		ServerId serverId = new ServerId( change.getServerId() );
		if (serverId.isItem()) {
			serverIds.add(serverId);
		}
	}
	
	private void log(BackendSession bs, SyncState newState) {
		String collectionPath = "obm:\\\\" + bs.getUser().getLoginAtDomain();
		logger.info("Allocate new synckey {} for collectionPath {} with {} last sync", 
				new Object[]{newState.getKey(), collectionPath, newState.getLastSync()});
	}
	
}
