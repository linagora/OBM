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
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.protocol.data.SyncRequestFields;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.beans.AccountInfos;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;

public class SyncWithCommand extends Sync {
	
	public SyncWithCommand(SyncDecoder decoder, SyncKey syncKey, String collectionId, SyncCommand command,
			String serverId, String clientId) throws SAXException, IOException {
		this(decoder, new SyncWithCommandTemplate(syncKey, collectionId, command, serverId, clientId));
	}
	
	protected SyncWithCommand(SyncDecoder decoder, TemplateDocument template) {
		super(decoder, template);
	}
	
	public static class SyncWithCommandTemplate extends TemplateDocument {

		protected final SyncKey syncKey;
		protected final String collectionId;
		protected final SyncCommand command;
		protected final String serverId;
		protected final String clientId;

		protected SyncWithCommandTemplate(SyncKey syncKey, String collectionId, SyncCommand command,
				String serverId, String clientId) throws SAXException, IOException {
			super("SyncWithCommandRequest.xml");
			this.syncKey = syncKey;
			this.collectionId = collectionId;
			this.command = command;
			this.serverId = serverId;
			this.clientId = clientId;
		}

		@Override
		protected void customize(Document document, AccountInfos accountInfos) {
			Element sk = DOMUtils.getUniqueElement(document.getDocumentElement(), SyncRequestFields.SYNC_KEY.getName());
			sk.setTextContent(syncKey.getSyncKey());
			Element collection = DOMUtils.getUniqueElement(document.getDocumentElement(), SyncRequestFields.COLLECTION_ID.getName());
			collection.setTextContent(collectionId);
			
			Element commandsEl = DOMUtils.getUniqueElement(document.getDocumentElement(), SyncRequestFields.COMMANDS.getName());
			Element commandEl = DOMUtils.createElement(commandsEl, command.asSpecificationValue());
			if (!Strings.isNullOrEmpty(serverId)) {
				DOMUtils.createElementAndText(commandEl, SyncRequestFields.SERVER_ID.getName(), serverId);
			}
			if (!Strings.isNullOrEmpty(clientId)) {
				DOMUtils.createElementAndText(commandEl, SyncRequestFields.CLIENT_ID.getName(), clientId);
			}
		}
	}
}
