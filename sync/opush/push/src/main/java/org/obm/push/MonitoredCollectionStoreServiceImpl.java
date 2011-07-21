package org.obm.push;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MonitoredCollectionStoreServiceImpl implements MonitoredCollectionStoreService {

	private static final String STORE_NAME = "monitoredCollectionService";
	private final ObjectStoreManager objectStoreManager;
	private final Cache store;
	
	@Inject MonitoredCollectionStoreServiceImpl(ObjectStoreManager objectStoreManager) {
		this.objectStoreManager = objectStoreManager;
		this.store = this.objectStoreManager.getStore( STORE_NAME );
	}

	@Override
	public Collection<SyncCollection> list(Credentials credentials) {
		Key key = buildKey(credentials);
		Element element = store.get(key);
		if (element != null) {
			return (List<SyncCollection>) element.getValue();
		} else {
			return Lists.newArrayList();
		}
	}

	@Override
	public void put(Credentials credentials,
			Collection<SyncCollection> collections) {
		remove(credentials);
		add(credentials, collections);
	}
	
	private void add(Credentials credentials,
			Collection<SyncCollection> collections) {
		Key key = buildKey(credentials);
		store.put( new Element(key, collections) );
	}
	
	private void remove(Credentials credentials) {
		Key key = buildKey(credentials);
		store.remove(key);
	}

	private Key buildKey(Credentials credentials) {
		return new Key(credentials);
	}

	private class Key implements Serializable {

		private final Credentials credentials;

		public Key(Credentials credentials) {
			super();
			this.credentials = credentials;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
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
			if (credentials == null) {
				if (other.credentials != null)
					return false;
			} else if (!credentials.equals(other.credentials))
				return false;
			return true;
		}
	}
}
