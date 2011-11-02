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
package org.obm.push.client.tests;

import java.util.HashMap;
import java.util.Map;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SyncKeyUtils {
	
	public static String getFolderSyncKey(Document doc){
		return DOMUtils.getElementText(doc.getDocumentElement(), "SyncKey");
	}
	
	public static void appendFolderSyncKey(Document doc, String syncKey){
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "SyncKey").setTextContent(syncKey);
	}
	
	public static void fillSyncKey(Element root, Map<String, String> sks) {
		NodeList nl = root.getElementsByTagName("Collection");

		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			String collectionId = DOMUtils.getElementText(col, "CollectionId");
			String syncKey = sks.get(collectionId);
			Element synckeyElem = DOMUtils.getUniqueElement(col, "SyncKey");
			if (synckeyElem == null) {
				synckeyElem = DOMUtils.getUniqueElement(col, "AirSync:SyncKey");
			}
			synckeyElem.setTextContent(syncKey);
		}

	}

	public static Map<String, String> processCollection(Element root) {
		Map<String, String> ret = new HashMap<String, String>();
		NodeList nl = root.getElementsByTagName("Collection");

		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			String collectionId = DOMUtils.getElementText(col, "CollectionId");
			String syncKey = DOMUtils.getElementText(col, "SyncKey");
			ret.put(collectionId, syncKey);
		}
		return ret;
	}

}
