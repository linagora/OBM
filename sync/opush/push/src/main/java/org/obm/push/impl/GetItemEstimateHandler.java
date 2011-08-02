package org.obm.push.impl;

import java.sql.SQLException;
import java.util.ArrayList;

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
import org.obm.push.protocol.GetItemEstimateProtocol;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncState;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetItemEstimateHandler extends WbxmlRequestHandler {

	private final UnsynchronizedItemService unSynchronizedItemCache;
	private final GetItemEstimateProtocol protocol;

	@Inject
	protected GetItemEstimateHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter, StateMachine stMachine,
			UnsynchronizedItemService unSynchronizedItemCache, GetItemEstimateProtocol protocol) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
		this.unSynchronizedItemCache = unSynchronizedItemCache;
		this.protocol = protocol;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {

		try {
			GetItemEstimateRequest estimateRequest = protocol.getRequest(doc);
			GetItemEstimateResponse response = doTheJob(bs, estimateRequest);
			Document document = protocol.encodeResponse(response);
			responder.sendResponse("ItemEstimate", document);

		} catch (InvalidSyncKeyException e) {
			Integer collectionId = e.getCollectionId();
			protocol.buildError(GetItemEstimateStatus.INVALID_SYNC_KEY, collectionId);
		} catch (CollectionNotFoundException e) {
			Integer collectionId = e.getCollectionId();
			protocol.buildError(GetItemEstimateStatus.INVALID_COLLECTION, collectionId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional
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

}
