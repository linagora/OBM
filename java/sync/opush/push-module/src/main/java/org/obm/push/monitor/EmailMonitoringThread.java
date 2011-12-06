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

import java.util.LinkedList;
import java.util.Set;

import org.minig.imap.IMAPException;
import org.minig.imap.IdleClient;
import org.minig.imap.idle.IIdleCallback;
import org.minig.imap.idle.IdleLine;
import org.minig.imap.idle.IdleTag;
import org.obm.locator.LocatorClientException;
import org.obm.push.backend.ICollectionChangeListener;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.impl.ObmSyncBackend;
import org.obm.push.impl.PushNotification;
import org.obm.push.mail.IEmailManager;
import org.obm.push.mail.ImapClientProvider;

public class EmailMonitoringThread extends OpushMonitoringThread implements IIdleCallback {
	
	/**
	 * SynchronizedSet, all accesses should be synchronized
	 */
	private Set<ICollectionChangeListener> ccls;
	protected ObmSyncBackend backend;
	protected IEmailManager emailManager;
	
	private BackendSession bs;
	private String collectionName;
	private Boolean remainConnected;  

	private IdleClient store;
	private final ImapClientProvider imapClientProvider;

	public EmailMonitoringThread(ObmSyncBackend cb,
			Set<ICollectionChangeListener> ccls, BackendSession bs,
			Integer collectionId, IEmailManager emailManager, 
			IContentsExporter contentsExporter,
			ImapClientProvider imapClientProvider) throws CollectionNotFoundException, DaoException {
		
		super(contentsExporter);
		
		this.ccls = ccls;
		this.imapClientProvider = imapClientProvider;
		this.remainConnected = false;
		this.backend = cb;
		this.emailManager = emailManager;
		this.bs = bs;
		collectionName = backend.getCollectionPathFor(collectionId);
	}

	public synchronized void startIdle() throws IMAPException, LocatorClientException {
		if (store == null) {
			store = imapClientProvider.getImapIdleClient(bs);
			store.login(emailManager.getActivateTLS());
			store.select(emailManager.parseMailBoxName(bs,
					collectionName));
			store.startIdle(this);
		}
		remainConnected = true;
		logger.info("Start monitoring for collection : '{}'", collectionName);
	}
	
	public synchronized void stopIdle() {
		if (store != null) {
			store.stopIdle();
			store.logout();
			store = null;
		}
		remainConnected = false;
		logger.info("Stop monitoring for collection : '{}'", collectionName);
	}

	@Override
	public synchronized void receive(IdleLine line) {
		if (line != null) {
		
			if ((IdleTag.EXISTS.equals(line.getTag()) || IdleTag.FETCH
					.equals(line.getTag()))) {
				
				stopIdle();
				emit(ccls);
			}
		}
	}

	@Override
	protected void addPushNotification(
			final LinkedList<PushNotification> pushNotifyList,
			final ICollectionChangeListener ccl) {

		this.stopIdle();
		pushNotifyList.add(new PushNotification(ccl));
	}

	@Override
	public synchronized void disconnectedCallBack() {
		if(store != null){
			try{
				stopIdle();
			} catch (Throwable e) {
				logger.error(e.getMessage(),e );
			}
		}
		if(remainConnected){
			try {
				startIdle();
			} catch (IMAPException e) {
				logger.error("SEND ERROR TO PDA",e );
				//TODO SEND ERROR TO PDA
			} catch (LocatorClientException e) {
				logger.error("SEND ERROR TO PDA",e );
				//TODO SEND ERROR TO PDA
			}	
		}
	}
	
}
