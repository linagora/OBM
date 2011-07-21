package org.obm.push;

import java.io.Serializable;
import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncedCollectionStoreServiceImpl implements SyncedCollectionStoreService {

	private static final String STORE_NAME = "syncedCollectionStoreService";
	private final ObjectStoreManager objectStoreManager;
	private final Cache store;
	
	@Inject SyncedCollectionStoreServiceImpl(ObjectStoreManager objectStoreManager) {
		this.objectStoreManager = objectStoreManager;
		this.store = this.objectStoreManager.getStore( STORE_NAME );
	}

	@Override
	public void put(Credentials credentials,
			Collection<SyncCollection> collections) {
		for(SyncCollection collection : collections){
			put(credentials, collection);
		}
	}

	private void put(Credentials credentials, SyncCollection collection) {
		Key key = buildKey(credentials, collection.getCollectionId());
		store.put( new Element(key, collection) );
	}
	
	@Override
	public SyncCollection get(Credentials credentials, Integer collectionId) {
		Key key = buildKey(credentials, collectionId);
		Element element = store.get(key);
		if (element != null) {
			return (SyncCollection) element.getValue();
		} else {
			return null;
		}
	}
	
	private Key buildKey(Credentials credentials, Integer collectionId) {
		return new Key(credentials, collectionId);
	}

	private class Key implements Serializable {

		private final Credentials credentials;
		private final int collectionId;

		public Key(Credentials credentials, int collectionId) {
			super();
			this.credentials = credentials;
			this.collectionId = collectionId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + collectionId;
			result = prime * result
					+ ((credentials == null) ? 0 : credentials.hashCode());
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
			return true;
		}
	}
}
