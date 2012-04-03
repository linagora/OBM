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
package org.obm.push.protocol;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncStatus;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.data.IDataEncoder;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncProtocol {
	
	private final SyncDecoder syncDecoder;

	@Inject
	private SyncProtocol(SyncDecoder syncDecoder) {
		this.syncDecoder = syncDecoder;
	}
	
	public SyncRequest getRequest(Document doc, BackendSession backendSession) 
			throws NoDocumentException, PartialException, ProtocolException, DaoException, CollectionPathException {
		if (doc == null) {
			throw new NoDocumentException("Document of Sync request is null.");
		}
		return new SyncRequest( syncDecoder.decodeSync(doc, backendSession) );
	}

	public Document endcodeResponse(SyncResponse syncResponse) {
		Document reply = DOMUtils.createDoc(null, "Sync");
		Element root = reply.getDocumentElement();
		
		final Element cols = DOMUtils.createElement(root, "Collections");
		for (SyncCollectionResponse collectionResponse: syncResponse.getCollectionResponses()) {

			Element ce = DOMUtils.createElement(cols, "Collection");
			if (collectionResponse.getSyncCollection().getDataClass() != null) {
				DOMUtils.createElementAndText(ce, "Class", collectionResponse.getSyncCollection().getDataClass());
			}
			
			if (!collectionResponse.isSyncStatevalid()) {
				DOMUtils.createElementAndText(ce, "CollectionId", collectionResponse.getSyncCollection().getCollectionId().toString());
				DOMUtils.createElementAndText(ce, "Status", SyncStatus.INVALID_SYNC_KEY.asXmlValue());
				DOMUtils.createElementAndText(ce, "SyncKey", "0");
			} else {
				Element sk = DOMUtils.createElement(ce, "SyncKey");
				DOMUtils.createElementAndText(ce, "CollectionId", collectionResponse.getSyncCollection().getCollectionId().toString());
				DOMUtils.createElementAndText(ce, "Status", SyncStatus.OK.asXmlValue());

				if (!collectionResponse.getSyncCollection().getSyncKey().equals("0")) {
					if (collectionResponse.getSyncCollection().getFetchIds().isEmpty()) {
						buildUpdateItemChange(syncResponse.getBackendSession(), collectionResponse, 
								syncResponse.getProcessedClientIds(), ce, syncResponse.getEncoderFactory());
					} else {
						buildFetchItemChange(syncResponse.getBackendSession(), collectionResponse, ce, 
								syncResponse.getEncoderFactory());
					}
				}
				
				sk.setTextContent(collectionResponse.getAllocateNewSyncKey());
			}
			
		}
		return reply;
	}
	
	public Document encodeResponse() {
		Document reply = DOMUtils.createDoc(null, "Sync");
		Element root = reply.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", SyncStatus.WAIT_INTERVAL_OUT_OF_RANGE.asXmlValue());
		DOMUtils.createElementAndText(root, "Limit", "59");
		return reply;
	}
	
	public Document encodeResponse(String error) {
		return buildErrorResponse("Sync", error);
	}	
	
	private Document buildErrorResponse(String type, String error) {
		Document ret = DOMUtils.createDoc(null, type);
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", error);
		return ret;
	}

	private void buildFetchItemChange(BackendSession bs, SyncCollectionResponse c, Element ce, EncoderFactory encoderFactory) {
		Element commands = DOMUtils.createElement(ce, "Responses");
		for (ItemChange ic : c.getItemChanges()) {
			Element add = DOMUtils.createElement(commands, "Fetch");
			DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
			DOMUtils.createElementAndText(add, "Status",
					SyncStatus.OK.asXmlValue());
			c.getSyncCollection().getOptions().initTruncation();
			serializeChange(bs, add, c.getSyncCollection(), ic, encoderFactory);
		}
	}
	
	private void serializeChange(BackendSession bs, Element col,
			SyncCollection c, ItemChange ic, EncoderFactory encoderFactory) {
		
		IApplicationData data = ic.getData();
		IDataEncoder encoder = encoderFactory.getEncoder(data);
		Element apData = DOMUtils.createElement(col, "ApplicationData");
		encoder.encode(bs, apData, data, c, true);
	}
	
	private void buildUpdateItemChange(BackendSession bs, SyncCollectionResponse c,	Map<String, String> processedClientIds, 
			Element ce, EncoderFactory encoderFactory) {
		
		Element responses = DOMUtils.createElement(ce, "Responses");
		if (c.getSyncCollection().isMoreAvailable()) {
			// MoreAvailable has to be before Commands
			DOMUtils.createElement(ce, "MoreAvailable");
		}
		
		Element commands = DOMUtils.createElement(ce, "Commands");
		
		List<ItemChange> itemChangesDeletion = c.getItemChangesDeletion();
		for (ItemChange ic: itemChangesDeletion) {
			serializeDeletion(commands, ic);
		}
		
		for (ItemChange ic : c.getItemChanges()) {
			String clientId = processedClientIds.get(ic.getServerId());
			if (itemChangeIsClientAddAck(clientId)) {
				Element add = DOMUtils.createElement(responses, "Add");
				DOMUtils.createElementAndText(add, "ClientId", clientId);
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status", SyncStatus.OK.asXmlValue());
			
			} else if (itemChangeIsClientChangeAck(processedClientIds, ic)) {
				Element add = DOMUtils.createElement(responses, "Change");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status", SyncStatus.OK.asXmlValue());
			} else { // New change done on server
				String commandName = selectCommandName(ic);
				Element command = DOMUtils.createElement(commands, commandName);
				DOMUtils.createElementAndText(command, "ServerId", ic.getServerId());
				serializeChange(bs, command, c.getSyncCollection(), ic, encoderFactory);
			}
			processedClientIds.remove(ic.getServerId());
		}

		// Send error for the remaining entry in the Map because the
		// client has requested the addition of a resource that already exists
		// on the server
		Set<Entry<String, String>> entries = new HashSet<Map.Entry<String, String>>(
				processedClientIds.entrySet());
		for (Entry<String, String> entry : entries) {
			if (entry.getKey() != null) {
				if (entry.getKey().startsWith(c.getSyncCollection().getCollectionId().toString())) {
					Element add = null;
					if (entry.getValue() != null) {
						add = DOMUtils.createElement(responses, "Add");
						DOMUtils.createElementAndText(add, "ClientId",
								entry.getValue());
					} else {
						add = DOMUtils.createElement(responses, "Change");
					}
					DOMUtils.createElementAndText(add, "ServerId",
							entry.getKey());
					// need send ok since we do not synchronize event with
					// ParticipationState need-action
					DOMUtils.createElementAndText(add, "Status",
							SyncStatus.OK.asXmlValue());
					processedClientIds.remove(entry.getKey());
				}
			}
		}
		if (responses.getChildNodes().getLength() == 0) {
			responses.getParentNode().removeChild(responses);
		}
		if (commands.getChildNodes().getLength() == 0) {
			commands.getParentNode().removeChild(commands);
		}
	}

	private String selectCommandName(ItemChange itemChange) {
		if (itemChange.isNew()) {
			return "Add";
		} else {
			return "Change";
		}
	}

	private boolean itemChangeIsClientChangeAck(
			Map<String, String> processedClientIds, ItemChange ic) {
		return processedClientIds.keySet().contains(ic.getServerId());
	}

	private boolean itemChangeIsClientAddAck(String clientId) {
		return clientId != null;
	}
	
	private static void serializeDeletion(Element commands, ItemChange ic) {
		Element del = DOMUtils.createElement(commands, "Delete");
		DOMUtils.createElementAndText(del, "ServerId", ic.getServerId());
	}
	
}
