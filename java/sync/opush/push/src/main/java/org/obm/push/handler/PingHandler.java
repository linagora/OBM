/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.handler;

import java.util.Set;

import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.FolderSyncRequiredException;
import org.obm.push.exception.MissingRequestParameterException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.PingProtocol;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.HearbeatDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class PingHandler extends WbxmlRequestHandler implements
		IContinuationHandler {
	
	private static final int MIN_SANE_HEARTBEAT_VALUE = 5;
	
	private final MonitoredCollectionDao monitoredCollectionDao;
	private final PingProtocol protocol;
	private final HearbeatDao hearbeatDao;

	@Inject
	protected PingHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			PingProtocol pingProtocol, MonitoredCollectionDao monitoredCollectionDao,
			CollectionDao collectionDao, HearbeatDao hearbeatDao,
			WBXMLTools wbxmlTools, DOMDumper domDumper) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools, domDumper);
		this.monitoredCollectionDao = monitoredCollectionDao;
		this.protocol = pingProtocol;
		this.hearbeatDao = hearbeatDao;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			PingRequest pingRequest = protocol.getRequest(doc);
			doTheJob(continuation, bs, pingRequest);

		} catch (MissingRequestParameterException e) {
			sendError(responder, PingStatus.MISSING_REQUEST_PARAMS);
		} catch (CollectionNotFoundException e) {
			sendError(responder, PingStatus.FOLDER_SYNC_REQUIRED);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, PingStatus.SERVER_ERROR);
		}
	}

	private void doTheJob(IContinuation continuation, BackendSession bs, PingRequest pingRequest) 
			throws MissingRequestParameterException, DaoException, CollectionNotFoundException {
		
		checkHeartbeatInterval(bs, pingRequest);
		checkSyncCollections(bs, pingRequest);
		startEmailMonitoringThreadIfNeeded(bs, pingRequest);
		suspendContinuation(continuation, bs, pingRequest);
	}

	private void checkHeartbeatInterval(BackendSession bs, PingRequest pingRequest) 
			throws DaoException, MissingRequestParameterException {
		
		if (pingRequest.getHeartbeatInterval() == null) {
			Long heartbeatInterval = hearbeatDao.findLastHearbeat(bs.getDevice());
			if (heartbeatInterval == null) {
				throw new MissingRequestParameterException();
			}
			pingRequest.setHeartbeatInterval(heartbeatInterval);
		} else {
			long heartbeatInterval = Math.max(MIN_SANE_HEARTBEAT_VALUE, pingRequest.getHeartbeatInterval());
			pingRequest.setHeartbeatInterval(heartbeatInterval);
			hearbeatDao.updateLastHearbeat(bs.getDevice(), pingRequest.getHeartbeatInterval());
		}
	}
	
	private void checkSyncCollections(BackendSession bs, PingRequest pingRequest)
			throws MissingRequestParameterException, CollectionNotFoundException, DaoException {
		
		Set<SyncCollection> syncCollections = pingRequest.getSyncCollections();
		if (syncCollections == null || syncCollections.isEmpty()) {
			Set<SyncCollection> lastMonitoredCollection = monitoredCollectionDao.list(bs.getCredentials(), bs.getDevice());
			if (lastMonitoredCollection.isEmpty()) {
				throw new MissingRequestParameterException();
			}
			pingRequest.setSyncCollections(lastMonitoredCollection);
		} else {
			monitoredCollectionDao.put(bs.getCredentials(), bs.getDevice(), syncCollections);
		}
		loadSyncKeys(pingRequest.getSyncCollections());
	}

	private void loadSyncKeys(Set<SyncCollection> syncCollections) throws CollectionNotFoundException, DaoException {
		for (SyncCollection collection: syncCollections) {
			String collectionPath = collectionDao.getCollectionPath(collection.getCollectionId());
			collection.setCollectionPath(collectionPath);
			collection.setSyncState(new SyncState(collectionPath));
		}
	}

	private void startEmailMonitoringThreadIfNeeded(BackendSession bs,
			PingRequest pingRequest) throws CollectionNotFoundException {
		for (SyncCollection syncCollection: pingRequest.getSyncCollections()) {
			if ("email".equalsIgnoreCase(syncCollection.getDataClass())) {
				backend.startEmailMonitoring(bs, syncCollection.getCollectionId());
			}
		}
	}

	private void suspendContinuation(IContinuation continuation,
			BackendSession bs, PingRequest pingRequest) {
		continuation.setLastContinuationHandler(this);
		CollectionChangeListener l = new CollectionChangeListener(bs, continuation, pingRequest.getSyncCollections());
		IListenerRegistration reg = backend.addChangeListener(l);
		continuation.setListenerRegistration(reg);
		continuation.setCollectionChangeListener(l);
		continuation.suspend(bs, pingRequest.getHeartbeatInterval());
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
			sendResponse(responder, document);
		} catch (FolderSyncRequiredException e) {
			sendError(responder, PingStatus.FOLDER_SYNC_REQUIRED);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, PingStatus.SERVER_ERROR);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, PingStatus.SERVER_ERROR);
		} catch (UnknownObmSyncServerException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, PingStatus.SERVER_ERROR);
		} catch (ProcessingEmailException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, PingStatus.SERVER_ERROR);
		} 
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendResponse("Ping", document);
	}

	private PingResponse buildResponse(boolean sendHierarchyChange, IContinuation continuation) 
			throws FolderSyncRequiredException, DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException {
		
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
	public void sendError(Responder responder, String errorStatus, IContinuation continuation) {
		Document document = protocol.buildError(errorStatus);
		sendResponse(responder, document);
	}

	private void sendError(Responder responder, PingStatus serverError) {
		sendError(responder, serverError.asXmlValue(), null);
	}
	
}
