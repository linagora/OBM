package org.obm.push.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.MSAttachementData;
import org.obm.push.data.EncoderFactory;
import org.obm.push.data.IDataEncoder;
import org.obm.push.exception.NotAllowedException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.search.StoreName;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.BodyPreference;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.MSEmailBodyType;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncCollectionOptions;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the ItemOperations command
 */
@Singleton
public class ItemOperationsHandler extends WbxmlRequestHandler {

	@Inject
	private ItemOperationsHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter,
			StateMachine stMachine) {

		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		boolean multipart = isAcceptMultipart(request);
		boolean gzip = isAcceptGZip(request);

		try {
			Element fetch = DOMUtils.getUniqueElement(doc.getDocumentElement(),
					"Fetch");
			Element emptyFolder = DOMUtils.getUniqueElement(
					doc.getDocumentElement(), "EmptyFolderContents");

			if (fetch != null) {
				fetchOperation(bs, responder, multipart, gzip, fetch);
			} else if (emptyFolder != null) {
				emptyFolderOperation(emptyFolder, bs, responder);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private boolean isAcceptGZip(ActiveSyncRequest request) {
		String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
		return acceptEncoding != null
				&& acceptEncoding.contains(HttpHeaderValues.GZIP);
	}

	private boolean isAcceptMultipart(ActiveSyncRequest request) {
		return "T".equals(request.getHeader("MS-ASAcceptMultiPart"))
				|| "T".equalsIgnoreCase(request.getParameter("AcceptMultiPart"));
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
	private void emptyFolderOperation(Element emptyFolder, BackendSession bs,
			Responder responder) throws IOException {
		int collectionId = Integer.parseInt(DOMUtils.getElementText(
				emptyFolder, "CollectionId"));
		Element deleteSubFolderElem = DOMUtils.getUniqueElement(emptyFolder,
				"DeleteSubFolders");
		ItemOperationsStatus status = null;
		try {
			String collectionPath = storage.getCollectionPath(
					collectionId);
			contentsImporter.emptyFolderContent(
					bs, collectionPath, deleteSubFolderElem != null);
			status = ItemOperationsStatus.SUCCESS;
		} catch (CollectionNotFoundException e) {
			status = ItemOperationsStatus.BLOCKED_ACCESS;
		} catch (NotAllowedException e) {
			status = ItemOperationsStatus.BLOCKED_ACCESS;
		}

		Document ret = DOMUtils.createDoc(null, "ItemOperations");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status",
				ItemOperationsStatus.SUCCESS.asXmlValue());
		Element response = DOMUtils.createElement(root, "Response");
		Element empty = DOMUtils.createElement(response, "EmptyFolderContents");
		DOMUtils.createElementAndText(empty, "Status", status.asXmlValue());
		DOMUtils.createElementAndText(empty, "AirSync:CollectionId", ""
				+ collectionId);
		responder.sendResponse("ItemOperations", ret);
	}

	private void fetchOperation(BackendSession bs, Responder responder,
			boolean multipart, boolean gzip, Element fetch) throws IOException {
		StoreName store = StoreName.getValue(DOMUtils.getElementText(fetch,
				"Store"));
		Document ret = DOMUtils.createDoc(null, "ItemOperations");
		if (StoreName.Mailbox.equals(store)) {
			InputStream stream = processMailboxFetch(bs, fetch, multipart, ret);
			if (multipart) {
				responder.sendMSSyncMultipartResponse("ItemOperations", ret,
						ImmutableList.of(stream), gzip);
			} else {
				responder.sendResponse("ItemOperations", ret);
			}
		} else {
			logger.error("ItemOperations is not implemented for store " + store);
		}
	}

	private InputStream processMailboxFetch(BackendSession bs, Element fetch,
			boolean multipart, Document ret) {
		
		String reference = DOMUtils.getElementText(fetch, "FileReference");
		String collectionId = DOMUtils.getElementText(fetch, "CollectionId");
		String serverId = DOMUtils.getElementText(fetch, "ServerId");
		Integer type = Integer.getInteger(DOMUtils
				.getElementText(fetch, "Type"));
		
		if (reference != null) {
			
			return processFileReferenceFetch(bs, reference, multipart, ret);
		} else if (collectionId != null && serverId != null) {
			
			return processCollectionFetch(bs, multipart,
					Integer.parseInt(collectionId), serverId, type, ret);
		}
		return null;
	}

	private InputStream processCollectionFetch(BackendSession bs, boolean multipart,
			Integer collectionId, String serverId, Integer type, Document ret) {

		FetchResult fetchResult = fetchItem(serverId, collectionId, bs);

		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status",
				ItemOperationsStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status",
				fetchResult.status.asXmlValue());
		DOMUtils.createElementAndText(fetchResp, "AirSync:ServerId", serverId);
		DOMUtils.createElementAndText(fetchResp, "AirSync:CollectionId",
				collectionId.toString());
		if (ItemOperationsStatus.SUCCESS.equals(fetchResult.status)) {
			Element dataElem = DOMUtils.createElement(fetchResp, "Properties");

			SyncCollection c = new SyncCollection();
			c.setCollectionId(collectionId);
			BodyPreference bp = new BodyPreference();
			bp.setType(MSEmailBodyType.getValueOf(type));
			
			SyncCollectionOptions options = new SyncCollectionOptions();
			options.addBodyPreference(bp);
			c.setOptions(options);
			
			IDataEncoder encoder = getEncoders().getEncoder(
					fetchResult.ic.get(0).getData());
			encoder.encode(bs, dataElem, fetchResult.ic.get(0).getData(), c,
					true);
			if (multipart) {
				Element data = DOMUtils.getUniqueElement(dataElem,
						"AirSyncBase:Data");
				String dataValue = "";
				if (data != null) {
					dataValue = data.getTextContent();
					Element pData = (Element) data.getParentNode();
					pData.removeChild(data);
					DOMUtils.createElementAndText(pData, "Part", "1");
				}
				return new ByteArrayInputStream(dataValue.getBytes());
			}
		}
		return null;
	}

	private static class FetchResult {
		public List<ItemChange> ic;
		public ItemOperationsStatus status;
	}

	private FetchResult fetchItem(String serverId, Integer collectionId,
			BackendSession bs) {
		
		List<String> fetchIds = ImmutableList.of(serverId);
		FetchResult fetchResult = new FetchResult();
		fetchResult.status = ItemOperationsStatus.SUCCESS;
		fetchResult.ic = null;
		try {
			String collectionPath = storage.getCollectionPath(
					collectionId);
			PIMDataType dataType = storage.getDataClass(
					collectionPath);

			fetchResult.ic = contentsExporter.fetch(bs, dataType, fetchIds);
			if (fetchResult.ic.size() == 0) {
				fetchResult.status = ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND;
			}
		} catch (ActiveSyncException e) {
			fetchResult.status = ItemOperationsStatus.DOCUMENT_LIBRARY_NOT_FOUND;
		}
		return fetchResult;
	}

	private InputStream processFileReferenceFetch(BackendSession bs,
			String reference, boolean multipart, Document ret) {

		FetchAttachmentResult fetchAttachmentResult = fetchAttachment(bs, reference);

		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status",
				ItemOperationsStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status",
				fetchAttachmentResult.status.asXmlValue());
		if (ItemOperationsStatus.SUCCESS.equals(fetchAttachmentResult.status)) {
			DOMUtils.createElementAndText(fetchResp,
					"AirSyncBase:FileReference", reference);
			Element properties = DOMUtils
					.createElement(fetchResp, "Properties");
			DOMUtils.createElementAndText(properties,
					"AirSyncBase:ContentType",
					fetchAttachmentResult.contentType);
			if (!multipart) {
				DOMUtils.createElementAndText(properties, "Data",
						fetchAttachmentResult.attch);
			} else {
				DOMUtils.createElementAndText(properties, "Part", "1");
			}
		}
		return fetchAttachmentResult.stream;
	}

	private static class FetchAttachmentResult {
		public ItemOperationsStatus status;
		public String attch;
		public InputStream stream;
		public String contentType;

	}

	private FetchAttachmentResult fetchAttachment(BackendSession bs, String reference) {

		FetchAttachmentResult fetchResult = new FetchAttachmentResult();
		fetchResult.status = ItemOperationsStatus.SUCCESS;
		fetchResult.attch = "";
		MSAttachementData data = null;
		try {
			data = contentsExporter.getEmailAttachement(bs, reference);
			fetchResult.contentType = data.getContentType();
		} catch (ObjectNotFoundException e) {
			fetchResult.status = ItemOperationsStatus.MAILBOX_INVALID_ATTACHMENT_ID;
		}

		if (data != null) {
			try {
				byte[] att = FileUtils.streamBytes(data.getFile(), true);
				fetchResult.attch = new String(att);
				fetchResult.stream = new ByteArrayInputStream(att);
			} catch (Throwable e) {
				fetchResult.status = ItemOperationsStatus.MAILBOX_ITEM_FAILED_CONVERSATION;
			}
		}
		return fetchResult;
	}

}
