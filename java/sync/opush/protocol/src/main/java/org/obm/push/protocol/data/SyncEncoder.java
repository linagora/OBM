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
package org.obm.push.protocol.data;

import java.util.List;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncRequestCollection;
import org.obm.push.protocol.bean.SyncRequestCollectionCommand;
import org.obm.push.protocol.bean.SyncRequestCollectionCommands;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncEncoder extends ActiveSyncDecoder {

	@Inject
	protected SyncEncoder() {}

	public Document encodeSync(SyncRequest request) {
		Document doc = DOMUtils.createDoc(null, "Sync");
		Element root = doc.getDocumentElement();
		
		appendPartial(root, request);
		appendWait(root, request);
		appendWindowSize(root, request);
		appendCollections(root, request);
		return doc;
	}

	private void appendPartial(Element root, SyncRequest request) {
		appendBoolean(root, SyncRequestFields.PARTIAL, request.isPartial());
	}

	private void appendWait(Element root, SyncRequest request) {
		appendInteger(root, SyncRequestFields.WAIT, request.getWaitInMinute());
	}
	
	private void appendWindowSize(Element root, SyncRequest request) {
		appendInteger(root, SyncRequestFields.WINDOW_SIZE, request.getWindowSize());
	}

	private void appendCollections(Element root, SyncRequest request) {
		List<SyncRequestCollection> requestCollections = request.getCollections();
		if (requestCollections != null && !requestCollections.isEmpty()) {
			Element collections = DOMUtils.createElement(root, SyncRequestFields.COLLECTIONS.getName());
			for (SyncRequestCollection collection : requestCollections) {
				appendCollection(collections, collection);
			}
		}
	}
	
	private void appendCollection(Element collections, SyncRequestCollection collection) {
		Element collectionEl = DOMUtils.createElement(collections, SyncRequestFields.COLLECTION.getName());
		appendString(collectionEl, SyncRequestFields.DATA_CLASS, collection.getDataClass());
		appendString(collectionEl, SyncRequestFields.SYNC_KEY, collection.getSyncKey().getSyncKey());
		appendInteger(collectionEl, SyncRequestFields.COLLECTION_ID, collection.getId());
		appendInteger(collectionEl, SyncRequestFields.WINDOW_SIZE, collection.getWindowSize());
		appendOptions(collectionEl, collection.getOptions());
		appendCommands(collectionEl, collection.getCommands());
	}

	private void appendOptions(Element collectionElement, SyncCollectionOptions options) {
		if (options == null) {
			return;
		}
		Element optionsElement = DOMUtils.createElement(collectionElement, SyncRequestFields.OPTIONS.getName());
		if (options.getFilterType() != null) {
			appendString(optionsElement, SyncRequestFields.FILTER_TYPE, options.getFilterType().asSpecificationValue());
		}
		appendInteger(optionsElement, SyncRequestFields.CONFLICT, options.getConflict());
		appendInteger(optionsElement, SyncRequestFields.MIME_TRUNCATION, options.getMimeTruncation());
		appendInteger(optionsElement, SyncRequestFields.MIME_SUPPORT, options.getMimeSupport());
		for (BodyPreference bodyPreference : options.getBodyPreferences()) {
			appendBodyPreference(optionsElement, bodyPreference);
		}
	}

	private void appendBodyPreference(Element optionsElement, BodyPreference bodyPreference) {
		Element bodyPreferenceEl = DOMUtils.createElement(optionsElement, SyncRequestFields.BODY_PREFERENCE.getName());
		if (bodyPreference.getType() != null) {
			appendInteger(bodyPreferenceEl, SyncRequestFields.TYPE, bodyPreference.getType().asXmlValue());
		}
		appendInteger(bodyPreferenceEl, SyncRequestFields.TRUNCATION_SIZE, bodyPreference.getTruncationSize());
		appendBoolean(bodyPreferenceEl, SyncRequestFields.ALL_OR_NONE, bodyPreference.isAllOrNone());
	}
	
	private void appendCommands(Element collectionElement, SyncRequestCollectionCommands commands) {
		if (commands == null) {
			return;
		}
		Element commandsElement = DOMUtils.createElement(collectionElement, SyncRequestFields.COMMANDS.getName());
		for (SyncRequestCollectionCommand command : commands.getCommands()) {
			appendCommand(commandsElement, command);
		}
	}

	private void appendCommand(Element commandsElement, SyncRequestCollectionCommand command) {
		Element commandElement = DOMUtils.createElement(commandsElement, command.getName());
		appendString(commandElement, SyncRequestFields.SERVER_ID, command.getServerId());
		appendString(commandElement, SyncRequestFields.CLIENT_ID, command.getClientId());
		if (command.getApplicationData() != null) {
			Node applicationData = commandElement.getOwnerDocument().importNode(command.getApplicationData(), true);
			commandElement.appendChild(applicationData);
		}
	}
}
