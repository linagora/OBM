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

import org.obm.push.bean.SyncKey;
import org.obm.push.protocol.FolderSyncProtocol;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.ResponseTransformer;
import org.obm.sync.push.client.beans.AccountInfos;
import org.obm.sync.push.client.beans.NS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Performs a FolderSync AS command with the given sync key
 */
public class FolderSync extends AbstractCommand<FolderSyncResponse> {

	private static final String TEMPLATE_NAME = "FolderSyncRequest.xml";
	
	public FolderSync(final SyncKey syncKey) throws SAXException, IOException {
		super(NS.FolderHierarchy, "FolderSync", new TemplateDocument(TEMPLATE_NAME) {
			@Override
			protected void customize(Document document, AccountInfos accountInfos) {
				Element sk = DOMUtils.getUniqueElement(document.getDocumentElement(), "SyncKey");
				sk.setTextContent(syncKey.getSyncKey());
			}
		});		
	}

	@Override
	protected FolderSyncResponse parseResponse(Document responseDocument) {
		return new FolderSyncProtocol().decodeResponse(responseDocument);
	}

	@Override
	protected ResponseTransformer<FolderSyncResponse> responseTransformer() {
		return new FolderSyncResponseTransformer();
	}
	
	private class FolderSyncResponseTransformer implements ResponseTransformer<FolderSyncResponse> {

		@Override
		public FolderSyncResponse parse(Document document) {
			return parseResponse(document);
		}
	}
}
