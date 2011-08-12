package org.obm.push.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.obm.annotations.transactional.Propagation;
import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.GetItemEstimateStatus;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.InvalidSyncKeyException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.protocol.GetItemEstimateProtocol;
import org.obm.push.protocol.bean.GetItemEstimateRequest;
import org.obm.push.protocol.bean.GetItemEstimateResponse;
import org.obm.push.protocol.bean.GetItemEstimateResponse.Estimate;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetItemEstimateHandler extends WbxmlRequestHandler {

	private final UnsynchronizedItemDao unSynchronizedItemCache;
	private final GetItemEstimateProtocol protocol;

	@Inject
	protected GetItemEstimateHandler(IBackend backend,
			EncoderFactory encoderFactory, IContentsImporter contentsImporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			UnsynchronizedItemDao unSynchronizedItemCache, CollectionDao collectionDao,
			GetItemEstimateProtocol protocol) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao);
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
			sendResponse(responder, document);

		} catch (InvalidSyncKeyException e) {
			sendErrorResponse(responder, 
					protocol.buildError(GetItemEstimateStatus.INVALID_SYNC_KEY, e.getCollectionId()), e);
		} catch (CollectionNotFoundException e) {
			sendErrorResponse(responder, 
					protocol.buildError(GetItemEstimateStatus.INVALID_COLLECTION, e.getCollectionId()), e);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
		} catch (UnknownObmSyncServerException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sendErrorResponse(Responder responder, Document document, Exception exception) {
		logger.error(exception.getMessage(), exception);
		sendResponse(responder, document);
	}
	
	private void sendResponse(Responder responder, Document document) {
		try {
			responder.sendResponse("GetItemEstimate", document);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional(propagation=Propagation.NESTED)
	private GetItemEstimateResponse doTheJob(BackendSession bs, GetItemEstimateRequest request) throws InvalidSyncKeyException, DaoException, CollectionNotFoundException, UnknownObmSyncServerException {
		
		final ArrayList<Estimate> estimates = new ArrayList<GetItemEstimateResponse.Estimate>();
		for (SyncCollection syncCollection: request.getSyncCollections()) {
			Integer collectionId = syncCollection.getCollectionId();
			String collectionPath = collectionDao.getCollectionPath(collectionId);
			PIMDataType dataType = PIMDataType.getPIMDataType(collectionPath);
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
