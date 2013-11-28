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
package fr.aliasource.obm.autoconf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;

import fr.aliasource.obm.utils.ConstantService;
import fr.aliasource.obm.utils.DOMUtils;

public class TemplateLoader {
	private static final Logger logger = LoggerFactory.getLogger(TemplateLoader.class);

	private final String configXml;
	private final ConstantService constants;

	public TemplateLoader(String configXml, ConstantService constants) {
		this.configXml = configXml;
		this.constants = constants;
	}

	public Document applyTemplate(LDAPAttributeSet attributeSet,
			HashMap<String, String> servicesHostNames) throws IOException {
		try {
			Document doc = DOMUtils.parse(new FileInputStream(configXml));
			generateXMLConfig(doc, attributeSet, servicesHostNames);
			return doc;
		} catch (Exception e) {
			logger.error("Invalid config.xml document", e);
			throw new IOException("Invalid config.xml document");
		}
	}

	private void generateXMLConfig(Document doc, LDAPAttributeSet attributeSet,
			HashMap<String, String> servicesHostNames) {

		Element root = doc.getDocumentElement();
		replaceInNode(attributeSet, servicesHostNames, root);
	}

	private void replaceInNode(LDAPAttributeSet attributeSet,
			HashMap<String, String> servicesHostNames, Element root) {
		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				NamedNodeMap atts = e.getAttributes();
				for (int j = 0; j < atts.getLength(); j++) {
					Attr at = (Attr) atts.item(j);
					String oldVal = at.getValue();
					String val = doAttributeExpansion(attributeSet, servicesHostNames, oldVal);
					if (!oldVal.equals(val)) {
						e.setAttribute(at.getName(), val);
					}
				}
				replaceInNode(attributeSet, servicesHostNames, e);
			}
		}
	}
	
	/**
	 * @param root
	 * @return
	 */
	public boolean isValidTemplate(Element root) {
		NodeList nl = root.getChildNodes();
		boolean ret = testAttributes(true, root);
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				ret = testAttributes(ret, e) && isValidTemplate(e);
				if (!ret) {
					return ret;
				}
			}
		}
		return ret;
	}

	private boolean testAttributes(boolean ret, Element e) {
		NamedNodeMap atts = e.getAttributes();
		for (int j = 0; j < atts.getLength(); j++) {
			Attr at = (Attr) atts.item(j);
			String oldVal = at.getValue();
			ret = ret && !(oldVal.contains("${") && oldVal.contains("}"));
			if (!ret) {
				logger.warn("template validity check failed on attribute "+at.getName()+" with value "+oldVal);
			}
		}
		return ret;
	}

	private String replaceServiceHostName(String line, String serviceParam, String serviceHostName) {
		if (serviceHostName != null && !serviceHostName.equals("")) {
			line = line.replace(serviceParam, serviceHostName);
		}
		return line;
	}
	
	@SuppressWarnings("rawtypes")
	private String doAttributeExpansion(LDAPAttributeSet attributeSet,
			HashMap<String, String> servicesHostNames, String line) {
		if (attributeSet != null) {
			Iterator iterator = attributeSet.iterator();
			while (iterator.hasNext()) {
				LDAPAttribute att = (LDAPAttribute) iterator.next();
				line = line.replace("${" + att.getName() + "}", att
							.getStringValue());
			}
		}
		line = replaceServiceHostName(line, "${imapMailHost}", servicesHostNames.get("imap_frontend"));
		line = replaceServiceHostName(line, "${smtpMailHost}",  servicesHostNames.get("smtp_out"));
		line = replaceServiceHostName(line, "${ldapHost}", constants.getStringValue("ldapServer"));
		line = replaceServiceHostName(line, "${obmSyncHost}",  servicesHostNames.get("obm_sync"));

		for (Object key : constants.getKeySet()) {
			String k = (String) key;
			line = line.replace("${" + k + "}", constants.getStringValue(k));
		}
		return line;
	}
}
