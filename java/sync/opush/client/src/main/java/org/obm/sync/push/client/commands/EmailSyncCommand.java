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

import org.obm.push.bean.FilterType;
import org.obm.push.bean.SyncKey;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.beans.AccountInfos;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class EmailSyncCommand extends Sync {

	public EmailSyncCommand(final SyncDecoder decoder, final SyncKey syncKey, final String collectionId,
			final FilterType filterType, final int windowSize) throws SAXException, IOException {
		this(decoder, "EmailSyncRequest.xml", syncKey, collectionId, filterType, windowSize);
	}
	
	protected EmailSyncCommand(SyncDecoder decoder, String templateName, final SyncKey syncKey, final String collectionId,
			final FilterType filterType, final int windowSize)
			throws SAXException, IOException {

		super(decoder, new TemplateDocument(templateName) {
			
			@Override
			protected void customize(Document document, AccountInfos accountInfos) {
				Element sk = DOMUtils.getUniqueElement(document.getDocumentElement(), "SyncKey");
				sk.setTextContent(syncKey.getSyncKey());
				Element collection = DOMUtils.getUniqueElement(document.getDocumentElement(), "CollectionId");
				collection.setTextContent(collectionId);
				Element filterTypeE = DOMUtils.getUniqueElement(document.getDocumentElement(), "FilterType");
				filterTypeE.setTextContent(filterType.asSpecificationValue());
				Element windowSizeE = DOMUtils.getUniqueElement(document.getDocumentElement(), "WindowSize");
				windowSizeE.setTextContent(String.valueOf(windowSize));
			}
		});
	}
	
}
