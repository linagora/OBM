/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.cyrus.imap.admin;

import java.util.Collection;
import java.util.List;

import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Acl;
import org.obm.push.minig.imap.StoreClient;

public interface Connection {
	
	interface Factory {
		Connection create(StoreClient storeClient);
	}
	
	void createUserMailboxes(Collection<ImapPath> paths)
			throws ImapOperationException, ConnectionException, ImapTimeoutException;

	void createUserMailboxes(Partition partition, Collection<ImapPath> paths) 
			throws ImapOperationException, ConnectionException, ImapTimeoutException;
	
	List<Acl> getAcl(ImapPath path) throws ImapOperationException, ConnectionException;
	
	void setAcl(ImapPath path, String identifier, Acl acl) throws ImapOperationException, ConnectionException, ImapTimeoutException;
	
	List<ImapPath> listMailboxes(String user) throws ImapOperationException, ConnectionException;
	
	void delete(ImapPath path) throws ImapOperationException, ConnectionException, ImapTimeoutException;
	
	void rename(ImapPath source, ImapPath target, Partition partition) throws ImapOperationException, ConnectionException;
	
	Quota getQuota(ImapPath path);
	
	void setQuota(ImapPath path, Quota quota) throws MailboxNotFoundException, ImapTimeoutException;
	
	void removeQuota(ImapPath path) throws MailboxNotFoundException, ImapTimeoutException;
	
	void shutdown() throws ImapTimeoutException;
	
}
