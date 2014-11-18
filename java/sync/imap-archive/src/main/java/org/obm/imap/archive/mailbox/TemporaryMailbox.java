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

package org.obm.imap.archive.mailbox;

import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapDeleteException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class TemporaryMailbox extends MailboxImpl implements CreatableMailbox {

	public static final String TEMPORARY_FOLDER = "TEMPORARY_ARCHIVE_FOLDER";
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private Mailbox mailbox;
		private DomainName domainName;
		
		public Builder from(Mailbox mailbox) {
			Preconditions.checkNotNull(mailbox);
			this.mailbox = mailbox;
			return this;
		}
		
		public Builder domainName(DomainName domainName) {
			Preconditions.checkNotNull(domainName);
			this.domainName = domainName;
			return this;
		}
		
		public TemporaryMailbox build() throws MailboxFormatException {
			Preconditions.checkState(mailbox != null);
			Preconditions.checkState(domainName != null);
			MailboxPaths mailboxPaths = temporaryMailbox(mailbox.getName());
			return new TemporaryMailbox( 
					mailboxPaths.getName(), 
					mailboxPaths.getUserAtDomain(),
					ArchivePartitionName.from(domainName),
					mailbox.getLogger(), 
					mailbox.getStoreClient());
		}
		
		@VisibleForTesting static MailboxPaths temporaryMailbox(String mailbox) throws MailboxFormatException {
			return MailboxPaths.from(mailbox).prepend(TEMPORARY_FOLDER);
		}
	}
	
	private final String userAtDomain;
	private final String archivePartitionName;
	
	private TemporaryMailbox(String name, String userAtDomain, String archivePartitionName, Logger logger, StoreClient storeClient) {
		super(name, logger, storeClient);
		this.userAtDomain = userAtDomain;
		this.archivePartitionName = archivePartitionName;
	}

	@Override
	public String getUserAtDomain() {
		return userAtDomain;
	}
	
	@Override
	public void create() throws ImapCreateException {
		if (!storeClient.create(name, archivePartitionName)) {
			throw new ImapCreateException(String.format("Wasn't able to create the temporary mailbox %s", name)); 
		}
		logger.debug("Created");
	}
	
	public void delete() throws ImapDeleteException {
		if (!storeClient.delete(name)) {
			throw new ImapDeleteException(String.format("Wasn't able to delete the temporary mailbox %s", name)); 
		}
		logger.debug("The folder {} was successfully deleted", name);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof TemporaryMailbox) {
			return super.equals(object);
		}
		return false;
	}
}
