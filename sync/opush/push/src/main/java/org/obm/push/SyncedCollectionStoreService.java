package org.obm.push;

import java.util.Collection;

import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

public interface SyncedCollectionStoreService {
	
	SyncCollection get(Credentials credentials, Device device, Integer collectionId);
	
	void put(Credentials credentials, Device device, Collection<SyncCollection> collections);

}
