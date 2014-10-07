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
package org.obm.sync.client.user;

import org.apache.http.client.HttpClient;
import org.obm.breakdownduration.bean.Watch;
import org.obm.configuration.module.LoggerModule;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.BreakdownGroups;
import org.obm.sync.Parameter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientAssert;
import org.obm.sync.locators.Locator;
import org.obm.sync.services.IUser;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Watch(BreakdownGroups.CLIENT_USER)
public class UserClient extends AbstractClientImpl implements IUser {
	
	@Singleton
	public static class Factory {

		protected final String origin;
		protected final SyncClientAssert syncClientAssert;
		protected final Locator locator;
		protected final Logger obmSyncLogger;

		@Inject
		protected Factory(@Named("origin")String origin,
				SyncClientAssert syncClientAssert, 
				Locator locator, 
				@Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger) {
			
			this.origin = origin;
			this.syncClientAssert = syncClientAssert;
			this.locator = locator;
			this.obmSyncLogger = obmSyncLogger;
		}
		
		public UserClient create(HttpClient httpClient) {
			return new UserClient(syncClientAssert, locator, obmSyncLogger, httpClient);
		}
	}
	
	private final Locator locator;

	private UserClient(SyncClientAssert syncClientAssert, 
			Locator locator, 
			@Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger, 
			HttpClient httpClient) {
		
		super(syncClientAssert, obmSyncLogger, httpClient);
		this.locator = locator;
	}

	@Override
	public String getUserEmail(AccessToken token) throws ServerFault {
		Multimap<String, Parameter> params = initParams(token);
		Document doc = execute(token, "/user/getUserEmail", params);
		exceptionFactory.checkServerFaultException(doc);
		return DOMUtils.getElementText(doc.getDocumentElement(), "value");
	}

	@Override
	protected Locator getLocator() {
		return locator;
	}
}
