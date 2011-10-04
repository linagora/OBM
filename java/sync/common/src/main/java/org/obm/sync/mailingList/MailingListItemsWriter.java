package org.obm.sync.mailingList;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serializes address book items to XML
 * 
 * @author adrienp
 * 
 */
public class MailingListItemsWriter extends AbstractItemsWriter {

	public Document getMailingListsAsXML(MailingList... mailingLists) {
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/mailingLists.xsd",
					"mailingLists");
			Element root = doc.getDocumentElement();
			for (MailingList ml : mailingLists) {
				appendMailingList(root, ml);
			}
			return doc;
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return null;
	}

	public String getMailingListsAsString(MailingList... mailingList) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Document doc = getMailingListsAsXML(mailingList);
			DOMUtils.serialise(doc, out);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return out.toString();
	}

	public Document getMailingListEmailsAsXML(List<MLEmail> emails) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/mailingListEmails.xsd",
					"mailingListEmails");
			Element root = doc.getDocumentElement();
			addEmail(root, emails);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return doc;
	}

	public String getMailingListEmailsAsString(List<MLEmail> emails) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Document doc = getMailingListEmailsAsXML(emails);
			DOMUtils.serialise(doc, out);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return out.toString();
	}
	
	private void appendMailingList(Element root, MailingList mailingList) {
		Element c = root;
		if (!"mailingList".equals(root.getNodeName())) {
			c = DOMUtils.createElement(root, "mailingList");
		}
		if (mailingList.getId() != null) {
			c.setAttribute("id", "" + mailingList.getId());
		}
		c.setAttribute("name", "" + mailingList.getName());
		addEmail(c, mailingList.getEmails());
	}

	private void addEmail(Element root, List<MLEmail> emails) {
		Element mle = root;
		if (!"mailingListEmails".equals(root.getNodeName())) {
			mle = DOMUtils.createElement(root, "mailingListEmails");
		}
		for (MLEmail email : emails) {
			Element c = DOMUtils.createElement(mle, "email");
			if (email.getId() != null) {
				c.setAttribute("id", email.getId().toString());
			}
			c.setAttribute("label", email.getLabel());
			c.setAttribute("address", email.getAddress());
		}
	}

}
