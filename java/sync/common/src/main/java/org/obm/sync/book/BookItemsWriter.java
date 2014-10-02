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
package org.obm.sync.book;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.TransformerException;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.items.AddressBookChangesResponse;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serializes address book items to XML
 */
public class BookItemsWriter extends AbstractItemsWriter {

	public Document getXMLDocumentFromAddressBooks(List<AddressBook> addressbooks) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/books.xsd", "books");
		Element root = doc.getDocumentElement();
		for (AddressBook book: addressbooks) {
			appendAddressBook(root, book);
		}
		return doc;
	}

	public Document getXMLDocumentFromContacts(List<Contact> contacts) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/contact.xsd", "contacts");
		Element root = doc.getDocumentElement();
		for (Contact contact : contacts) {
			appendContact(root, contact);
		}
		return doc;
	}

	public Document getXMLDocumentFrom(AddressBookChangesResponse response) {
		Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/folder-changes.xsd", "addressbook-changes");
		Element root = doc.getDocumentElement();
		root.setAttribute("lastSync", DateHelper.asString(response.getLastSync()));

		Element addressbooks = DOMUtils.createElement(root, "addressbooks");
		createFolderChanges(response.getBooksChanges(), addressbooks);
		Element contacts = DOMUtils.createElement(root, "contacts");
		createContactChanges(response.getContactChanges(), contacts);

		return doc;
	}

	public Document getXMLDocumentFrom(Folder folder) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/contact.xsd", "folder");
		Element root = doc.getDocumentElement();
		appendFolder(root, folder);
		return doc;
	}

	public Document getXMLDocumentFrom(FolderChanges folderChanges) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/folder-changes.xsd", "folder-changes");
		Element root = doc.getDocumentElement();
		root.setAttribute("lastSync", DateHelper.asString(folderChanges.getLastSync()));
		createFolderChanges(folderChanges, root);
		return doc;
	}

	public Document getXMLDocumentFrom(int count) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/addressbookcount.xsd", "addressbook-count");
		Element root = doc.getDocumentElement();
		appendCountContacts(root, count);
		return doc;
	}

	public Document getXMLDocumentFrom(Contact contact) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/contact.xsd", "contact");
		Element root = doc.getDocumentElement();
		appendContact(root, contact);
		return doc;
	}

	public Document getXMLDocumentFrom(ContactChanges contactChanges) {
		Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact-changes.xsd", "contact-changes");
		Element root = doc.getDocumentElement();
		root.setAttribute("lastSync", DateHelper.asString(contactChanges.getLastSync()));

		createContactChanges(contactChanges, root);

		return doc;
	}

	public String getContactAsString(Contact contact) {
		try {
			return DOMUtils.serialize(getXMLDocumentFrom(contact));
		} catch (TransformerException e) {
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	private void appendContact(Element parent, Contact contact) {
		if (!"contact".equals(parent.getNodeName())) {
			parent = DOMUtils.createElement(parent, "contact");
		}

		if (contact.getUid() != null) {
			parent.setAttribute("uid", "" + contact.getUid());
		}

		parent.setAttribute("collected", "" + contact.isCollected());

		createIfNotNull(parent, "hash", contact.hashCode());
		createIfNotNull(parent, "commonname", contact.getCommonname());
		createIfNotNull(parent, "first", contact.getFirstname());
		createIfNotNull(parent, "last", contact.getLastname());
		createIfNotNull(parent, "service", contact.getService());
		createIfNotNull(parent, "title", contact.getTitle());
		createIfNotNull(parent, "aka", contact.getAka());
		createIfNotNull(parent, "comment", contact.getComment());
		createIfNotNull(parent, "company", contact.getCompany());

		createIfNotNull(parent, "middlename", contact.getMiddlename());
		createIfNotNull(parent, "suffix", contact.getSuffix());
		createIfNotNull(parent, "manager", contact.getManager());
		createIfNotNull(parent, "assistant", contact.getAssistant());
		createIfNotNull(parent, "spouse", contact.getSpouse());
		if(contact.getFolderId() != null){
			createIfNotNull(parent, "addressbookid", String.valueOf(contact.getFolderId()));
		}
		
		String bday = null;
		if (contact.getBirthday() != null) {
			bday = DateHelper.asString(contact.getBirthday());
		}
		createIfNotNull(parent, "birthday", bday);

		String anni = null;
		if (contact.getAnniversary() != null) {
			anni = DateHelper.asString(contact.getAnniversary());
		}
		createIfNotNull(parent, "anniversary", anni);
		createIfNotNull(parent, "caluri", contact.getCalUri());
		
		addPhones(parent, contact.getPhones());
		addAddress(parent, contact.getAddresses());
		addWebsite(parent, contact.getWebsites());
		addEmail(parent, contact.getEmails());
		addIM(parent, contact.getImIdentifiers());
	}

	private void appendAddressBook(Element root, AddressBook book) {
		Element c = root;
		if (!"book".equals(root.getNodeName())) {
			c = DOMUtils.createElement(root, "book");
		}

		c.setAttribute("uid", Integer.toString(book.getUid()));
		c.setAttribute("name", book.getName());
		c.setAttribute("readonly", String.valueOf(book.isReadOnly()));
	}

	
	private void addIM(Element root,
			Map<String, InstantMessagingId> imIdentifiers) {
		Element e = DOMUtils.createElement(root, "instantmessaging");
		for (Entry<String, InstantMessagingId> entry: imIdentifiers.entrySet()) {
			Element c = DOMUtils.createElement(e, "im");
			InstantMessagingId p = entry.getValue();
			c.setAttribute("label", entry.getKey());
			c.setAttribute("protocol", p.getProtocol());
			c.setAttribute("address", p.getId());
		}
	}

	private void addEmail(Element root, Map<String, EmailAddress> emails) {
		Element e = DOMUtils.createElement(root, "emails");
		for (Entry<String, EmailAddress> entry: emails.entrySet()) {
			Element c = DOMUtils.createElement(e, "mail");
			EmailAddress p = entry.getValue();
			c.setAttribute("label", entry.getKey());
			c.setAttribute("value", p.get());
		}
	}

	private void addWebsite(final Element root, final HashSet<Website> websites) {
		final Element e = DOMUtils.createElement(root, "websites");
		for (final Website website: websites) {
			Element c = DOMUtils.createElement(e, "site");
			c.setAttribute("label", website.getLabel());
			c.setAttribute("url", website.getUrl());
		}
	}

	private void addAddress(Element root, Map<String, Address> addresses) {
		Element e = DOMUtils.createElement(root, "addresses");
		for (Entry<String, Address> entry: addresses.entrySet()) {
			Element c = DOMUtils.createElement(e, "address");
			Address p = entry.getValue();
			c.setAttribute("label", entry.getKey());
			c.setAttribute("zip", p.getZipCode());
			c.setAttribute("town", p.getTown());
			c.setAttribute("country", p.getCountry());
			c.setAttribute("expressPostal", p.getExpressPostal());
			c.setAttribute("state", p.getState());
			c.setTextContent(p.getStreet());
		}
	}

	private void addPhones(Element root, Map<String, Phone> phones) {
		Element e = DOMUtils.createElement(root, "phones");
		for (Entry<String, Phone> entry: phones.entrySet()) {
			Element c = DOMUtils.createElement(e, "phone");
			Phone p = entry.getValue();
			c.setAttribute("label", entry.getKey());
			c.setAttribute("number", p.getNumber());
		}
	}

	private void createContactChanges(ContactChanges cc, Element root) {
		
		Element removed = DOMUtils.createElement(root, "removed");
		for (int eid : cc.getRemoved()) {
			Element e = DOMUtils.createElement(removed, "contact");
			e.setAttribute("uid", "" + eid);
		}

		Element updated = DOMUtils.createElement(root, "updated");
		for (Contact ev : cc.getUpdated()) {
			appendContact(updated, ev);
		}
	}

	private void createFolderChanges(FolderChanges fc, Element root) {
		Element removed = DOMUtils.createElement(root, "removed");
		for (Folder ev : fc.getRemoved()) {
			appendFolder(removed, ev);
		}

		Element updated = DOMUtils.createElement(root, "updated");
		for (Folder ev : fc.getUpdated()) {
			appendFolder(updated, ev);
		}
	}

	private void appendFolder(Element root, Folder folder) {
		Element f = root;
		if (!"folder".equals(root.getNodeName())) {
			f = DOMUtils.createElement(root, "folder");
		}

		if (folder.getUid() != null) {
			f.setAttribute("uid", "" + folder.getUid());
		}

		createIfNotNull(f, "name", folder.getName());
		createIfNotNull(f, "ownerDisplayName", folder.getOwnerDisplayName());
		createIfNotNull(f, "ownerLoginAtDomain", folder.getOwnerLoginAtDomain());
	}

	private void appendCountContacts(Element root, int count) {
		createIfNotNull(root, "count", count);
	}

}
