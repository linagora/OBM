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

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.obm.push.client.tests.SyncKeyUtils.fillSyncKey;
import static org.obm.push.client.tests.SyncKeyUtils.processCollection;
@Ignore
public class TestGetItemEstimate extends AbstractPushTest {

	public void testGetItemEstimate() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("FullSyncRequest.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);
		Map<String, String> sks = processCollection(ret.getDocumentElement());
		
		in = loadDataFile("GetItemEstimateRequest.xml");
		doc = DOMUtils.parse(in);
		fillSyncKey(doc.getDocumentElement(), sks);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	}

	public void testGetItemEstimateBadCollectionId() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("FullSyncRequest.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		Map<String, String> sks = processCollection(ret.getDocumentElement());
		in = loadDataFile("GetItemEstimateRequestErrorBadCollectionId.xml");
		doc = DOMUtils.parse(in);
		NodeList nl = doc.getDocumentElement().getElementsByTagName(
				"Collection");
		Iterator<String> vals = sks.values().iterator();
		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			if (vals.hasNext()) {
				String syncKey = vals.next();
				Element synckeyElem = DOMUtils.getUniqueElement(col, "SyncKey");
				if (synckeyElem == null) {
					synckeyElem = DOMUtils.getUniqueElement(col,
							"AirSync:SyncKey");
				}
				synckeyElem.setTextContent(syncKey);
			}
		}

		DOMUtils.logDom(doc);
		ret = postXml25("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml120("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	}

	public void testGetItemEstimateBadSyncKey() throws Exception {
		InputStream in = loadDataFile("GetItemEstimateRequestBadSyncKey.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml120("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml25("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

	}
}
