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
package org.obm.push.monitor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.PushNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpushMonitoringThread {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private final IContentsExporter contentsExporter;
	
	public OpushMonitoringThread(IContentsExporter contentsExporter) {
		this.contentsExporter = contentsExporter;
	}
	
	protected void emit(final Set<ICollectionChangeListener> ccls) {
		final LinkedList<PushNotification> pushNotifyList = listPushNotification(ccls);
		for (PushNotification pushNotify: pushNotifyList) {
			pushNotify.emit();
		}
	}
	
	protected LinkedList<PushNotification> listPushNotification(
			Set<ICollectionChangeListener> ccls) {
		
		final LinkedList<PushNotification> pushNotifyList = new LinkedList<PushNotification>();
		synchronized (ccls) {
			for (final ICollectionChangeListener ccl : ccls) {

				final Collection<SyncCollection> monitoredCollections = ccl
						.getMonitoredCollections();

				final BackendSession backendSession = ccl.getSession();
				for (SyncCollection syncCollection : monitoredCollections) {

					try {
						int count = contentsExporter.getItemEstimateSize(backendSession,
								syncCollection.getOptions().getFilterType(),
								syncCollection.getCollectionId(),
								syncCollection.getSyncState());

						if (count > 0) {
							addPushNotification(pushNotifyList, ccl);
						}
					} catch (CollectionNotFoundException e) {
						logger.error(e.getMessage(), e);
					} catch (DaoException e) {
						logger.error(e.getMessage(), e);
					} catch (UnknownObmSyncServerException e) {
						logger.error(e.getMessage(), e);
					} catch (ProcessingEmailException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}

			return pushNotifyList;
		}
	}

	protected void addPushNotification(
			final LinkedList<PushNotification> pushNotifyList,
			final ICollectionChangeListener ccl) {

		pushNotifyList.add(new PushNotification(ccl));
	}
	
}
