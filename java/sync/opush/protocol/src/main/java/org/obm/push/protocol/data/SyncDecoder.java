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
package org.obm.push.protocol.data;

import java.util.List;

import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionCommand;
import org.obm.push.bean.SyncCollectionCommands;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncCollectionRequest;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
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
		Element root = doc.getDocumentElement();
		
		SyncRequest.Builder requestBuilder = SyncRequest.builder()
				.waitInMinute(getWait(root))
				.partial(isPartial(root))
				.windowSize(getWindowSize(root));
		
		NodeList collectionNodes = root.getElementsByTagName(SyncRequestFields.COLLECTION.getName());
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			requestBuilder.addCollection(getCollection((Element)collectionNodes.item(i)));
		}
		
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

	@VisibleForTesting SyncCollectionRequest getCollection(Element collection) {
		return SyncCollectionRequest.builder()
			.dataType(PIMDataType.fromSpecificationValue(uniqueStringFieldValue(collection, SyncRequestFields.DATA_CLASS)))
			.syncKey(syncKey(uniqueStringFieldValue(collection, SyncRequestFields.SYNC_KEY)))
			.collectionId(uniqueIntegerFieldValue(collection, SyncRequestFields.COLLECTION_ID))
			.deletesAsMoves(uniqueBooleanFieldValue(collection, SyncRequestFields.DELETES_AS_MOVES))
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

	@VisibleForTesting SyncCollectionCommands.Request getCommands(Element commandsElement) {
		SyncCollectionCommands.Request.Builder builder = SyncCollectionCommands.Request.builder();
		if (commandsElement != null) {
			NodeList collectionNodes = commandsElement.getChildNodes();
			for (int i = 0; i < collectionNodes.getLength(); i++) {
				SyncCollectionCommand.Request command = getCommand((Element)collectionNodes.item(i));
				builder.addCommand(command);
			}
		}
		return builder.build();
	}
	
	@VisibleForTesting SyncCollectionCommand.Request getCommand(Element commandElement) {
		SyncCommand syncCommand = SyncCommand.fromSpecificationValue(commandElement.getNodeName());
		
		return SyncCollectionCommand.Request.builder()
			.commandType(syncCommand)
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
		
		SyncResponse.Builder builder = SyncResponse.builder()
				.status(getCollectionStatus(root));
		
		NodeList collectionNodes = root.getElementsByTagName(SyncRequestFields.COLLECTION.getName());
		for (int i = 0; i < collectionNodes.getLength(); i++) {
			builder.addResponse(buildCollectionResponse((Element)collectionNodes.item(i)));
		}
		
		return builder.build();
	}

	private SyncCollectionResponse buildCollectionResponse(Element collectionEl) {
		PIMDataType dataType = dataType(uniqueStringFieldValue(collectionEl, SyncResponseFields.DATA_CLASS));
		SyncCollectionResponse.Builder syncCollectionBuilder = SyncCollectionResponse
				.builder()
				.dataType(dataType)
				.syncKey(new SyncKey(uniqueStringFieldValue(collectionEl, SyncResponseFields.SYNC_KEY)))
				.collectionId(uniqueIntegerFieldValue(collectionEl, SyncResponseFields.COLLECTION_ID))
				.status(getCollectionStatus(collectionEl))
				.moreAvailable(getMoreAvailable(collectionEl))
				.responses(appendCommands(dataType, collectionEl));
		return syncCollectionBuilder.build();
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

	private SyncCollectionCommands.Response appendCommands(PIMDataType dataType, Element collectionEl) {
		SyncCollectionCommands.Response.Builder builder = SyncCollectionCommands.Response.builder();
		Element commandsEl = DOMUtils.getUniqueElement(collectionEl, SyncResponseFields.COMMANDS.getName());
		if (commandsEl != null) {
			NodeList collectionNodes = commandsEl.getChildNodes();
			for (int i = 0; i < collectionNodes.getLength(); i++) {
				builder.addCommand(getCommand((Element)collectionNodes.item(i), dataType));
			}
		}
		
		Element responsesEl = DOMUtils.getUniqueElement(collectionEl, SyncResponseFields.RESPONSES.getName());
		if (responsesEl != null) {
			NodeList collectionNodes = responsesEl.getChildNodes();
			for (int i = 0; i < collectionNodes.getLength(); i++) {
				builder.addCommand(getCommand((Element)collectionNodes.item(i), dataType));
			}
		}
		return builder.build();	 		
	}

	@VisibleForTesting SyncCollectionCommand.Response getCommand(Element commandElement, PIMDataType dataType) {
		SyncCommand syncCommand = SyncCommand.fromSpecificationValue(commandElement.getNodeName());
		Element applicationDataElement = DOMUtils.getUniqueElement(commandElement, SyncRequestFields.APPLICATION_DATA.getName());
		IApplicationData applicationData = decodeApplicationData(applicationDataElement, dataType, syncCommand);
		
		return SyncCollectionCommand.Response.builder()
			.commandType(syncCommand)
 			.serverId(uniqueStringFieldValue(commandElement, SyncRequestFields.SERVER_ID))
 			.clientId(uniqueStringFieldValue(commandElement, SyncRequestFields.CLIENT_ID))
 			.applicationData(applicationData)
 			.build();
	}

	private IApplicationData decodeApplicationData(Element applicationData, PIMDataType dataType, SyncCommand syncCommand) {
		if (syncCommand != SyncCommand.ADD && syncCommand != SyncCommand.CHANGE) {
			return null;
 		}
		if (dataType != null) {
			IApplicationData data = decoderFactory.decode(applicationData, dataType);
			if (data == null && syncCommand.requireApplicationData()) {
				throw new ProtocolException("No decodable " + dataType + " data for " + applicationData);
			}
			return data;
		}
		return null;
 	}
}
