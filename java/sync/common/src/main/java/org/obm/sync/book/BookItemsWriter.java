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
package org.obm.sync.book;

import java.util.HashSet;
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

	public void appendContact(Element root, Contact contact) {
		Element c = root;
		if (!"contact".equals(root.getNodeName())) {
			c = DOMUtils.createElement(root, "contact");
		}

		if (contact.getUid() != null) {
			c.setAttribute("uid", "" + contact.getUid());
		}

		c.setAttribute("collected", "" + contact.isCollected());

		createIfNotNull(c, "commonname", contact.getCommonname());
		createIfNotNull(c, "first", contact.getFirstname());
		createIfNotNull(c, "last", contact.getLastname());
		createIfNotNull(c, "service", contact.getService());
		createIfNotNull(c, "title", contact.getTitle());
		createIfNotNull(c, "aka", contact.getAka());
		createIfNotNull(c, "comment", contact.getComment());
		createIfNotNull(c, "company", contact.getCompany());

		createIfNotNull(c, "middlename", contact.getMiddlename());
		createIfNotNull(c, "suffix", contact.getSuffix());
		createIfNotNull(c, "manager", contact.getManager());
		createIfNotNull(c, "assistant", contact.getAssistant());
		createIfNotNull(c, "spouse", contact.getSpouse());
		if(contact.getFolderId() != null){
			createIfNotNull(c, "addressbookid", String.valueOf(contact.getFolderId()));
		}
		
		String bday = null;
		if (contact.getBirthday() != null) {
			bday = DateHelper.asString(contact.getBirthday());
		}
		createIfNotNull(c, "birthday", bday);

		String anni = null;
		if (contact.getAnniversary() != null) {
			anni = DateHelper.asString(contact.getAnniversary());
		}
		createIfNotNull(c, "anniversary", anni);
		createIfNotNull(c, "caluri", contact.getCalUri());
		
		addPhones(c, contact.getPhones());
		addAddress(c, contact.getAddresses());
		addWebsite(c, contact.getWebsites());
		addEmail(c, contact.getEmails());
		addIM(c, contact.getImIdentifiers());
	}

	public void appendAddressBook(Element root, AddressBook book) {
		Element c = root;
		if (!"book".equals(root.getNodeName())) {
			c = DOMUtils.createElement(root, "book");
		}

		c.setAttribute("uid", Integer.toString(book.getUid().getId()));
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

	public Document writeChanges(ContactChanges contactChanges) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact-changes.xsd",
					"contact-changes");
			Element root = doc.getDocumentElement();
			root.setAttribute("lastSync", DateHelper.asString(contactChanges.getLastSync()));

			createContactChanges(contactChanges, root);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return doc;
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

	public String getContactAsString(Contact contact) {
		String out = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact.xsd", "contact");
			Element root = doc.getDocumentElement();
			appendContact(root, contact);
			out = DOMUtils.serialize(doc);
		} catch (TransformerException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return out;
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

	public void appendFolder(Element root, Folder folder) {
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

	public Document writeAddressBookChanges(AddressBookChangesResponse response) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/folder-changes.xsd",
					"addressbook-changes");
			Element root = doc.getDocumentElement();
			root.setAttribute("lastSync", DateHelper.asString(response.getLastSync()));

			Element addressbooks = DOMUtils.createElement(root, "addressbooks");
			createFolderChanges(response.getBooksChanges(), addressbooks);
			Element contacts = DOMUtils.createElement(root, "contacts");
			createContactChanges(response.getContactChanges(), contacts);

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return doc;
	}

	public Document writeListAddressBooksChanged(FolderChanges folderChanges) {
		Document doc = null;
		doc = DOMUtils.createDoc("http://www.obm.org/xsd/sync/folder-changes.xsd", "folder-changes");
		Element root = doc.getDocumentElement();
		root.setAttribute("lastSync", DateHelper.asString(folderChanges.getLastSync()));
		createFolderChanges(folderChanges, root);
		return doc;
	}

	public void appendCountContacts(Element root, int count) {
		createIfNotNull(root, "count", count);
	}

}
