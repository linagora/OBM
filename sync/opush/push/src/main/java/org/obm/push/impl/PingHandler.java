package org.obm.push.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.MonitoredCollectionStoreService;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.PingRequest;
import org.obm.push.bean.PingResponse;
import org.obm.push.data.EncoderFactory;
import org.obm.push.exception.FolderSyncRequiredException;
import org.obm.push.exception.MissingRequestParameterException;
import org.obm.push.protocol.PingProtocol;
import org.obm.push.bean.SyncCollection;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ISyncStorage;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PingHandler extends WbxmlRequestHandler implements
		IContinuationHandler {
	
	private final MonitoredCollectionStoreService monitoredCollectionService;
	private final PingProtocol protocol;

	@Inject
	protected PingHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, ISyncStorage storage,
			IContentsExporter contentsExporter, StateMachine stMachine,
			MonitoredCollectionStoreService monitoredCollectionService,
			PingProtocol pingProtocol, CollectionDao collectionDao) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine, collectionDao);
		this.monitoredCollectionService = monitoredCollectionService;
		this.protocol = pingProtocol;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			PingRequest pingRequest = protocol.getRequest(doc);
			doTheJob(continuation, bs, pingRequest);

		} catch (MissingRequestParameterException e) {
			logger.error("Don't know what to monitor", e);
			sendError(responder, PingStatus.MISSING_REQUEST_PARAMS);
		} catch (CollectionNotFoundException e) {
			logger.error("unable to start monitoring, collection not found", e);
			sendError(responder, PingStatus.FOLDER_SYNC_REQUIRED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			sendError(responder, PingStatus.SERVER_ERROR);
		}
	}

	@Transactional
	private void doTheJob(IContinuation continuation, BackendSession bs, PingRequest pingRequest) 
			throws SQLException, MissingRequestParameterException, ActiveSyncException {
		
		if (pingRequest.getHeartbeatInterval() == null) {
			Long heartbeatInterval = storage.findLastHearbeat(bs.getLoginAtDomain(), bs.getDevId());
			if (heartbeatInterval == null) {
				throw new MissingRequestParameterException();
			}
			pingRequest.setHeartbeatInterval(heartbeatInterval);
		} else {
			storage.updateLastHearbeat(bs.getLoginAtDomain(), bs.getDevId(), pingRequest.getHeartbeatInterval());
		}
		if (pingRequest.getHeartbeatInterval() < 5) {
			pingRequest.setHeartbeatInterval(5l);
		}
		if (pingRequest.getSyncCollections().isEmpty()) {
			Set<SyncCollection> lastMonitoredCollection = monitoredCollectionService.list(bs.getCredentials(), bs.getDevice());
			if (lastMonitoredCollection.isEmpty()) {
				throw new MissingRequestParameterException();
			}
			pingRequest.setSyncCollections(lastMonitoredCollection);
		} else {
			monitoredCollectionService.put(bs.getCredentials(), bs.getDevice(), pingRequest.getSyncCollections());
		}

		for (SyncCollection syncCollection: pingRequest.getSyncCollections()) {
			if ("email".equalsIgnoreCase(syncCollection.getDataClass())) {
				backend.startEmailMonitoring(bs, syncCollection.getCollectionId());
			}
		}

		continuation.setLastContinuationHandler(this);
		CollectionChangeListener l = new CollectionChangeListener(bs, continuation, pingRequest.getSyncCollections());
		IListenerRegistration reg = backend.addChangeListener(l);
		continuation.setListenerRegistration(reg);
		continuation.setCollectionChangeListener(l);
		logger.info("suspend for {} seconds", pingRequest.getHeartbeatInterval());
		continuation.suspend(pingRequest.getHeartbeatInterval() * 1000);
	}

	@Override
	public void sendResponseWithoutHierarchyChanges(BackendSession bs, Responder responder,
			IContinuation continuation) {
		sendResponse(bs, responder, false, continuation);
	}
	
	@Override
	public void sendResponse(BackendSession bs, Responder responder,
			boolean sendHierarchyChange, IContinuation continuation) {
		
		try {
			PingResponse response = buildResponse(sendHierarchyChange, continuation);
			Document document = protocol.encodeResponse(response);
			responder.sendResponse("Ping", document);
		} catch (FolderSyncRequiredException e) {
			logger.error("unable to start monitoring, collection not found", e);
			sendError(responder, PingStatus.FOLDER_SYNC_REQUIRED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional
	private PingResponse buildResponse(boolean sendHierarchyChange, IContinuation continuation) throws FolderSyncRequiredException, SQLException {
		if (sendHierarchyChange) {
			throw new FolderSyncRequiredException();
		}
		
		final Set<SyncCollection> changes = backend.getChangesSyncCollections(continuation.getCollectionChangeListener());
		if (changes.isEmpty()) {
			return new PingResponse(changes, PingStatus.NO_CHANGES);
		} else {
			return new PingResponse(changes, PingStatus.CHANGES_OCCURED);
		}
	}

	@Override
	public void sendError(Responder responder, String errorStatus,
			IContinuation continuation) {
		Document document = protocol.buildError(errorStatus);
		try {
			responder.sendResponse("Ping", document);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sendError(Responder responder, PingStatus serverError) {
		Document document = protocol.buildError(serverError);
		try {
			responder.sendResponse("Ping", document);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
