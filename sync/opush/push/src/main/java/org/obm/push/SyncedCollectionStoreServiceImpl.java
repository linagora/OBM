package org.obm.push;

import java.io.Serializable;
import java.util.Collection;

import net.sf.ehcache.Element;

import org.obm.push.bean.SyncCollection;
import org.obm.push.impl.Credentials;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncedCollectionStoreServiceImpl extends AbstractStoreService implements SyncedCollectionStoreService {

	private static final String STORE_NAME = "syncedCollectionStoreService";
	
	@Inject  SyncedCollectionStoreServiceImpl(
			ObjectStoreManager objectStoreManager) {
		super(objectStoreManager);
	}

	@Override
	protected String getStoreName() {
		return STORE_NAME;
	}

	@Override
	public void put(Credentials credentials, Device device,
			Collection<SyncCollection> collections) {
		for(SyncCollection collection : collections){
			put(credentials, device, collection);
		}
	}

	private void put(Credentials credentials, Device device, SyncCollection collection) {
		Key key = buildKey(credentials, device, collection.getCollectionId());
		store.put( new Element(key, collection) );
	}
	
	@Override
	public SyncCollection get(Credentials credentials, Device device, Integer collectionId) {
		Key key = buildKey(credentials, device, collectionId);
		Element element = store.get(key);
		if (element != null) {
			return (SyncCollection) element.getValue();
		} else {
			return null;
		}
	}
	
	private Key buildKey(Credentials credentials, Device device, Integer collectionId) {
		return new Key(credentials, device, collectionId);
	}

	private class Key implements Serializable {

		private final Credentials credentials;
		private final int collectionId;
		private final Device device;

		public Key(Credentials credentials, Device device, int collectionId) {
			super();
			this.credentials = credentials;
			this.device = device;
			this.collectionId = collectionId;
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
			return true;
		}

		private SyncedCollectionStoreServiceImpl getOuterType() {
			return SyncedCollectionStoreServiceImpl.this;
		}
	}
}
