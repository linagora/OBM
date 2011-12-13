package org.obm.sync.client.book;

import java.util.Date;
import java.util.List;

import javax.naming.NoPermissionException;

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
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.locators.Locator;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.utils.DOMUtils;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BookClient extends AbstractClientImpl implements IAddressBook {

	private final BookItemsParser respParser;
	private final BookItemsWriter biw;
	private final Locator Locator;

	@Inject
	private BookClient(SyncClientException syncClientException, Locator Locator) {
		super(syncClientException);
		this.Locator = Locator;
		this.respParser = new BookItemsParser();
		this.biw = new BookItemsWriter();
	}

	@Override
	public Contact createContact(AccessToken token, Integer addressBookId, Contact contact) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("addressBookId", String.valueOf(addressBookId));
		params.put("contact", biw.getContactAsString(contact));
		Document doc = execute(token, "/book/createContact", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public Contact getContactFromId(AccessToken token, Integer addressBookId, Integer contactId) 
			throws ServerFault, ContactNotFoundException {
		
		Multimap<String, String> params = initParams(token);
		params.put("addressBookId", String.valueOf(addressBookId));
		params.put("contactId", String.valueOf(contactId));
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
		Document doc = execute(token, "/book/listContactsChanged", params);
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
	public Contact modifyContact(AccessToken token, Integer addressBookId, Contact contact) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("addressBookId", String.valueOf(addressBookId));
		String ct = biw.getContactAsString(contact);
		params.put("contact", ct);
		Document doc = execute(token, "/book/modifyContact", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public Contact removeContact(AccessToken token, Integer addressBookId , Integer contactId) 
			throws ServerFault, ContactNotFoundException, NoPermissionException {
		
		Multimap<String, String> params = initParams(token);
		params.put("addressBookId", String.valueOf(addressBookId));
		params.put("contactId", String.valueOf(contactId));
		Document doc = execute(token, "/book/removeContact", params);
		exceptionFactory.checkRemoveContactException(doc);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public List<Contact> searchContact(AccessToken token, String query, int limit) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("query", query);
		params.put("limit", "" + limit);
		Document doc = execute(token, "/book/searchContact", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListContact(doc.getDocumentElement());
	}

	@Override
	public List<Contact> searchContactInGroup(AccessToken token, AddressBook group, String query, int limit) throws ServerFault {
		Multimap<String, String> params = initParams(token);
		params.put("query", query);
		params.put("limit", String.valueOf(limit));
		params.put("group", String.valueOf(group.getUid()));
		Document doc = execute(token, "/book/searchContactInGroup", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseListContact(doc.getDocumentElement());
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
		params.put("addressBookId", String.valueOf(addressBookId));
		Document doc = execute(token, "/book/listContactsChanged", params);
		exceptionFactory.checkServerFaultException(doc);
		return respParser.parseChanges(doc);
	}

	@Override
	protected Locator getLocator() {
		return Locator;
	}
	
}