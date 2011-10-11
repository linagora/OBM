package org.obm.push.store.ehcache;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Element;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.store.UnsynchronizedItemDao;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UnsynchronizedItemDaoEhcacheImpl extends AbstractEhcacheDao implements UnsynchronizedItemDao {

	private static final String STORE_NAME = "unsynchronizedItemService";
	
	@Inject UnsynchronizedItemDaoEhcacheImpl(ObjectStoreManager objectStoreManager) {
		super(objectStoreManager);
	}
	
	@Override
	protected String getStoreName() {
		return STORE_NAME;
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
	public void storeItemsToRemove(Credentials credentials, Device device, int collectionId, Collection<ItemChange> ic) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.DELETE);
		storeItems(ic, key);
	}

	@Override
	public void storeItemsToAdd(Credentials credentials, Device device, int collectionId, Collection<ItemChange> ic) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.ADD);
		storeItems(ic, key);
	}
	
	@Override
	public Set<ItemChange> listItemsToRemove(Credentials credentials, Device device, int collectionId) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.DELETE);
		return listItem(key);
	}

	@Override
	public void clearItemsToRemove(Credentials credentials, Device device, int collectionId) {
		Key key = buildKey(credentials, device, collectionId, UnsynchronizedItemType.DELETE);
		clearItems(key);
	}

	private void storeItems(Collection<ItemChange> ic, Key key) {
		HashSet<ItemChange> itemChanges = Sets.newHashSet(ic);
		store.put( new Element(key, itemChanges) );
	}
	
	private void clearItems(Key key) {
		store.put( new Element(key, Sets.newHashSet()) );
	}
	
	private Set<ItemChange> listItem(Key key) {
		Element element = store.get(key);
		if (element != null) {
			return (Set<ItemChange>) element.getValue();
		} else {
			return new HashSet<ItemChange>();
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
