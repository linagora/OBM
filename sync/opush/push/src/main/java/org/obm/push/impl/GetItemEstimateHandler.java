package org.obm.push.impl;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.UnsynchronizedItemService;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.data.EncoderFactory;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.FilterType;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncCollectionOptions;
import org.obm.push.store.SyncState;
import org.obm.push.tnefconverter.RTFUtils;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetItemEstimateHandler extends WbxmlRequestHandler {

	private final UnsynchronizedItemService unSynchronizedItemCache;

	@Inject
	private GetItemEstimateHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter, StateMachine stMachine,
			UnsynchronizedItemService unSynchronizedItemCache) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
		this.unSynchronizedItemCache = unSynchronizedItemCache;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");

		final List<SyncCollection> syncCollections = createListSyncCollection(
				bs, doc);

		final Document document = DOMUtils.createDoc(null, "GetItemEstimate");

		final Element rootElement = document.getDocumentElement();

		try {
			for (final SyncCollection syncCollection : syncCollections) {
				createResponse(bs, rootElement, syncCollection);
			}

			responder.sendResponse("ItemEstimate", document);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void createResponse(BackendSession bs,
			final Element root,
			final SyncCollection syncCollection) throws DOMException, SQLException {
		
		final Element responseElement = DOMUtils
				.createElement(root, "Response");
		try {

			Integer collectionId = syncCollection.getCollectionId();
			if (collectionId != null) {

				final SyncState state = stMachine.getSyncState(
						syncCollection.getCollectionId(),
						syncCollection.getSyncKey());
				if (!state.isValid()) {

					buildError(responseElement, syncCollection
							.getCollectionId().toString(),
							GetItemEstimateStatus.INVALID_SYNC_KEY);
				} else {

					createStatusElement(responseElement);
					final Element collectionElement = createCollectionElement(responseElement);
					createClassElement(syncCollection, collectionElement);
					createCollectionIdElement(syncCollection, collectionElement);
					createEstimateElement(bs, syncCollection, collectionId, state,
							collectionElement);
				}

			} else {
				logger.warn("no mapping for collection with id "
						+ syncCollection.getCollectionId());
				throw new CollectionNotFoundException();
			}

		} catch (CollectionNotFoundException e) {
			buildError(responseElement, syncCollection.getCollectionId()
					.toString(), GetItemEstimateStatus.INVALID_COLLECTION);
		} catch (ActiveSyncException e) {
			buildError(responseElement, syncCollection.getCollectionId()
					.toString(), GetItemEstimateStatus.INVALID_COLLECTION);
		}
	}

	private void createStatusElement(final Element responseElement) {
		DOMUtils.createElementAndText(responseElement, "Status", "1");
	}

	private Element createCollectionElement(final Element responseElement) {
		return DOMUtils.createElement(responseElement, "Collection");
	}

	private void createClassElement(SyncCollection syncCollection,
			Element collectionElement) {
		if (syncCollection.getDataClass() != null) {
			DOMUtils.createElementAndText(collectionElement, "Class",
					syncCollection.getDataClass());
		}
	}

	private void createCollectionIdElement(SyncCollection syncCollection,
			Element collectionElement) {
		DOMUtils.createElementAndText(collectionElement, "CollectionId",
				syncCollection.getCollectionId().toString());
	}

	private void createEstimateElement(BackendSession bs,
			SyncCollection syncCollection, Integer collectionId,
			SyncState state, Element collectionElement)
			throws ActiveSyncException, DOMException, SQLException {

		int unSynchronizedItemNb = unSynchronizedItemCache.list(bs.getCredentials(), syncCollection.getCollectionId()).size();
		int count = contentsExporter.getCount(bs, state, syncCollection.getOptions().getFilterType(), collectionId);

		Element estim = DOMUtils.createElement(collectionElement, "Estimate");
		estim.setTextContent(String.valueOf(count + unSynchronizedItemNb));
	}

	private List<SyncCollection> createListSyncCollection(BackendSession bs,
			Document doc) {
		final List<SyncCollection> syncCollections = new LinkedList<SyncCollection>();
		final NodeList collections = doc.getDocumentElement()
				.getElementsByTagName("Collection");
		for (int i = 0; i < collections.getLength(); i++) {

			final Element ce = (Element) collections.item(i);
			final String dataClass = DOMUtils.getElementText(ce, "Class");
			final String filterType = DOMUtils.getElementText(ce, "FilterType");
			final String syncKey = DOMUtils.getElementText(ce, "SyncKey");

			final Element fid = DOMUtils.getUniqueElement(ce, "CollectionId");

			String collectionId = null;
			if (fid == null) {
				collectionId = RTFUtils.getFolderId(bs.getDevId(), dataClass);
			} else {
				collectionId = fid.getTextContent();
			}

			final SyncCollection sc = new SyncCollection();
			sc.setDataClass(dataClass);
			sc.setSyncKey(syncKey);
			sc.setCollectionId(Integer.parseInt(collectionId));
			SyncCollectionOptions options = new SyncCollectionOptions();
			options.setFilterType(FilterType.getFilterType(filterType));
			sc.setOptions(options);

			try {
				String collectionPath = storage.getCollectionPath(
						Integer.valueOf(collectionId));
				PIMDataType dataType = storage.getDataClass(
						collectionPath);

				sc.setCollectionPath(collectionPath);
				sc.setDataType(dataType);
			} catch (NumberFormatException e) {
				buildError(ce, collectionId,
						GetItemEstimateStatus.INVALID_COLLECTION);
			} catch (CollectionNotFoundException e) {
				buildError(ce, collectionId,
						GetItemEstimateStatus.INVALID_COLLECTION);
			}

			syncCollections.add(sc);
		}
		return syncCollections;
	}

	private void buildError(Element response, String collectionId,
			GetItemEstimateStatus status) {
		DOMUtils.createElementAndText(response, "Status", status.asXmlValue());
		Element ce = DOMUtils.createElement(response, "Collection");
		DOMUtils.createElementAndText(ce, "CollectionId", collectionId);
	}

}
