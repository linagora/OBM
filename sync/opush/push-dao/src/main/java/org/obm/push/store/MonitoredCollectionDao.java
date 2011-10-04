package org.obm.push.store;

import java.util.Set;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.SyncCollection;

public interface MonitoredCollectionDao {
	
	Set<SyncCollection> list(Credentials credentials, Device device);
	
	void put(Credentials credentials, Device device, Set<SyncCollection> collections);

}
