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

import java.io.IOException;

import org.apache.commons.lang.NotImplementedException;
import org.obm.push.bean.Device;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.StoreName;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.exception.activesync.ServerErrorException;
import org.obm.push.protocol.bean.ItemOperationsRequest;
import org.obm.push.protocol.bean.ItemOperationsRequest.EmptyFolderContentsRequest;
import org.obm.push.protocol.bean.ItemOperationsRequest.Fetch;
import org.obm.push.protocol.bean.ItemOperationsResponse;
import org.obm.push.protocol.bean.ItemOperationsResponse.EmptyFolderContentsResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult.FetchAttachmentResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult.FetchItemResult;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class ItemOperationsProtocol implements ActiveSyncProtocol<ItemOperationsRequest, ItemOperationsResponse> {

	@Singleton
	public static class Factory {

		private final EncoderFactory encoderFactory;

		@Inject
		@VisibleForTesting Factory(EncoderFactory encoderFactory) {
			this.encoderFactory = encoderFactory;
		}
		
		public ItemOperationsProtocol create(Device device, boolean isMultipart) {
			return new ItemOperationsProtocol(encoderFactory, device, isMultipart);
		}
		
	}
	
	private final EncoderFactory encoderFactory;
	private final Device device;
	private final boolean isMultipart;

	@VisibleForTesting ItemOperationsProtocol(EncoderFactory encoderFactory, Device device, boolean isMultipart) {
		this.encoderFactory = encoderFactory;
		this.device = device;
		this.isMultipart = isMultipart;
	}

	@Override
	public ItemOperationsRequest decodeRequest(Document document) {
		Element root = document.getDocumentElement();
		Fetch fetch = buildFetch(root);
		EmptyFolderContentsRequest emptyFolderContents = buildEmptyFolderContents(root);
		
		ItemOperationsRequest itemOperationsRequest = new ItemOperationsRequest();
		itemOperationsRequest.setFetch(fetch);
		itemOperationsRequest.setEmptyFolderContents(emptyFolderContents);
		return itemOperationsRequest;
	}

	/* package */ Fetch buildFetch(Element root) {
		Element fetchNode = DOMUtils.getUniqueElement(root, "Fetch");
		if (fetchNode != null) {
			StoreName storeName = StoreName.fromSpecificationValue(DOMUtils.getElementText(fetchNode,	"Store"));
			String reference = DOMUtils.getElementText(fetchNode, "FileReference");
			String collectionId = DOMUtils.getElementText(fetchNode, "CollectionId");
			String serverId = DOMUtils.getElementText(fetchNode, "ServerId");

			Fetch fetch = new Fetch();
			fetch.setStoreName(storeName);
			fetch.setFileReference(reference);
			fetch.setCollectionId(collectionId);
			fetch.setServerId(serverId);
			fetch.setType(getType(fetchNode));
			return fetch;
		}
		return null;
	}

	private MSEmailBodyType getType(Element fetchNode) {
		String typeAsTextInteger = DOMUtils.getElementText(fetchNode, "Type");
		if (typeAsTextInteger != null) {
			return MSEmailBodyType.getValueOf(Integer.valueOf(typeAsTextInteger));
		}
		return null;
	}

	// <?xml version="1.0" encoding="UTF-8"?>
	// <ItemOperations>
	// <EmptyFolderContents>
	// <CollectionId>68</CollectionId>
	// <Options>
	// <DeleteSubFolders/>
	// </Options>
	// </EmptyFolderContents>
	// </ItemOperations>
	private EmptyFolderContentsRequest buildEmptyFolderContents(Element root) {
		Element emptyFolder = DOMUtils.getUniqueElement(root, "EmptyFolderContents");
		if (emptyFolder != null) {
			int collectionId = Integer.valueOf(DOMUtils.getElementText(emptyFolder, "CollectionId"));
			Element deleteSubFolderElem = DOMUtils.getUniqueElement(emptyFolder, "DeleteSubFolders");

			EmptyFolderContentsRequest emptyFolderContents = new EmptyFolderContentsRequest();
			emptyFolderContents.setCollectionId(collectionId);
			emptyFolderContents.setDeleteSubFolderElem(deleteSubFolderElem != null);
			return emptyFolderContents;
		}
		return null;
	}

	@Override
	public Document encodeResponse(ItemOperationsResponse response) throws ProtocolException {
		try {
			Document document = DOMUtils.createDoc(null, "ItemOperations");
			Element root = document.getDocumentElement();
			if (response.getEmptyFolderContentsResult() != null) {
				encodeEmptyFolderOperation(response.getEmptyFolderContentsResult(), root);
			} else if (response.getMailboxFetchResult() != null) {
				encodeMailboxFetchResult(response.getMailboxFetchResult(), root);
			}
			return document;
		} catch (IOException e) {
			throw new ServerErrorException(e);
		}
	}
	
	private void encodeEmptyFolderOperation(EmptyFolderContentsResult result, Element root) {
		DOMUtils.createElementAndText(root, "Status", ItemOperationsStatus.SUCCESS.asSpecificationValue());
		Element response = DOMUtils.createElement(root, "Response");
		Element empty = DOMUtils.createElement(response, "EmptyFolderContents");
		DOMUtils.createElementAndText(empty, "Status", result.getItemOperationsStatus().asSpecificationValue());
		DOMUtils.createElementAndText(empty, "AirSync:CollectionId", String.valueOf(result.getCollectionId()));
	}

	private void encodeMailboxFetchResult(MailboxFetchResult mailboxFetchResult, Element root) throws IOException {
		if (mailboxFetchResult.getFetchItemResult() != null) {
			encodeFetchItemResult(root, mailboxFetchResult.getFetchItemResult());
		} else if (mailboxFetchResult.getFileReferenceFetch() != null) {
			encodeFetchAttachmentResult(root, mailboxFetchResult.getFileReferenceFetch());
		}
	}

	private void encodeFetchItemResult(Element root, FetchItemResult fetchItemResult) throws IOException {
		DOMUtils.createElementAndText(root, "Status", ItemOperationsStatus.SUCCESS.asSpecificationValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status", fetchItemResult.getStatus().asSpecificationValue());
		DOMUtils.createElementAndText(fetchResp, "AirSync:ServerId", fetchItemResult.getServerId());

		if (ItemOperationsStatus.SUCCESS == fetchItemResult.getStatus() &&
				fetchItemResult.getSyncCollection() != null) {
			encodeItemChange(fetchResp, fetchItemResult.getItemChange());
		}
	}

	private void encodeItemChange(Element fetchResp, ItemChange itemChange) throws IOException {
		if (itemChange != null && itemChange.getData() != null) {
			Element dataElem = DOMUtils.createElement(fetchResp, "Properties");
			IApplicationData data = itemChange.getData();
			encoderFactory.encode(device, dataElem, data, true);
		}
	}
	
	private void encodeFetchAttachmentResult(Element root, FetchAttachmentResult fetchAttachmentResult) {
		DOMUtils.createElementAndText(root, "Status", ItemOperationsStatus.SUCCESS.asSpecificationValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status", fetchAttachmentResult.getStatus().asSpecificationValue());
		
		if (fetchAttachmentResult.getReference() != null) {
			DOMUtils.createElementAndText(fetchResp, "AirSyncBase:FileReference", fetchAttachmentResult.getReference());
		}
		if (fetchAttachmentResult.getContentType() != null) {
			Element properties = DOMUtils.createElement(fetchResp, "Properties");
			DOMUtils.createElementAndText(properties, "AirSyncBase:ContentType", fetchAttachmentResult.getContentType());
			if (!isMultipart) {
				DOMUtils.createElementAndText(properties, "Data", new String(fetchAttachmentResult.getAttch(), Charsets.UTF_8));
			} else {
				DOMUtils.createElementAndText(properties, "Part", "1");
			}
		}
	}
	
	public Document encodeErrorRespponse(ItemOperationsStatus status) {
		Document document = DOMUtils.createDoc(null, "ItemOperations");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", status.asSpecificationValue());
		return document;
	}

	@Override
	public Document encodeRequest(ItemOperationsRequest request) throws ProtocolException {
		throw new NotImplementedException();
	}

	@Override
	public ItemOperationsResponse decodeResponse(Document responseDocument) throws ProtocolException {
		throw new NotImplementedException();
	}
}
