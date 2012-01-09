/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.search;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SearchResult;
import org.obm.push.bean.StoreName;
import org.obm.push.contacts.ContactConverter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.IAddressBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObmSearchContact implements ISearchSource {

	private final static Logger logger = LoggerFactory.getLogger(ObmSearchContact.class);
	private final IAddressBook bookClient;
	private final LoginService login;
	
	@Inject
	private ObmSearchContact(IAddressBook bookClient, LoginService login) {
		super();
		this.bookClient = bookClient;
		this.login = login;
	}
	
	@Override
	public StoreName getStoreName() {
		return StoreName.GAL;
	}

	@Override
	public List<SearchResult> search(BackendSession bs, String query, Integer limit) {
		IAddressBook bc = getBookClient();
		AccessToken token = login.login(bs.getUser().getLoginAtDomain(), bs.getPassword());
		List<SearchResult> ret = new LinkedList<SearchResult>();
		ContactConverter cc = new ContactConverter();
		try {
			List<Contact> contacts = bc.searchContact(token, query, limit);
			for (Contact contact: contacts) {
				ret.add(cc.convertToSearchResult(contact));
			}
		} catch (ServerFault e) {
			logger.error(e.getMessage(), e);
		} finally {
			login.logout(token);
		}
		return ret;
	}
	
	private IAddressBook getBookClient() {
		return bookClient;
	}
	
}
