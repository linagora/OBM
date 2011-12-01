package org.obm.push.store.ehcache;

import junit.framework.Assert;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.transaction.TransactionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.annotations.transactional.Transactional;
import org.obm.annotations.transactional.TransactionalModule;
import org.obm.push.exception.EhcacheRollbackException;

import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class EhcacheTransactionalModeTest {

	public static class Module extends AbstractModule {
		@Override
		protected void configure() {
			install(new TransactionalModule());
			install(new GuiceBerryModule());
		}
	}
	
	@Rule public final GuiceBerryRule guiceBerry =
			new GuiceBerryRule(Module.class);

	@Inject private TestClass xaCacheInstance;
	
	public static class TestClass {

		@Transactional
		public void put(Cache xaCache, Element element) {
			xaCache.put(element);
		}
		
		@Transactional
		public void putAndthrowException(Cache xaCache, Element element) throws EhcacheRollbackException {
			put(xaCache, element);
			throw new EhcacheRollbackException();
		}
		
		@Transactional
		public int getSizeElement(CacheManager manager, String cache) {
			return getSizeElementWithoutTransactional(manager, cache);
		}
		
		public int getSizeElementWithoutTransactional(CacheManager manager, String cache) {
			return manager.getCache(cache).getSize();
		}
		
	}
	
	private CacheManager manager;
	private Cache xaCache;
	private final static String XA_CACHE_NAME = "TEST";

	@Before
	public void init() {
		this.manager = CacheManager.create();
	    this.xaCache = new Cache(
	            new CacheConfiguration(XA_CACHE_NAME, 1000)
	                .transactionalMode(CacheConfiguration.TransactionalMode.XA));
	    manager.addCache(xaCache);
	}
	
	@After
	public void removeCache() {
		manager.removalAll();
	}
	
	@Test
	public void callEhcacheMethod() {
		xaCacheInstance.put(xaCache, buildElement() );
		Assert.assertEquals(1, xaCacheInstance.getSizeElement(manager, XA_CACHE_NAME));
	}
	
	@Test
	public void callEhcacheMethodAndThrowException() {
		try {	
			xaCacheInstance.putAndthrowException(xaCache, buildElement() );
			Assert.assertTrue(false);
		} catch (EhcacheRollbackException e) {
			// ehcache rollback
		}
		Assert.assertEquals(0, xaCacheInstance.getSizeElement(manager, XA_CACHE_NAME));
	}
	
	@Test(expected=TransactionException.class)
	public void callEhcacheMethodWithoutTransactional() {
		xaCacheInstance.put(xaCache, buildElement() );	
		xaCacheInstance.getSizeElementWithoutTransactional(manager, XA_CACHE_NAME);
	}
	
	private Element buildElement() {
		return new Element("key", "value");
	}
	
}
