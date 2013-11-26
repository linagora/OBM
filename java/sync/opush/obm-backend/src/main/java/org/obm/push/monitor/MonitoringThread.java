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
package org.obm.push.monitor;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.exception.DaoException;
import org.obm.push.service.PushNotification;
import org.obm.push.service.PushPublishAndSubscribe;
import org.obm.push.state.IStateMachine;
import org.obm.push.store.CollectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public abstract class MonitoringThread implements Runnable {
	
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	protected final CollectionDao collectionDao;
	private final Set<ICollectionChangeListener> ccls;
	private final long freqMillisec;
	private final PushPublishAndSubscribe pushPublishAndSubscribe;
	private boolean stopped;
	
	protected abstract ChangedCollections getChangedCollections(Date lastSync) throws ChangedCollectionsException, DaoException;
	
	protected MonitoringThread(long freqMillisec,
			Set<ICollectionChangeListener> ccls,
			CollectionDao collectionDao, PIMBackend backend,
			PushPublishAndSubscribe.Factory pubSubFactory, IContentsExporter contentsExporter,
			IStateMachine stateMachine) {
		super();
		
		this.pushPublishAndSubscribe = pubSubFactory.create(backend, contentsExporter, stateMachine);
		this.freqMillisec = freqMillisec;
		this.stopped = false;
		this.ccls = ccls;
		this.collectionDao = collectionDao;
	}
	
	@Override
	public void run() {
		try {
			Date lastSync = getBaseLastSync();
			logger.info("Starting monitoring thread with reference date {}", lastSync);
			while (!stopped) {
				try {
					try {
						Thread.sleep(freqMillisec);
					} catch (InterruptedException e) {
						stopped = true;
						continue;
					}
						
					synchronized (ccls) {
						if (ccls.isEmpty()) {
							continue;
						}
						
						ChangedCollections changedCollections = getChangedCollections(lastSync);
						
						if (changedCollections.hasChanges()) {
							logger.info("changes detected : {}", changedCollections.toString());
						}
						
						List<PushNotification> toNotify = pushPublishAndSubscribe.listPushNotification(selectListenersToNotify(changedCollections, ccls));
						notifyListeners(toNotify);
						
						lastSync = changedCollections.getLastSync();
					}

				} catch (ChangedCollectionsException e1) {
					logger.error(e1.getMessage(), e1);
				} catch (DaoException e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (ChangedCollectionsException e1) {
			logger.error(e1.getMessage(), e1);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
		}	
	}

	private void notifyListeners(List<PushNotification> toNotify) {
		for (PushNotification listener: toNotify) {
			listener.emit();
		}
	}

	private Set<ICollectionChangeListener> selectListenersToNotify(ChangedCollections changedCollections,
			Set<ICollectionChangeListener> ccls) {
		
		if (changedCollections.getChanges().isEmpty()) {
			return ImmutableSet.<ICollectionChangeListener>of();
		}
		
		HashSet<ICollectionChangeListener> listeners = new HashSet<ICollectionChangeListener>();
		for (ICollectionChangeListener listener: ccls) {
			if (listener.monitorOneOf(changedCollections)) {
				listeners.add(listener);
			}
		}
		
		return listeners;
		
	}

	private Date getBaseLastSync() throws ChangedCollectionsException, DaoException {
		ChangedCollections collections = getChangedCollections(new Date(0));
		return collections.getLastSync();
	}
	
	protected static class ChangedCollectionsException extends Exception {
		public ChangedCollectionsException(Throwable cause) {
			super(cause);
		}
	}
	
}
