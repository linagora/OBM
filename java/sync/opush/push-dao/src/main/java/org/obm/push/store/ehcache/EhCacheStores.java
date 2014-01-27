/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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

import com.google.common.collect.ImmutableSet;

public interface EhCacheStores {

	public static final String MONITORED_COLLECTION_STORE = "monitoredCollectionService";
	public static final String SYNCED_COLLECTION_STORE = "syncedCollectionStoreService";
	public static final String UNSYNCHRONIZED_ITEM_STORE = "unsynchronizedItemService";
	public static final String MAIL_SNAPSHOT_STORE = "mailSnapshotStore";
	public static final String MAIL_WINDOWING_INDEX_STORE = "mailWindowingIndexStore";
	public static final String MAIL_WINDOWING_CHUNKS_STORE = "mailWindowingChunksStore";
	public static final String SYNC_KEYS_STORE = "syncKeysStore";
	
	public static final ImmutableSet<String> STORES = ImmutableSet.of(
			MONITORED_COLLECTION_STORE,
			SYNCED_COLLECTION_STORE,
			UNSYNCHRONIZED_ITEM_STORE,
			MAIL_SNAPSHOT_STORE,
			MAIL_WINDOWING_INDEX_STORE,
			MAIL_WINDOWING_CHUNKS_STORE,
			SYNC_KEYS_STORE);
	
}
