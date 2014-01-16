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
package org.obm.push.backend;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.ListenerRegistration;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.monitor.CalendarMonitoringThread;
import org.obm.push.monitor.ContactsMonitoringThread;
import org.obm.push.service.DateService;
import org.obm.push.state.IStateMachine;
import org.obm.push.store.CollectionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class OBMBackend implements IBackend {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final CollectionDao collectionDao;
	private final IContentsExporter contentsExporter;
	private final CalendarMonitoringThread calendarPushMonitor;
	private final ContactsMonitoringThread contactsPushMonitor;
	private final Set<ICollectionChangeListener> registeredListeners;
	private final MailMonitoringBackend emailBackend;
	private final DateService dateService;
	private final IStateMachine stateMachine;
	private final boolean enablePush;
	
	@Inject
	private OBMBackend(CollectionDao collectionDao,
			IContentsExporter contentsExporter,
			CalendarMonitoringThread.Factory calendarMonitoringThreadFactory,
			ContactsMonitoringThread.Factory contactsMonitoringThreadFactory, 
			MailMonitoringBackend emailBackend,
			DateService dateService,
			IStateMachine stateMachine,
			@Named("enable-push") boolean enablePush) {
		
		this.collectionDao = collectionDao;
		this.contentsExporter = contentsExporter;
		this.emailBackend = emailBackend;
		this.dateService = dateService;
		this.stateMachine = stateMachine;
		this.enablePush = enablePush;
		
		this.registeredListeners = Collections
				.synchronizedSet(new HashSet<ICollectionChangeListener>());
		
		this.calendarPushMonitor = calendarMonitoringThreadFactory
				.createClient(5000, this.registeredListeners);
		
		this.contactsPushMonitor = contactsMonitoringThreadFactory
				.createClient(5000, this.registeredListeners);
		
	}

	@Override
	public void startMonitoring() {
		if (enablePush) {
			startMonitoringThreads(calendarPushMonitor, contactsPushMonitor);
		}
	}
	
	private void startMonitoringThreads(
			CalendarMonitoringThread calendarPushMonitor,
			ContactsMonitoringThread contactsPushMonitor) {
		
		Thread calThread = new Thread(calendarPushMonitor);
		calThread.setDaemon(true);
		calThread.start();

		Thread contactThread = new Thread(contactsPushMonitor);
		contactThread.setDaemon(true);
		contactThread.start();
	}

	@Override
	public void startEmailMonitoring(UserDataRequest udr, Integer collectionId) throws CollectionNotFoundException, DaoException {
		if (enablePush) {
			emailBackend.startMonitoringCollection(udr, collectionId, registeredListeners);
		}
	}

	@Override
	public String getWasteBasket() {
		return "Trash";
	}

	@Override
	public IListenerRegistration addChangeListener(ICollectionChangeListener ccl) {
		ListenerRegistration ret = new ListenerRegistration(ccl,
				registeredListeners);
		synchronized (registeredListeners) {
			registeredListeners.add(ccl);
		}
		logger.info("[" + ccl.getSession().getUser().getLoginAtDomain()
				+ "] change listener registered on backend");
		return ret;
	}

	@Override
	public void resetCollection(UserDataRequest udr, Integer collectionId) throws DaoException {
		logger.info("reset Collection {} For Full Sync devId {}", collectionId, udr.getDevId());
		try {
			collectionDao.resetCollection(udr.getDevice(), collectionId);
		} catch (RuntimeException re) {
			logger.error(re.getMessage(), re);
			throw re;
		}
	}

	@Override
	public Set<SyncCollectionResponse> getChangesSyncCollections(ICollectionChangeListener collectionChangeListener) 
			throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException, ProcessingEmailException,
			ConversionException, FilterTypeChangedException, HierarchyChangedException {
		
		final Set<SyncCollectionResponse> syncCollectionsChanged = new HashSet<SyncCollectionResponse>();
		final UserDataRequest userDataRequest = collectionChangeListener.getSession();
		
		for (AnalysedSyncCollection syncCollection: collectionChangeListener.getMonitoredCollections()) {
			SyncCollectionResponse.Builder builder = SyncCollectionResponse.builder()
					.collectionId(syncCollection.getCollectionId())
					.dataType(syncCollection.getDataType())
					.syncKey(syncCollection.getSyncKey());
			
			ItemSyncState itemSyncState = stateMachine.getItemSyncState(syncCollection.getSyncKey());
			if (itemSyncState == null) {
				itemSyncState = ItemSyncState.builder()
						.syncDate(dateService.getEpochPlusOneSecondDate())
						.syncKey(syncCollection.getSyncKey())
						.build();
			}
			
			int count = getItemEstimateSize(userDataRequest, syncCollection, itemSyncState);
			if (count > 0) {
				syncCollectionsChanged.add(builder.build());
			}
		}
		
		return syncCollectionsChanged;
	}
	
	private int getItemEstimateSize(UserDataRequest udr, AnalysedSyncCollection syncCollection, ItemSyncState itemSyncState) throws DaoException,
		CollectionNotFoundException, UnexpectedObmSyncServerException, 
		ProcessingEmailException, ConversionException, FilterTypeChangedException, HierarchyChangedException {
		
		return contentsExporter.getItemEstimateSize(udr, syncCollection, itemSyncState);
	}
}
