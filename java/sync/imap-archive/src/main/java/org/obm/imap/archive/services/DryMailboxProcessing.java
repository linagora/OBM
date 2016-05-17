/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2016  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.services;

import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapQuotaException;
import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.ImapSetAclException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.imap.archive.mailbox.CreatableMailbox;
import org.obm.imap.archive.mailbox.Mailbox;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.imap.IMAPException;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.DateTimeProvider;

@Singleton
public class DryMailboxProcessing extends MailboxProcessing {
	
	@Inject
	@VisibleForTesting DryMailboxProcessing(DateTimeProvider dateTimeProvider,
			ProcessedFolderDao processedFolderDao,
			ImapArchiveConfigurationService imapArchiveConfigurationService) {
		super(dateTimeProvider, processedFolderDao, imapArchiveConfigurationService);
	}
	
	@Override
	protected void folderProcessed(ProcessedFolder.Builder processedFolder) throws DaoException {
	}
	
	@Override
	protected void createFolder(CreatableMailbox creatableMailbox, Mailbox mailbox, DomainName domainName, Logger logger) 
			throws MailboxNotFoundException, ImapSelectException, ImapSetAclException, ImapCreateException, ImapQuotaException {
	}
	
	@Override
	protected void processingImapCopy(Mailbox mailbox, MessageSet mailUids, ProcessedTask processedTask) 
			throws IMAPException, MailboxFormatException, MailboxNotFoundException {
	}
	
	@Override
	protected void processingImapMove(Mailbox mailbox, MessageSet mailUids, ProcessedTask processedTask) 
			throws ImapSelectException, MailboxNotFoundException {
	}
}
