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
package org.obm.sync.mailingList;

import java.util.List;

import javax.xml.transform.TransformerException;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.items.AbstractItemsWriter;
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
		String out = "";
		try {
			Document doc = getMailingListsAsXML(mailingList);
			out = DOMUtils.serialize(doc);
		} catch (TransformerException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return out;
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
		String out = "";
		try {
			Document doc = getMailingListEmailsAsXML(emails);
			out = DOMUtils.serialize(doc);
		} catch (TransformerException ex) {
			logger.error(ex.getMessage(), ex);
		}
		return out;
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
