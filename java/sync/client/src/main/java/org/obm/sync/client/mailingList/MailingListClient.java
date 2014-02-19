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
package org.obm.sync.client.mailingList;

import java.util.List;

import org.apache.http.client.HttpClient;
import org.obm.breakdownduration.bean.Watch;
import org.obm.configuration.module.LoggerModule;
import org.obm.sync.BreakdownGroups;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientAssert;
import org.obm.sync.locators.Locator;
import org.obm.sync.mailingList.MLEmail;
import org.obm.sync.mailingList.MailingList;
import org.obm.sync.mailingList.MailingListItemsParser;
import org.obm.sync.mailingList.MailingListItemsWriter;
import org.obm.sync.services.IMailingList;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Watch(BreakdownGroups.CLIENT_MAILING_LIST)
public class MailingListClient extends AbstractClientImpl implements IMailingList {

	@Singleton
	public static class Factory {

		private final SyncClientAssert syncClientAssert;
		private final Locator locator;
		private final Logger obmSyncLogger;

		@Inject
		private Factory(SyncClientAssert syncClientAssert, Locator locator, @Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger) {
			this.syncClientAssert = syncClientAssert;
			this.locator = locator;
			this.obmSyncLogger = obmSyncLogger;
		}

		public MailingListClient create(HttpClient httpClient) {
			return new MailingListClient(syncClientAssert, locator, obmSyncLogger, httpClient);
		}
	}
	
	private final MailingListItemsParser mlParser;
	private final MailingListItemsWriter mlWriter;
	private final Locator locator;

	private MailingListClient(SyncClientAssert syncClientAssert, 
			Locator locator, 
			@Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger, 
			HttpClient httpClient) {
		
		super(syncClientAssert, obmSyncLogger, httpClient);
		this.locator = locator;
		this.mlParser = new MailingListItemsParser();
		this.mlWriter = new MailingListItemsWriter();
	}

	@Override
	public MailingList createMailingList(AccessToken token, MailingList mailingList) throws ServerFault {
		if(mailingList == null){
			return null;
		}
		Multimap<String, String> params = initParams(token);
		params.put("mailingList", mlWriter.getMailingListsAsString(mailingList));
		Document doc = execute(token, "/mailingList/createMailingList", params);
		exceptionFactory.checkServerFaultException(doc);
		return mlParser.parseMailingList(doc.getDocumentElement());
	}

	@Override
	public MailingList getMailingListFromId(AccessToken token,  Integer id) throws ServerFault {
		if (id == null) {
			return null;
		}
		Multimap<String, String> params = initParams(token);
		params.put("id", id.toString());
		Document doc = execute(token, "/mailingList/getMailingListFromId", params);
		exceptionFactory.checkServerFaultException(doc);
		return mlParser.parseMailingList(doc.getDocumentElement());
	}

	@Override
	public List<MailingList> listAllMailingList(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, "/mailingList/listAllMailingList", params);
		exceptionFactory.checkServerFaultException(doc);
		List<MailingList> addressBooks = mlParser.parseListMailingList(doc);
		return addressBooks;
	}
	
	@Override
	public MailingList modifyMailingList(AccessToken token, MailingList mailingList) throws ServerFault {
		if (mailingList == null) {
			return null;
		}
		Multimap<String, String> params = initParams(token);
		String ml = mlWriter.getMailingListsAsString(mailingList);
		params.put("mailingList", ml);
		Document doc = execute(token, "/mailingList/modifyMailingList", params);
		exceptionFactory.checkServerFaultException(doc);
		return mlParser.parseMailingList(doc.getDocumentElement());
	}

	@Override
	public void removeMailingList(AccessToken token, Integer id) throws ServerFault {
		if (id == null) {
			return;
		}
		Multimap<String, String> params = initParams(token);
		params.put("id", id.toString());
		executeVoid(token, "/mailingList/removeMailingList", params);
	}

	@Override
	public List<MLEmail> addEmails(AccessToken token, Integer mailingListId, List<MLEmail> email) throws ServerFault {
		if (mailingListId == null || email == null) {
			return null;
		}
		Multimap<String, String> params = initParams(token);
		params.put("mailingListId", mailingListId.toString());
		params.put("mailingListEmails", mlWriter.getMailingListEmailsAsString(email));
		Document doc = execute(token, "/mailingList/addEmails", params);
		exceptionFactory.checkServerFaultException(doc);
		return mlParser.parseMailingListEmails(doc);
	}
	
	@Override
	public void removeEmail(AccessToken token, Integer mailingListId, Integer emailId) throws ServerFault {
		if (mailingListId == null || emailId == null) {
			return;
		}
		Multimap<String, String> params = initParams(token);
		params.put("mailingListId", mailingListId.toString());
		params.put("mailingListEmailId", emailId.toString());
		executeVoid(token, "/mailingList/removeEmail", params);
	}
	
	@Override
	protected Locator getLocator() {
		return locator;
	}
	
}
