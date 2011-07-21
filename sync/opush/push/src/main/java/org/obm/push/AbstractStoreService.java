package org.obm.push;

import net.sf.ehcache.Cache;

public abstract class AbstractStoreService {
	
	protected final ObjectStoreManager objectStoreManager;
	protected final Cache store;
	
	protected AbstractStoreService(ObjectStoreManager objectStoreManager) {
		this.objectStoreManager = objectStoreManager;
		this.store = this.objectStoreManager.getStore( getStoreName() );
	}
	
	protected abstract String getStoreName();
}
