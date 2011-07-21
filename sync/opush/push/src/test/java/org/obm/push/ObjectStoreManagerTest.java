package org.obm.push;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class ObjectStoreManagerTest {

	private ObjectStoreManager opushCacheManager;

	@Before
	public void init() {
		// by default, loading one store in objectStoreManager.xml
		this.opushCacheManager = new ObjectStoreManager();
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
