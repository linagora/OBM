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

import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.StoreName;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedStoreException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.mail.ImapTimeoutException;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ItemOperationsHandler extends WbxmlRequestHandler {

	private static final String NAMESPACE = "ItemOperations";
	private final ItemOperationsProtocol.Factory protocolFactory;
	private final MailBackend mailBackend;
	private final CollectionPathHelper collectionPathHelper;

	@Inject
	protected ItemOperationsHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter,
			StateMachine stMachine, ItemOperationsProtocol.Factory protocolFactory,
			CollectionDao collectionDao, WBXMLTools wbxmlTools,
			MailBackend mailBackend, DOMDumper domDumper, CollectionPathHelper collectionPathHelper) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.protocolFactory = protocolFactory;
		this.mailBackend = mailBackend;
		this.collectionPathHelper = collectionPathHelper;
	}
	
	@Override
	public void process(IContinuation continuation, UserDataRequest udr,
			Document doc, ActiveSyncRequest request, Responder responder) {

		boolean acceptMultipart = isAcceptMultipart(request);
		boolean acceptGZip = isAcceptGZip(request);
		ItemOperationsProtocol protocol = protocolFactory.create(udr.getDevice(), acceptMultipart);
		try {
			ItemOperationsRequest itemOperationRequest = protocol.decodeRequest(doc);
			ItemOperationsResponse response = doTheJob(udr, itemOperationRequest);
			Document document = protocol.encodeResponse(response);
			sendResponse(responder, document, response, acceptGZip, acceptMultipart);
		} catch (CollectionNotFoundException e) {
			sendErrorResponse(responder, protocol, ItemOperationsStatus.DOCUMENT_LIBRARY_STORE_UNKNOWN, e);
		} catch (UnsupportedStoreException e) {
			sendErrorResponse(responder, protocol, ItemOperationsStatus.DOCUMENT_LIBRARY_STORE_UNKNOWN, e);
		} catch (ProcessingEmailException e) {
			sendErrorResponse(responder, protocol, ItemOperationsStatus.SERVER_ERROR, e);
		} catch (ProtocolException e) {
			sendErrorResponse(responder, protocol, ItemOperationsStatus.SERVER_ERROR, e);
		} catch (ImapTimeoutException e) {
			sendErrorResponse(responder, protocol, ItemOperationsStatus.SERVER_ERROR, e);
		}
	}
	
	private void sendErrorResponse(Responder responder, ItemOperationsProtocol protocol,
			ItemOperationsStatus status, Exception exception) {
		
		logger.error(exception.getMessage(), exception);
		responder.sendWBXMLResponse(NAMESPACE, protocol.encodeErrorRespponse(status));
	}
	
	@VisibleForTesting void sendResponse(Responder responder, Document document, ItemOperationsResponse response,
			boolean isGzip, boolean isMultipart) {
		
		if (isMultipart && response.hasFileReference()) {
			responder.sendMSSyncMultipartResponse(NAMESPACE, document, 
					Arrays.asList(response.getAttachmentData()), isGzip);
		} else {
			responder.sendWBXMLResponse(NAMESPACE, document);
		}
	}
	
	private ItemOperationsResponse doTheJob(UserDataRequest udr, ItemOperationsRequest itemOperationRequest)
			throws CollectionNotFoundException, UnsupportedStoreException, ProcessingEmailException {
		
		ItemOperationsResponse response = new ItemOperationsResponse();
		Fetch fetch = itemOperationRequest.getFetch();
		EmptyFolderContentsRequest emptyFolderContents = itemOperationRequest.getEmptyFolderContents();
		if (fetch != null) {
			response.setMailboxFetchResult(fetchOperation(udr, fetch));
		} else if (emptyFolderContents != null) {
			EmptyFolderContentsResult result = emptyFolderOperation(udr, emptyFolderContents);
			response.setEmptyFolderContentsResult(result);
		}
		return response;
	}

	private MailboxFetchResult fetchOperation(UserDataRequest udr, Fetch fetch)
			throws CollectionNotFoundException, UnsupportedStoreException, ProcessingEmailException {
		
		final StoreName store = fetch.getStoreName();
		if (StoreName.Mailbox.equals(store)) {
			return processMailboxFetch(udr, fetch);
		} else {
			throw new UnsupportedStoreException();
		}
	}

	private MailboxFetchResult processMailboxFetch(UserDataRequest udr, Fetch fetch)
			throws CollectionNotFoundException, ProcessingEmailException {
		
		MailboxFetchResult mailboxFetchResponse = new MailboxFetchResult();
		if (fetch.getFileReference() != null) {
			
			FetchAttachmentResult fileReferenceFetch = processFileReferenceFetch(udr, fetch.getFileReference());
			mailboxFetchResponse.setFetchAttachmentResult(fileReferenceFetch);
			
		} else if (fetch.getCollectionId() != null && fetch.getServerId() != null) {
			try {
				Integer collectionId = Integer.valueOf(fetch.getCollectionId());
				mailboxFetchResponse.setFetchItemResult(fetchItem(fetch.getServerId(), collectionId, fetch.getType(), udr));
			} catch (NumberFormatException e) {
				throw new CollectionNotFoundException(e);
			}
		}
		return mailboxFetchResponse;
	}

	private FetchAttachmentResult processFileReferenceFetch(UserDataRequest udr, String reference) throws CollectionNotFoundException, ProcessingEmailException {

		FetchAttachmentResult fetchAttachmentResult = fetchAttachment(udr, reference);
		if (ItemOperationsStatus.SUCCESS.equals(fetchAttachmentResult.getStatus())) {
			fetchAttachmentResult.setReference(reference);
		}
		return fetchAttachmentResult;
	}

	private FetchAttachmentResult fetchAttachment(UserDataRequest udr, String reference) throws CollectionNotFoundException, 
		ProcessingEmailException {

		FetchAttachmentResult fetchResult = new FetchAttachmentResult();
		try {
			MSAttachementData data = mailBackend.getAttachment(udr, reference);
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

	private FetchItemResult fetchItem(String serverId, Integer collectionId, 
			MSEmailBodyType type, UserDataRequest udr) {
		
		FetchItemResult fetchResult = new FetchItemResult();
		fetchResult.setServerId(serverId);
		try {
			String collectionPath = collectionDao.getCollectionPath(collectionId);
			PIMDataType dataType = collectionPathHelper.recognizePIMDataType(collectionPath);
			
			List<BodyPreference> bodyPreferences = bodyPreferencesFromBodyType(type);

			SyncCollection syncCollection = new SyncCollection(dataType, ImmutableList.of(serverId), bodyPreferences);
			syncCollection.setCollectionId(collectionId);
			syncCollection.setCollectionPath(collectionPath);
			syncCollection.setOptions(new SyncCollectionOptions(bodyPreferences));
			
			List<ItemChange> itemChanges = contentsExporter.fetch(udr, syncCollection);
			
			if (itemChanges.isEmpty()) {
				fetchResult.setStatus(ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND);
			} else {
				fetchResult.setItemChange(itemChanges.get(0));
				fetchResult.setSyncCollection(syncCollection);
				fetchResult.setStatus(ItemOperationsStatus.SUCCESS);
			}
		} catch (CollectionNotFoundException e) {
			fetchResult.setStatus(ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND);
		} catch (DaoException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (ProcessingEmailException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (CollectionPathException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (UnexpectedObmSyncServerException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (ConversionException e) {
			fetchResult.setStatus(ItemOperationsStatus.SERVER_ERROR);
		}
		return fetchResult;
	}

	private List<BodyPreference> bodyPreferencesFromBodyType(MSEmailBodyType type) {
		if (type != null) {
			return ImmutableList.of(BodyPreference.builder().bodyType(type).build());
		}
		return ImmutableList.of();
	}
	
	private EmptyFolderContentsResult emptyFolderOperation(
			UserDataRequest udr, EmptyFolderContentsRequest request) {
		
		EmptyFolderContentsResult emptyFolderContentsResult = new EmptyFolderContentsResult();
		try {
			String collectionPath = collectionDao.getCollectionPath(request.getCollectionId());
			contentsImporter.emptyFolderContent(udr, collectionPath, request.isDeleteSubFolderElem());
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SUCCESS);
		} catch (CollectionNotFoundException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.BLOCKED_ACCESS);
		} catch (NotAllowedException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.BLOCKED_ACCESS);
		} catch (DaoException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (ProcessingEmailException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SERVER_ERROR);
		} catch (CollectionPathException e) {
			emptyFolderContentsResult.setItemOperationsStatus(ItemOperationsStatus.SERVER_ERROR);
		}
		return emptyFolderContentsResult;
	}

	@VisibleForTesting static boolean isAcceptGZip(ActiveSyncRequest request) {
		String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
		return acceptEncoding != null
				&& acceptEncoding.contains(HttpHeaderValues.GZIP);
	}

	@VisibleForTesting static boolean isAcceptMultipart(ActiveSyncRequest request) {
		return "T".equals(request.getHeader("MS-ASAcceptMultiPart"))
				|| "T".equalsIgnoreCase(request.getParameter("AcceptMultiPart"));
	}
}
