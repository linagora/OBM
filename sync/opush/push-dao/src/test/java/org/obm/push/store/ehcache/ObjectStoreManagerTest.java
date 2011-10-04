package org.obm.push.store.ehcache;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.store.StoreNotFoundException;
import org.obm.push.store.ehcache.ObjectStoreManager;
import org.obm.push.store.ehcache.StoreManagerConfigurationTest;

public class ObjectStoreManagerTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager opushCacheManager;

	public ObjectStoreManagerTest() {
		super();
	}
	
	@Before
	public void init() throws StoreNotFoundException {
		// by default, loading one store in objectStoreManager.xml
		this.opushCacheManager = new ObjectStoreManager( super.initConfigurationServiceMock() );
	}

	@Test
	public void loadStores() {
		List<String> stores = opushCacheManager.listStores();
		Assert.assertNotNull(stores);
		Assert.assertEquals(3, stores.size());
	}
	
	@Test
	public void createNewThreeCachesAndRemoveOne() {
		opushCacheManager.createNewStore("test 1");
		opushCacheManager.createNewStore("test 2");
		opushCacheManager.createNewStore("test 3");
		
		opushCacheManager.removeStore("test 2");
		
		Assert.assertNotNull(opushCacheManager.getStore("test 1"));
		Assert.assertNotNull(opushCacheManager.getStore("test 3"));

		Assert.assertNull(opushCacheManager.getStore("test 2"));
		
		Assert.assertEquals(5, opushCacheManager.listStores().size());
	}
	
	@Test
	public void createAndRemoveCache() {
		opushCacheManager.createNewStore("test 1");
		opushCacheManager.removeStore("test 1");
		
		Assert.assertNull(opushCacheManager.getStore("test 1"));
	}

	@Test
	public void createTwoIdenticalCache() {
		opushCacheManager.createNewStore("test 1");
		opushCacheManager.createNewStore("test 1");
		Assert.assertNotNull(opushCacheManager.getStore("test 1"));

		Assert.assertEquals(4, opushCacheManager.listStores().size());
	}

}
