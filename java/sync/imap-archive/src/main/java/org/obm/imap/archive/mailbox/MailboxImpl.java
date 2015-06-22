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

import java.util.List;

import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.ImapSetAclException;
import org.obm.push.exception.ImapMessageNotFoundException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class MailboxImpl implements Mailbox {

	public static final String ALL_IMAP_RIGHTS = "lrswipkxtecda";
	public static final String READ_SEENFLAG_IMAP_RIGHTS = "lrs";

	public static MailboxImpl from(String name, Logger logger, StoreClient storeClient) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(name != "");
		Preconditions.checkNotNull(logger);
		Preconditions.checkNotNull(storeClient);
		return new MailboxImpl(name, logger, storeClient);
	}
	
	protected final String name;
	protected final Logger logger;
	protected final StoreClient storeClient;
	
	protected MailboxImpl(String name, Logger logger, StoreClient storeClient) {
		this.name = name;
		this.logger = logger;
		this.storeClient = storeClient;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Logger getLogger() {
		return logger;
	}
	
	@Override
	public StoreClient getStoreClient() {
		return storeClient;
	}
	
	@Override
	public void select() throws MailboxNotFoundException, ImapSelectException {
		if (!storeClient.select(name)) {
			throw new ImapSelectException(String.format("Wasn't able to select %s mailbox", name));
		}
		logger.debug("Mailbox {} selected", name);
	}

	@Override
	public void grantReadRightsTo(String user) throws ImapSetAclException { 
		setAcl(user, READ_SEENFLAG_IMAP_RIGHTS);
	}

	@Override
	public void grantAllRightsTo(String user) throws ImapSetAclException { 
		setAcl(user, ALL_IMAP_RIGHTS);
	}
	
	@VisibleForTesting void setAcl(String user, String acl) throws ImapSetAclException {
		if (!storeClient.setAcl(name, user, acl)) {
			throw new ImapSetAclException(String.format("Wasn't able to give to %s ACL rights %s on %s mailbox", user, acl, name));
		}
		logger.debug("ACL rights {} defined to {} on {} mailbox", acl, user, name);
	}
	
	@Override
	public MessageSet uidSearch(SearchQuery searchQuery) {
		return storeClient.uidSearch(searchQuery);
	}
	
	@Override
	public MessageSet uidCopy(MessageSet messages, Mailbox mailbox) throws MailboxNotFoundException {
		return storeClient.uidCopy(messages, mailbox.getName());
	}

	@Override
	public boolean uidStore(MessageSet messageSet, Flag imapArchiveFlag) {
		return storeClient.uidStore(messageSet, new FlagsList(ImmutableList.of(imapArchiveFlag)), true);
	}
	
	@Override
	public List<InternalDate> fetchInternalDate(MessageSet messageSet) throws ImapMessageNotFoundException {
		if (messageSet.isEmpty()) {
			return ImmutableList.of();
		}
		List<InternalDate> uidFetchInternalDate = storeClient.uidFetchInternalDate(messageSet);
		if (uidFetchInternalDate.isEmpty()) {
			throw new ImapMessageNotFoundException(String.format("No email for uid %s", messageSet));
		}
		return uidFetchInternalDate;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(name);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof MailboxImpl) {
			MailboxImpl that = (MailboxImpl) object;
			return Objects.equal(this.name, that.name);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", name)
			.toString();
	}
}
