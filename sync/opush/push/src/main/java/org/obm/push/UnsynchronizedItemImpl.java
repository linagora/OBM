package org.obm.push;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.obm.push.impl.Credentials;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UnsynchronizedItemImpl implements UnsynchronizedItemService {

	private final ObjectStoreManager objectStoreManager;
	private final Cache store;
	
	@Inject UnsynchronizedItemImpl(ObjectStoreManager objectStoreManager) {
		this.objectStoreManager = objectStoreManager;
		this.store = this.objectStoreManager.getStore( getStoreName() );
	}

	String getStoreName() {
		return "unsynchronizedItemService";
	}

	@Override
	public void storeItemToAdd(Credentials credentials, int collectionId, ItemChange ic) {
		Key key = buildKey(credentials, collectionId, UnsynchronizedItemType.ADD);
		storeItem(ic, key);
	}

	@Override
	public Set<ItemChange> listItemToAdd(Credentials credentials, int collectionId) {
		Key key = buildKey(credentials, collectionId, UnsynchronizedItemType.ADD);
		return listItem(key);
	}

	@Override
	public void clearItemToAdd(Credentials credentials, int collectionId) {
		Key key = buildKey(credentials, collectionId, UnsynchronizedItemType.ADD);
		clearItem(key);
	}

	@Override
	public void storeItemToRemove(Credentials credentials, int collectionId, ItemChange ic) {
		Key key = buildKey(credentials, collectionId, UnsynchronizedItemType.DELETE);
		storeItem(ic, key);
	}

	@Override
	public Set<ItemChange> listItemToRemove(Credentials credentials, int collectionId) {
		Key key = buildKey(credentials, collectionId, UnsynchronizedItemType.DELETE);
		return listItem(key);
	}

	@Override
	public void clearItemToRemove(Credentials credentials, int collectionId) {
		Key key = buildKey(credentials, collectionId, UnsynchronizedItemType.DELETE);
		clearItem(key);
	}

	private void storeItem(ItemChange ic, Key key) {
		Set<ItemChange> itemChanges = listItem(key);
		itemChanges.add(ic);
		store.put( new Element(key, itemChanges) );
	}
	
	private void clearItem(Key key) {
		Set<ItemChange> itemChanges = listItem(key);
		itemChanges.clear();
		store.put( new Element(key, itemChanges) );
	}
	
	private Set<ItemChange> listItem(Key key) {
		Element element = store.get(key);
		if (element != null) {
			return (Set<ItemChange>) element.getValue();
		} else {
			return new HashSet<ItemChange>();
		}
	}

	private Key buildKey(Credentials credentials, Integer collectionId, 
			UnsynchronizedItemType unsynchronizedItemType) {
		
		return new Key(credentials, collectionId, unsynchronizedItemType);
	}

	private class Key implements Serializable {

		private final Credentials credentials;
		private final int collectionId;
		private final UnsynchronizedItemType unsynchronizedItemType;
		
		public Key(Credentials credentials, int collectionId, UnsynchronizedItemType unsynchronizedItemType) {
			super();
			this.credentials = credentials;
			this.collectionId = collectionId;
			this.unsynchronizedItemType = unsynchronizedItemType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + collectionId;
			result = prime * result
					+ ((credentials == null) ? 0 : credentials.hashCode());
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
			if (collectionId != other.collectionId)
				return false;
			if (credentials == null) {
				if (other.credentials != null)
					return false;
			} else if (!credentials.equals(other.credentials))
				return false;
			if (unsynchronizedItemType != other.unsynchronizedItemType)
				return false;
			return true;
		}		
	}

}
