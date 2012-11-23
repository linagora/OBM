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
package org.obm.push.store.ehcache;

import java.util.List;

import net.sf.ehcache.Element;

import org.obm.push.bean.DeviceId;
import org.obm.push.bean.Snapshot;
import org.obm.push.bean.SnapshotKey;
import org.obm.push.store.SnapshotDao;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SnapshotDaoEhcacheImpl extends AbstractEhcacheDao implements SnapshotDao {

	@Inject  SnapshotDaoEhcacheImpl(ObjectStoreManager objectStoreManager) {
		super(objectStoreManager);
	}
	
	@Override
	protected String getStoreName() {
		return ObjectStoreManager.MAIL_SNAPSHOT_STORE;
	}

	@Override
	public Snapshot get(DeviceId deviceId, String syncKey, Integer collectionId) {
		SnapshotKey key = SnapshotKey.builder()
			.deviceId(deviceId)
			.syncKey(syncKey)
			.collectionId(collectionId)
			.build();
		Element element = store.get(key);
		if (element != null) {
			return (Snapshot) element.getValue();
		}
		return null;
	}

	@Override
	public void put(Snapshot snapshot) {
		SnapshotKey key = SnapshotKey.builder()
				.deviceId(snapshot.getDeviceId())
				.syncKey(snapshot.getSyncKey())
				.collectionId(snapshot.getCollectionId())
				.build();
		store.put(new Element(key, snapshot));
	}

	private class SnapshotHasDeviceIdPredicate implements Predicate<SnapshotKey> {

		private final DeviceId deviceId;
		
		private SnapshotHasDeviceIdPredicate(DeviceId deviceId) {
			this.deviceId = deviceId;
		}
		
		@Override
		public boolean apply(SnapshotKey input) {
			return Objects.equal(deviceId, input.getDeviceId()); 
		}
	}
	
	@Override
	public void deleteAll(DeviceId deviceId) {
		List<SnapshotKey> keys = store.getKeys();
		Iterable<SnapshotKey> toRemove = Iterables.filter(keys, new SnapshotHasDeviceIdPredicate(deviceId));
		for (SnapshotKey snapshotKey : toRemove) {
			store.remove(snapshotKey);
		}
	}
	
}
