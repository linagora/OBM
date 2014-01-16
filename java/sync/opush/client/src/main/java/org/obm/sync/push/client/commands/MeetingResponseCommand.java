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

import org.obm.push.protocol.MeetingProtocol;
import org.obm.push.protocol.bean.MeetingHandlerResponse;
import org.obm.push.protocol.data.MeetingRequestFields;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.ResponseTransformer;
import org.obm.sync.push.client.beans.AccountInfos;
import org.obm.sync.push.client.beans.NS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class MeetingResponseCommand extends AbstractCommand<MeetingHandlerResponse> {
	
	public MeetingResponseCommand(final String collectionId, final String requestId) throws SAXException, IOException {
		super(NS.MeetingResponse, "MeetingResponse", new TemplateDocument("MeetingResponseRequest.xml") {

			@Override
			protected void customize(Document document, AccountInfos accountInfos) {
				Element collection = DOMUtils.getUniqueElement(document.getDocumentElement(), MeetingRequestFields.COLLECTION_ID.getName());
				collection.setTextContent(collectionId);
				Element request = DOMUtils.getUniqueElement(document.getDocumentElement(), MeetingRequestFields.REQUEST_ID.getName());
				request.setTextContent(requestId);
			}});
	}

	@Override
	protected MeetingHandlerResponse parseResponse(Document document) {
		return new MeetingProtocol().decodeResponse(document);
	}

	@Override
	protected ResponseTransformer<MeetingHandlerResponse> responseTransformer() {
		return new MeetingHandlerResponseTransformer();
	}
	
	private class MeetingHandlerResponseTransformer implements ResponseTransformer<MeetingHandlerResponse> {

		@Override
		public MeetingHandlerResponse parse(Document document) {
			return parseResponse(document);
		}
	}
}
