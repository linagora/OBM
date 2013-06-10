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
package org.obm.push.protocol.data;

import java.util.Set;

import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollectionRequest;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.protocol.bean.AnalysedPingRequest;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.HearbeatDao;
import org.obm.push.store.MonitoredCollectionDao;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PingAnalyser {

	protected static final long MIN_SANE_HEARTBEAT_VALUE = 5L;
	
	private final CollectionDao collectionDao;
	private final HearbeatDao hearbeatDao;
	private final MonitoredCollectionDao monitoredCollectionDao;
	private final ICollectionPathHelper collectionPathHelper;
	private final StateMachine stateMachine;

	@Inject
	protected PingAnalyser(CollectionDao collectionDao, HearbeatDao hearbeatDao, MonitoredCollectionDao monitoredCollectionDao,
			ICollectionPathHelper collectionPathHelper, StateMachine stateMachine) {
		
		this.collectionDao = collectionDao;
		this.hearbeatDao = hearbeatDao;
		this.monitoredCollectionDao = monitoredCollectionDao;
		this.collectionPathHelper = collectionPathHelper;
		this.stateMachine = stateMachine;
	}

	public AnalysedPingRequest analysePing(UserDataRequest udr, PingRequest pingRequest) 
			throws DaoException, CollectionPathException, MissingRequestParameterException {
		
		AnalysedPingRequest.Builder builder = AnalysedPingRequest.builder()
			.heartbeatInterval(checkHeartbeatInterval(udr, pingRequest))
			.syncCollections(checkSyncCollections(udr, pingRequest));
		return builder.build();
	}

	private long checkHeartbeatInterval(UserDataRequest udr, PingRequest pingRequest) 
			throws DaoException, MissingRequestParameterException {
		
		if (pingRequest.getHeartbeatInterval() == null) {
			Long heartbeatInterval = hearbeatDao.findLastHearbeat(udr.getDevice());
			if (heartbeatInterval == null) {
				throw new MissingRequestParameterException();
			}
			return heartbeatInterval;
		} else {
			long heartbeatInterval = Math.max(MIN_SANE_HEARTBEAT_VALUE, pingRequest.getHeartbeatInterval());
			hearbeatDao.updateLastHearbeat(udr.getDevice(), heartbeatInterval);
			return heartbeatInterval;
		}
	}
	
	private Set<AnalysedSyncCollection> checkSyncCollections(UserDataRequest udr, PingRequest pingRequest)
			throws MissingRequestParameterException, CollectionNotFoundException, DaoException, CollectionPathException {
		
		Set<AnalysedSyncCollection> analysedSyncCollections = Sets.newHashSet();
		Set<SyncCollectionRequest> syncCollections = pingRequest.getSyncCollections();
		if (syncCollections == null || syncCollections.isEmpty()) {
			Set<AnalysedSyncCollection> lastMonitoredCollection = monitoredCollectionDao.list(udr.getCredentials(), udr.getDevice());
			if (lastMonitoredCollection.isEmpty()) {
				throw new MissingRequestParameterException();
			}
			analysedSyncCollections.addAll(lastMonitoredCollection);
		} else {
			analysedSyncCollections.addAll(loadSyncKeys(udr, syncCollections));
		}
		return analysedSyncCollections;
	}

	private Set<AnalysedSyncCollection> loadSyncKeys(UserDataRequest udr, Set<SyncCollectionRequest> syncCollections) {
		Set<AnalysedSyncCollection> analysedSyncCollections = Sets.newHashSet();
		for (SyncCollectionRequest collection: syncCollections) {
			String collectionPath = collectionDao.getCollectionPath(collection.getCollectionId());
			
			AnalysedSyncCollection.Builder syncCollectionRequestBuilder = AnalysedSyncCollection.builderCopyOf(collection)
					.collectionPath(collectionPath)
					.dataType(collectionPathHelper.recognizePIMDataType(collectionPath));
			
			ItemSyncState lastKnownState = stateMachine.lastKnownState(udr.getDevice(), collection.getCollectionId());
			if (lastKnownState != null) {
				syncCollectionRequestBuilder.syncKey(lastKnownState.getSyncKey());
			} else {
				syncCollectionRequestBuilder.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY);
			}
			analysedSyncCollections.add(syncCollectionRequestBuilder.build());
		}
		return analysedSyncCollections;
	}
}
