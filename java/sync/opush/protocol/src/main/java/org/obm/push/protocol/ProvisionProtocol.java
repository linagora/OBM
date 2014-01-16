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
package org.obm.push.protocol;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.Policy;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.ProvisionPolicyStatus;
import org.obm.push.bean.ProvisionStatus;
import org.obm.push.exception.InvalidPolicyKeyException;
import org.obm.push.protocol.bean.ProvisionRequest;
import org.obm.push.protocol.bean.ProvisionResponse;
import org.obm.push.protocol.provisioning.MSEAS12Dot0PolicyProtocol;
import org.obm.push.protocol.provisioning.MSEAS12Dot1PolicyProtocol;
import org.obm.push.protocol.provisioning.PolicyDecoder;
import org.obm.push.protocol.provisioning.PolicyProtocol;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Singleton;

public class ProvisionProtocol implements ActiveSyncProtocol<ProvisionRequest, ProvisionResponse> {

	@Singleton
	public static class Factory {
		
		public ProvisionProtocol createProtocol(ProtocolVersion protocolVersion) {
			switch (protocolVersion) {
			case V120:
				return new ProvisionProtocol(new MSEAS12Dot0PolicyProtocol());
			case V121:
				return new ProvisionProtocol(new MSEAS12Dot1PolicyProtocol());
			}
			throw new IllegalArgumentException();
		}
	}
	
	private final PolicyProtocol policyProtocol;

	private ProvisionProtocol(PolicyProtocol policyProtocol) {
		this.policyProtocol = policyProtocol;
	}
	
	@Override
	public ProvisionRequest decodeRequest(Document doc) throws InvalidPolicyKeyException {
		String policyType = DOMUtils.getUniqueElement(doc.getDocumentElement(),	"PolicyType").getTextContent();
		Element pKeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(), "PolicyKey");
		Long policyKey = getPolicyKey(pKeyElem);
		return ProvisionRequest.builder()
					.policyType(policyType)
					.policyKey(policyKey)
					.build();
	}

	private Long getPolicyKey(Element pKeyElem) throws InvalidPolicyKeyException {
		try {
			return DOMUtils.getElementLong(pKeyElem);
		} catch (NumberFormatException e) {
			throw new InvalidPolicyKeyException(e);
		}
	}

	@Override
	public ProvisionResponse decodeResponse(Document doc) {
		Element pe = doc.getDocumentElement();
		
		String generalStatus = DOMUtils.getElementText(pe, "Status");
		ProvisionStatus status = ProvisionStatus.fromSpecificationValue(Integer.valueOf(generalStatus));
		Element policies = DOMUtils.getUniqueElement(pe, "Policies");
		Element policyElement = DOMUtils.getUniqueElement(policies, "Policy");
		
		Element pte = DOMUtils.getUniqueElement(policyElement, "PolicyType");
		String policyType = pte.getTextContent();
		
		String substatus = DOMUtils.getElementText(policyElement, "Status");
		ProvisionPolicyStatus policyStatus = ProvisionPolicyStatus.fromSpecificationValue(Integer.valueOf(substatus));
		
		Long policyKey = null;
		Element pke = DOMUtils.getUniqueElement(policyElement, "PolicyKey");
		if (pke != null) {
			policyKey = Long.valueOf(pke.getTextContent());
		}
		
		Policy policy = null;
		Element datae = DOMUtils.getUniqueElement(policyElement, "Data");
		if (datae != null) {
			policy = PolicyDecoder.decode();
		}
		
		return ProvisionResponse.builder()
					.policyType(policyType)
					.policyKey(policyKey)
					.policy(policy)
					.policyStatus(policyStatus)
					.status(status)
					.build();
	}

	@Override
	public Document encodeResponse(ProvisionResponse provisionResponse) throws FactoryConfigurationError {
		Document ret = DOMUtils.createDoc(null, "Provision");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", provisionResponse.getStatus().getSpecificationValue());
		Element policies = DOMUtils.createElement(root, "Policies");
		Element policyNode = DOMUtils.createElement(policies, "Policy");
		DOMUtils.createElementAndText(policyNode, "PolicyType", provisionResponse.getPolicyType());
		DOMUtils.createElementAndText(policyNode, "Status", provisionResponse.getPolicyStatus().getSpecificationValue());
		
		Long policyKey = provisionResponse.getPolicyKey();
		if (policyKey != null) {
			DOMUtils.createElementAndText(policyNode, "PolicyKey", String.valueOf(policyKey));
		}
		
		Policy policy = provisionResponse.getPolicy();
		if (policy != null) {
			Element data = DOMUtils.createElement(policyNode, "Data");
			policyProtocol.appendPolicy(data, policy);
		}
		return ret;
	}
	
	public Document encodeErrorResponse(ProvisionStatus errorStatus) {
		Document document = DOMUtils.createDoc(null, "Provision");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", errorStatus.getSpecificationValue());
		return document;
	}

	@Override
	public Document encodeRequest(ProvisionRequest provisionRequest) {
		Document document = DOMUtils.createDoc(null, "Provision");
		Element root = document.getDocumentElement();
		
		Element policies = DOMUtils.createElement(root, "Policies");
		Element policy = DOMUtils.createElement(policies, "Policy");
		
		DOMUtils.createElementAndText(policy, "PolicyType", provisionRequest.getPolicyType());
		DOMUtils.createElementAndText(policy, "PolicyKey", String.valueOf(provisionRequest.getPolicyKey()));
		
		return document;
	}
}
