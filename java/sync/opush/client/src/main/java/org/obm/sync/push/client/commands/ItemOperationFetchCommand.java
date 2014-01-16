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

import java.io.IOException;
import java.util.List;

import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.ItemOperationFetchResponse;
import org.obm.sync.push.client.ItemOperationResponse;
import org.obm.sync.push.client.ResponseTransformer;
import org.obm.sync.push.client.beans.AccountInfos;
import org.obm.sync.push.client.beans.NS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class ItemOperationFetchCommand extends AbstractCommand<ItemOperationResponse> {

	public ItemOperationFetchCommand(int collectionId, String...serverIds) throws SAXException, IOException {
		this(collectionId, null, serverIds);
	}
	
	public ItemOperationFetchCommand(final int collectionId, final MSEmailBodyType bodyType, final String...serverIds)
			throws SAXException, IOException {
		
		super(NS.ItemOperations, "ItemOperations", new TemplateDocument("ItemOperationsFetchRequest.xml") {

			@Override
			protected void customize(Document document, AccountInfos accountInfos) {
				Element documentElement = document.getDocumentElement();
				
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
			
		});
	}

	@Override
	protected ItemOperationResponse parseResponse(Document document) {
		Element documentElement = document.getDocumentElement();
		NodeList fetchs = documentElement.getElementsByTagName("Fetch");
		
		List<ItemOperationFetchResponse> fetchResponses = Lists.newArrayList();
		for (int i = 0 ; i < fetchs.getLength(); i++) {
			Element fetch = (Element) fetchs.item(i);
			String statusAsString = DOMUtils.getElementText(fetch, "Status");
			String serverId = DOMUtils.getElementText(fetch, "ServerId");
			Element data = DOMUtils.getUniqueElement(fetch, "Properties");
			fetchResponses.add(new ItemOperationFetchResponse(
					ItemOperationsStatus.fromSpecificationValue(statusAsString), serverId, data));
		}
		
		String status = DOMUtils.getElementText(documentElement, "Status");
		if (Strings.isNullOrEmpty(status)) {
			return new ItemOperationResponse(fetchResponses, null);
		}
		return new ItemOperationResponse(fetchResponses, ItemOperationsStatus.fromSpecificationValue(status));
	}

	@Override
	protected ResponseTransformer<ItemOperationResponse> responseTransformer() {
		return new ItemOperationResponseTransformer();
	}
	
	private class ItemOperationResponseTransformer implements ResponseTransformer<ItemOperationResponse> {

		@Override
		public ItemOperationResponse parse(Document document) {
			return parseResponse(document);
		}
	}
}
