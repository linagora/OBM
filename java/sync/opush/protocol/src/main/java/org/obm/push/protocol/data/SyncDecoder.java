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
import java.util.Set;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncRequest.Builder;
import org.obm.push.protocol.bean.SyncRequestCollection;
import org.obm.push.protocol.bean.SyncRequestCollectionCommand;
import org.obm.push.protocol.bean.SyncRequestCollectionCommands;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncDecoder extends ActiveSyncDecoder {

	private final DecoderFactory decoderFactory;

	@Inject
	protected SyncDecoder(DecoderFactory decoderFactory) {
		this.decoderFactory = decoderFactory;
	}

	public SyncRequest decodeSync(Document doc) 
			throws PartialException, ProtocolException, DaoException, CollectionPathException {
		Builder requestBuilder = SyncRequest.builder();
		Element root = doc.getDocumentElement();
		
		requestBuilder.waitInMinute(getWait(root));
		requestBuilder.partial(isPartial(root));
		requestBuilder.windowSize(getWindowSize(root));
		
		List<SyncRequestCollection> syncRequestCollections = Lists.newArrayList();
		NodeList collectionNodes = root.getElementsByTagName(SyncRequestFields.COLLECTION.getName());
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			syncRequestCollections.add(getCollection((Element)collectionNodes.item(i)));
		}
		requestBuilder.collections(syncRequestCollections);
		
		return requestBuilder.build();
	}

	@VisibleForTesting Integer getWait(Element root) {
		return uniqueIntegerFieldValue(root, SyncRequestFields.WAIT);
	}

	@VisibleForTesting Boolean isPartial(Element root) {
		return uniqueBooleanFieldValue(root, SyncRequestFields.PARTIAL);
	}

	@VisibleForTesting Integer getWindowSize(Element root) {
		return uniqueIntegerFieldValue(root, SyncRequestFields.WINDOW_SIZE);
	}

	@VisibleForTesting SyncRequestCollection getCollection(Element collection) {
		return SyncRequestCollection.builder()
			.id(uniqueIntegerFieldValue(collection, SyncRequestFields.COLLECTION_ID))
			.syncKey(syncKey(uniqueStringFieldValue(collection, SyncRequestFields.SYNC_KEY)))
			.dataClass(uniqueStringFieldValue(collection, SyncRequestFields.DATA_CLASS))
			.windowSize(uniqueIntegerFieldValue(collection, SyncRequestFields.WINDOW_SIZE))
			.options(getOptions(DOMUtils.getUniqueElement(collection, SyncRequestFields.OPTIONS.getName())))
			.commands(getCommands(DOMUtils.getUniqueElement(collection, SyncRequestFields.COMMANDS.getName())))
			.build();
	}

	private SyncKey syncKey(String syncKey) {
		if(Strings.isNullOrEmpty(syncKey)) {
			return null;
		}
		return new SyncKey(syncKey);
	}
	
	@VisibleForTesting SyncCollectionOptions getOptions(Element optionElement) {
		if(optionElement == null) {
			return null;
		}
		
		SyncCollectionOptions options = new SyncCollectionOptions();
		options.setConflict(uniqueIntegerFieldValue(optionElement, SyncRequestFields.CONFLICT));
		options.setMimeSupport(uniqueIntegerFieldValue(optionElement, SyncRequestFields.MIME_SUPPORT));
		options.setMimeTruncation(uniqueIntegerFieldValue(optionElement, SyncRequestFields.MIME_TRUNCATION));
		
		String filterType = uniqueStringFieldValue(optionElement, SyncRequestFields.FILTER_TYPE);
		if (Strings.isNullOrEmpty(filterType)) {
			options.setFilterType(null);
		} else {
			options.setFilterType(FilterType.fromSpecificationValue(filterType));
		}
		
		options.setBodyPreferences(getBodyPreferences(optionElement));
		return options;
	}

	@VisibleForTesting SyncRequestCollectionCommands getCommands(Element commandsElement) {
		if (commandsElement == null) {
			return null;
		}

		List<String> fetchIds = Lists.newArrayList();
		List<SyncRequestCollectionCommand> commands = Lists.newArrayList();
		
		NodeList collectionNodes = commandsElement.getChildNodes();
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			SyncRequestCollectionCommand command = getCommand((Element)collectionNodes.item(i));
			commands.add(command);
			if (command.getName().equals("Fetch")) {
				fetchIds.add(command.getServerId());
			}
		}
		
		return SyncRequestCollectionCommands.builder().commands(commands).fetchIds(fetchIds).build();
	}
	
	@VisibleForTesting SyncRequestCollectionCommand getCommand(Element commandElement) {
		return SyncRequestCollectionCommand.builder()
			.name(commandElement.getNodeName())
			.serverId(uniqueStringFieldValue(commandElement, SyncRequestFields.SERVER_ID))
			.clientId(uniqueStringFieldValue(commandElement, SyncRequestFields.CLIENT_ID))
			.applicationData(DOMUtils.getUniqueElement(commandElement, SyncRequestFields.APPLICATION_DATA.getName()))
			.build();
	}

	@VisibleForTesting List<BodyPreference> getBodyPreferences(Element optionElement) {
		NodeList bodyPreferenceNodes = optionElement.getElementsByTagName(SyncRequestFields.BODY_PREFERENCE.getName());
		List<BodyPreference> bodyPreferences = Lists.newArrayList();
		for (int i = 0; i < bodyPreferenceNodes.getLength(); i++) {
			bodyPreferences.add(getBodyPreference((Element)bodyPreferenceNodes.item(i)));
		}
		return bodyPreferences;
	}

	@VisibleForTesting BodyPreference getBodyPreference(Element bodyPreferenceElement) {
		Integer truncation = uniqueIntegerFieldValue(bodyPreferenceElement, SyncRequestFields.TRUNCATION_SIZE);
		Integer type = uniqueIntegerFieldValue(bodyPreferenceElement, SyncRequestFields.TYPE);
		Boolean allOrNone = uniqueBooleanFieldValue(bodyPreferenceElement, SyncRequestFields.ALL_OR_NONE);
		
		BodyPreference.Builder builder = BodyPreference.builder();
		if (truncation != null) {
			builder.truncationSize(truncation);
		}
		if (type != null) {
			builder.bodyType(MSEmailBodyType.getValueOf(type));
		}
		if (allOrNone != null) {
			builder.allOrNone(allOrNone);
		}
		return builder.build();
	}

	public SyncResponse decodeSyncResponse(Document responseDocument) {
		Element root = responseDocument.getDocumentElement();
		
		SyncStatus status = getCollectionStatus(root);
		SyncClientCommands.Builder clientCommandsBuilder = SyncClientCommands.builder();
		List<SyncCollectionResponse> responseCollections = Lists.newArrayList();
		NodeList collectionNodes = root.getElementsByTagName(SyncRequestFields.COLLECTION.getName());
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			ProcessedSyncCollectionResponse collection = buildCollectionResponse((Element)collectionNodes.item(i));
			responseCollections.add(collection.getSyncCollectionResponse());
			clientCommandsBuilder.merge(collection.getClientCommands());
		}
		
		return new SyncResponse(responseCollections, clientCommandsBuilder.build(), status);
	}

	private ProcessedSyncCollectionResponse buildCollectionResponse(Element collectionEl) {
		SyncCollection syncCollection = new SyncCollection();

		syncCollection.setSyncKey(syncKey(uniqueStringFieldValue(collectionEl, SyncResponseFields.SYNC_KEY)));
		syncCollection.setCollectionId(uniqueIntegerFieldValue(collectionEl, SyncResponseFields.COLLECTION_ID));
		syncCollection.setStatus(getCollectionStatus(collectionEl));
		syncCollection.setMoreAvailable(getMoreAvailable(collectionEl));
		syncCollection.setDataType(dataType(uniqueStringFieldValue(collectionEl, SyncResponseFields.DATA_CLASS)));
		
		SyncClientCommands clientCommands = appendResponsesAndClientCommands(syncCollection, collectionEl);
		appendCommands(syncCollection, collectionEl);
		
		SyncCollectionResponse syncCollectionResponse = new SyncCollectionResponse(syncCollection);
		syncCollectionResponse.setNewSyncKey(syncCollection.getSyncKey());
		syncCollectionResponse.setCollectionValidity(true);
		syncCollectionResponse.setItemChanges(identifyChanges(syncCollection.getChanges()));
		syncCollectionResponse.setItemChangesDeletion(identifyDeletions(syncCollection.getChanges()));
		
		return new ProcessedSyncCollectionResponse(syncCollectionResponse, clientCommands);
	}
	
	private PIMDataType dataType(String dataClass) {
		return PIMDataType.recognizeDataType(dataClass);
	}

	private SyncStatus getCollectionStatus(Element collectionEl) {
		String status = uniqueStringFieldValue(collectionEl, SyncResponseFields.STATUS);
		if (!Strings.isNullOrEmpty(status)) {
			return SyncStatus.fromSpecificationValue(status);
		}
		return null;
	}

	private boolean getMoreAvailable(Element collectionEl) {
		Boolean moreAvailable = uniqueBooleanFieldValue(collectionEl, SyncResponseFields.MORE_AVAILABLE);
		return Objects.firstNonNull(moreAvailable, false);
	}

	private SyncClientCommands appendResponsesAndClientCommands(SyncCollection syncCollection, Element collectionEl) {
		Element responsesEl = DOMUtils.getUniqueElement(collectionEl, SyncResponseFields.RESPONSES.getName());
		if (responsesEl == null) {
			return SyncClientCommands.empty();
		}

		List<String> fetchIds = Lists.newArrayList();
		SyncClientCommands.Builder clientCommandsBuilder = SyncClientCommands.builder();
		NodeList collectionNodes = responsesEl.getChildNodes();
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			SyncCollectionChange change = buildChangeFromCommandElement((Element)collectionNodes.item(i), syncCollection.getDataType());
			syncCollection.addChange(change);
			if (SyncCommand.FETCH.equals(change.getCommand())) {
				fetchIds.add(change.getServerId());
			}
			if (SyncCommand.ADD.equals(change.getCommand())) {
				clientCommandsBuilder.putAdd(new SyncClientCommands.Add(change.getClientId(), change.getServerId()));
			} else {
				clientCommandsBuilder.putChange(new SyncClientCommands.Change(change.getServerId()));
			}
		}
		syncCollection.setFetchIds(fetchIds);
		return clientCommandsBuilder.build();
	}

	private void appendCommands(SyncCollection syncCollection, Element collectionEl) {
		Element commandsEl = DOMUtils.getUniqueElement(collectionEl, SyncResponseFields.COMMANDS.getName());
		if (commandsEl == null) {
			return;
		}
		
		NodeList collectionNodes = commandsEl.getChildNodes();
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			syncCollection.addChange(buildChangeFromCommandElement((Element)collectionNodes.item(i), syncCollection.getDataType()));
		}
	}

	private SyncCollectionChange buildChangeFromCommandElement(Element commandElement, PIMDataType dataType) {
		SyncRequestCollectionCommand command = getCommand(commandElement);

		IApplicationData applicationData = getCommandApplicationData(command, dataType);
		
		SyncCommand syncCommand = SyncCommand.fromSpecificationValue(command.getName());
		SyncCollectionChange change = new SyncCollectionChange(
				command.getServerId(), command.getClientId(), syncCommand, applicationData, dataType);
		return change;
	}

	private IApplicationData getCommandApplicationData(SyncRequestCollectionCommand command, PIMDataType dataType) {
		if (decoderFactory != null && dataType != null && command.getApplicationData() != null) {
			return decoderFactory.decode(command.getApplicationData(), dataType);
		}
		return null;
	}

	private List<ItemChange> identifyChanges(Set<SyncCollectionChange> changes) {
		List<ItemChange> itemChanges = Lists.newArrayList();
		for (SyncCollectionChange change : changes) {
			if (!SyncCommand.DELETE.equals(change.getCommand())) {
				ItemChange itemChange = new ItemChange(change.getServerId());
				itemChange.setNew(isNewChange(change));
				itemChange.setData(change.getData());
				itemChanges.add(itemChange);
			}
		}
		return itemChanges;
	}

	private boolean isNewChange(SyncCollectionChange change) {
		return SyncCommand.ADD.equals(change.getCommand());
	}

	private List<ItemDeletion> identifyDeletions(Set<SyncCollectionChange> changes) {
		List<ItemDeletion> deletions = Lists.newArrayList();
		for (SyncCollectionChange change : changes) {
			if (SyncCommand.DELETE.equals(change.getCommand())) {
				deletions.add(ItemDeletion.builder().serverId(change.getServerId()).build());
			}
		}
		return deletions;
	}
	
	private static class ProcessedSyncCollectionResponse {
		
		private final SyncCollectionResponse syncCollectionResponse;
		private final SyncClientCommands clientCommands;

		public ProcessedSyncCollectionResponse(
				SyncCollectionResponse syncCollectionResponse,
				SyncClientCommands clientCommands) {
			
			this.syncCollectionResponse = syncCollectionResponse;
			this.clientCommands = clientCommands;
		}

		public SyncCollectionResponse getSyncCollectionResponse() {
			return syncCollectionResponse;
		}

		public SyncClientCommands getClientCommands() {
			return clientCommands;
		}
	}
}
