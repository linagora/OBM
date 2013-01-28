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
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.store.UnsynchronizedItemDao;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UnsynchronizedItemDaoEhcacheImpl extends AbstractEhcacheDao implements UnsynchronizedItemDao {

	@Inject UnsynchronizedItemDaoEhcacheImpl(ObjectStoreManager objectStoreManager) {
		super(objectStoreManager);
	}
	
	@Override
	protected String getStoreName() {
		return ObjectStoreManager.UNSYNCHRONIZED_ITEM_STORE;
	}

	@Override
	public Set<ItemChange> listItemsToAdd(Credentials credentials, Device device, int collectionId) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.ADD);
		return listItem(key);
	}

	@Override
	public void clearItemsToAdd(Credentials credentials, Device device, int collectionId) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.ADD);
		clearItems(key);
	}

	@Override
	public void storeItemsToRemove(Credentials credentials, Device device, int collectionId, Collection<ItemDeletion> ic) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.DELETE);
		storeItems(ic, key);
	}

	@Override
	public void storeItemsToAdd(Credentials credentials, Device device, int collectionId, Collection<ItemChange> ic) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.ADD);
		storeItems(ic, key);
	}
	
	@Override
	public Set<ItemDeletion> listItemsToRemove(Credentials credentials, Device device, int collectionId) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.DELETE);
		return listItem(key);
	}

	@Override
	public void clearItemsToRemove(Credentials credentials, Device device, int collectionId) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.DELETE);
		clearItems(key);
	}

	@Override
	public boolean hasAnyItemsFor(Credentials credentials, Device device, int collectionId) {
		return !listItemsToAdd(credentials, device, collectionId).isEmpty()
			|| !listItemsToRemove(credentials, device, collectionId).isEmpty();
	}

	private <T> void storeItems(Collection<T> ic, Key key) {
		HashSet<T> itemChanges = Sets.newHashSet(ic);
		store.put( new Element(key, itemChanges) );
	}
	
	private void clearItems(Key key) {
		store.put( new Element(key, Sets.newHashSet()) );
	}
	
	private <T> Set<T> listItem(Key key) {
		Element element = store.get(key);
		if (element != null) {
			return (Set<T>) element.getValue();
		} else {
			return new HashSet<T>();
		}
	}

	private Key buildKey(Credentials credentials, Device device, Integer collectionId, 
			UnsynchronizedItemType unsynchronizedItemType) {
		
		return new Key(credentials, device, collectionId, unsynchronizedItemType);
	}

	private class Key implements Serializable {

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
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + collectionId;
			result = prime * result
					+ ((credentials == null) ? 0 : credentials.hashCode());
			result = prime * result
					+ ((device == null) ? 0 : device.hashCode());
			result = prime
					* result
					+ ((unsynchronizedItemType == null) ? 0
							: unsynchronizedItemType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (collectionId != other.collectionId)
				return false;
			if (credentials == null) {
				if (other.credentials != null)
					return false;
			} else if (!credentials.equals(other.credentials))
				return false;
			if (device == null) {
				if (other.device != null)
					return false;
			} else if (!device.equals(other.device))
				return false;
			if (unsynchronizedItemType != other.unsynchronizedItemType)
				return false;
			return true;
		}
		
		private UnsynchronizedItemDaoEhcacheImpl getOuterType() {
			return UnsynchronizedItemDaoEhcacheImpl.this;
		}
	}

}
