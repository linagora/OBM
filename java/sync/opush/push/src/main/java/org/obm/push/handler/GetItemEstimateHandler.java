package org.obm.push.handler;

import java.util.ArrayList;
import java.util.Set;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.GetItemEstimateStatus;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.InvalidSyncKeyException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.Responder;
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
			logger.warn(e.getMessage(), e);
			sendResponse(responder, 
					protocol.buildError(GetItemEstimateStatus.INVALID_SYNC_KEY, e.getCollectionId()));
		} catch (CollectionNotFoundException e) {
			sendErrorResponse(responder, 
					protocol.buildError(GetItemEstimateStatus.INVALID_COLLECTION, e.getCollectionId()), e);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
		} catch (UnknownObmSyncServerException e) {
			logger.error(e.getMessage(), e);
		} catch (ProcessingEmailException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sendErrorResponse(Responder responder, Document document, Exception exception) {
		logger.error(exception.getMessage(), exception);
		sendResponse(responder, document);
	}
	
	private void sendResponse(Responder responder, Document document) {
		responder.sendResponse("GetItemEstimate", document);
	}

	private GetItemEstimateResponse doTheJob(BackendSession bs, GetItemEstimateRequest request) throws InvalidSyncKeyException, DaoException, CollectionNotFoundException, 
		UnknownObmSyncServerException, ProcessingEmailException {
		
		final ArrayList<Estimate> estimates = new ArrayList<GetItemEstimateResponse.Estimate>();
		
		for (SyncCollection syncCollection: request.getSyncCollections()) {
		
			Integer collectionId = syncCollection.getCollectionId();
			String collectionPath = collectionDao.getCollectionPath(collectionId);
			
			syncCollection.setCollectionPath(collectionPath);
			syncCollection.setDataType( PIMDataType.getPIMDataType(collectionPath) );

			String syncKey = syncCollection.getSyncKey();
			SyncState state = stMachine.getSyncState(syncKey);
			if (state == null) {
				throw new InvalidSyncKeyException(syncKey);
			}
			
			int unSynchronizedItemNb = listItemToAddSize(bs, syncCollection);
			int count = contentsExporter.getItemEstimateSize(bs, syncCollection.getOptions().getFilterType(), collectionId, state);
			estimates.add( new Estimate(syncCollection, count + unSynchronizedItemNb) );
		}
		
		return new GetItemEstimateResponse(estimates);
	}

	private int listItemToAddSize(BackendSession bs, SyncCollection syncCollection) {
		Set<ItemChange> listItemToAdd = unSynchronizedItemCache.listItemsToAdd(bs.getCredentials(), 
				bs.getDevice(), syncCollection.getCollectionId());
		return listItemToAdd.size();
	}

}
