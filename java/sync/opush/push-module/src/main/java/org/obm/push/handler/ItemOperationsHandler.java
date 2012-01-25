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
package org.obm.push.handler;

import java.util.Arrays;
import java.util.List;

import org.obm.push.IContentsExporter;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.StoreName;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.PIMDataTypeNotFoundException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedStoreException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.Responder;
import org.obm.push.mail.MailBackend;
import org.obm.push.protocol.ItemOperationsProtocol;
import org.obm.push.protocol.bean.ItemOperationsRequest;
import org.obm.push.protocol.bean.ItemOperationsRequest.EmptyFolderContentsRequest;
import org.obm.push.protocol.bean.ItemOperationsRequest.Fetch;
import org.obm.push.protocol.bean.ItemOperationsResponse;
import org.obm.push.protocol.bean.ItemOperationsResponse.EmptyFolderContentsResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult.FetchAttachmentResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult.FetchItemResult;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.utils.FileUtils;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ItemOperationsHandler extends WbxmlRequestHandler {

	private static final String NAMESPACE = "ItemOperations";
	private final ItemOperationsProtocol protocol;
	private final MailBackend mailBackend;
	
	@Inject
	protected ItemOperationsHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter,
			StateMachine stMachine, ItemOperationsProtocol protocol,
			CollectionDao collectionDao, WBXMLTools wbxmlTools,
			MailBackend mailBackend) {
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools);
		this.protocol = protocol;
		this.mailBackend = mailBackend;
	}
	
	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {

		try {
			ItemOperationsRequest itemOperationRequest = protocol.getRequest(request, doc);
			ItemOperationsResponse response = doTheJob(bs, itemOperationRequest);
			Document document = protocol.encodeResponse(response, bs);
			sendResponse(responder, document, response);
		} catch (CollectionNotFoundException e) {
			sendErrorResponse(responder, ItemOperationsStatus.DOCUMENT_LIBRARY_STORE_UNKNOWN, e);
		} catch (UnsupportedStoreException e) {
			sendErrorResponse(responder, ItemOperationsStatus.DOCUMENT_LIBRARY_STORE_UNKNOWN, e);
		} catch (ProcessingEmailException e) {
			sendErrorResponse(responder, ItemOperationsStatus.SERVER_ERROR, e);
		} 
	}
	
	private void sendErrorResponse(Responder responder, ItemOperationsStatus status, Exception exception) {
		logger.error(exception.getMessage(), exception);
		responder.sendWBXMLResponse(NAMESPACE, protocol.encodeErrorRespponse(status));
	}
	
	private void sendResponse(Responder responder, Document document, ItemOperationsResponse response) {
		
		if (response.isMultipart()) {
			responder.sendMSSyncMultipartResponse(NAMESPACE, document, 
					Arrays.asList(response.getAttachmentData()), response.isGzip());
		} else {
			responder.sendWBXMLResponse(NAMESPACE, document);
		}
	}
	
	private ItemOperationsResponse doTheJob(BackendSession bs, ItemOperationsRequest itemOperationRequest) throws CollectionNotFoundException, 
		UnsupportedStoreException, ProcessingEmailException {
		
		ItemOperationsResponse response = new ItemOperationsResponse();
		Fetch fetch = itemOperationRequest.getFetch();
		EmptyFolderContentsRequest emptyFolderContents = itemOperationRequest.getEmptyFolderContents();
		if (fetch != null) {
			response.setMailboxFetchResult(fetchOperation(bs, fetch));
		} else if (emptyFolderContents != null) {
			EmptyFolderContentsResult result = emptyFolderOperation(bs, emptyFolderContents);
			response.setEmptyFolderContentsResult(result);
		}
		response.setMultipart(itemOperationRequest.isMultipart());
		response.setGzip(itemOperationRequest.isGzip());
		return response;
	}

	private MailboxFetchResult fetchOperation(BackendSession bs, Fetch fetch) throws CollectionNotFoundException, UnsupportedStoreException, 
		ProcessingEmailException {
		
		final StoreName store = fetch.getStoreName();
		if (StoreName.Mailbox.equals(store)) {
			return processMailboxFetch(bs, fetch);
		} else {
			throw new UnsupportedStoreException();
		}
	}

	private MailboxFetchResult processMailboxFetch(BackendSession bs, Fetch fetch) throws CollectionNotFoundException, ProcessingEmailException {
		
		MailboxFetchResult mailboxFetchResponse = new MailboxFetchResult();
		if (fetch.getFileReference() != null) {
			
			FetchAttachmentResult fileReferenceFetch = processFileReferenceFetch(bs, fetch.getFileReference());
			mailboxFetchResponse.setFetchAttachmentResult(fileReferenceFetch);
			
		} else if (fetch.getCollectionId() != null && fetch.getServerId() != null) {
			try {
				Integer collectionId = Integer.valueOf(fetch.getCollectionId());
				mailboxFetchResponse.setFetchItemResult(fetchItem(fetch.getServerId(), collectionId, fetch.getType(), bs));
			} catch (NumberFormatException e) {
				throw new CollectionNotFoundException();
			}
		}
		return mailboxFetchResponse;
	}

	private FetchAttachmentResult processFileReferenceFetch(BackendSession bs, String reference) throws CollectionNotFoundException, ProcessingEmailException {

		FetchAttachmentResult fetchAttachmentResult = fetchAttachment(bs, reference);
		if (ItemOperationsStatus.SUCCESS.equals(fetchAttachmentResult.getStatus())) {
			fetchAttachmentResult.setReference(reference);
		}
		return fetchAttachmentResult;
	}

	private FetchAttachmentResult fetchAttachment(BackendSession bs, String reference) throws CollectionNotFoundException, 
		ProcessingEmailException {

		FetchAttachmentResult fetchResult = new FetchAttachmentResult();
		try {
			MSAttachementData data = mailBackend.getAttachment(bs, reference);
			fetchResult.setContentType(data.getContentType());
			try {
				fetchResult.setAttch(FileUtils.streamBytes(data.getFile(), true));
				fetchResult.setStatus(ItemOperationsStatus.SUCCESS);
			} catch (Throwable e) {
				fetchResult.setStatus(ItemOperationsStatus.MAILBOX_ITEM_FAILED_CONVERSATION);
			}
		} catch (AttachementNotFoundException e) {
			fetchResult.setStatus(ItemOperationsStatus.MAILBOX_INVALID_ATTACHMENT_ID);
		}

		return fetchResult;
	}

	private FetchItemResult fetchItem(String serverId, Integer collectionId, MSEmailBodyType type, BackendSession bs) {
		
		FetchItemResult fetchResult = new FetchItemResult();
		fetchResult.setServerId(serverId);
		try {
			String collectionPath = collectionDao.getCollectionPath(collectionId);
			PIMDataType dataType = PIMDataType.getPIMDataType(collectionPath);
			
			List<ItemChange> itemChanges = contentsExporter.fetch(bs, ImmutableList.of(serverId), dataType);
			if (itemChanges.isEmpty()) {
				fetchResult.setStatus(ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND);
			} else {
				fetchResult.setItemChange(itemChanges.get(0));
				SyncCollection c = new SyncCollection();
				c.setCollectionId(collectionId);
				BodyPreference bp = new BodyPreference();
				bp.setType(type);
				SyncCollectionOptions options = new SyncCollectionOptions();
				options.addBodyPreference(bp);
				c.setOptions(options);
				fetchResult.setSyncCollection(c);
				fetchResult.setStatus(ItemOperationsStatus.SUCCESS);
			}
		} catch (CollectionNotFoundException e) {
			fetchResult.setStatus(ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND);
		} catch (DaoException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (ProcessingEmailException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (PIMDataTypeNotFoundException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (UnexpectedObmSyncServerException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		}
		return fetchResult;
	}
	
	private EmptyFolderContentsResult emptyFolderOperation(
			BackendSession bs, EmptyFolderContentsRequest request) {
		
		EmptyFolderContentsResult emptyFolderContentsResult = new EmptyFolderContentsResult();
		try {
			String collectionPath = collectionDao.getCollectionPath(request.getCollectionId());
			contentsImporter.emptyFolderContent(bs, collectionPath, request.isDeleteSubFolderElem());
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SUCCESS);
		} catch (CollectionNotFoundException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.BLOCKED_ACCESS);
		} catch (NotAllowedException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.BLOCKED_ACCESS);
		} catch (DaoException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (ProcessingEmailException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (PIMDataTypeNotFoundException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SERVER_ERROR);
		}
		return emptyFolderContentsResult;
	}
	
}
