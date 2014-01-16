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
package org.obm.push.handler;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.backend.IContinuation;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public abstract class XmlRequestHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(XmlRequestHandler.class);
	private final DOMDumper domDumper;

	protected XmlRequestHandler(DOMDumper domDumper) {
		this.domDumper = domDumper;
	}
	
	@Override
	public void process(IContinuation continuation, UserDataRequest udr,
			ActiveSyncRequest request, Responder responder) throws IOException {

		InputStream in = request.getInputStream();
		Document doc = null;
		if (in != null) {
			try {
				doc = DOMUtils.parse(in);
				domDumper.dumpXml(doc);
			} catch (SAXException e) {
				logger.error("Error parsing command xml data.", e);
			} catch (FactoryConfigurationError e) {
				logger.error("Error parsing command xml data.", e);
			}
		}
		process(udr, doc, responder);
	}

	protected abstract void process(UserDataRequest udr, Document doc, Responder responder);

}
