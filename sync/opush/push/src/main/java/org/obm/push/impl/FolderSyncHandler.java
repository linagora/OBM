package org.obm.push.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.InvalidSyncKeyException;
import org.obm.push.exception.NoDocumentException;
import org.obm.push.protocol.FolderSyncProtocol;
import org.obm.push.protocol.bean.FolderSyncRequest;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.w3c.dom.Document;

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
			responder.sendResponse("FolderHierarchy", ret);
			
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
		} catch (InvalidSyncKeyException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
		} catch (NoDocumentException e) {
			answerOpushIsAlive(responder);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	private void sendError(Responder resp, FolderSyncStatus status) {
		try {
			resp.sendResponse("FolderHierarchy", protocol.encodeErrorResponse(status));
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
	}
	
	private void answerOpushIsAlive(Responder responder) {
		try {
			responder.sendResponseFile("text/plain", new ByteArrayInputStream(
					"OPUSH IS ALIVE\n".getBytes()));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Transactional
	private FolderSyncResponse doTheJob(BackendSession bs,
			FolderSyncRequest folderSyncRequest) throws
			InvalidSyncKeyException, CollectionNotFoundException,
			ActiveSyncException, DaoException {
		
		// FIXME we know that we do not monitor hierarchy, so just respond
		// that nothing changed
		
		SyncState state = stMachine.getFolderSyncState(bs.getDevice(),
				hierarchyExporter.getRootFolderUrl(bs), folderSyncRequest.getSyncKey());
		
		if (!state.isValid()) {
			throw new InvalidSyncKeyException();
		}
		hierarchyExporter.configure(state, null, null, 0, 0);
								
		List<ItemChange> changed = hierarchyExporter.getChanged(bs);
		
		if (!isSynckeyValid(folderSyncRequest.getSyncKey(), changed)) {
			throw new InvalidSyncKeyException();
		}
		
		String newSyncKey = stMachine.allocateNewSyncKey(bs,
				hierarchyExporter.getRootFolderId(bs), null);
		
		FolderSyncResponse folderSyncResponse = new FolderSyncResponse(changed, newSyncKey);
		return folderSyncResponse;
	}

	private boolean isSynckeyValid(String syncKey, List<ItemChange> changed) {
		if (!"0".equals(syncKey)) {
			for (ItemChange sf : changed) {
				if (sf.isNew()) {
					return false;
				}
			}
		}
		return true;
	}
	
}
