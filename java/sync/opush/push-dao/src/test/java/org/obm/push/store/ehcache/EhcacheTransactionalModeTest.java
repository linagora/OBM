/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.store.ehcache;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.transaction.TransactionException;
import net.sf.ehcache.transaction.TransactionTimeoutException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.annotations.transactional.Transactional;
import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.TestConfigurationModule;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.exception.EhcacheRollbackException;

import bitronix.tm.TransactionManagerServices;

import com.google.common.collect.ImmutableList;
import com.google.guiceberry.GuiceBerryModule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class) @Slow
public class EhcacheTransactionalModeTest {

	public static class Module extends AbstractModule {
		@Override
		protected void configure() {
			install(new TransactionalModule());
			install(new GuiceBerryModule());
			install(new TestConfigurationModule());
		}
	}
	
	@Rule public final JUnitGuiceRule guiceBerry = new JUnitGuiceRule(Module.class);

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
		
		@Transactional
		public void putAndWait(Cache xaCache, Element element, int timeInMilliseconds) throws InterruptedException {
			xaCache.put(element);
			Thread.sleep(timeInMilliseconds);
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
		manager.shutdown();
		TransactionManagerServices.getTransactionManager().shutdown();
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
	
	@Test
	public void transactionIsShorterThanCacheTimeout() throws InterruptedException, ExecutionException {
		manager.getTransactionController().setDefaultTransactionTimeout(1);
		List<Future<Boolean>> futures = executeTwoPutInParallel(100);
		assertFuturesSuccess(futures);
	}

	private void assertFuturesSuccess(List<Future<Boolean>> futures)
			throws InterruptedException, ExecutionException {
		for (Future<Boolean> future: futures) {
			Boolean result = future.get();
			Assert.assertTrue(result);
		}
	}
	
	@Test(expected=TransactionTimeoutException.class)
	public void transactionIsGreaterThanCacheTimeout() throws Throwable {
		manager.getTransactionController().setDefaultTransactionTimeout(1);
		try {
			List<Future<Boolean>> futures = executeTwoPutInParallel(1100);
			
			for (Future<Boolean> future: futures) {
				future.get();
			}
			
		} catch (ExecutionException e) {
			Throwable cause = e.getCause();
			throw cause;
		}
		
	}
	
	private List<Future<Boolean>> executeTwoPutInParallel(int timeInMilliseconds) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		List<Future<Boolean>> futures = executor.invokeAll(
				ImmutableList.of(
						putInCacheTask(timeInMilliseconds), 
						putInCacheTask(timeInMilliseconds)));
		return futures;
	}
	
	private Callable<Boolean> putInCacheTask(final int timeInMilliseconds) {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				xaCacheInstance.putAndWait(xaCache, buildElement(), timeInMilliseconds);
				return true;
			}
		};
	}
	
}
