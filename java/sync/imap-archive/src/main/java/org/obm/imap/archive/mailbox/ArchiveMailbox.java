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

import org.obm.imap.archive.beans.Year;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapQuotaException;
import org.obm.imap.archive.exception.ImapStoreException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

public class ArchiveMailbox extends MailboxImpl implements CreatableMailbox {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private Mailbox mailbox;
		private Year year;
		private DomainName domainName;
		private String archiveMainFolder;
		private String cyrusPartitionSuffix;
		
		public Builder from(Mailbox mailbox) {
			Preconditions.checkNotNull(mailbox);
			this.mailbox = mailbox;
			return this;
		}
		
		public Builder year(Year year) {
			Preconditions.checkNotNull(year);
			this.year = year;
			return this;
		}
		
		public Builder domainName(DomainName domainName) {
			Preconditions.checkNotNull(domainName);
			this.domainName = domainName;
			return this;
		}
		
		public Builder archiveMainFolder(String archiveMainFolder) {
			Preconditions.checkNotNull(archiveMainFolder);
			this.archiveMainFolder = archiveMainFolder;
			return this;
		}
		
		public Builder cyrusPartitionSuffix(String cyrusPartitionSuffix) {
			Preconditions.checkNotNull(cyrusPartitionSuffix);
			this.cyrusPartitionSuffix = cyrusPartitionSuffix;
			return this;
		}
		
		public ArchiveMailbox build() throws MailboxFormatException {
			Preconditions.checkState(mailbox != null);
			Preconditions.checkState(year != null);
			Preconditions.checkState(domainName != null);
			Preconditions.checkState(archiveMainFolder != null);
			Preconditions.checkState(cyrusPartitionSuffix != null);
			MailboxPaths mailboxPaths = archiveMailbox(mailbox.getName(), year, archiveMainFolder);
			return new ArchiveMailbox( 
					mailboxPaths.getName(), 
					mailboxPaths.getUserAtDomain(),
					ArchivePartitionName.from(domainName, cyrusPartitionSuffix),
					mailbox.getLogger(), 
					mailbox.getStoreClient());
		}
		
		@VisibleForTesting static MailboxPaths archiveMailbox(String mailbox, Year year, String archiveMainFolder) throws MailboxFormatException {
			return MailboxPaths.from(mailbox).prepend(Joiner.on(MailboxPaths.IMAP_FOLDER_SEPARATOR).join(archiveMainFolder, year.serialize()));
		}
	}
	
	private final String userAtDomain;
	private final String archivePartitionName;
	
	private ArchiveMailbox(String name, String userAtDomain, String archivePartitionName, Logger logger, StoreClient storeClient) {
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
			throw new ImapCreateException(String.format("Wasn't able to create the archive mailbox %s", name)); 
		}
		logger.debug("Created");
	}

	public void uidStoreSeen(MessageSet messagesSet) throws ImapStoreException {
		if (!storeClient.uidStore(messagesSet, new FlagsList(ImmutableSet.of(Flag.SEEN)), true)) {
			throw new ImapStoreException(String.format("Wasn't able to add flags on mails in the archive mailbox %s", name)); 
		}
		logger.debug("Stored");
	}

	@Override
	public void setMaxQuota(int quotaMaxSize) throws MailboxNotFoundException, ImapQuotaException {
		if (!storeClient.setQuota(name, quotaMaxSize)) {
			throw new ImapQuotaException(String.format("Wasn't able to give the MAX %d quota to the archive mailbox %s", quotaMaxSize, name)); 
		}
		logger.debug("Max quota was successfully set on folder {}", name);
	}
	
	@Override
	public int hashCode(){
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ArchiveMailbox) {
			return super.equals(object);
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
