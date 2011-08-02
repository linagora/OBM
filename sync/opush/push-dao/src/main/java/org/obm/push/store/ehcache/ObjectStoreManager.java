package org.obm.push.store.ehcache;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;

import org.obm.configuration.ConfigurationService;
import org.obm.configuration.store.StoreNotFoundException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObjectStoreManager {

	private final static int MAX_ELEMENT_IN_MEMORY = 5000;
	private final CacheManager singletonManager;

	@Inject ObjectStoreManager(ConfigurationService configurationService) throws StoreNotFoundException {
		InputStream storeConfiguration = configurationService.getStoreConfiguration();
		this.singletonManager = new CacheManager(storeConfiguration);
	}

	public void createNewStore(String storeName) {
		if (getStore(storeName) == null) {
			this.singletonManager.addCache(createStore(storeName));
		}
	}

	private Cache createStore(String storeName) {
		return new Cache(createStoreConfiguration(storeName));
	}

	private CacheConfiguration createStoreConfiguration(String storeName) {
		return new CacheConfiguration(storeName, MAX_ELEMENT_IN_MEMORY);
	}

	public Cache getStore(String storeName) {
		return this.singletonManager.getCache(storeName);
	}

	public void removeStore(String storeName) {
		this.singletonManager.removeCache(storeName);
	}

	public List<String> listStores() {
		return Arrays.asList(this.singletonManager.getCacheNames());
	}

}
