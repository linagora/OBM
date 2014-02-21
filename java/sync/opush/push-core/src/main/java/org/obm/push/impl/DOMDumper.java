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
package org.obm.push.impl;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.TransformerException;

import org.obm.push.configuration.LoggerModule;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.DOMUtils.XMLVersion;
import org.obm.push.utils.stream.UTF8Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class DOMDumper {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Logger trimREQlogger;
	private final Logger fullREQLogger;

	@Inject
	private  DOMDumper(@Named(LoggerModule.TRIMMED_REQUEST)Logger trimRequestlogger, 
			@Named(LoggerModule.FULL_REQUEST)Logger fullLogger) {
		
		this.trimREQlogger = trimRequestlogger;
		this.fullREQLogger = fullLogger;
	}
	
	/**
	 * Seeing email/cal/contact data is a security issue for some
	 * administrators. Remove data from a copy of the DOM before printing.
	 * 
	 * @param doc
	 */
	public void dumpXml(Document doc) {
		dumpXml(doc, XMLVersion.XML_10);
	}
	
	public void dumpXml(Document doc, XMLVersion xmlVersion) {
		try {
			fullRequestLogging(DOMUtils.cloneDOM(doc), xmlVersion);
			trimRequestLogging(DOMUtils.cloneDOM(doc), xmlVersion);
		} catch (TransformerException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void fullRequestLogging(Document doc, XMLVersion xmlVersion) throws TransformerException {
		if (fullREQLogger.isInfoEnabled()) {
			log(fullREQLogger, doc, xmlVersion);
		}
	}

	private void trimRequestLogging(Document doc, XMLVersion xmlVersion) throws TransformerException {
		if (trimREQlogger.isInfoEnabled()) {
			NodeList nl = doc.getElementsByTagName("ApplicationData");
			for (int i = 0; i < nl.getLength(); i++) {
				Node e = nl.item(i);
				NodeList children = e.getChildNodes();
				for (int j = 0; j < children.getLength(); j++) {
					Node child = children.item(j);
					e.removeChild(child);
				}
				e.setTextContent("[trimmed_output]");
			}
			log(trimREQlogger, doc, xmlVersion);
		}
	}
	
	private void log(Logger logger, Document doc, XMLVersion xmlVersion) throws TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DOMUtils.serialize(doc, out, true, xmlVersion);
		logger.info(UTF8Utils.asString(out));
	}
	
}
