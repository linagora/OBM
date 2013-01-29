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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.exception.activesync.ServerErrorException;
import org.obm.push.protocol.bean.AnalysedSyncRequest;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.data.SyncAnalyser;
import org.obm.push.protocol.data.SyncDecoder;
import org.obm.push.protocol.data.SyncEncoder;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class SyncProtocol implements ActiveSyncProtocol<SyncRequest, SyncResponse>{

	@Singleton
	public static class Factory {
		
		private final SyncDecoder syncDecoder;
		private final SyncAnalyser syncAnalyser;
		private final SyncEncoder syncEncoder;
		private final EncoderFactory encoderFactory;

		@Inject
		public Factory(SyncDecoder syncDecoder, SyncAnalyser syncAnalyser,
				SyncEncoder syncEncoder, EncoderFactory encoderFactory) {
			this.syncDecoder = syncDecoder;
			this.syncAnalyser = syncAnalyser;
			this.syncEncoder = syncEncoder;
			this.encoderFactory = encoderFactory;
		}
		
		public SyncProtocol create(UserDataRequest udr) {
			return new SyncProtocol(syncDecoder, syncAnalyser, syncEncoder, encoderFactory, udr);
		}
	}
	
	private final SyncDecoder syncDecoder;
	private final SyncAnalyser syncAnalyser;
	private final SyncEncoder syncEncoder;
	private final EncoderFactory encoderFactory;
	private final UserDataRequest udr;

	@VisibleForTesting SyncProtocol(SyncDecoder syncDecoder, SyncAnalyser syncAnalyser,
			SyncEncoder syncEncoder, EncoderFactory encoderFactory, UserDataRequest udr) {
		this.syncDecoder = syncDecoder;
		this.syncAnalyser = syncAnalyser;
		this.syncEncoder = syncEncoder;
		this.encoderFactory = encoderFactory;
		this.udr = udr;
	}
	
	@Override
	public SyncRequest decodeRequest(Document doc) 
			throws NoDocumentException, ProtocolException, DaoException, CollectionPathException {
		if (doc == null) {
			throw new NoDocumentException("Document of Sync request is null.");
		}
		return syncDecoder.decodeSync(doc);
	}
	
	public AnalysedSyncRequest analyzeRequest(UserDataRequest userDataRequest, SyncRequest syncRequest) 
			throws PartialException, ProtocolException, DaoException, CollectionPathException {
		Preconditions.checkNotNull(userDataRequest);
		Preconditions.checkNotNull(syncRequest);
		
		return new AnalysedSyncRequest( syncAnalyser.analyseSync(userDataRequest, syncRequest) );
	}

	@Override
	public Document encodeResponse(SyncResponse syncResponse) throws ProtocolException {
		try {
			Document reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();

			final Element cols = DOMUtils.createElement(root, "Collections");
			for (SyncCollectionResponse collectionResponse: syncResponse.getCollectionResponses()) {

				final Element ce = DOMUtils.createElement(cols, "Collection");
				if (collectionResponse.getSyncCollection().getDataClass() != null) {
					DOMUtils.createElementAndText(ce, "Class", collectionResponse.getSyncCollection().getDataClass());
				}
				
				SyncStatus status = collectionResponse.getSyncCollection().getStatus();
				if (!collectionResponse.collectionValidity()) {
					DOMUtils.createElementAndText(ce, "CollectionId", collectionResponse.getSyncCollection().getCollectionId().toString());
					DOMUtils.createElementAndText(ce, "Status", SyncStatus.OBJECT_NOT_FOUND.asSpecificationValue());
				} else if (status != SyncStatus.OK) {
					DOMUtils.createElementAndText(ce, "CollectionId", collectionResponse.getSyncCollection().getCollectionId().toString());
					DOMUtils.createElementAndText(ce, "Status", collectionResponse.getSyncCollection().getStatus().asSpecificationValue());
					if (status == SyncStatus.INVALID_SYNC_KEY) {
						DOMUtils.createElementAndText(ce, "SyncKey", "0");
					}
				} else {
					Element sk = DOMUtils.createElement(ce, "SyncKey");
					DOMUtils.createElementAndText(ce, "CollectionId", collectionResponse.getSyncCollection().getCollectionId().toString());
					DOMUtils.createElementAndText(ce, "Status", SyncStatus.OK.asSpecificationValue());
	
					if (!collectionResponse.getSyncCollection().getSyncKey().equals(SyncKey.INITIAL_FOLDER_SYNC_KEY)) {
						if (collectionResponse.getSyncCollection().getFetchIds().isEmpty()) {
							buildUpdateItemChange(collectionResponse, syncResponse.getClientCommands(), ce);
						} else {
							buildFetchItemChange(collectionResponse, ce);
						}
					}
					
					sk.setTextContent(collectionResponse.getAllocateNewSyncKey().getSyncKey());
				}
			}
			return reply;
		} catch (IOException e) {
			throw new ServerErrorException(e);
		}
	}
	
	public Document encodeResponse() {
		Document reply = DOMUtils.createDoc(null, "Sync");
		Element root = reply.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", SyncStatus.WAIT_INTERVAL_OUT_OF_RANGE.asSpecificationValue());
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

	private void buildFetchItemChange(SyncCollectionResponse c, Element ce) throws IOException {
		
		Element commands = DOMUtils.createElement(ce, "Responses");
		for (ItemChange ic : c.getItemChanges()) {
			Element add = DOMUtils.createElement(commands, "Fetch");
			DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
			DOMUtils.createElementAndText(add, "Status",
					SyncStatus.OK.asSpecificationValue());
			c.getSyncCollection().getOptions().initTruncation();
			serializeChange(add, ic);
		}
	}
	
	private void serializeChange(Element col, ItemChange ic) throws IOException {
		if (encoderFactory != null && ic.getData() != null) {
			Element apData = DOMUtils.createElement(col, "ApplicationData");
			encoderFactory.encode(udr.getDevice(), apData, ic.getData(), true);
		}
	}
	
	private void buildUpdateItemChange(SyncCollectionResponse c, SyncClientCommands syncClientCommands, 
			Element ce) throws IOException {
		
		Element responses = DOMUtils.createElement(ce, "Responses");
		if (c.getSyncCollection().isMoreAvailable()) {
			// MoreAvailable has to be before Commands
			DOMUtils.createElement(ce, "MoreAvailable");
		}
		
		Element commands = DOMUtils.createElement(ce, "Commands");
		
		List<ItemDeletion> itemChangesDeletion = c.getItemChangesDeletion();
		for (ItemDeletion deletion: itemChangesDeletion) {
			serializeDeletion(commands, deletion);
		}
		
		Map<String, String> processedClientIds = buildProcessedClientIds(syncClientCommands);
		for (ItemChange ic : c.getItemChanges()) {
			if (itemChangeIsClientAddAck(syncClientCommands, ic)) {
				SyncClientCommands.Add clientAdd = syncClientCommands.getAddWithServerId(ic.getServerId()).get();
				Element add = DOMUtils.createElement(responses, "Add");
				DOMUtils.createElementAndText(add, "ClientId", clientAdd.clientId);
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status", SyncStatus.OK.asSpecificationValue());
			
			} else if (itemChangeIsClientChangeAck(syncClientCommands, ic)) {
				Element add = DOMUtils.createElement(responses, "Change");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status", SyncStatus.OK.asSpecificationValue());
			} else { // New change done on server
				String commandName = selectCommandName(ic);
				Element command = DOMUtils.createElement(commands, commandName);
				DOMUtils.createElementAndText(command, "ServerId", ic.getServerId());
				serializeChange(command, ic);
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
							SyncStatus.OK.asSpecificationValue());
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

	private Map<String, String> buildProcessedClientIds(SyncClientCommands syncClientCommands) {
		Map<String, String> processedClientIds = Maps.newHashMap();
		for (SyncClientCommands.Add add : syncClientCommands.getAdds()) {
			processedClientIds.put(add.serverId, add.clientId);
		}
		for (SyncClientCommands.Change change : syncClientCommands.getChanges()) {
			processedClientIds.put(change.serverId, null);
		}
		return processedClientIds;
	}

	private String selectCommandName(ItemChange itemChange) {
		if (itemChange.isNew()) {
			return "Add";
		} else {
			if (itemChange.isDeleted()) {
				return "Delete";
			} else {
				return "Change";
			}
		}
	}

	private boolean itemChangeIsClientChangeAck(SyncClientCommands syncClientCommands, ItemChange itemChange) {
		return syncClientCommands.hasChangeWithServerId(itemChange.getServerId());
	}

	private boolean itemChangeIsClientAddAck(SyncClientCommands syncClientCommands, ItemChange itemChange) {
		return syncClientCommands.hasAddWithServerId(itemChange.getServerId());
	}
	
	private static void serializeDeletion(Element commands, ItemDeletion deletion) {
		Element del = DOMUtils.createElement(commands, "Delete");
		DOMUtils.createElementAndText(del, "ServerId", deletion.getServerId());
	}

	@Override
	public Document encodeRequest(SyncRequest request) throws ProtocolException {
		return syncEncoder.encodeSync(request);
	}

	@Override
	public SyncResponse decodeResponse(Document responseDocument) throws ProtocolException {
		return syncDecoder.decodeSyncResponse(responseDocument);
	}
	
}
