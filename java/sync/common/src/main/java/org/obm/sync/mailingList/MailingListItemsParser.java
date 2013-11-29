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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.items.AbstractItemsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;

public class MailingListItemsParser extends AbstractItemsParser {

	private static final Logger logger = LoggerFactory
			.getLogger(MailingListItemsParser.class);

	public MailingList parseMailingList(String parameter) throws SAXException,
			IOException, FactoryConfigurationError {
		Document doc = DOMUtils.parse(new ByteArrayInputStream(parameter
				.getBytes(Charsets.UTF_8)));

		Element root = doc.getDocumentElement();
		return parseMailingList(root);
	}

	public List<MailingList> parseListMailingList(Document doc) {
		Element documentElement = doc.getDocumentElement();
		List<MailingList> ret = new LinkedList<MailingList>();
		NodeList mlsNodeList = documentElement
				.getElementsByTagName("mailingList");
		for (int i = 0; i < mlsNodeList.getLength(); i++) {
			Element e = (Element) mlsNodeList.item(i);
			MailingList ml = parseMailingList(e);
			ret.add(ml);
		}
		return ret;
	}

	public MailingList parseMailingList(Element root) {
		Element mlEle = root;
		if (!mlEle.getTagName().equalsIgnoreCase("mailingList")) {
			mlEle = DOMUtils.getUniqueElement(root, "mailingList");
		}
		MailingList ml = new MailingList();
		String id = mlEle.getAttribute("id");
		if (id != null && id.length() > 0) {
			try {
				ml.setId(Integer.valueOf(id));
			} catch (NumberFormatException e) {
				//DO NOTHING
			}
		}
		ml.setName(mlEle.getAttribute("name"));
		ml.addEmails(parseEmails(mlEle));
		return ml;
	}

	private List<MLEmail> parseEmails(Element uniqueElement) {
		String[] attrs = { "id", "label", "address" };
		String[][] values = DOMUtils.getAttributes(uniqueElement, "email",
				attrs);
		List<MLEmail> ret = new ArrayList<MLEmail>(values.length);
		for (String[] p : values) {
			MLEmail e = new MLEmail(p[1], p[2]);
			String id = p[0];
			if (id != null && id.length() > 0) {
				try {
					e.setId(Integer.valueOf(id));
				} catch (NumberFormatException x) {
					//DO NOTHING
				}
			}
			ret.add(e);
		}
		return ret;
	}

	public List<MLEmail> parseMailingListEmails(String mailingListEmails) {
		try {
			Document doc = DOMUtils.parse(new ByteArrayInputStream(
					mailingListEmails.getBytes(Charsets.UTF_8)));
			return parseEmails(doc.getDocumentElement());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return new ArrayList<MLEmail>(0);
	}

	public List<MLEmail> parseMailingListEmails(Document doc) {
		return parseEmails(doc.getDocumentElement());
	}
}
