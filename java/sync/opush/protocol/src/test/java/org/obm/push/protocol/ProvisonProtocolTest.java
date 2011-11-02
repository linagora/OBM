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
package org.obm.push.protocol;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.ProvisionStatus;
import org.obm.push.exception.InvalidPolicyKeyException;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProvisonProtocolTest {
	
	private static final long _3378841480L = 3378841480L;
	private static final String MS_EAS_PROVISIONING_WBXML = "MS-EAS-Provisioning-WBXML";
	private ProvisionProtocol provisionProtocol;
	
	@Before
	public void init() {
		provisionProtocol = new ProvisionProtocol();
	}
	
	@Test
	public void parseRequest() {
		Document document = buildRequestObjectTypeDocument(String.valueOf(_3378841480L));
		try {
			provisionProtocol.getRequest(document);
			Assert.assertTrue(true);
		} catch (InvalidPolicyKeyException e) {
			Assert.assertTrue(false);
		}
	}

	@Test(expected=InvalidPolicyKeyException.class)
	public void parseRequestWithWrongPolicyKey() throws InvalidPolicyKeyException {
		provisionProtocol.getRequest( buildRequestObjectTypeDocument("3378841480ZZD") );
	}
	
	private Document buildRequestObjectTypeDocument(String policyKey) throws FactoryConfigurationError {
		Document document = DOMUtils.createDoc(null, "Provision");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", ProvisionStatus.SUCCESS.asXmlValue());
		Element policies = DOMUtils.createElement(root, "Policies");
		Element policyNode = DOMUtils.createElement(policies, "Policy");
		DOMUtils.createElementAndText(policyNode, "PolicyType", MS_EAS_PROVISIONING_WBXML);
		DOMUtils.createElementAndText(policyNode, "Status", ProvisionStatus.SUCCESS.asXmlValue());
		DOMUtils.createElementAndText(policyNode, "PolicyKey", policyKey);
		return document;
	}	
	
}
