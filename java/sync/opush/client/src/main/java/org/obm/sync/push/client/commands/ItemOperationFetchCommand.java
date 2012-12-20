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

import java.util.List;

import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.ItemOperationFetchResponse;
import org.obm.sync.push.client.ItemOperationResponse;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;

public class ItemOperationFetchCommand extends TemplateBasedCommand<ItemOperationResponse> {

	private final int collectionId;
	private final String[] serverIds;
	private final MSEmailBodyType bodyType;

	public ItemOperationFetchCommand(int collectionId, String...serverIds) {
		this(collectionId, null, serverIds);
	}
	public ItemOperationFetchCommand(int collectionId, MSEmailBodyType bodyType, String...serverIds) {
		super(NS.ItemOperations, "ItemOperations", "ItemOperationsFetchRequest.xml");
		this.collectionId = collectionId;
		this.bodyType = bodyType;
		this.serverIds = serverIds;
	}
	
	@Override
	protected void customizeTemplate(AccountInfos ai, OPClient opc) {
		Element documentElement = tpl.getDocumentElement();
		
		for (String serverId : serverIds) {
			Element fetchElement = DOMUtils.createElement(documentElement, "Fetch");
			DOMUtils.createElementAndText(fetchElement, "Store", "Mailbox");
			DOMUtils.createElementAndText(fetchElement, "AirSync:CollectionId", String.valueOf(collectionId));
			DOMUtils.createElementAndText(fetchElement, "AirSync:ServerId", serverId);
			customizeOptions(fetchElement);
		}
	}

	private void customizeOptions(Element fetchElement) {
		if (bodyType != null) {
			Element options = DOMUtils.createElement(fetchElement, "Options");
			Element bodyPreferences = DOMUtils.createElement(options, "AirSyncBase:BodyPreference");
			DOMUtils.createElementAndText(bodyPreferences, "AirSyncBase:Type", bodyType.asXmlValue());
			
		}
	}
	@Override
	protected ItemOperationResponse parseResponse(Element root) {
		NodeList fetchs = root.getElementsByTagName("Fetch");
		
		List<ItemOperationFetchResponse> fetchResponses = Lists.newArrayList();
		for (int i = 0 ; i < fetchs.getLength(); i++) {
			Element fetch = (Element) fetchs.item(i);
			String statusAsString = DOMUtils.getElementText(fetch, "Status");
			String serverId = DOMUtils.getElementText(fetch, "ServerId");
			Element data = DOMUtils.getUniqueElement(fetch, "Properties");
			fetchResponses.add(new ItemOperationFetchResponse(
					ItemOperationsStatus.fromSpecificationValue(statusAsString), serverId, data));
		}
		return new ItemOperationResponse(fetchResponses);
	}

}
