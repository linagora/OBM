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
package org.obm.sync.push.client.commands;

import javax.xml.transform.TransformerException;

import org.obm.push.bean.ProvisionPolicyStatus;
import org.obm.push.bean.ProvisionStatus;
import org.obm.sync.push.client.ProvisionResponse;
import org.obm.sync.push.client.ProvisionResponse.Builder;
import org.obm.sync.push.client.ResponseTransformer;
import org.obm.sync.push.client.beans.NS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.base.Throwables;

public abstract class Provision extends AbstractCommand<ProvisionResponse> {

	public Provision(TemplateDocument template) {
		super(NS.Provision, "Provision", template);
	}

	@Override
	protected ProvisionResponse parseResponse(Document root) throws TransformerException {
		
		
		Node statusNode = root.getDocumentElement().getFirstChild();
		Node policiesNode = statusNode.getNextSibling();
		Node policyNode = policiesNode.getFirstChild();
		Node policyTypeNode = policyNode.getFirstChild();
		Node policyStatusNode = policyTypeNode.getNextSibling();
		Node policyKeyNode = policyStatusNode.getNextSibling();
		
		Builder provisionResponseBuilder = ProvisionResponse.builder()
			.policyType(policyTypeNode.getTextContent())
			.policyKey(policyStatusValue(policyKeyNode))
			.provisionStatus(provisionStatus(statusNode.getTextContent()))
			.policyStatus(policyStatus(policyStatusNode.getTextContent())); 

		if (policyKeyNode != null) {
			provisionResponseBuilder.policyData((Element) policyKeyNode.getNextSibling());
		}
		
		return provisionResponseBuilder	.build();
	}

	private ProvisionPolicyStatus policyStatus(String textContent) {
		return ProvisionPolicyStatus.fromSpecificationValue(Integer.valueOf(textContent));
	}

	private ProvisionStatus provisionStatus(String textContent) {
		return ProvisionStatus.fromSpecificationValue(Integer.valueOf(textContent));
	}

	private Long policyStatusValue(Node policyKeyNode) {
		if (policyKeyNode != null) {
			return Long.valueOf(policyKeyNode.getTextContent());
		}
		return null;
	}

	@Override
	protected ResponseTransformer<ProvisionResponse> responseTransformer() {
		return new ProvisionResponseTransformer();
	}
	
	private class ProvisionResponseTransformer implements ResponseTransformer<ProvisionResponse> {

		@Override
		public ProvisionResponse parse(Document document) {
			try {
				return parseResponse(document);
			} catch (TransformerException e) {
				Throwables.propagate(e);
			}
			return null;
		}
	}
}
