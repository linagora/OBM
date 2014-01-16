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
package org.obm.push.protocol;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.GetItemEstimateStatus;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.protocol.bean.Estimate;
import org.obm.push.protocol.bean.GetItemEstimateRequest;
import org.obm.push.protocol.bean.GetItemEstimateResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class GetItemEstimateProtocol implements ActiveSyncProtocol<GetItemEstimateRequest, GetItemEstimateResponse> {

	@Override
	public GetItemEstimateRequest decodeRequest(Document doc) throws CollectionNotFoundException {
		final NodeList collections = doc.getDocumentElement().getElementsByTagName("Collection");
		int nbElements = collections.getLength();
		GetItemEstimateRequest.Builder getItemEstimateRequestBuilder = GetItemEstimateRequest.builder();
		for (int i = 0; i < nbElements; i++) {
	
			final Element ce = (Element) collections.item(i);
			final String dataClass = DOMUtils.getElementText(ce, "Class");
			final String filterType = DOMUtils.getElementText(ce, "FilterType");
			final SyncKey syncKey = new SyncKey(DOMUtils.getElementText(ce, "SyncKey"));
			final Element fid = DOMUtils.getUniqueElement(ce, "CollectionId");
			final String collectionId = fid.getTextContent();
	
			try {
				getItemEstimateRequestBuilder.add(AnalysedSyncCollection.builder()
						.dataType(PIMDataType.recognizeDataType(dataClass))
						.syncKey(syncKey)
						.options(buildOptions(filterType))
						.collectionId(Integer.valueOf(collectionId))
						.build());
			} catch (NumberFormatException e) {
				throw new CollectionNotFoundException(e);
			}
		}
		return getItemEstimateRequestBuilder
			.build();
	}

	private SyncCollectionOptions buildOptions(String filterType) {
		SyncCollectionOptions options = new SyncCollectionOptions();
		if (filterType != null) {
			options.setFilterType(FilterType.fromSpecificationValue(filterType));
		}
		return options;
	}

	@Override
	public GetItemEstimateResponse decodeResponse(Document doc) {
		Element ier = doc.getDocumentElement();
		
		GetItemEstimateResponse.Builder getItemEstimateResponseBuilder = GetItemEstimateResponse.builder();
		NodeList responses = ier.getElementsByTagName("Response");
		for (int i = 0; i < responses.getLength(); i++) {
			Element response = (Element) responses.item(i);
			
			Element collection = DOMUtils.getUniqueElement(response, "Collection");
			Integer collectionId = Integer.valueOf(DOMUtils.getElementText(collection, "CollectionId"));
			SyncCollectionResponse.Builder builder = SyncCollectionResponse.builder()
				.collectionId(collectionId)
				.status(SyncStatus.fromSpecificationValue(DOMUtils.getElementText(response, "Status")));
			
			int estimateSize = Integer.valueOf(DOMUtils.getElementText(collection, "Estimate"));
			getItemEstimateResponseBuilder.add(Estimate.builder()
					.collection(builder.build())
					.estimate(estimateSize)
					.build());
		}
		
		return getItemEstimateResponseBuilder
			.build();
	}

	@Override
	public Document encodeResponse(GetItemEstimateResponse response) {
		final Document document = createDocument();
		for (Estimate estimate: response.getEstimates()) {
			final Element responseElement = createResponseNode(document);
			createStatusElement(estimate.getCollection().getStatus(), responseElement);
			final Element collectionElement = DOMUtils.createElement(responseElement, "Collection");
			createCollectionIdElement(estimate.getCollection(), collectionElement);
			createEstimateElement(estimate.getEstimate(), collectionElement);
		}
		return document;
	}

	private Document createDocument() throws FactoryConfigurationError {
		return DOMUtils.createDoc(null, "GetItemEstimate");
	}

	private Element createResponseNode(final Document document) {
		final Element rootElement = document.getDocumentElement();
		final Element responseElement = DOMUtils.createElement(rootElement, "Response");
		return responseElement;
	}

	@VisibleForTesting void createStatusElement(SyncStatus syncStatus, final Element responseElement) {
		if (syncStatus == SyncStatus.OK) {
			DOMUtils.createElementAndText(responseElement, "Status", GetItemEstimateStatus.OK.getSpecificationValue());
		}
		if (syncStatus == SyncStatus.INVALID_SYNC_KEY) {
			DOMUtils.createElementAndText(responseElement, "Status", GetItemEstimateStatus.INVALID_SYNC_KEY.getSpecificationValue());
		}
		if (syncStatus == SyncStatus.NEED_RETRY) {
			DOMUtils.createElementAndText(responseElement, "Status", GetItemEstimateStatus.NEED_SYNC.getSpecificationValue());
		}
	}
	
	private void createCollectionIdElement(SyncCollectionResponse syncCollection, Element collectionElement) {
		DOMUtils.createElementAndText(collectionElement, "CollectionId", syncCollection.getCollectionId());
	}

	private void createEstimateElement(int estimate, Element collectionElement) {
		Element estim = DOMUtils.createElement(collectionElement, "Estimate");
		estim.setTextContent(String.valueOf(estimate));
	}

	@Override
	public Document encodeRequest(GetItemEstimateRequest request) {
		Document ret = DOMUtils.createDoc(null, "GetItemEstimate");
		Element giee = ret.getDocumentElement();

		for (AnalysedSyncCollection syncCollection : request.getSyncCollections()) {
			Element collection = DOMUtils.createElement(giee, "Collection");
			if (!Strings.isNullOrEmpty(syncCollection.getDataClass())) {
				DOMUtils.createElementAndText(collection, "Class", syncCollection.getDataClass());
			}
			
			SyncCollectionOptions syncCollectionOptions = syncCollection.getOptions();
			if (syncCollectionOptions != null && syncCollectionOptions.getFilterType() != null) {
				DOMUtils.createElementAndText(collection, "FilterType", syncCollectionOptions.getFilterType().asSpecificationValue());
			}
			
			DOMUtils.createElementAndText(collection, "SyncKey", syncCollection.getSyncKey().getSyncKey());
			DOMUtils.createElementAndText(collection, "CollectionId", syncCollection.getCollectionId());
		}
		
		return ret;
	}
	
	public Document buildError(GetItemEstimateStatus status, Integer collectionId) {
		Document document = createDocument();
		Element responseNode = createResponseNode(document);
		DOMUtils.createElementAndText(responseNode, "Status", status.getSpecificationValue());
		if (collectionId != null) {
			Element ce = DOMUtils.createElement(responseNode, "Collection");
			DOMUtils.createElementAndText(ce, "CollectionId", String.valueOf(collectionId));
		}
		return document;
	}

}
