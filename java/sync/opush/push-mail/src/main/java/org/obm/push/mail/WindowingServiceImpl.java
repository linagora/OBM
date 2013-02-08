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
package org.obm.push.mail;

import org.obm.push.bean.SyncKey;
import org.obm.push.mail.EmailChanges.Builder;
import org.obm.push.mail.EmailChanges.Splitter;
import org.obm.push.mail.bean.WindowingIndexKey;
import org.obm.push.store.WindowingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class WindowingServiceImpl implements WindowingService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final WindowingDao windowingDao;

	@Inject
	@VisibleForTesting WindowingServiceImpl(WindowingDao windowingDao) {
		this.windowingDao = windowingDao;
	}

	@Override
	public EmailChanges popNextPendingElements(WindowingIndexKey key, int maxSize, SyncKey newSyncKey) {
		Preconditions.checkArgument(key != null);
		Preconditions.checkArgument(maxSize > 0);
		Preconditions.checkArgument(newSyncKey != null);

		logger.info("retrieve a maximum of {} changes for key {}", maxSize, key);
		
		EmailChanges changes = getEnoughChunks(key, maxSize);
		Splitter splittedToFitWindowSize = splitToFitWindowSize(changes, maxSize);
		windowingDao.pushNextRequestPendingElements(key, newSyncKey, splittedToFitWindowSize.getLeft());
		return splittedToFitWindowSize.getFit();
	}

	private Splitter splitToFitWindowSize(EmailChanges changes, int maxSize) {
		Splitter parts = changes.splitToFit(maxSize);

		logger.info("a chunk has been splitted, fit:{} and left:{}", parts.getFit(), parts.getLeft());
		
		return parts;
	}

	private EmailChanges getEnoughChunks(WindowingIndexKey key, int maxSize) {
		Builder builder = EmailChanges.builder();
		for (EmailChanges changes: windowingDao.consumingChunksIterable(key)) {

			logger.info("a chunk is retrieved {}", changes);
			
			builder.merge(changes);
			if (builder.sumOfChanges() >= maxSize) {
				break;
			}
		}
		return builder.build();
	}
	
	@Override
	public void pushPendingElements(WindowingIndexKey key, SyncKey syncKey, EmailChanges changes, int windowSize) {
		
		logger.info("pushing windowing elements, key:{}, syncKey:{}, changes:{}, windowSize:{}", 
				new Object[]{key, syncKey, changes, windowSize});
		
		for (EmailChanges chunk: ImmutableList.copyOf(changes.partition(windowSize)).reverse()) {
			windowingDao.pushPendingElements(key, syncKey, chunk);
		}
	}

	@Override
	public boolean hasPendingElements(WindowingIndexKey key, SyncKey syncKey) {
		SyncKey windowingSyncKey = windowingDao.getWindowingSyncKey(key);
		
		if (windowingSyncKey == null) {
			logger.info("no pending windowing for key {}", key);
			return false;
		} else if(!windowingSyncKey.equals(syncKey)) {
			logger.info("reseting a pending windowing for key {} and syncKey {} by a new syncKey {}",
					new Object[] {key, windowingSyncKey, syncKey});
			windowingDao.removePreviousCollectionWindowing(key);
			return false;
		} else {
			logger.info("there is a pending windowing for key {}, syncKey is {}", key, windowingSyncKey);
			return true;
		}
	}

}
