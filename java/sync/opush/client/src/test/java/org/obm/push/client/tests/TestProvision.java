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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Ignore("It's necessary to do again all tests")
public class TestProvision extends AbstractPushTest {

	public void testSettingsProvisionProtocol121() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("Provision", doc, "Provision");
		assertNotNull(ret);
	}
	
	public void testSettingsProvisionProtocol120() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml120("Provision", doc, "Provision");
		assertNotNull(ret);
	}

	public void testSettingsProvisionProtocol25() throws Exception {
		optionsQuery();

		InputStream in = loadDataFile("ProvisionRequest1Protocol2.5.xml");
		Document doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		Document ret = postXml("Provision", doc, "Provision", "0", "2.5", false);
		assertNotNull(ret);
		
		String policyKey = DOMUtils.getElementText(ret.getDocumentElement(), "PolicyKey");

		in = loadDataFile("ProvisionRequest2Protocol2.5.xml");
		doc = DOMUtils.parse(in);
		Element elemPolicy = DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyKey");
		elemPolicy.setTextContent(policyKey);
		DOMUtils.logDom(doc);
		ret = postXml("Provision", doc, "Provision", "0", "2.5", false);
		
		policyKey = DOMUtils.getElementText(ret.getDocumentElement(), "PolicyKey");

		in = loadDataFile("ProvisionRequest2Protocol2.5.xml");
		doc = DOMUtils.parse(in);
		elemPolicy = DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyKey");
		elemPolicy.setTextContent(policyKey);
		DOMUtils.logDom(doc);
		ret = postXml("Provision", doc, "Provision", "0", "2.5", false);
		Element policy = DOMUtils.getUniqueElement(ret.getDocumentElement(), "Policy");
		String status = DOMUtils.getElementText(policy, "Status");
		assertEquals("5", status);
		
	}
}
