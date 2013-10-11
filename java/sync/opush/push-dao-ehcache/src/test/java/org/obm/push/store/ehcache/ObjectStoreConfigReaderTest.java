/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.obm.annotations.transactional.TransactionProvider;
import org.obm.configuration.ConfigurationService;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.slf4j.Logger;

import bitronix.tm.BitronixTransactionManager;
import bitronix.tm.TransactionManagerServices;

@Slow
@RunWith(SlowFilterRunner.class)
public class ObjectStoreConfigReaderTest {

	@Rule public TemporaryFolder tempFolder =  new TemporaryFolder();
	
	private static final int MAX_MEMORY_IN_MB = 64;
	
	private TransactionProvider transactionProvider;
	private ConfigurationService configurationService;
	private TestingEhCacheConfiguration config;
	private ObjectStoreManager opushCacheManager;
	private Logger logger;
	private BitronixTransactionManager transactionManager;

	@Before
	public void setup() throws IOException {
		logger = createNiceMock(Logger.class);
		transactionManager = TransactionManagerServices.getTransactionManager();
		
		IMocksControl control = createControl();
		transactionProvider = control.createMock(TransactionProvider.class);
		expect(transactionProvider.get()).andReturn(transactionManager).anyTimes();
		control.replay();
		
		configurationService = new EhCacheConfigurationService().mock(tempFolder);
		config = new TestingEhCacheConfiguration();
		config.withMaxMemoryInMB(MAX_MEMORY_IN_MB);
		opushCacheManager = new ObjectStoreManager(configurationService, config, logger, transactionProvider);
	}
	
	@After
	public void tearDown() {
		opushCacheManager.shutdown();
		transactionManager.shutdown();
	}
	
	@Test
	public void testGetRunningMaxMemoryInMB() {
		ObjectStoreConfigReader objectStoreConfigReader = new ObjectStoreConfigReader(opushCacheManager);
		assertThat(objectStoreConfigReader.getRunningMaxMemoryInMB()).isEqualTo(MAX_MEMORY_IN_MB);
	}
	
	@Test
	public void testGetRunningStoresMaxMemoryInMB() {
		ObjectStoreConfigReader objectStoreConfigReader = new ObjectStoreConfigReader(opushCacheManager);
		Map<String, Long> runningStores = objectStoreConfigReader.getRunningStoresMaxMemoryInMB();
		assertThat(runningStores).hasSize(EhCacheStores.STORES.size());
		for (Entry<String, Long> entry : runningStores.entrySet()) {
			assertThat(entry.getValue()).isGreaterThan(0);
		}
	}
	
	@Test
	public void testGetRunningStoresPercentages() {
		ObjectStoreConfigReader objectStoreConfigReader = new ObjectStoreConfigReader(opushCacheManager);
		Map<String, Percentage> runningStores = objectStoreConfigReader.getRunningStoresPercentages();
		assertThat(runningStores).hasSize(EhCacheStores.STORES.size());
		for (Entry<String, Percentage> entry : runningStores.entrySet()) {
			assertThat(entry.getValue().getIntValue()).isGreaterThan(0);
		}
	}
}
