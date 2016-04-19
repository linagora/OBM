/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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


package org.obm.imap.archive.mailbox;

import org.obm.imap.archive.exception.ImapDeleteException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class TemporaryMailbox extends CreatableMailboxImpl implements Mailbox {

	public static final String TEMPORARY_FOLDER = "TEMPORARY_ARCHIVE_FOLDER";
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private Mailbox mailbox;
		private DomainName domainName;
		private String cyrusPartitionSuffix;
		
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
		
		public Builder cyrusPartitionSuffix(String cyrusPartitionSuffix) {
			Preconditions.checkNotNull(cyrusPartitionSuffix);
			this.cyrusPartitionSuffix = cyrusPartitionSuffix;
			return this;
		}
		
		public TemporaryMailbox build() throws MailboxFormatException {
			Preconditions.checkState(mailbox != null);
			Preconditions.checkState(domainName != null);
			Preconditions.checkState(cyrusPartitionSuffix != null);
			MailboxPaths mailboxPaths = temporaryMailbox(mailbox);
			return new TemporaryMailbox( 
					mailboxPaths.getName(), 
					mailbox.isSharedMailbox(),
					mailboxPaths.getUserAtDomain(),
					ArchivePartitionName.from(domainName, cyrusPartitionSuffix),
					mailbox.getLogger(), 
					mailbox.getStoreClient());
		}
		
		@VisibleForTesting static MailboxPaths temporaryMailbox(Mailbox mailbox) throws MailboxFormatException {
			return MailboxPaths.from(mailbox.getName(), mailbox.isSharedMailbox()).prepend(TEMPORARY_FOLDER);
		}
	}
	
	private TemporaryMailbox(String name, boolean sharedMailbox, String userAtDomain, String archivePartitionName, Logger logger, StoreClient storeClient) {
		super(name, logger, storeClient, sharedMailbox, userAtDomain, archivePartitionName);
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
