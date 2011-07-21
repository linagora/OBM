package org.obm.push;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.sf.ehcache.Element;

import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MonitoredCollectionStoreServiceImpl extends AbstractStoreService implements MonitoredCollectionStoreService {

	private static final String STORE_NAME = "monitoredCollectionService";
	
	@Inject  MonitoredCollectionStoreServiceImpl(
			ObjectStoreManager objectStoreManager) {
		super(objectStoreManager);
	}

	@Override
	protected String getStoreName() {
		return STORE_NAME;
	}
	
	@Override
	public Collection<SyncCollection> list(Credentials credentials, Device device) {
		Key key = buildKey(credentials, device);
		Element element = store.get(key);
		if (element != null) {
			return (List<SyncCollection>) element.getValue();
		} else {
			return Lists.newArrayList();
		}
	}

	@Override
	public void put(Credentials credentials, Device device,
			Collection<SyncCollection> collections) {
		Key key = buildKey(credentials, device);
		remove(key);
		add(key, collections);
	}
	
	private void add(Key key, Collection<SyncCollection> collections) {
		store.put( new Element(key, collections) );
	}
	
	private void remove(Key key) {
		store.remove(key);
	}

	private Key buildKey(Credentials credentials, Device device) {
		return new Key(credentials, device);
	}

	private class Key implements Serializable {

		private final Credentials credentials;
		private final Device device;

		public Key(Credentials credentials, Device device) {
			super();
			this.credentials = credentials;
			this.device = device;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
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

		private MonitoredCollectionStoreServiceImpl getOuterType() {
			return MonitoredCollectionStoreServiceImpl.this;
		}

		
		
	}
}
