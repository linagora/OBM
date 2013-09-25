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
package org.obm.push;

import java.util.Map;

import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;
import org.obm.push.store.ehcache.EhCacheStores;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class EhCacheStoresPercentageLoader {

	private static final int NUMBER_OF_STORES = EhCacheStores.STORES.size();
	private static final int ONE_HUNDRED = 100;
	private static final int AVERAGE_VALUE = ONE_HUNDRED / NUMBER_OF_STORES;

	public static Map<String, Percentage> loadStoresPercentage() {
		
		return Maps.newHashMap(Maps.toMap(EhCacheStores.STORES, 
				new Function<String, Percentage>() {
					private int decrementStoreCount = NUMBER_OF_STORES;
					
					@Override
					public Percentage apply(String name) {
						decrementStoreCount--;
						if (decrementStoreCount == 0) { // Last store takes remaining percentage 
							return Percentage.of(ONE_HUNDRED - AVERAGE_VALUE * (NUMBER_OF_STORES - 1));
						}
						return Percentage.of(AVERAGE_VALUE);
					}
				}));
	}
}
