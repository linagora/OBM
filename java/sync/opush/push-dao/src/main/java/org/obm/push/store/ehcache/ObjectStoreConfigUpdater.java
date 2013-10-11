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

import static org.obm.push.store.ehcache.EhCacheStores.STORES;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.MemoryUnit;

import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.obm.push.utils.jvm.JvmUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

public class ObjectStoreConfigUpdater {

	@VisibleForTesting final ObjectStoreManager storeManager;
	@VisibleForTesting final ObjectStoreConfigReader configReader;

	public ObjectStoreConfigUpdater(ObjectStoreManager storeManager) {
		this.storeManager = storeManager;
		this.configReader = storeManager.createConfigReader();
	}
	
	public void updateMaxMemoryInMB(long newMaxMemoryInMB) {
		Preconditions.checkArgument(newMaxMemoryInMB > 0, "MaxMemoryInMB must be greater than zero");
		Preconditions.checkArgument(newMaxMemoryInMB < JvmUtils.maxRuntimeJvmMemoryInMB(), 
				"MaxMemoryInMB is greater than jvm Xmx");
		
		resizeMaxMemory(newMaxMemoryInMB);
	}
	
	public void updateStoresMaxMemory(Map<String, Percentage> storesPercentage) {
		Preconditions.checkArgument(STORES.containsAll(storesPercentage.keySet()), "Unexpected store name");
		Preconditions.checkArgument(STORES.size() == storesPercentage.size(), "All stores must be given");
		
		resizeStoresMaxMemory(ObjectStoreManager.checkGlobalPercentage(storesPercentage));
	}

	private void resizeStoresMaxMemory(Map<String, Percentage> checkGlobalPercentage) {
		for (Entry<String, Percentage> newStorePercentage : checkGlobalPercentage.entrySet()) {
			CacheConfiguration cacheConfig = storeManager.requiredStoreConfiguration(newStorePercentage.getKey());
			cacheConfig.setMaxBytesLocalHeap(newStorePercentage.getValue().get());
			cacheConfig.setMaxBytesLocalHeap(MemoryUnit.MEGABYTES.toBytes(newStorePercentage.getValue().applyTo(configReader.getRunningMaxMemoryInMB())));
		}
	}

	private void resizeMaxMemory(long newMaxMemoryInMB) {
		Map<String, Percentage> runningStoresPercentages = configReader.getRunningStoresPercentages();
		reducePercentagesToFit(newMaxMemoryInMB);
		storeManager.getConfiguration().setMaxBytesLocalHeap(MemoryUnit.MEGABYTES.toBytes(newMaxMemoryInMB));
		resizeStoresMaxMemory(runningStoresPercentages);
	}

	private void reducePercentagesToFit(long newMaxMemoryInMB) {
		long runningMaxMemoryInMB = configReader.getRunningMaxMemoryInMB();
		if (runningMaxMemoryInMB < newMaxMemoryInMB) {
			return;
		}
		
		int percentageToReduceForEach = Ints.checkedCast(newMaxMemoryInMB * 100 / runningMaxMemoryInMB);
		for (String storeName : STORES) {
			CacheConfiguration cacheConfig = storeManager.requiredStoreConfiguration(storeName);
			cacheConfig.setMaxBytesLocalHeap(
					Percentage.of(percentageToReduceForEach).applyTo(cacheConfig.getMaxBytesLocalHeap()));
		}
	}
}
