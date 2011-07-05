package org.obm.push.store;

import org.obm.push.store.IStorageFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StorageFactory implements IStorageFactory {

	private final SyncStorage syncStorage;

	@Inject
	private StorageFactory(SyncStorage syncStorage) {
		this.syncStorage = syncStorage;
	}
	
	@Override
	public ISyncStorage createStorage() {
		return syncStorage;
	}

}
