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

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.obm.push.bean.SyncKey;
import org.obm.push.mail.EmailChanges;
import org.obm.push.store.WindowingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WindowingDaoEhcacheImpl implements WindowingDao {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Cache chunksStore;
	private final Cache indexStore;
	
	@Inject 
	private WindowingDaoEhcacheImpl(ObjectStoreManager objectStoreManager) {
		chunksStore = objectStoreManager.getStore(ObjectStoreManager.MAIL_WINDOWING_CHUNKS_STORE);
		indexStore = objectStoreManager.getStore(ObjectStoreManager.MAIL_WINDOWING_INDEX_STORE);
	}

	@Override
	public Iterable<EmailChanges> consumingChunksIterable(final SyncKey syncKey) {
		return new Iterable<EmailChanges>() {
			@Override
			public Iterator<EmailChanges> iterator() {
				return new Iterator<EmailChanges>() {

					@Override
					public boolean hasNext() {
						return getIndex(syncKey) != null;
					}

					@Override
					public EmailChanges next() {
						Integer index = getIndex(syncKey);
						if (index == null) {
							throw new NoSuchElementException();
						}
						EmailChanges emailChanges = consumeChunk(syncKey, index);
						udpateIndex(syncKey, index);
						return emailChanges;
					}

					private EmailChanges consumeChunk(SyncKey syncKey, Integer index) {
						ChunkKey key = new ChunkKey(index, syncKey);
						Element emailChanges = chunksStore.get(key);
						chunksStore.remove(key);
						return (EmailChanges) emailChanges.getValue();
					}

					private void udpateIndex(final SyncKey syncKey, int index) {
						if (index == 0) {
							indexStore.remove(syncKey);
						} else {
							indexStore.put(new Element(syncKey, index - 1));
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}
	
	@Override
	public void pushPendingElements(SyncKey syncKey, EmailChanges partition) {
		logger.debug("put windowing EmailChanges with key {} : {}", syncKey, partition);
		int lastIndex = getNextIndex(syncKey);
		chunksStore.put(new Element(new ChunkKey(lastIndex, syncKey), partition));
		indexStore.put(new Element(syncKey, lastIndex));
	}

	public static class ChunkKey implements Serializable {
		
		private int index;
		private SyncKey syncKey;
		
		public ChunkKey(int index, SyncKey syncKey) {
			this.index = index;
			this.syncKey = syncKey;
		}
		
		@Override
		public final int hashCode(){
			return Objects.hashCode(index, syncKey);
		}
		
		@Override
		public final boolean equals(Object object){
			if (object instanceof ChunkKey) {
				ChunkKey that = (ChunkKey) object;
				return Objects.equal(this.index, that.index)
					&& Objects.equal(this.syncKey, that.syncKey);
			}
			return false;
		}		
		
	}
	
	private int getNextIndex(SyncKey syncKey) {
		Integer index = getIndex(syncKey);
		if (index != null) {
			return index + 1;
		}
		return 0;
	}

	private Integer getIndex(SyncKey syncKey) {
		Element indexElement = indexStore.get(syncKey);
		return (Integer) (indexElement != null ? indexElement.getValue() : null);
	}

	@Override
	public boolean hasPendingElements(SyncKey syncKey) {
		return indexStore.get(syncKey) != null;
	}
}
