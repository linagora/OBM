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

import java.util.Iterator;

import org.obm.imap.archive.beans.Year;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

class ArchiveMailbox extends Mailbox {

	public static final char IMAP_FOLDER_SEPARATOR = '/';
	public static final String ARCHIVE_MAIN_FOLDER = "ARCHIVE";
	private static final String AT = "@";
	private static final String INBOX = "INBOX";
	private static final String ARCHIVE_PARTITION_SUFFIX = "_archive";
	
	public static ArchiveMailbox from(Mailbox mailbox, Year year, DomainName domainName) throws MailboxFormatException {
		Preconditions.checkNotNull(mailbox);
		Preconditions.checkNotNull(year);
		Preconditions.checkNotNull(domainName);
		return new ArchiveMailbox( 
				archiveMailbox(mailbox.name, year), 
				getUserAtDomain(mailbox.name, domainName),
				archivePartitionName(domainName),
				mailbox.logger, 
				mailbox.storeClient);
	}
	
	@VisibleForTesting static String archiveMailbox(String mailbox, Year year) throws MailboxFormatException {
		Iterator<String> split = Splitter.on(IMAP_FOLDER_SEPARATOR).split(mailbox).iterator();
		String mainPart = nextMandatoryElement(split, mailbox);
		ImmutableList.Builder<String> splitPath = ImmutableList.<String> builder()
				.add(mainPart);
		
		String userPart = nextMandatoryElement(split, mailbox);
		if (userPart.contains(AT)) {
			Iterator<String> inbox = Splitter.on(AT).split(userPart).iterator();
			String user = nextMandatoryElement(inbox, mailbox);
			String domain = nextMandatoryElement(inbox, mailbox);
			splitPath.add(user).add(ARCHIVE_MAIN_FOLDER).add(year.serialize()).add(INBOX + AT + domain);
		} else {
			splitPath.add(userPart).add(ARCHIVE_MAIN_FOLDER).add(year.serialize());
			while (split.hasNext()) {
				splitPath.add(split.next());
			}
		}
		return Joiner.on(IMAP_FOLDER_SEPARATOR).join(splitPath.build());
	}

	@VisibleForTesting static String getUserAtDomain(String mailbox, DomainName domainName) throws MailboxFormatException {
		Iterator<String> split = Splitter.on(IMAP_FOLDER_SEPARATOR).split(mailbox).iterator();
		nextMandatoryElement(split, mailbox);
		String userPart = nextMandatoryElement(split, mailbox);
		if (userPart.contains(AT)) {
			return userPart;
		}
		return userPart + AT + domainName.get();
	}
	
	@VisibleForTesting static String archivePartitionName(DomainName domainName) {
		return domainName.get().replace('.', '_').concat(ARCHIVE_PARTITION_SUFFIX);
	}
	
	private static String nextMandatoryElement(Iterator<String> iterator, String mailbox) throws MailboxFormatException {
		if (!iterator.hasNext()) {
			throw new MailboxFormatException(mailbox);
		}
		return iterator.next();
	}
	
	private final String userAtDomain;
	private final String archivePartitionName;
	
	private ArchiveMailbox(String name, String userAtDomain, String archivePartitionName, Logger logger, StoreClient storeClient) {
		super(name, logger, storeClient);
		this.userAtDomain = userAtDomain;
		this.archivePartitionName = archivePartitionName;
	}

	public String getUserAtDomain() {
		return userAtDomain;
	}
	
	public void create() throws ImapCreateException {
		if (!storeClient.create(name, archivePartitionName)) {
			throw new ImapCreateException(String.format("Wasn't able to create the archive mailbox %s", name)); 
		}
		logger.debug("Created");
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
