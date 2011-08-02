package org.obm.push.store.ehcache;

import net.sf.ehcache.Cache;

public abstract class AbstractEhcacheDao {
	
	protected final ObjectStoreManager objectStoreManager;
	protected final Cache store;
	
	protected AbstractEhcacheDao(ObjectStoreManager objectStoreManager) {
		this.objectStoreManager = objectStoreManager;
		this.store = this.objectStoreManager.getStore( getStoreName() );
	}
	
	protected abstract String getStoreName();
}
