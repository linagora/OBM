/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.services;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.imap.archive.mailbox.ArchiveMailbox;
import org.obm.imap.archive.mailbox.Mailbox;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.imap.IMAPException;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.collect.FluentIterable;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class DryRunImapArchiveProcessing extends ImapArchiveProcessing {

	@Inject
	/* visibility for injection */ 
		DryRunImapArchiveProcessing(DateTimeProvider dateTimeProvider,
			SchedulingDatesService schedulingDatesService,
			StoreClientFactory storeClientFactory,
			ArchiveTreatmentDao archiveTreatmentDao,
			ProcessedFolderDao processedFolderDao,
			ImapArchiveConfigurationService imapArchiveConfigurationService) {
		
		super(dateTimeProvider, schedulingDatesService, storeClientFactory,
				archiveTreatmentDao, processedFolderDao, imapArchiveConfigurationService);
	}
	
	@Override
	protected void logStart(Logger logger, ObmDomain domain) {
		logger.info("Starting IMAP Archive in {} for domain {}", ArchiveTreatmentKind.DRY_RUN, domain.getName());
	}
	
	@Override
	protected void folderProcessed(ProcessedFolder.Builder processedFolder) throws DaoException {
	}
	
	@Override
	protected void processingImapCopy(Mailbox mailbox, ArchiveMailbox archiveMailbox, FluentIterable<Long> mailUids, DomainName domainName, ProcessedFolder.Builder processedFolder, Logger logger) 
			throws IMAPException, MailboxFormatException, MailboxNotFoundException {
	}
}
