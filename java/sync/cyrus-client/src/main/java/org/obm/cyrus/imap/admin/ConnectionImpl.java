/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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

import java.util.List;

import org.obm.push.mail.bean.Acl;
import org.obm.push.minig.imap.StoreClient;

public class ConnectionImpl implements Connection {

	private StoreClient storeClient;

	public static class Factory implements Connection.Factory {
		@Override
		public ConnectionImpl create(StoreClient storeClient) {
			return new ConnectionImpl(storeClient);
		}
	}

	public ConnectionImpl(StoreClient storeClient) {
		this.storeClient = storeClient;
	}
	
	@Override
	public void createUserMailboxes(ImapPath... paths)
			throws ImapOperationException, ConnectionException {
		for (ImapPath path : paths) {
			storeClient.create(path.format());
		}
	}

	@Override
	public void createUserMailboxes(Partition partition, ImapPath... paths)
			throws ImapOperationException, ConnectionException {
		for (ImapPath path : paths) {
			storeClient.create(path.format(), partition.getName());
		}
	}

	@Override
	public List<Acl> getAcl(ImapPath path) throws ImapOperationException, ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAcl(ImapPath path, String identifier, Acl acl)
			throws ImapOperationException, ConnectionException {
		storeClient.setAcl(path.format(), identifier, acl.format());
	}

	@Override
	public List<ImapPath> listMailboxes(String user)
			throws ImapOperationException, ConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(ImapPath path) throws ImapOperationException,
			ConnectionException {
		storeClient.delete(path.format());
	}

	@Override
	public void rename(ImapPath source, ImapPath target, Partition partition)
			throws ImapOperationException, ConnectionException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Quota getQuota(ImapPath path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQuota(ImapPath path, Quota quota) {
		if (quota.isLimited()) {
			storeClient.setQuota(path.format(), quota.getLimit() * 1024); // Cyrus uses Kb
		} else {
			storeClient.removeQuota(path.format());
		}
	}

	@Override
	public void removeQuota(ImapPath path) {
		storeClient.removeQuota(path.format());
	}

	@Override
	public void shutdown() {
		storeClient.logout();
	}

}
