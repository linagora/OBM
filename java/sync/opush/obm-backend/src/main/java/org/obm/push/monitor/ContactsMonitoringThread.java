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
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.ChangedCollections;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.service.PushPublishAndSubscribe;
import org.obm.push.state.IStateMachine;
import org.obm.push.store.CollectionDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

public class ContactsMonitoringThread extends MonitoringThread {

	@Singleton
	public static class Factory {
		private final CollectionDao collectionDao;
		private final PushPublishAndSubscribe.Factory pubSubFactory;
		private final ContactsBackend contactsBackend;
		private final IContentsExporter contentsExporter;
		private final IStateMachine stateMachine;

		@Inject
		private Factory(CollectionDao collectionDao, ContactsBackend contactsBackend,
				PushPublishAndSubscribe.Factory pubSubFactory, IContentsExporter contentsExporter, IStateMachine stateMachine) {
			this.collectionDao = collectionDao;
			this.contactsBackend = contactsBackend;
			this.pubSubFactory = pubSubFactory;
			this.contentsExporter = contentsExporter;
			this.stateMachine = stateMachine;
		}

		public ContactsMonitoringThread createClient(long freqMs,
				Set<ICollectionChangeListener> ccls) {
			
			return new ContactsMonitoringThread(freqMs, ccls,
					this.collectionDao, this.contactsBackend, pubSubFactory, contentsExporter, stateMachine);
		}
	}
	
	private ContactsMonitoringThread(long freqMs, Set<ICollectionChangeListener> ccls, CollectionDao collectionDao, 
			ContactsBackend contactsBackend, PushPublishAndSubscribe.Factory pubSubFactory, IContentsExporter contentsExporter, IStateMachine stateMachine) {
		
		super(freqMs, ccls, collectionDao, contactsBackend, pubSubFactory, contentsExporter, stateMachine);
	}

	@Override
	protected ChangedCollections getChangedCollections(Date lastSync) throws ChangedCollectionsException {
		try {
			return collectionDao.getContactChangedCollections(lastSync);
		} catch (DaoException e) {
			throw new ChangedCollectionsException(e);
		}
	}

}
