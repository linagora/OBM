package org.obm.push;

import java.util.Set;

import org.obm.push.bean.SyncCollection;
import org.obm.push.impl.Credentials;

public interface MonitoredCollectionStoreService {
	
	Set<SyncCollection> list(Credentials credentials, Device device);
	
	void put(Credentials credentials, Device device, Set<SyncCollection> collections);

}
