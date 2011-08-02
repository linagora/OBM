package org.obm.push.impl;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.UnsynchronizedItemService;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.GetItemEstimateRequest;
import org.obm.push.bean.GetItemEstimateResponse;
import org.obm.push.bean.GetItemEstimateResponse.Estimate;
import org.obm.push.data.EncoderFactory;
import org.obm.push.exception.InvalidSyncKeyException;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.FilterType;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncCollectionOptions;
import org.obm.push.store.SyncState;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetItemEstimateHandler extends WbxmlRequestHandler {

	private final UnsynchronizedItemService unSynchronizedItemCache;

	@Inject
	protected GetItemEstimateHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter, StateMachine stMachine,
			UnsynchronizedItemService unSynchronizedItemCache) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
		this.unSynchronizedItemCache = unSynchronizedItemCache;
	}

	@Override
	@Transactional
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {

		try {
			GetItemEstimateRequest estimateRequest = getRequest(doc);
			GetItemEstimateResponse response = doTheJob(bs, estimateRequest);
			Document document = createResponse(response);
			responder.sendResponse("ItemEstimate", document);

		} catch (InvalidSyncKeyException e) {
			Integer collectionId = e.getCollectionId();
			buildError(GetItemEstimateStatus.INVALID_SYNC_KEY, collectionId);
		} catch (CollectionNotFoundException e) {
			Integer collectionId = e.getCollectionId();
			buildError(GetItemEstimateStatus.INVALID_COLLECTION, collectionId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private GetItemEstimateRequest getRequest(Document doc) throws CollectionNotFoundException {
		final NodeList collections = doc.getDocumentElement().getElementsByTagName("Collection");
		int nbElements = collections.getLength();
		final ArrayList<SyncCollection> syncCollections = new ArrayList<SyncCollection>(nbElements);
		for (int i = 0; i < nbElements; i++) {
	
			final Element ce = (Element) collections.item(i);
			final String dataClass = DOMUtils.getElementText(ce, "Class");
			final String filterType = DOMUtils.getElementText(ce, "FilterType");
			final String syncKey = DOMUtils.getElementText(ce, "SyncKey");
			final Element fid = DOMUtils.getUniqueElement(ce, "CollectionId");
			final String collectionId = fid.getTextContent();
	
			final SyncCollection sc = new SyncCollection();
			sc.setDataClass(dataClass);
			sc.setSyncKey(syncKey);
			SyncCollectionOptions options = new SyncCollectionOptions();
			options.setFilterType(FilterType.getFilterType(filterType));
			sc.setOptions(options);
			try {
				sc.setCollectionId(Integer.valueOf(collectionId));
			} catch (NumberFormatException e) {
				throw new CollectionNotFoundException(e);
			}
			syncCollections.add(sc);
		}
		return new GetItemEstimateRequest(syncCollections);
	}

	private GetItemEstimateResponse doTheJob(BackendSession bs, GetItemEstimateRequest request) throws InvalidSyncKeyException, ActiveSyncException, SQLException {
		
		final ArrayList<Estimate> estimates = new ArrayList<GetItemEstimateResponse.Estimate>();
		for (SyncCollection syncCollection: request.getSyncCollections()) {
			Integer collectionId = syncCollection.getCollectionId();
			String collectionPath = storage.getCollectionPath(collectionId);
			PIMDataType dataType = storage.getDataClass(collectionPath);
			syncCollection.setCollectionPath(collectionPath);
			syncCollection.setDataType(dataType);

			final SyncState state = stMachine.getSyncState(
					syncCollection.getCollectionId(),
					syncCollection.getSyncKey());
			if (!state.isValid()) {
				throw new InvalidSyncKeyException();
			}
			int unSynchronizedItemNb = unSynchronizedItemCache.listItemToAdd(bs.getCredentials(), bs.getDevice(), syncCollection.getCollectionId()).size();
			int count = contentsExporter.getCount(bs, state, syncCollection.getOptions().getFilterType(), collectionId);
			int estimate = count + unSynchronizedItemNb;
			estimates.add(new Estimate(syncCollection, estimate));
		}
		return new GetItemEstimateResponse(estimates);
	}
	
	private Document createResponse(GetItemEstimateResponse response) {
		
		final Document document = createDocument();
		final Element responseElement = createResponseNode(document);
		DOMUtils.createElementAndText(responseElement, "Status", "1");
		for (Estimate estimate: response.getEstimates()) {
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

	
	private void createCollectionIdElement(SyncCollection syncCollection, Element collectionElement) {
		DOMUtils.createElementAndText(collectionElement, "CollectionId",
				syncCollection.getCollectionId().toString());
	}

	private void createEstimateElement(int estimate, Element collectionElement) {
		Element estim = DOMUtils.createElement(collectionElement, "Estimate");
		estim.setTextContent(String.valueOf(estimate));
	}

	private Document buildError(GetItemEstimateStatus status, Integer collectionId) {
		Document document = createDocument();
		Element responseNode = createResponseNode(document);
		DOMUtils.createElementAndText(responseNode, "Status", status.asXmlValue());
		if (collectionId != null) {
			Element ce = DOMUtils.createElement(responseNode, "Collection");
			DOMUtils.createElementAndText(ce, "CollectionId", String.valueOf(collectionId));
		}
		return document;
	}

}
