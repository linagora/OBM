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
package org.obm.sync.push.client.commands;

import org.obm.sync.push.client.ProvisionResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class Provision extends TemplateBasedCommand<ProvisionResponse> {

	public Provision(Document doc) {
		super(NS.Provision, "Provision", doc);
	}

	public Provision(String template) {
		super(NS.Provision, "Provision", template);
	}

	@Override
	protected ProvisionResponse parseResponse(Element root) {
		Node statusNode = root.getFirstChild();
		
		Node policiesNode = statusNode.getNextSibling();
		Node policyNode = policiesNode.getFirstChild();
		Node policyTypeNode = policyNode.getFirstChild();
		Node policyStatusNode = policyTypeNode.getNextSibling();
		Node policyKeyNode = policyStatusNode.getNextSibling();

		Long policyStatusValue = null;
		if (policyKeyNode != null) {
			policyStatusValue = Long.valueOf(policyKeyNode.getTextContent());
		}
		boolean policyDataIsPresent = (policyKeyNode != null) && (policyKeyNode.getNextSibling() != null);

		return ProvisionResponse.builder()
				.provisionStatus(Integer.valueOf(statusNode.getTextContent()))
				.policyStatus(Integer.valueOf(policyStatusNode.getTextContent()))
				.policyKey(policyStatusValue)
				.policyType(policyTypeNode.getTextContent())
				.hasPolicyData(policyDataIsPresent)
				.build();
	}

}
