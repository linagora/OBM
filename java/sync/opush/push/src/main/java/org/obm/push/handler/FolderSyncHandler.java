package org.obm.push.handler;

import java.util.List;

import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Device;
import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.InvalidSyncKeyException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.FolderSyncProtocol;
import org.obm.push.protocol.bean.FolderSyncRequest;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FolderSyncHandler extends WbxmlRequestHandler {

	private final IHierarchyExporter hierarchyExporter;
	private final FolderSyncProtocol protocol;
	
	@Inject
	protected FolderSyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IHierarchyExporter hierarchyExporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			CollectionDao collectionDao, FolderSyncProtocol protocol) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao);
		
		this.hierarchyExporter = hierarchyExporter;
		this.protocol = protocol;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		
		try {
			FolderSyncRequest folderSyncRequest = protocol.getRequest(doc);
			FolderSyncResponse folderSyncResponse = doTheJob(bs, folderSyncRequest);
			Document ret = protocol.encodeResponse(folderSyncResponse);
			sendResponse(responder, ret);
			
		} catch (InvalidSyncKeyException e) {
			logger.warn(e.getMessage(), e);
			sendResponse(responder, protocol.encodeErrorResponse(FolderSyncStatus.INVALID_SYNC_KEY));
		} catch (NoDocumentException e) {
			sendError(responder, FolderSyncStatus.INVALID_REQUEST, e);
		} catch (DaoException e) {
			sendError(responder, FolderSyncStatus.SERVER_ERROR, e);
		} catch (UnknownObmSyncServerException e) {
			sendError(responder, FolderSyncStatus.SERVER_ERROR, e);
		} catch (InvalidServerId e) {
			sendError(responder, FolderSyncStatus.INVALID_REQUEST, e);
		} catch (CollectionNotFoundException e) {
			sendError(responder, FolderSyncStatus.INVALID_REQUEST, e);
		}
	}

	private void sendResponse(Responder responder, Document ret) {
		responder.sendResponse("FolderHierarchy", ret);
	}
	
	private void sendError(Responder responder, FolderSyncStatus status, Exception exception) {
		logger.error(exception.getMessage(), exception);
		sendResponse(responder, protocol.encodeErrorResponse(status));
	}
	
	private FolderSyncResponse doTheJob(BackendSession bs, FolderSyncRequest folderSyncRequest) throws InvalidSyncKeyException, 
		DaoException, UnknownObmSyncServerException, InvalidServerId, CollectionNotFoundException {
		
		if (isFirstSync(folderSyncRequest)) {
			return getFolderSyncResponse(bs);
		} else {
			
			String syncKey = folderSyncRequest.getSyncKey();
			SyncState syncState = stMachine.getSyncState(syncKey);
			if (syncState == null) {
				throw new InvalidSyncKeyException(syncKey);
			}
			return getFolderSyncResponse(bs);
		}
	}

	private boolean isFirstSync(FolderSyncRequest folderSyncRequest) {
		return folderSyncRequest.getSyncKey().equals("0");
	}

	private FolderSyncResponse getFolderSyncResponse(BackendSession bs) throws DaoException, CollectionNotFoundException, 
			UnknownObmSyncServerException, InvalidServerId {
		
		List<ItemChange> changed = getFolderChanges(bs);
		ImmutableList<ItemChange> deleted = ImmutableList.<ItemChange>of();
		String newSyncKey = stMachine.allocateNewSyncKey(bs, getCollectionId(bs), null, changed, deleted);
		return new FolderSyncResponse(changed, newSyncKey);
	}

	private List<ItemChange> getFolderChanges(BackendSession bs) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException {
		return hierarchyExporter.getChanged(bs);
	}
	
	private int getCollectionId(BackendSession bs) throws DaoException {
		return getOrAddCollectionId(bs.getDevice(), hierarchyExporter.getRootFolderUrl(bs));
	}

	private int getOrAddCollectionId(Device deviceId, String rootFolderUrl) throws DaoException {
		try {
			return collectionDao.getCollectionMapping(deviceId, rootFolderUrl);
		} catch (CollectionNotFoundException e) {
			return collectionDao.addCollectionMapping(deviceId, rootFolderUrl);
		}
	}

}