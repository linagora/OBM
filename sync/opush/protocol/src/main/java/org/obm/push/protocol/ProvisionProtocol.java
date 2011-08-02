package org.obm.push.protocol;

import javax.xml.parsers.FactoryConfigurationError;
import org.obm.push.exception.InvalidPolicyKeyException;
import org.obm.push.protocol.bean.ProvisionRequest;
import org.obm.push.protocol.bean.ProvisionResponse;
import org.obm.push.protocol.provisioning.Policy;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProvisionProtocol {

	public ProvisionRequest getRequest(Document doc) throws InvalidPolicyKeyException {
		String policyType = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"PolicyType").getTextContent();
		Element pKeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"PolicyKey");
		int policyKey = 0;
		if (pKeyElem != null) {
			try {
				policyKey = Integer.valueOf(pKeyElem.getTextContent());
			} catch (NumberFormatException e) {
				throw new InvalidPolicyKeyException(e);
			}
		}
		return new ProvisionRequest(policyType, policyKey);
	}

	public Document encodeResponse(ProvisionResponse provisionResponse)
			throws FactoryConfigurationError {
		Document ret = DOMUtils.createDoc(null, "Provision");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", "1");
		Element policies = DOMUtils.createElement(root, "Policies");
		Element policyNode = DOMUtils.createElement(policies, "Policy");
		DOMUtils.createElementAndText(policyNode, "PolicyType", provisionResponse.getPolicyType());
		DOMUtils.createElementAndText(policyNode, "Status", String.valueOf(provisionResponse.getStatus()));
		
		String policyKey = String.valueOf(provisionResponse.getPolicyKey());
		if (policyKey != null) {
			DOMUtils.createElementAndText(policyNode, "PolicyKey", policyKey);
		}
		
		Policy policy = provisionResponse.getPolicy();
		if (policy != null) {
			Element data = DOMUtils.createElement(policyNode, "Data");
			policy.serialize(data);
		}
		return ret;
	}
	
}
