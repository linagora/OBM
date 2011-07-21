package org.obm.push;

import java.util.Collection;

import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

public interface MonitoredCollectionStoreService {
	
	Collection<SyncCollection> list(Credentials credentials);
	
	void put(Credentials credentials, Collection<SyncCollection> collections);

}
