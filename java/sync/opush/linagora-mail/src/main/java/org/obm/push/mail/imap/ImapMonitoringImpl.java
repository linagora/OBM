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
package org.obm.push.mail.imap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.MailMonitoringBackend;
import org.obm.push.backend.PushMonitoringManager;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.mail.MailBackend;
import org.obm.push.mail.MailException;
import org.obm.push.mail.MailboxService;
import org.obm.push.monitor.EmailMonitoringThread;
import org.obm.push.service.PushPublishAndSubscribe;
import org.obm.push.service.PushPublishAndSubscribe.Factory;
import org.obm.push.service.impl.MappingService;
import org.obm.push.state.IStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ImapMonitoringImpl implements MailMonitoringBackend {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<Integer, EmailMonitoringThread> emailPushMonitors;
	private final LinagoraImapClientProvider imapClientProvider;
	private final MailboxService emailManager;
	private final MappingService mappingService;
	private final Factory pubSubFactory;
	private final MailBackend mailBackend;
	private final IContentsExporter contentsExporter;
	private final IStateMachine stateMachine;
	
	
	@Inject
	private ImapMonitoringImpl(LinagoraImapClientProvider imapClientProvider,
			MappingService mappingService, MailboxService emailManager,
			MailBackend mailBackend, PushPublishAndSubscribe.Factory pubSubFactory, IContentsExporter contentsExporter,
			IStateMachine stateMachine) {
		this.mailBackend = mailBackend;
		this.pubSubFactory = pubSubFactory;
		this.contentsExporter = contentsExporter;
		this.emailPushMonitors = Collections
				.synchronizedMap(new HashMap<Integer, EmailMonitoringThread>());
		this.emailManager = emailManager;
		this.imapClientProvider = imapClientProvider;
		this.mappingService = mappingService;
		this.stateMachine = stateMachine;
	}
	
	private class Manager implements PushMonitoringManager {

		private final Set<ICollectionChangeListener> registeredListeners;
		private PushPublishAndSubscribe pushPublishAndSubscribe;

		public Manager(Set<ICollectionChangeListener> registeredListeners) {
			this.registeredListeners = registeredListeners;
			pushPublishAndSubscribe = pubSubFactory.create(mailBackend, contentsExporter, stateMachine);
		}
		
		@Override
		public void emit() {
			pushPublishAndSubscribe.emit(registeredListeners);
		}
		
	}
	
	@Override
	public void startMonitoringCollection(UserDataRequest udr, Integer collectionId,
			Set<ICollectionChangeListener> registeredListeners) throws CollectionNotFoundException, DaoException {
		
		String collectionName = mappingService.getCollectionPathFor(collectionId);
		
		EmailMonitoringThread emt = null;
		synchronized (emailPushMonitors) {
			emt = emailPushMonitors.get(collectionId);
		}
		try {
			if (emt != null) {
				emt.stopIdle();
			} else {
				emt = new EmailMonitoringThread(
						new Manager(registeredListeners), udr, collectionName, emailManager, imapClientProvider);
			}

			emt.startIdle();

			synchronized (emailPushMonitors) {
				emailPushMonitors.put(collectionId, emt);
			}
		} catch (MailException e) {
			stopIdle(emt, collectionId, e);
		} catch (IMAPException e) {
			stopIdle(emt, collectionId, e);
		}
	}
	
	private void stopIdle(EmailMonitoringThread emt, Integer collectionId, Exception exception) {
		logger.error("Error while starting idle on collection [ " + collectionId + " ]", exception);
		if (emt != null) {
			emt.stopIdle();	
		}
	}

	
}
