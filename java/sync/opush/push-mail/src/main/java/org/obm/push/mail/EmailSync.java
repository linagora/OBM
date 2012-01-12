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

import java.util.Comparator;
import java.util.Date;
import java.util.Set;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Email;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.store.EmailDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class EmailSync implements IEmailSync {

	private final class UidComparator implements Comparator<Email> {
		@Override
		public int compare(Email o1, Email o2) {
			return (int) (o1.getUid() - o2.getUid());
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(EmailSync.class);
	private final EmailDao emailDao;

	@Inject
	public EmailSync(EmailDao emailDao) {
		this.emailDao = emailDao;
	}

	@Override
	public MailChanges getSync(BackendSession bs, MailboxService mailboxService, SyncState state, String collectionName, Integer collectionId) throws DaoException, MailException {
		Set<Email> emailsFromIMAP = mailboxService.fetchEmails(bs, collectionName, state.getLastSync());
		Set<Email> alreadySyncedEmails = emailDao.listSyncedEmails(bs.getDevice().getDatabaseId(), collectionId, state);
		Set<Email> newAndUpdatedEmails = Sets.difference(emailsFromIMAP, alreadySyncedEmails);
		Set<Email> deletedEmails = findDeletedEmails(emailsFromIMAP, alreadySyncedEmails);
		MailChanges mailChanges = new MailChanges(deletedEmails, newAndUpdatedEmails);
		loggerInfo(state.getLastSync(), emailsFromIMAP, mailChanges, alreadySyncedEmails);
		return mailChanges;
	}

	private Set<Email> findDeletedEmails(Set<Email> emailsFromIMAP, Set<Email> alreadySyncedEmails) {
		Set<Email> deletedEmails = 
				org.obm.push.utils.collection.Sets.difference(
						alreadySyncedEmails, emailsFromIMAP, new UidComparator());
		return deletedEmails;
	}

	private void loggerInfo(Date syncStartDate, Set<Email> emailsFromIMAP, MailChanges mailChanges, Set<Email> alreadySyncedEmails) {
		logger.info("Synchronization date {}", syncStartDate);
		logger.info("{} email(s) from imap", emailsFromIMAP.size());
		logger.info("{} new or updated emails", mailChanges.getNewAndUpdatedEmails().size());
		logger.info("{} already synced emails", alreadySyncedEmails.size());
	}

}
