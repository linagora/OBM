package org.obm.push;

import java.util.Collection;

import org.obm.push.impl.Credentials;
import org.obm.push.store.SyncCollection;

public interface SyncedCollectionStoreService {
	
	SyncCollection get(Credentials credentials, Integer collectionId);
	
	void put(Credentials credentials, Collection<SyncCollection> collections);

}
