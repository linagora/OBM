package org.obm.push.store;

import java.util.Collection;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.SyncCollection;

public interface SyncedCollectionDao {
	
	SyncCollection get(Credentials credentials, Device device, Integer collectionId);
	
	void put(Credentials credentials, Device device, Collection<SyncCollection> collections);

}
