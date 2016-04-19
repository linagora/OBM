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
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.Logger;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class DeletableMailbox extends MailboxImpl {


	public static DeletableMailbox from(String name, Logger logger, StoreClient storeClient) {
		return new Builder()
			.name(name)
			.logger(logger)
			.storeClient(storeClient)
			.build();
	}
	
	public static class Builder {
		
		private String name;
		private Logger logger;
		private StoreClient storeClient;
		private boolean sharedMailbox;
		
		public Builder name(String name) {
			Preconditions.checkNotNull(name);
			Preconditions.checkArgument(name != "");
			this.name = name;
			return this;
		}
		
		public Builder logger(Logger logger) {
			Preconditions.checkNotNull(logger);
			this.logger = logger;
			return this;
		}
		
		public Builder storeClient(StoreClient storeClient) {
			Preconditions.checkNotNull(storeClient);
			this.storeClient = storeClient;
			return this;
		}
		
		public Builder sharedMailbox(boolean sharedMailbox) {
			this.sharedMailbox = sharedMailbox;
			return this;
		}
		
		public DeletableMailbox build() {
			Preconditions.checkState(name != null);
			Preconditions.checkState(logger != null);
			Preconditions.checkState(storeClient != null);
			return new DeletableMailbox(name, logger, storeClient, sharedMailbox);
		}
	}
	
	private DeletableMailbox(String name, Logger logger, StoreClient storeClient, boolean sharedMailbox) {
		super(name, logger, storeClient, sharedMailbox);
	}
	
	public String getName() {
		return name;
	}

	public void delete() throws ImapDeleteException {
		if (!storeClient.delete(name)) {
			throw new ImapDeleteException(String.format("Wasn't able to delete %s mailbox", name));
		}
		logger.debug("Mailbox {} deleted", name);
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(name);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof DeletableMailbox) {
			DeletableMailbox that = (DeletableMailbox) object;
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
