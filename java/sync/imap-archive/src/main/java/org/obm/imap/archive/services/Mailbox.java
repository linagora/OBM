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

import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.ImapSetAclException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

class Mailbox {

	public static final String ALL_IMAP_RIGHTS = "lrswipkxtecda";
	public static final String READ_IMAP_RIGHTS = "lr";

	public static Mailbox from(String name, Logger logger, StoreClient storeClient) {
		Preconditions.checkNotNull(name);
		Preconditions.checkArgument(name != "");
		Preconditions.checkNotNull(logger);
		Preconditions.checkNotNull(storeClient);
		return new Mailbox(name, logger, storeClient);
	}
	
	protected final String name;
	protected final Logger logger;
	protected final StoreClient storeClient;
	
	protected Mailbox(String name, Logger logger, StoreClient storeClient) {
		this.name = name;
		this.logger = logger;
		this.storeClient = storeClient;
	}

	public String getName() {
		return name;
	}
	
	public void select() throws MailboxNotFoundException, ImapSelectException {
		if (!storeClient.select(name)) {
			throw new ImapSelectException(String.format("Wasn't able to select %s mailbox", name));
		}
		logger.debug("Mailbox {} selected", name);
	}

	public void grantReadRightsTo(String user) throws ImapSetAclException { 
		setAcl(user, READ_IMAP_RIGHTS);
	}

	public void grantAllRightsTo(String user) throws ImapSetAclException { 
		setAcl(user, ALL_IMAP_RIGHTS);
	}
	
	@VisibleForTesting void setAcl(String user, String acl) throws ImapSetAclException {
		if (!storeClient.setAcl(name, user, acl)) {
			throw new ImapSetAclException(String.format("Wasn't able to give to %s ACL rights %s on %s mailbox", user, acl, name));
		}
		logger.debug("ACL rights {} defined to {} on {} mailbox", acl, user, name);
	}
	
	public MessageSet uidSearch(SearchQuery searchQuery) {
		return storeClient.uidSearch(searchQuery);
	}
	
	public long uidNext() throws MailboxNotFoundException {
		return storeClient.uidNext(name);
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(name);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Mailbox) {
			Mailbox that = (Mailbox) object;
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
