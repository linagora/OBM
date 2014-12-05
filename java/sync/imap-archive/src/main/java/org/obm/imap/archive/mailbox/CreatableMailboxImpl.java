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

import org.obm.imap.archive.exception.ImapAnnotationException;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapQuotaException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.AnnotationEntry;
import org.obm.push.mail.bean.AttributeValue;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.Logger;

public abstract class CreatableMailboxImpl extends MailboxImpl implements CreatableMailbox {
	
	private final String userAtDomain;
	private final String archivePartitionName;

	protected CreatableMailboxImpl(String name, Logger logger, StoreClient storeClient, String userAtDomain, String archivePartitionName) {
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
			throw new ImapCreateException(String.format("Wasn't able to create the mailbox %s", name)); 
		}
		logger.debug("Created");
	}

	@Override
	public void setMaxQuota(int quotaMaxSize) throws MailboxNotFoundException, ImapQuotaException {
		if (!storeClient.setQuota(name, quotaMaxSize)) {
			throw new ImapQuotaException(String.format("Wasn't able to give the MAX %d quota to the mailbox %s", quotaMaxSize, name)); 
		}
		logger.debug("Max quota was successfully set on folder {}", name);
	}

	@Override
	public void setSharedSeenAnnotation() throws MailboxNotFoundException, ImapAnnotationException {
		if (!storeClient.setAnnotation(name, AnnotationEntry.SHAREDSEEN, AttributeValue.sharedValue(Boolean.TRUE.toString()))) {
			throw new ImapAnnotationException(String.format("Wasn't able to set the annotation on the mailbox %s", name)); 
		}
		logger.debug("Annotation was successfully set on folder {}", name);
	}
}
