package org.obm.push;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	public void add(Credentials credentials, int collectionId, ItemChange ic) {
		Key key = buildKey(credentials, collectionId);

		List<ItemChange> itemChanges = list(key);
		itemChanges.add(ic);
		store.put( new Element(key, itemChanges) );
	}

	@Override
	public List<ItemChange> list(Credentials credentials, int collectionId) {
		Key key = buildKey(credentials, collectionId);
		return list(key);
	}

	@Override
	public void clear(Credentials credentials, int collectionId) {
		Key key = buildKey(credentials, collectionId);

		List<ItemChange> itemChanges = list(key);
		itemChanges.clear();
		store.put( new Element(key, itemChanges) );
	}

	private List<ItemChange> list(Key key) {
		Element element = store.get(key);
		if (element != null) {
			return (List<ItemChange>) element.getValue();
		} else {
			return new ArrayList<ItemChange>();
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
