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
