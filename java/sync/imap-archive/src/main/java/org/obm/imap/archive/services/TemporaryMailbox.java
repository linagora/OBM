/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

import org.obm.imap.archive.exception.ImapDeleteException;
import org.obm.imap.archive.exception.ImapStoreException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

class TemporaryMailbox extends ArchiveMailbox {

	public static final String TEMPORARY_FOLDER = "TEMP";
	
	public static TemporaryMailbox from(Mailbox mailbox, DomainName domainName) throws MailboxFormatException {
		Preconditions.checkNotNull(mailbox);
		Preconditions.checkNotNull(domainName);
		MailboxPaths temporaryMailbox = temporaryMailbox(mailbox.name);
		return new TemporaryMailbox( 
				temporaryMailbox.getName(), 
				temporaryMailbox.getUserAtDomain(),
				archivePartitionName(domainName),
				mailbox.logger, 
				mailbox.storeClient);
	}
	
	@VisibleForTesting static MailboxPaths temporaryMailbox(String mailbox) throws MailboxFormatException {
		return MailboxPaths.from(mailbox).prepend(TEMPORARY_FOLDER);
	}
	
	private TemporaryMailbox(String name, String userAtDomain, String archivePartitionName, Logger logger, StoreClient storeClient) {
		super(name, userAtDomain, archivePartitionName, logger, storeClient);
	}
	
	public void delete() throws ImapDeleteException {
		if (!storeClient.delete(name)) {
			throw new ImapDeleteException(String.format("Wasn't able to delete the temporary mailbox %s", name)); 
		}
		logger.debug("The folder {} was successfully deleted", name);
	}

	public void uidStoreDeleted(MessageSet messagesSet) throws ImapStoreException {
		if (!storeClient.uidStore(messagesSet, new FlagsList(ImmutableSet.of(Flag.DELETED)), true)) {
			throw new ImapStoreException(String.format("Wasn't able to add flags on mails in the archive mailbox %s", name)); 
		}
		logger.debug("Deleted flag stored for {} on mailbox {}", messagesSet, name);
	}

	public void expunge() {
		storeClient.expunge();
		logger.debug("Expunge processed on mailbox {}", name);
	}
	
	@Override
	public int hashCode(){
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof TemporaryMailbox) {
			return super.equals(object);
		}
		return false;
	}
}
