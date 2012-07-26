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
package org.obm.sync.server.handler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.naming.NoPermissionException;
import javax.xml.parsers.FactoryConfigurationError;

import org.obm.configuration.ContactConfiguration;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookItemsParser;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.exception.ContactNotFoundException;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.server.Request;
import org.obm.sync.server.XmlResponder;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.utils.DateHelper;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.contact.AddressBookBindingImpl;
import fr.aliacom.obm.common.session.SessionManagement;

@Singleton
public class AddressBookHandler extends SecureSyncHandler {

	private final IAddressBook binding;
	private final BookItemsParser bip;
	private final ContactConfiguration contactConfiguration;

	@Inject
	private AddressBookHandler(SessionManagement sessionManagement, AddressBookBindingImpl addressBookBindingImpl, 
			ContactConfiguration contactConfiguration) {
		
		super(sessionManagement);
		this.binding = addressBookBindingImpl;
		this.contactConfiguration = contactConfiguration;
		this.bip = new BookItemsParser();
	}

	@Override
	public void handle(Request request, XmlResponder responder) throws Exception {
		
		AccessToken token = getCheckedToken(request);
		
		String method = request.getMethod();
		if ("isReadOnly".equals(method)) {
			isReadOnly(token, request, responder);
		} else if ("listBooks".equals(method)) {
			listBooks(token, responder);
		} else if ("listAllBooks".equals(method)) {
			listAllBooks(token, responder);
		} else if ("listAllChanges".equals(method)) {
			listContactsChanged(token, request, responder);
		} else if ("createContact".equals(method)) {
			createContact(token, request, responder);
		} else if ("createContactInBook".equals(method)) {
			createContact(token, request, responder);
		} else if ("modifyContact".equals(method)) {
			modifyContact(token, request, responder);
		} else if ("removeContact".equals(method)) {
			removeContact(token, request, responder);
		} else if ("getContactFromId".equals(method)) {
			getContactFromId(token, request, responder);
		} else if ("getContactTwinKeys".equals(method)) {
			getContactTwinKeys(token, request, responder);
		} else if ("searchContact".equals(method)) {
			searchContact(token, request, responder);
		} else if ("searchContactInGroup".equals(method)) {
			searchContactInGroup(token, request, responder);
		} else if ("countContactsInGroup".equals(method)) {
			countContactsInGroup(token, request, responder);
		} else if ("getAddressBookSync".equals(method)) {
			getAddressBookSync(token, request, responder);
		} else if ("unsubscribeBook".equals(method)) {
			unsubscribeBook(token, request, responder);
		} else if ("listAddressBooksChanged".equals(method)) {
			listAddressBooksChanged(token, request, responder);
		} else if ("searchContactsInSynchronizedAddressBooks".equals(method)) {
			searchContactsInSynchronizedAddressBooks(token, request, responder);
		} else {
			responder.sendError("Cannot handle method '" + method + "'");
		}
	}

	private void unsubscribeBook(AccessToken token, Request request,
			XmlResponder responder) throws ServerFault {
		boolean ret = binding.unsubscribeBook(token, getBookId(request));
		responder.sendBoolean(ret);
	}

	private void getContactTwinKeys(AccessToken at, Request request, XmlResponder responder) throws SAXException, IOException, FactoryConfigurationError {
		Contact contact = getContactFromParams(request);
		KeyList ret = binding.getContactTwinKeys(at, contact);
		responder.sendKeyList(ret);
	}

	private BookType type(Request request) {
		return BookType.valueOf(request.getParameter("book"));
	}

	private void getContactFromId(AccessToken at, Request request, XmlResponder responder) 
			throws ServerFault, ContactNotFoundException {
		
		Integer contactId = Integer.valueOf(p(request, "id") );
		Integer addressBookId = getBookId(request);
		Contact ret = binding.getContactFromId(at, addressBookId, contactId);
		responder.sendContact(ret);
	}

	private void removeContact(AccessToken at, Request request, XmlResponder responder) 
			throws ServerFault, ContactNotFoundException, NoPermissionException {
		
		Integer contactId = Integer.valueOf(p(request, "id"));
		Integer addressBookId = getBookId(request);
		Contact ret = binding.removeContact(at, addressBookId, contactId);
		responder.sendContact(ret);
	}

	private void modifyContact(AccessToken at, Request request, XmlResponder responder) 
			throws NoPermissionException, ServerFault, ContactNotFoundException {
		
		try {
			Integer addressBookId = getBookId(request);
			Contact contact = getContactFromParams(request);
			Contact ret = binding.modifyContact(at, addressBookId, contact);
			responder.sendContact(ret);
		} catch (SAXException e) {
			throw new ServerFault(e);
		} catch (IOException e) {
			throw new ServerFault(e);
		} catch (FactoryConfigurationError e) {
			throw new ServerFault(e);
		}
	}

	private void createContact(AccessToken at, Request request, XmlResponder responder) 
			throws ServerFault, NoPermissionException {
		
		try {
			Integer addressBookId = getBookId(request);	
			Contact contact = getContactFromParams(request);
			Contact ret = binding.createContact(at, addressBookId, contact);
			responder.sendContact(ret);
		} catch (SAXException e) {
			throw new ServerFault(e);
		} catch (IOException e) {
			throw new ServerFault(e);
		} catch (FactoryConfigurationError e) {
			throw new ServerFault(e);
		}
	}
	
	private Contact getContactFromParams(Request request)
			throws SAXException, IOException, FactoryConfigurationError {
		return bip.parseContact(p(request, "contact"));
	}
	
    private void listContactsChanged(AccessToken at, Request request, XmlResponder responder) throws ServerFault {
		Date lastSync = getLastSyncFromRequest(request);
		String addressBookId = p(request, "bookId");
		ContactChanges contactChanges = null;
		if (addressBookId == null) {
			contactChanges = binding.listContactsChanged(at, lastSync);
		} else {
			contactChanges = binding.listContactsChanged(at, lastSync, Integer.valueOf(addressBookId));
		}
		responder.sendContactChanges(contactChanges);
	}

	private Date getLastSyncFromRequest(Request request) {
		return DateHelper.asDate(p(request, "lastSync"));
	}

	private void listBooks(AccessToken at, XmlResponder responder) throws ServerFault {
		BookType[] ret = binding.listBooks(at);
		String[] st = new String[ret.length];
		for (int i = 0; i < st.length; i++) {
			st[i] = ret[i].toString();
		}
		responder.sendArrayOfString(st);
	}

	private void listAllBooks(AccessToken at, XmlResponder responder) throws ServerFault {
		List<AddressBook> ret = binding.listAllBooks(at);
		responder.sendListAddressBooks(ret);
	}
	
	private void isReadOnly(AccessToken at, Request request, XmlResponder responder) throws ServerFault {
		boolean ret = binding.isReadOnly(at, type(request));
		responder.sendBoolean(ret);
	}

	private void searchContact(AccessToken at, Request request, XmlResponder responder) throws ServerFault {
		String query = p(request, "query");
		int limit = Integer.valueOf(p(request, "limit"));
		Integer offset = getOffset(request);
		List<Contact> ret = binding.searchContact(at, query, limit, offset);
		responder.sendListContact(ret);
	}

	private void searchContactInGroup(AccessToken at, Request request, XmlResponder responder) throws ServerFault {
		String query = p(request, "query");
		int limit = Integer.valueOf(p(request, "limit"));
		int groupId = Integer.valueOf(p(request, "group"));
		Integer offset = getOffset(request);
		AddressBook book = getAddressBookFromUid(at, groupId);
		List<Contact> ret = binding.searchContactInGroup(at, book, query, limit, offset);
		responder.sendListContact(ret);
	}

	private void countContactsInGroup(AccessToken at, Request request, XmlResponder responder) throws SQLException, ServerFault {
		int groupId = Integer.valueOf(p(request, "group"));
		AddressBook book = getAddressBookFromUid(at, groupId);
		int ret = binding.countContactsInGroup(at, book.getUid());
		responder.sendCountContacts(ret);
	}

	private AddressBook getAddressBookFromUid(AccessToken at, int uid) throws ServerFault {
		List<AddressBook> allBooks = binding.listAllBooks(at);
		for (AddressBook book: allBooks) {
			if (book.getUid() == uid) {
				return book;
			}
		}
		throw new RuntimeException("book with uid " + uid + " not found");
	}

	private Integer getBookId(Request request) {
		String bookId = p(request, "bookId");
		if (bookId == null) {
			if (isUsersBookType(request)) {
				return contactConfiguration.getAddressBookUserId();
			} else {
				return null;
			}
		}
		return Integer.valueOf(bookId);
	}

	private Boolean isUsersBookType(Request request) {
		BookType bookType = type(request);
		if (bookType == null) {
			return null;
		}
		if (bookType == BookType.users) {
			return true;
		} else {
			return false;
		}
	}
	
	private void getAddressBookSync(AccessToken token, Request request,
			XmlResponder responder) throws ServerFault {
		Date lastSync = getLastSyncFromRequest(request);
		AddressBookChangesResponse response = binding.getAddressBookSync(token, lastSync);
		responder.sendAddressBookChanges(response);	
	}
	
	private void listAddressBooksChanged(AccessToken token, Request request, XmlResponder responder) throws ServerFault {
		Date lastSync = getLastSyncFromRequest(request);
		FolderChanges folderChanges = binding.listAddressBooksChanged(token, lastSync);
		responder.sendlistAddressBooksChanged(folderChanges);	
	}
	
	private void searchContactsInSynchronizedAddressBooks(AccessToken token, Request request, 
			XmlResponder responder) throws ServerFault {
		
		String query = p(request, "query");
		int limit = Integer.valueOf(p(request, "limit"));
		Integer offset = getOffset(request);
		List<Contact> ret = binding.searchContactsInSynchronizedAddressBooks(token, query, limit, offset);
		responder.sendListContact(ret);
	}
	
	private Integer getOffset(Request request) {
		String offset = p(request, "offset");
		return Strings.isNullOrEmpty(offset) ? null : Integer.valueOf(offset);
	}

}
