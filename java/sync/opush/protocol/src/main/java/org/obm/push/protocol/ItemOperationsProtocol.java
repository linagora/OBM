package org.obm.push.protocol;

import org.eclipse.jetty.http.HttpHeaderValues;
import org.eclipse.jetty.http.HttpHeaders;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.StoreName;
import org.obm.push.protocol.bean.ItemOperationsRequest;
import org.obm.push.protocol.bean.ItemOperationsRequest.EmptyFolderContentsRequest;
import org.obm.push.protocol.bean.ItemOperationsRequest.Fetch;
import org.obm.push.protocol.bean.ItemOperationsResponse;
import org.obm.push.protocol.bean.ItemOperationsResponse.EmptyFolderContentsResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult.FetchAttachmentResult;
import org.obm.push.protocol.bean.ItemOperationsResponse.MailboxFetchResult.FetchItemResult;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.data.IDataEncoder;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ItemOperationsProtocol {

	private final EncoderFactory encoderFactory;

	@Inject
	/* package */ ItemOperationsProtocol(EncoderFactory encoderFactory) {
		this.encoderFactory = encoderFactory;
	}
	
	public ItemOperationsRequest getRequest(ActiveSyncRequest request, Document document) {
		boolean multipart = isAcceptMultipart(request);
		boolean gzip = isAcceptGZip(request);
		Element root = document.getDocumentElement();
		Fetch fetch = buildFetch(root);
		EmptyFolderContentsRequest emptyFolderContents = buildEmptyFolderContents(root);
		
		ItemOperationsRequest itemOperationsRequest = new ItemOperationsRequest();
		itemOperationsRequest.setFetch(fetch);
		itemOperationsRequest.setEmptyFolderContents(emptyFolderContents);
		itemOperationsRequest.setMultipart(multipart);
		itemOperationsRequest.setGzip(gzip);
		return itemOperationsRequest;
	}

	/* package */ Fetch buildFetch(Element root) {
		Element fetchNode = DOMUtils.getUniqueElement(root, "Fetch");
		if (fetchNode != null) {
			StoreName storeName = StoreName.getValue(DOMUtils.getElementText(fetchNode,	"Store"));
			String reference = DOMUtils.getElementText(fetchNode, "FileReference");
			String collectionId = DOMUtils.getElementText(fetchNode, "CollectionId");
			String serverId = DOMUtils.getElementText(fetchNode, "ServerId");
			Integer type = Integer.valueOf(DOMUtils.getElementText(fetchNode, "Type"));

			Fetch fetch = new Fetch();
			fetch.setStoreName(storeName);
			fetch.setFileReference(reference);
			fetch.setCollectionId(collectionId);
			fetch.setServerId(serverId);
			fetch.setType(type);
			return fetch;
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

	private boolean isAcceptGZip(ActiveSyncRequest request) {
		String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
		return acceptEncoding != null
				&& acceptEncoding.contains(HttpHeaderValues.GZIP);
	}

	private boolean isAcceptMultipart(ActiveSyncRequest request) {
		return "T".equals(request.getHeader("MS-ASAcceptMultiPart"))
				|| "T".equalsIgnoreCase(request.getParameter("AcceptMultiPart"));
	}

	public Document encodeResponse(ItemOperationsResponse response, BackendSession bs) {
		Document document = DOMUtils.createDoc(null, "ItemOperations");
		Element root = document.getDocumentElement();
		if (response.getEmptyFolderContentsResult() != null) {
			encodeEmptyFolderOperation(response.getEmptyFolderContentsResult(), root);
		} else if (response.getMailboxFetchResult() != null) {
			encodeMailboxFetchResult(response.getMailboxFetchResult(), root, response.isMultipart(), bs);
		}
		return document;
	}
	
	private void encodeEmptyFolderOperation(EmptyFolderContentsResult result, Element root) {
		DOMUtils.createElementAndText(root, "Status", ItemOperationsStatus.SUCCESS.asXmlValue());
		Element response = DOMUtils.createElement(root, "Response");
		Element empty = DOMUtils.createElement(response, "EmptyFolderContents");
		DOMUtils.createElementAndText(empty, "Status", result.getItemOperationsStatus().asXmlValue());
		DOMUtils.createElementAndText(empty, "AirSync:CollectionId", String.valueOf(result.getCollectionId()));
	}

	private void encodeMailboxFetchResult(
			MailboxFetchResult mailboxFetchResult, Element root, boolean multipart, BackendSession bs) {
		
		if (mailboxFetchResult.getFetchItemResult() != null) {
			encodeFetchItemResult(bs, root, mailboxFetchResult.getFetchItemResult());
		} else if (mailboxFetchResult.getFileReferenceFetch() != null) {
			encodeFetchAttachmentResult(root, mailboxFetchResult.getFileReferenceFetch(), multipart);
		}
	}

	
	private void encodeFetchItemResult(BackendSession bs, Element root, FetchItemResult fetchItemResult) {
		DOMUtils.createElementAndText(root, "Status",
				ItemOperationsStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status", fetchItemResult.getStatus().asXmlValue());
		DOMUtils.createElementAndText(fetchResp, "AirSync:ServerId", fetchItemResult.getServerId());

		if (ItemOperationsStatus.SUCCESS == fetchItemResult.getStatus() &&
				fetchItemResult.getSyncCollection() != null) {
			Element dataElem = DOMUtils.createElement(fetchResp, "Properties");
			IApplicationData data = fetchItemResult.getItemChange().getData();
			IDataEncoder encoder = encoderFactory.getEncoder(data);
			encoder.encode(bs, dataElem, data, fetchItemResult.getSyncCollection(),	true);
		}
	}
	
	private void encodeFetchAttachmentResult(Element root, FetchAttachmentResult fetchAttachmentResult, boolean multipart) {
		DOMUtils.createElementAndText(root, "Status", ItemOperationsStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(root, "Response");
		Element fetchResp = DOMUtils.createElement(resp, "Fetch");
		DOMUtils.createElementAndText(fetchResp, "Status", fetchAttachmentResult.getStatus().asXmlValue());
		
		if (fetchAttachmentResult.getReference() != null) {
			DOMUtils.createElementAndText(fetchResp, "AirSyncBase:FileReference", fetchAttachmentResult.getReference());
		}
		if (fetchAttachmentResult.getContentType() != null) {
			Element properties = DOMUtils.createElement(fetchResp, "Properties");
			DOMUtils.createElementAndText(properties, "AirSyncBase:ContentType", fetchAttachmentResult.getContentType());
			if (!multipart) {
				DOMUtils.createElementAndText(properties, "Data", new String(fetchAttachmentResult.getAttch()));
			} else {
				DOMUtils.createElementAndText(properties, "Part", "1");
			}
		}
	}
	
	public Document encodeErrorRespponse(ItemOperationsStatus status) {
		Document document = DOMUtils.createDoc(null, "ItemOperations");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", status.asXmlValue());
		return document;
	}
	
}
