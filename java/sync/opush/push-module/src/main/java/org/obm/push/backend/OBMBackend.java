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
package org.obm.push.backend;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.obm.push.IContentsExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.ListenerRegistration;
import org.obm.push.monitor.CalendarMonitoringThread;
import org.obm.push.monitor.ContactsMonitoringThread;
import org.obm.push.protocol.provisioning.MSEASProvisioingWBXML;
import org.obm.push.protocol.provisioning.MSWAPProvisioningXML;
import org.obm.push.protocol.provisioning.Policy;
import org.obm.push.store.CollectionDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class OBMBackend implements IBackend {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final CollectionDao collectionDao;
	private final IContentsExporter contentsExporter;
	private final CalendarMonitoringThread calendarPushMonitor;
	private final ContactsMonitoringThread contactsPushMonitor;
	private final LoginService loginService;
	private final Set<ICollectionChangeListener> registeredListeners;
	private final MailMonitoringBackend emailBackend;
	
	@Inject
	private OBMBackend(CollectionDao collectionDao,
			IContentsExporter contentsExporter,
			CalendarMonitoringThread.Factory calendarMonitoringThreadFactory,
			ContactsMonitoringThread.Factory contactsMonitoringThreadFactory, 
			LoginService loginService,
			MailMonitoringBackend emailBackend) {
		
		this.collectionDao = collectionDao;
		this.contentsExporter = contentsExporter;
		this.loginService = loginService;
		this.emailBackend = emailBackend;
		
		this.registeredListeners = Collections
				.synchronizedSet(new HashSet<ICollectionChangeListener>());
		
		this.calendarPushMonitor = calendarMonitoringThreadFactory
				.createClient(5000, this.registeredListeners);
		
		this.contactsPushMonitor = contactsMonitoringThreadFactory
				.createClient(5000, this.registeredListeners);
		
	}

	@Override
	public void startMonitoring() {
		startMonitoringThreads(calendarPushMonitor, contactsPushMonitor);
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
	public void startEmailMonitoring(BackendSession bs, Integer collectionId) throws CollectionNotFoundException, DaoException {
		emailBackend.startMonitoringCollection(bs, collectionId, registeredListeners);
	}

	@Override
	public String getWasteBasket() {
		return "Trash";
	}

	@Override
	public Policy getDevicePolicy(BackendSession bs) {
		if (bs.getProtocolVersion().compareTo(new BigDecimal("2.5")) <= 0) {
			return new MSWAPProvisioningXML();
		} else {
			return new MSEASProvisioingWBXML(bs.getProtocolVersion());
		}
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
	public void resetCollection(BackendSession bs, Integer collectionId) throws DaoException {
		logger.info("reset Collection {} For Full Sync devId {}", 
				new Object[]{collectionId, bs.getDevId()});
		try {
			collectionDao.resetCollection(bs.getDevice(), collectionId);
		} catch (RuntimeException re) {
			logger.error(re.getMessage(), re);
			throw re;
		}
	}

	@Override
	public AccessToken authenticate(String loginAtDomain, String password) throws AuthFault {
		return loginService.authenticate(loginAtDomain, password);
	}

	@Override
	public Set<SyncCollection> getChangesSyncCollections(ICollectionChangeListener collectionChangeListener) 
			throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException, ProcessingEmailException {
		
		final Set<SyncCollection> syncCollectionsChanged = new HashSet<SyncCollection>();
		final BackendSession backendSession = collectionChangeListener.getSession();
		
		for (SyncCollection syncCollection: collectionChangeListener.getMonitoredCollections()) {
			
			int count = getItemEstimateSize(backendSession, syncCollection);
			if (count > 0) {
				syncCollectionsChanged.add(syncCollection);
			}
		}
		
		return syncCollectionsChanged;
	}
	
	private int getItemEstimateSize(BackendSession backendSession, SyncCollection syncCollection) 
			throws DaoException, CollectionNotFoundException, UnexpectedObmSyncServerException, ProcessingEmailException {
		
		return contentsExporter.getItemEstimateSize(backendSession, syncCollection.getSyncState(),
				syncCollection.getCollectionId(), syncCollection.getOptions().getFilterType(), syncCollection.getDataType());
	}
	
}
