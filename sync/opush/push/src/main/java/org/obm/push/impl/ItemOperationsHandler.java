package org.obm.push.impl;

import java.util.List;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.ItemOperationsRequest;
import org.obm.push.bean.ItemOperationsRequest.EmptyFolderContentsRequest;
import org.obm.push.bean.ItemOperationsRequest.Fetch;
import org.obm.push.bean.ItemOperationsResponse;
import org.obm.push.bean.ItemOperationsResponse.EmptyFolderContentsResult;
import org.obm.push.bean.ItemOperationsResponse.MailboxFetchResult;
import org.obm.push.bean.ItemOperationsResponse.MailboxFetchResult.FetchAttachmentResult;
import org.obm.push.bean.ItemOperationsResponse.MailboxFetchResult.FetchItemResult;
import org.obm.push.bean.BodyPreference;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.data.EncoderFactory;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionOptions;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NotAllowedException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.exception.UnsupportedStoreException;
import org.obm.push.protocol.ItemOperationsProtocol;
import org.obm.push.search.StoreName;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ISyncStorage;
import org.obm.push.utils.FileUtils;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ItemOperationsHandler extends WbxmlRequestHandler {

	private final ItemOperationsProtocol protocol;

	@Inject
	protected ItemOperationsHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter,
			StateMachine stMachine, ItemOperationsProtocol protocol,
			CollectionDao collectionDao) {
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine, collectionDao);
		this.protocol = protocol;
	}

	
	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {

		try {
			ItemOperationsRequest itemOperationRequest = protocol.getRequest(request, doc);
			ItemOperationsResponse response = doTheJob(bs, itemOperationRequest);
			Document document = protocol.encodeResponse(response, bs);
			responder.sendResponse("ItemOperations", document);
		} catch (CollectionNotFoundException e) {
			logger.error("Collection not found");
			//DOCUMENT_LIBRARY_STORE_UNKNOWN
		} catch (UnsupportedStoreException e) {
			logger.error("ItemOperations is not implemented for this store");
			//DOCUMENT_LIBRARY_STORE_UNKNOWN
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}


	@Transactional
	private ItemOperationsResponse doTheJob(BackendSession bs, ItemOperationsRequest itemOperationRequest)
			throws CollectionNotFoundException, UnsupportedStoreException {
		
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

	private MailboxFetchResult fetchOperation(BackendSession bs, Fetch fetch) throws CollectionNotFoundException, UnsupportedStoreException {
		
		final StoreName store = fetch.getStoreName();
		if (StoreName.Mailbox.equals(store)) {
			return processMailboxFetch(bs, fetch);
		} else {
			throw new UnsupportedStoreException();
		}
	}

	private MailboxFetchResult processMailboxFetch(BackendSession bs, Fetch fetch) throws CollectionNotFoundException {
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

	private FetchAttachmentResult processFileReferenceFetch(BackendSession bs, String reference) {

		FetchAttachmentResult fetchAttachmentResult = fetchAttachment(bs, reference);
		if (ItemOperationsStatus.SUCCESS.equals(fetchAttachmentResult.getStatus())) {
			fetchAttachmentResult.setReference(reference);
		}
		return fetchAttachmentResult;
	}

	private FetchAttachmentResult fetchAttachment(BackendSession bs, String reference) {

		FetchAttachmentResult fetchResult = new FetchAttachmentResult();
		try {
			MSAttachementData data = contentsExporter.getEmailAttachement(bs, reference);
			fetchResult.setContentType(data.getContentType());
			try {
				fetchResult.setAttch(FileUtils.streamBytes(data.getFile(), true));
				fetchResult.setStatus(ItemOperationsStatus.SUCCESS);
			} catch (Throwable e) {
				fetchResult.setStatus(ItemOperationsStatus.MAILBOX_ITEM_FAILED_CONVERSATION);
			}
		} catch (ObjectNotFoundException e) {
			fetchResult.setStatus(ItemOperationsStatus.MAILBOX_INVALID_ATTACHMENT_ID);
		}

		return fetchResult;
	}

	private FetchItemResult fetchItem(String serverId, Integer collectionId, Integer type, BackendSession bs) {
		
		FetchItemResult fetchResult = new FetchItemResult();
		try {
			String collectionPath = collectionDao.getCollectionPath(collectionId);
			PIMDataType dataType = storage.getDataClass(collectionPath);
			
			List<ItemChange> itemChanges = contentsExporter.fetch(bs, dataType, ImmutableList.of(serverId));
			if (itemChanges.isEmpty()) {
				fetchResult.setStatus(ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND);
			} else {
				fetchResult.setItemChange(itemChanges.get(0));
				SyncCollection c = new SyncCollection();
				c.setCollectionId(collectionId);
				BodyPreference bp = new BodyPreference();
				bp.setType(MSEmailBodyType.getValueOf(type));
				SyncCollectionOptions options = new SyncCollectionOptions();
				options.addBodyPreference(bp);
				c.setOptions(options);
				fetchResult.setSyncCollection(c);
				fetchResult.setStatus(ItemOperationsStatus.SUCCESS);
			}
		} catch (CollectionNotFoundException e) {
			fetchResult.setStatus(ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND);
		} catch (ActiveSyncException e) {
			fetchResult.setStatus(ItemOperationsStatus.DOCUMENT_LIBRARY_CONNECTION_FAILED);
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
		}
		return emptyFolderContentsResult;
	}
	
}
