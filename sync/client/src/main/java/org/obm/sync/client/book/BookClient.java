package org.obm.sync.client.book;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.KeyList;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.BookItemsParser;
import org.obm.sync.book.BookItemsWriter;
import org.obm.sync.book.BookType;
import org.obm.sync.book.Contact;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;

public class BookClient extends AbstractClientImpl implements IAddressBook {

	private BookItemsParser respParser;
	private BookItemsWriter biw;

	public BookClient(String obmSyncServicesUrl) {
		super(obmSyncServicesUrl);
		respParser = new BookItemsParser();
		biw = new BookItemsWriter();
	}

	@Override
	public Contact createContact(AccessToken token, BookType book,
			Contact contact) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		params.put("contact", biw.getContactAsString(contact));
		Document doc = execute("/book/createContact", params);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public Contact createContactWithoutDuplicate(AccessToken token,
			BookType book, Contact contact) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		params.put("contact", biw.getContactAsString(contact));
		Document doc = execute("/book/createContactWithoutDuplicate", params);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public Contact getContactFromId(AccessToken token, BookType book, String id)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		params.put("id", id);
		Document doc = execute("/book/getContactFromId", params);
		try {
			checkServerError(doc);
			return respParser.parseContact(doc.getDocumentElement());
		} catch (ServerFault se) {
			return null;
		}
	}

	@Override
	public KeyList getContactTwinKeys(AccessToken token, BookType book,
			Contact contact) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		params.put("contact", biw.getContactAsString(contact));
		Document doc = execute("/book/getContactTwinKeys", params);
		return respParser.parseKeyList(doc);
	}

	@Override
	public ContactChanges getSync(AccessToken token, BookType book,
			Date lastSync) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}

		Document doc = execute("/book/getSync", params);

		return respParser.parseChanges(doc);
	}

	@Override
	public boolean isReadOnly(AccessToken token, BookType book)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		Document doc = execute("/book/isReadOnly", params);
		return "true".equals(respParser.parseArrayOfString(doc)[0]);
	}

	@Override
	public BookType[] listBooks(AccessToken token) {
		Map<String, String> params = initParams(token);
		Document doc = execute("/book/listBooks", params);
		String[] sa = respParser.parseArrayOfString(doc);
		BookType[] bts = new BookType[sa.length];
		for (int i = 0; i < sa.length; i++) {
			bts[i] = BookType.valueOf(sa[i]);
		}
		return bts;
	}

	@Override
	public List<AddressBook> listAllBooks(AccessToken token) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		Document doc = execute("/book/listAllBooks", params);
		List<AddressBook> addressBooks = respParser.parseListAddressBook(doc);
		return addressBooks;
	}
	
	@Override
	public Contact modifyContact(AccessToken token, BookType book,
			Contact contact) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		String ct = biw.getContactAsString(contact);
		params.put("contact", ct);
		Document doc = execute("/book/modifyContact", params);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public Contact removeContact(AccessToken token, BookType book, String uid)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("book", book.toString());
		params.put("id", uid);
		Document doc = execute("/book/removeContact", params);
		return respParser.parseContact(doc.getDocumentElement());
	}

	@Override
	public List<Contact> searchContact(AccessToken token, String query,
			int limit) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("query", query);
		params.put("limit", "" + limit);
		Document doc = execute("/book/searchContact", params);
		return respParser.parseListContact(doc.getDocumentElement());
	}

	@Override
	public List<Contact> searchContactInGroup(AccessToken token, AddressBook group, String query,
			int limit) throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("query", query);
		params.put("limit", String.valueOf(limit));
		params.put("group", String.valueOf(group.getUid()));
		Document doc = execute("/book/searchContactInGroup", params);
		return respParser.parseListContact(doc.getDocumentElement());
	}
	
	@Override
	public FolderChanges getFolderSync(AccessToken token, Date lastSync)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		if (lastSync != null) {
			params.put("lastSync", DateHelper.asString(lastSync));
		} else {
			params.put("lastSync", "0");
		}

		Document doc = execute("/book/getFolderSync", params);

		return respParser.parseFolderChanges(doc);
	}
}
