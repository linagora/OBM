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

import java.util.Map;
import java.util.Set;

import net.sf.ehcache.config.MemoryUnit;

import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ObjectStoreConfigReader {

	protected final ObjectStoreManager storeManager;

	public ObjectStoreConfigReader(ObjectStoreManager storeManager) {
		this.storeManager = storeManager;
	}
	
	public long getRunningMaxMemoryInMB() {
		return MemoryUnit.BYTES.toMegaBytes(storeManager.getConfiguration().getMaxBytesLocalHeap());
	}

	public Map<String, Percentage> getRunningStoresPercentages() {
		return getRunningStorePercentages(EhCacheStores.STORES);
	}

	public Map<String, Long> getRunningStoresMaxMemoryInMB() {
		return getRunningStoresMaxMemoryInMB(EhCacheStores.STORES);
	}

	private Map<String, Percentage> getRunningStorePercentages(Set<String> storesToEdit) {
		return Maps.asMap(storesToEdit, new Function<String, Percentage>() {
			@Override
			public Percentage apply(String storeName) {
				return Percentage.of(storeManager.requiredStoreConfiguration(storeName).getMaxBytesLocalHeapPercentage());
			}
		});
	}

	private Map<String, Long> getRunningStoresMaxMemoryInMB(Set<String> storesToEdit) {
		return Maps.asMap(storesToEdit, new Function<String, Long>() {
			@Override
			public Long apply(String storeName) {
				return MemoryUnit.BYTES.toMegaBytes(storeManager.requiredStoreConfiguration(storeName).getMaxBytesLocalHeap());
			}
		});
	}
}
