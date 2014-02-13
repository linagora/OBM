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
package org.obm.push.mail;

import org.obm.push.backend.MonitoringService;
import org.obm.push.backend.PushMonitoringManager;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.MailException;
import org.obm.push.exception.activesync.TimeoutException;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.mail.imap.LinagoraImapClientProvider;
import org.obm.push.mail.imap.idle.IIdleCallback;
import org.obm.push.mail.imap.idle.IdleClient;
import org.obm.push.mail.imap.idle.IdleLine;
import org.obm.push.mail.imap.idle.IdleTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmailMonitoringThread implements MonitoringService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private class Callback implements IIdleCallback {

		@Override
		public synchronized void receive(IdleLine line) {
			if (line != null) {
			
				if ((IdleTag.EXISTS.equals(line.getTag()) || IdleTag.FETCH
						.equals(line.getTag()))) {
					
					stopIdle();
					pushMonitorManager.emit();
				}
			}
		}

		@Override
		public synchronized void disconnectedCallBack() {
			if(store != null){
				try{
					stopIdle();
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (remainConnected){
				try {
					startIdle();
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
	
	/**
	 * SynchronizedSet, all accesses should be synchronized
	 */
	protected MailboxService emailManager;
	private final UserDataRequest udr;
	private final String collectionPath;
	private Boolean remainConnected;  
	private IdleClient store;
	private final LinagoraImapClientProvider imapClientProvider;
	private final PushMonitoringManager pushMonitorManager;
	private final String mailBoxName;

	public EmailMonitoringThread(
			PushMonitoringManager pushMonitorManager,
			UserDataRequest udr,
			String collectionPath, MailboxService emailManager, 
			LinagoraImapClientProvider imapClientProvider) throws MailException {
		
		this.pushMonitorManager = pushMonitorManager;
		this.collectionPath = collectionPath;
		this.imapClientProvider = imapClientProvider;
		this.remainConnected = false;
		this.emailManager = emailManager;
		this.udr = udr;
		mailBoxName = emailManager.parseMailBoxName(udr, collectionPath);
	}

	public synchronized void startIdle() throws IMAPException, ImapTimeoutException {
		if (store == null) {
			store = imapClientProvider.getImapIdleClient(udr);
			store.login(emailManager.getActivateTLS());
			store.select(mailBoxName);
			store.startIdle(new Callback());
		}
		remainConnected = true;
		logger.info("Start monitoring for collection : '{}'", collectionPath);
	}
	
	public synchronized void stopIdle() {
		try {
			if (store != null) {
				store.stopIdle();
					store.logout();
				store = null;
			}
			remainConnected = false;
			logger.info("Stop monitoring for collection : '{}'", collectionPath);
		} catch (ImapTimeoutException e) {
			throw new TimeoutException(e);
		}
	}

	@Override
	public void stopMonitoring() {
		this.stopIdle();
	}

}
