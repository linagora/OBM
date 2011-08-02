package org.obm.push;

import java.util.Collection;

import org.obm.push.bean.SyncCollection;
import org.obm.push.impl.Credentials;

public interface SyncedCollectionStoreService {
	
	SyncCollection get(Credentials credentials, Device device, Integer collectionId);
	
	void put(Credentials credentials, Device device, Collection<SyncCollection> collections);

}
