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
package org.obm.push.mail;

import java.util.List;

import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;
import org.obm.push.mail.bean.Snapshot;
import org.obm.push.store.SnapshotDao;
import org.obm.push.store.SyncKeysDao;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SnapshotServiceImpl implements SnapshotService {

	private final SnapshotDao snapshotDao;
	private final SyncKeysDao syncKeysDao;

	@Inject
	@VisibleForTesting SnapshotServiceImpl(SnapshotDao snapshotDao, SyncKeysDao syncKeysDao) {
		this.snapshotDao = snapshotDao;
		this.syncKeysDao = syncKeysDao;
	}

	@Override
	public Snapshot getSnapshot(DeviceId deviceId, SyncKey syncKey, Integer collectionId) {
		return snapshotDao.get(deviceId, syncKey, collectionId);
	}

	@Override
	public void storeSnapshot(Snapshot snapshot) {
		syncKeysDao.put(snapshot.getDeviceId(), snapshot.getCollectionId(), snapshot.getSyncKey());
		snapshotDao.put(snapshot);
	}

	@Override
	public void actualizeSnapshot(DeviceId deviceId, SyncKey syncKey, Integer collectionId, SyncKey newSyncKey) {
		Snapshot snapshot = getSnapshot(deviceId, syncKey, collectionId);
		storeSnapshot(Snapshot.builder()
				.actualizeSnapshot(snapshot, newSyncKey));
	}
	
	@Override
	public void deleteSnapshotAndSyncKeys(DeviceId deviceId, int collectionId) {
		List<SyncKey> syncKeys = syncKeysDao.get(deviceId, collectionId);
		if (syncKeys != null) {
			delete(deviceId, collectionId, syncKeys);
		}
	}

	private void delete(DeviceId deviceId, int collectionId, List<SyncKey> syncKeys) {
		for (SyncKey syncKey : syncKeys) {
			snapshotDao.delete(deviceId, syncKey, collectionId);
		}
		syncKeysDao.delete(deviceId, collectionId);
	}
}
