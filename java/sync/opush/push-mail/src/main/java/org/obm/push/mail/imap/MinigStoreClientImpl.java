/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.mail.imap;

import org.obm.push.bean.Resource;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.activesync.TimeoutException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.push.technicallog.bean.KindToBeLogged;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.TechnicalLogging;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class MinigStoreClientImpl implements MinigStoreClient {
	
	@Singleton
	public static class Factory implements MinigStoreClient.Factory {

		private final StoreClient.Factory storeClientFactory;

		@Inject
		private Factory(StoreClient.Factory storeClientFactory) {
			this.storeClientFactory = storeClientFactory;
		}
		
		@Override
		public MinigStoreClient create(String hostname, String login, String password) {
			StoreClient storeClient = storeClientFactory.create(hostname, login, password);
			return new MinigStoreClientImpl(storeClient);
		}
		
	}
	
	private final StoreClient storeClient;
	
	protected MinigStoreClientImpl(StoreClient storeClient) {
		this.storeClient = storeClient;
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onEndOfMethod=true, resourceType=ResourceType.IMAP_CONNECTION)
	public void close() {
		try {
			storeClient.logout();
		} catch (ImapTimeoutException e) {
			throw new TimeoutException(e);
		}
	}

	@Override
	public StoreClient getStoreClient() {
		return storeClient;
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onStartOfMethod=true, resourceType=ResourceType.IMAP_CONNECTION)
	public void login(Boolean activeteTLS) throws IMAPException {
		try {
			storeClient.login(activeteTLS);
		} catch (ImapTimeoutException e) {
			throw new TimeoutException(e);
		}
	}
	
	@Override
	public int compareTo(Resource o) {
		Preconditions.checkArgument(o instanceof MinigStoreClient);
		return 0;
	}
}
