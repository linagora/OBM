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
package org.obm.sync.client.book;

import java.util.Date;
import java.util.List;

import javax.naming.NoPermissionException;

import org.obm.configuration.module.LoggerModule;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookItemsParser;
import org.obm.sync.book.BookItemsWriter;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.sync.exception.InvalidContactException;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.locators.Locator;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.utils.DateHelper;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class BookClient extends AbstractClientImpl implements IAddressBook {

	private final BookItemsParser respParser;
	private final BookItemsWriter biw;
	private final Locator Locator;

	@Inject
	@VisibleForTesting BookClient(SyncClientException syncClientException, Locator Locator, @Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger) {
		super(syncClientException, obmSyncLogger);
		this.Locator = Locator;
		this.respParser = new BookItemsParser();
		this.biw = new BookItemsWriter();
	}

	@Override
	public Contact createContact(AccessToken token, Integer addressBookId, Contact contact, String clientId) 
			throws ServerFault, NoPermissionException, InvalidContactException {
		
		Multimap<String, String> params = initParams(token);
		params.put("bookId", String.valueOf(addressBookId));
		params.put("contact", biw.getContactAsString(contact));
		params.put("clientId", clientId);
		Document doc = execute(token, "/book/createContact", params);
		exceptionFactory.checkCreateContactException(doc);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public Contact getContactFromId(AccessToken token, Integer addressBookId, Integer contactId) 
			throws ServerFault, ContactNotFoundException {
		
		Multimap<String, String> params = initParams(token);
		params.put("bookId", String.valueOf(addressBookId));
		params.put("id", String.valueOf(contactId));
		Document doc = execute(token, "/book/getContactFromId", params);
		exceptionFactory.checkContactNotFoundException(doc);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public KeyList getContactTwinKeys(AccessToken token, Contact contact) {
		Multimap<String, String> params = initParams(token);
		params.put("contact", biw.getContactAsString(contact));
		Document doc = execute(token, "/book/getContactTwinKeys", params);
		return respParser.parseKeyList(doc);
	}

	@Override
	public ContactChanges listContactsChanged(AccessToken token, Date lastSync) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("lastSync", DateHelper.asString(lastSync));
		Document doc = execute(token, "/book/listAllChanges", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseChanges(doc);
	}

	@Override
	public boolean isReadOnly(AccessToken token, BookType book) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("book", book.toString());
		Document doc = execute(token, "/book/isReadOnly", params);
		exceptionFactory.checkServerFaultException(doc);
		return "true".equals(respParser.parseArrayOfString(doc)[0]);
	}

	@Override
	public BookType[] listBooks(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, "/book/listBooks", params);
		exceptionFactory.checkServerFaultException(doc);
		String[] sa = respParser.parseArrayOfString(doc);
		BookType[] bts = new BookType[sa.length];
		for (int i = 0; i < sa.length; i++) {
			bts[i] = BookType.valueOf(sa[i]);
		}
		return bts;
	}

	@Override
	public List<AddressBook> listAllBooks(AccessToken token) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		Document doc = execute(token, "/book/listAllBooks", params);
		exceptionFactory.checkServerFaultException(doc);
		List<AddressBook> addressBooks = respParser.parseListAddressBook(doc);
		return addressBooks;
	}
	
	@Override
	public Contact modifyContact(AccessToken token, Integer addressBookId, Contact contact) 
			throws ServerFault, NoPermissionException, ContactNotFoundException, InvalidContactException {
		
		Multimap<String, String> params = initParams(token);
		params.put("bookId", String.valueOf(addressBookId));
		String ct = biw.getContactAsString(contact);
		params.put("contact", ct);
		Document doc = execute(token, "/book/modifyContact", params);
		exceptionFactory.checkModifyContactException(doc);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public Contact removeContact(AccessToken token, Integer addressBookId , Integer contactId) 
			throws ServerFault, ContactNotFoundException, NoPermissionException {
		
		Multimap<String, String> params = initParams(token);
		params.put("bookId", String.valueOf(addressBookId));
		params.put("id", String.valueOf(contactId));
		Document doc = execute(token, "/book/removeContact", params);
		exceptionFactory.checkRemoveContactException(doc);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public List<Contact> searchContact(AccessToken token, String query, int limit, Integer offset) throws ServerFault {
		Preconditions.checkNotNull(offset);
		Multimap<String, String> params = initParams(token);
		params.put("query", query);
		params.put("limit", "" + limit);
		Document doc = execute(token, "/book/searchContact", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListContact(doc.getDocumentElement());
	}

	@Override
	public List<Contact> searchContactInGroup(AccessToken token, AddressBook group, String query, int limit, Integer offset) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("query", query);
		params.put("limit", String.valueOf(limit));
		params.put("offset", String.valueOf(offset));
		params.put("group", String.valueOf(group.getUid()));
		Document doc = execute(token, "/book/searchContactInGroup", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListContact(doc.getDocumentElement());
	}
	
	@Override
	public int countContactsInGroup(AccessToken token, int gid) throws ServerFault {
		Multimap<String, String> params = initParams(token);
	    params.put("group", String.valueOf(gid));
	    Document doc = execute(token, "/book/countContactsInGroup", params);
	    exceptionFactory.checkServerFaultException(doc);
	    return respParser.parseCountContactsInGroup(doc.getDocumentElement());
	}

	
	@Override
	public AddressBookChangesResponse getAddressBookSync(AccessToken token, Date lastSync) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}

		Document doc = execute(token, "/book/getAddressBookSync", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseAddressBookChanges(doc);
	}
	
	@Override
	public boolean unsubscribeBook(AccessToken token, Integer addressBookId) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("bookId", String.valueOf(addressBookId));
		Document doc = execute(token, "/book/unsubscribeBook", params);
		exceptionFactory.checkServerFaultException(doc);
		return "true".equalsIgnoreCase(DOMUtils.getElementText(doc
				.getDocumentElement(), "value"));
	}

	@Override
	public FolderChanges listAddressBooksChanged(AccessToken token, Date lastSync) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("lastSync", DateHelper.asString(lastSync));
		Document doc = execute(token, "/book/listAddressBooksChanged", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseFolderChangesResponse(doc);
	}
	
	@Override
	public ContactChanges listContactsChanged(AccessToken token, Date lastSync, Integer addressBookId) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("lastSync", DateHelper.asString(lastSync));
		params.put("bookId", String.valueOf(addressBookId));
		Document doc = execute(token, "/book/listAllChanges", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseChanges(doc);
	}

	@Override
	public List<Contact> searchContactsInSynchronizedAddressBooks(AccessToken token, String query, int limit, Integer offset) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("query", query);
		params.put("limit", String.valueOf(limit));
		if( offset != null ) {
			params.put("offset", String.valueOf(offset));
		}
		Document doc = execute(token, "/book/searchContactsInSynchronizedAddressBooks", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListContact(doc.getDocumentElement());
	}
	
	@Override
	protected Locator getLocator() {
		return Locator;
	}
	
}
