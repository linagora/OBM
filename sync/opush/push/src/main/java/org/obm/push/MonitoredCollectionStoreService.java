package org.obm.push;

import java.util.Set;

import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

public interface MonitoredCollectionStoreService {
	
	Set<SyncCollection> list(Credentials credentials, Device device);
	
	void put(Credentials credentials, Device device, Set<SyncCollection> collections);

}
