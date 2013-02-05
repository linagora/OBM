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

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.slf4j.Logger;

import bitronix.tm.TransactionManagerServices;

@RunWith(SlowFilterRunner.class) @Slow
public class ObjectStoreManagerTest extends StoreManagerConfigurationTest {

	private ObjectStoreManager opushCacheManager;
	private Logger logger;

	public ObjectStoreManagerTest() {
		super();
	}
	
	@Before
	public void init() {
		logger = EasyMock.createNiceMock(Logger.class);
		this.opushCacheManager = new ObjectStoreManager(super.initConfigurationServiceMock(), logger);
	}

	@After
	public void shutdown() {
		opushCacheManager.shutdown();
		TransactionManagerServices.getTransactionManager().shutdown();
	}

	@Test
	public void loadStores() {
		List<String> stores = opushCacheManager.listStores();
		Assert.assertNotNull(stores);
		Assert.assertEquals(8, stores.size());
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
		
		Assert.assertEquals(10, opushCacheManager.listStores().size());
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

		Assert.assertEquals(9, opushCacheManager.listStores().size());
	}

}
