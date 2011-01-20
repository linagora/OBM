package org.obm.sync.book;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.items.ContactChanges;
import org.obm.sync.items.FolderChanges;
import org.obm.sync.utils.DOMUtils;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serializes address book items to XML
 * 
 * @author tom
 * 
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

		c.setAttribute("uid", Integer.toString(book.getUid()));
		c.setAttribute("name", book.getName());
	}

	
	private void addIM(Element root,
			Map<String, InstantMessagingId> imIdentifiers) {
		Element e = DOMUtils.createElement(root, "instantmessaging");
		for (String s : imIdentifiers.keySet()) {
			Element c = DOMUtils.createElement(e, "im");
			InstantMessagingId p = imIdentifiers.get(s);
			c.setAttribute("label", s);
			c.setAttribute("protocol", p.getProtocol());
			c.setAttribute("address", p.getId());
		}
	}

	private void addEmail(Element root, Map<String, Email> emails) {
		Element e = DOMUtils.createElement(root, "emails");
		for (String s : emails.keySet()) {
			Element c = DOMUtils.createElement(e, "mail");
			Email p = emails.get(s);
			c.setAttribute("label", s);
			c.setAttribute("value", p.getEmail());
		}
	}

	private void addWebsite(Element root, Map<String, Website> websites) {
		Element e = DOMUtils.createElement(root, "websites");
		for (String s : websites.keySet()) {
			Element c = DOMUtils.createElement(e, "site");
			Website p = websites.get(s);
			c.setAttribute("label", s);
			c.setAttribute("url", p.getUrl());
		}
	}

	private void addAddress(Element root, Map<String, Address> addresses) {
		Element e = DOMUtils.createElement(root, "addresses");
		for (String s : addresses.keySet()) {
			Element c = DOMUtils.createElement(e, "address");
			Address p = addresses.get(s);
			c.setAttribute("label", s);
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
		for (String s : phones.keySet()) {
			Element c = DOMUtils.createElement(e, "phone");
			Phone p = phones.get(s);
			c.setAttribute("label", s);
			c.setAttribute("number", p.getNumber());
		}
	}

	public Document writeChanges(ContactChanges cc) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact-changes.xsd",
					"contact-changes");
			Element root = doc.getDocumentElement();
			root
					.setAttribute("lastSync", DateHelper.asString(cc
							.getLastSync()));

			Element removed = DOMUtils.createElement(root, "removed");
			for (int eid : cc.getRemoved()) {
				Element e = DOMUtils.createElement(removed, "contact");
				e.setAttribute("uid", "" + eid);
			}

			Element updated = DOMUtils.createElement(root, "updated");
			for (Contact ev : cc.getUpdated()) {
				appendContact(updated, ev);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return doc;
	}

	public String getContactAsString(Contact contact) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/contact.xsd", "contact");
			Element root = doc.getDocumentElement();
			appendContact(root, contact);
			DOMUtils.serialise(doc, out);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return out.toString();
	}

	public Document writeFolderChanges(FolderChanges fc) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/folder-changes.xsd",
					"folder-changes");
			Element root = doc.getDocumentElement();
			root
					.setAttribute("lastSync", DateHelper.asString(fc
							.getLastSync()));

			Element removed = DOMUtils.createElement(root, "removed");
			for (int eid : fc.getRemoved()) {
				Element e = DOMUtils.createElement(removed, "folder");
				e.setAttribute("uid", "" + eid);
			}

			Element updated = DOMUtils.createElement(root, "updated");
			for (Folder ev : fc.getUpdated()) {
				appendFolder(updated, ev);
			}

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}

		return doc;
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
	}

}
