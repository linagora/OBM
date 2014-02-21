/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.io.IOException;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.annotations.transactional.TransactionProvider;
import org.obm.push.configuration.OpushConfiguration;
import org.obm.transaction.TransactionManagerRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectStoreManagerTest {

	@Rule public TemporaryFolder tempFolder =  new TemporaryFolder();
	@Rule public TransactionManagerRule transactionManagerRule = new TransactionManagerRule();
	
	private ObjectStoreManager opushCacheManager;
	private EhCacheConfiguration config;
	private OpushConfiguration opushConfiguration;
	private Logger logger;
	private TransactionProvider transactionProvider;

	
	@Before
	public void init() throws IOException {
		logger = LoggerFactory.getLogger(getClass());
		
		IMocksControl control = createControl();
		transactionProvider = control.createMock(TransactionProvider.class);
		expect(transactionProvider.get()).andReturn(transactionManagerRule.getTransactionManager()).anyTimes();
		control.replay();
		
		opushConfiguration = new EhCacheOpushConfiguration().mock(tempFolder);
		config = new TestingEhCacheConfiguration();
		opushCacheManager = new ObjectStoreManager(opushConfiguration, config, logger, transactionProvider);
	}

	@After
	public void shutdown() {
		opushCacheManager.shutdown();
	}

	@Test
	public void loadStores() {
		assertThat(opushCacheManager.listStores()).hasSize(7);
	}
	
	@Test
	public void createNewThreeCachesAndRemoveOne() {
		opushCacheManager.createNewStore("test 1");
		opushCacheManager.createNewStore("test 2");
		opushCacheManager.createNewStore("test 3");
		
		opushCacheManager.removeStore("test 2");

		assertThat(opushCacheManager.getStore("test 1")).isNotNull();
		assertThat(opushCacheManager.getStore("test 3")).isNotNull();
		assertThat(opushCacheManager.getStore("test 2")).isNull();
		assertThat(opushCacheManager.listStores()).hasSize(9);
	}
	
	@Test
	public void createAndRemoveCache() {
		opushCacheManager.createNewStore("test 1");
		opushCacheManager.removeStore("test 1");

		assertThat(opushCacheManager.getStore("test 1")).isNull();
	}

	@Test
	public void createTwoIdenticalCache() {
		opushCacheManager.createNewStore("test 1");
		opushCacheManager.createNewStore("test 1");
		
		assertThat(opushCacheManager.getStore("test 1")).isNotNull();
		assertThat(opushCacheManager.listStores()).hasSize(8);
	}
}
