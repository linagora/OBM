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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Element;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.store.UnsynchronizedItemDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UnsynchronizedItemDaoEhcacheImpl extends AbstractEhcacheDao implements UnsynchronizedItemDao {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject UnsynchronizedItemDaoEhcacheImpl(ObjectStoreManager objectStoreManager) {
		super(objectStoreManager);
	}
	
	@Override
	protected String getStoreName() {
		return ObjectStoreManager.UNSYNCHRONIZED_ITEM_STORE;
	}

	@Override
	public Set<ItemChange> listItemsToAdd(SyncKey syncKey) {
		Key_2_4_2_4 key = buildKey(syncKey, UnsynchronizedItemType.ADD);
		return listItem(key);
	}

	@Override
	public void clearItemsToAdd(SyncKey syncKey) {
		Key_2_4_2_4 key = buildKey(syncKey, UnsynchronizedItemType.ADD);
		clearItems(key);
	}

	@Override
	public void storeItemsToRemove(SyncKey syncKey, Collection<ItemDeletion> ic) {
		Key_2_4_2_4 key = buildKey(syncKey, UnsynchronizedItemType.DELETE);
		storeItems(ic, key);
	}

	@Override
	public void storeItemsToAdd(SyncKey syncKey, Collection<ItemChange> ic) {
		Key_2_4_2_4 key = buildKey(syncKey, UnsynchronizedItemType.ADD);
		storeItems(ic, key);
	}
	
	@Override
	public Set<ItemDeletion> listItemsToRemove(SyncKey syncKey) {
		Key_2_4_2_4 key = buildKey(syncKey, UnsynchronizedItemType.DELETE);
		return listItem(key);
	}

	@Override
	public void clearItemsToRemove(SyncKey syncKey) {
		Key_2_4_2_4 key = buildKey(syncKey, UnsynchronizedItemType.DELETE);
		clearItems(key);
	}

	@Override
	public boolean hasAnyItemsFor(SyncKey syncKey) {
		return !listItemsToAdd(syncKey).isEmpty()
			|| !listItemsToRemove(syncKey).isEmpty();
	}

	private <T> void storeItems(Collection<T> ic, Key_2_4_2_4 key) {
		HashSet<T> itemChanges = Sets.newHashSet(ic);
		store.put( new Element(key, itemChanges) );
		logger.debug("Put Key : {} Ic : {}", key, itemChanges);
	}
	
	private void clearItems(Key_2_4_2_4 key) {
		store.put( new Element(key, Sets.newHashSet()) );
		logger.debug("Clear Key : {}", key);
	}
	
	private <T> Set<T> listItem(Key_2_4_2_4 key) {
		Element element = store.get(key);
		logger.debug("List Key : {} Element : {}", key, element);
		if (element != null) {
			return (Set<T>) element.getValue();
		} else {
			return new HashSet<T>();
		}
	}

	private Key_2_4_2_4 buildKey(SyncKey syncKey, UnsynchronizedItemType unsynchronizedItemType) {
		
		return new Key_2_4_2_4(syncKey, unsynchronizedItemType);
	}

	@Deprecated
	public static Key key(Credentials credentials, Device device, int collectionId, UnsynchronizedItemType unsynchronizedItemType) {
		return new Key(credentials, device, collectionId, unsynchronizedItemType);
	}
	
	@Deprecated
	@VisibleForTesting static class Key implements Serializable {

		private static final long serialVersionUID = 3512553571924589754L;
		
		private final Credentials credentials;
		private final int collectionId;
		private final UnsynchronizedItemType unsynchronizedItemType;
		private final Device device;
		
		public Key(Credentials credentials, Device device, int collectionId, UnsynchronizedItemType unsynchronizedItemType) {
			super();
			this.credentials = credentials;
			this.device = device;
			this.collectionId = collectionId;
			this.unsynchronizedItemType = unsynchronizedItemType;
		}
		
		private Object readResolve() {
			return new Key_2_4_2_4(SyncKey.INITIAL_FOLDER_SYNC_KEY, unsynchronizedItemType);
		}
		
		@Override
		public int hashCode(){
			return Objects.hashCode(credentials, collectionId, unsynchronizedItemType, device);
		}
		
		@Override
		public boolean equals(Object object){
			if (object instanceof Key) {
				Key that = (Key) object;
				return Objects.equal(this.credentials, that.credentials)
					&& Objects.equal(this.collectionId, that.collectionId)
					&& Objects.equal(this.unsynchronizedItemType, that.unsynchronizedItemType)
					&& Objects.equal(this.device, that.device);
			}
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
				.add("credentials", credentials)
				.add("collectionId", collectionId)
				.add("unsynchronizedItemType", unsynchronizedItemType)
				.add("device", device)
				.toString();
		}
		
	}

	public static Key_2_4_2_4 key(SyncKey syncKey, UnsynchronizedItemType unsynchronizedItemType) {
		return new Key_2_4_2_4(syncKey, unsynchronizedItemType);
	}
	
	@VisibleForTesting static class Key_2_4_2_4 implements Serializable {

		private static final long serialVersionUID = 7445154857275520575L;
		
		private final SyncKey syncKey;
		private final UnsynchronizedItemType unsynchronizedItemType;
		
		public Key_2_4_2_4(SyncKey syncKey, UnsynchronizedItemType unsynchronizedItemType) {
			super();
			this.syncKey = syncKey;
			this.unsynchronizedItemType = unsynchronizedItemType;
		}
		
		@Override
		public int hashCode(){
			return Objects.hashCode(syncKey, unsynchronizedItemType);
		}
		
		@Override
		public boolean equals(Object object){
			if (object instanceof Key_2_4_2_4) {
				Key_2_4_2_4 that = (Key_2_4_2_4) object;
				return Objects.equal(this.syncKey, that.syncKey)
					&& Objects.equal(this.unsynchronizedItemType, that.unsynchronizedItemType);
			}
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
				.add("syncKey", syncKey)
				.add("unsynchronizedItemType", unsynchronizedItemType)
				.toString();
		}
		
	}
}
