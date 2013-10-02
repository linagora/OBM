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

import org.obm.push.ContinuationTransactionMap;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.store.SnapshotDao;
import org.obm.push.store.SyncKeysDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.store.WindowingDao;
import org.obm.sync.LifecycleListener;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class EhCacheDaoModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(EhCacheConfiguration.class).to(EhCacheConfigurationFileImpl.class);
		bind(EhCacheStatistics.class).to(EhCacheStatisticsImpl.class);
		bind(StoreManager.class).to(ObjectStoreManager.class);

		bind(MonitoredCollectionDao.class).to(MonitoredCollectionDaoEhcacheImpl.class);
		bind(SyncedCollectionDao.class).to(SyncedCollectionDaoEhcacheImpl.class);
		bind(UnsynchronizedItemDao.class).to(UnsynchronizedItemDaoEhcacheImpl.class);
		bind(ContinuationTransactionMap.class).to(ContinuationTransactionMapImpl.class);
		bind(SnapshotDao.class).to(SnapshotDaoEhcacheImpl.class);
		bind(WindowingDao.class).to(WindowingDaoEhcacheImpl.class);
		bind(SyncKeysDao.class).to(SyncKeysDaoEhcacheImpl.class);

		Multibinder<LifecycleListener> lifecycleListeners = Multibinder.newSetBinder(binder(), LifecycleListener.class);
		lifecycleListeners.addBinding().to(StoreManager.class);
		lifecycleListeners.addBinding().to(NonTransactionalObjectStoreManager.class);
	}

}
