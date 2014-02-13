/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import org.obm.push.ContinuationService;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Device;
import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.FolderSyncRequiredException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.TimeoutException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.protocol.PingProtocol;
import org.obm.push.protocol.bean.AnalysedPingRequest;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.data.MissingRequestParameterException;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class PingHandler extends WbxmlRequestHandler implements IContinuationHandler {
	
	private final MonitoredCollectionDao monitoredCollectionDao;
	private final PingProtocol protocol;
	private final ContinuationService continuationService;
	private final boolean enablePush;

	@Inject
	protected PingHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter,
			IContentsExporter contentsExporter, StateMachine stMachine,
			PingProtocol pingProtocol, MonitoredCollectionDao monitoredCollectionDao,
			CollectionDao collectionDao, WBXMLTools wbxmlTools, DOMDumper domDumper, ContinuationService continuationService,
			@Named("enable-push") boolean enablePush) {
		
		super(backend, encoderFactory, contentsImporter,
				contentsExporter, stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.monitoredCollectionDao = monitoredCollectionDao;
		this.protocol = pingProtocol;
		this.continuationService = continuationService;
		this.enablePush = enablePush;
	}

	@Override
	public void process(IContinuation continuation, UserDataRequest udr,
			Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			PingRequest pingRequest = protocol.decodeRequest(doc);
			AnalysedPingRequest analyzedPingRequest = protocol.analyzeRequest(udr, pingRequest);
			
			doTheJob(continuation, udr, analyzedPingRequest);

		} catch (MissingRequestParameterException e) {
			sendError(udr.getDevice(), responder, PingStatus.MISSING_REQUEST_PARAMS);
		} catch (CollectionNotFoundException e) {
			sendError(udr.getDevice(), responder, PingStatus.FOLDER_SYNC_REQUIRED);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (CollectionPathException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (TimeoutException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		}
	}

	private void doTheJob(IContinuation continuation, UserDataRequest udr, AnalysedPingRequest pingRequest) 
			throws DaoException, CollectionNotFoundException, CollectionPathException {
		
		continuationService.cancel(udr.getDevice());
		startEmailMonitoringThreadIfNeeded(udr, pingRequest);
		monitoreCollection(udr, pingRequest);
		suspendContinuation(continuation, udr, pingRequest);
	}

	private void monitoreCollection(UserDataRequest udr, AnalysedPingRequest pingRequest) {
		monitoredCollectionDao.put(udr.getCredentials(), udr.getDevice(), pingRequest.getSyncCollections());
	}

	private void startEmailMonitoringThreadIfNeeded(UserDataRequest udr, AnalysedPingRequest pingRequest) 
			throws CollectionNotFoundException, DaoException {
		
		for (AnalysedSyncCollection syncCollection: pingRequest.getSyncCollections()) {
			if ("email".equalsIgnoreCase(syncCollection.getDataClass())) {
				backend.startEmailMonitoring(udr, syncCollection.getCollectionId());
			}
		}
	}

	private void suspendContinuation(IContinuation continuation, UserDataRequest udr, AnalysedPingRequest pingRequest) {
		
		continuation.setLastContinuationHandler(this);
		CollectionChangeListener l = new CollectionChangeListener(udr, continuation, pingRequest.getSyncCollections());
		IListenerRegistration reg = backend.addChangeListener(l);
		continuation.setListenerRegistration(reg);
		continuation.setCollectionChangeListener(l);
		
		continuationService.suspend(udr, continuation, pingRequest.getHeartbeatInterval(), PingStatus.NO_CHANGES.asSpecificationValue());
	}
	
	@Override
	public void sendResponseWithoutHierarchyChanges(UserDataRequest udr, Responder responder, IContinuation continuation) {
		sendResponse(udr, responder, false, continuation);
	}
	
	@Override
	public void sendResponse(UserDataRequest udr, Responder responder,
			boolean sendHierarchyChange, IContinuation continuation) {
		
		try {
			PingResponse response = buildResponse(sendHierarchyChange, continuation);
			Document document = protocol.encodeResponse(response);
			sendResponse(responder, document);
		} catch (FolderSyncRequiredException e) {
			sendError(udr.getDevice(), responder, PingStatus.FOLDER_SYNC_REQUIRED);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (UnexpectedObmSyncServerException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (ProcessingEmailException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (ConversionException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (FilterTypeChangedException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.SERVER_ERROR);
		} catch (HierarchyChangedException e) {
			logger.error(e.getMessage(), e);
			sendError(udr.getDevice(), responder, PingStatus.FOLDER_SYNC_REQUIRED);
		}
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendWBXMLResponse("Ping", document);
	}

	private PingResponse buildResponse(boolean sendHierarchyChange, IContinuation continuation) 
			throws FolderSyncRequiredException, DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException,
			FilterTypeChangedException, HierarchyChangedException {
		
		if (!enablePush) {
			// Lie to the phone, try to convince it a new sync is required
			// Return empty collections since CHANGES_OCCURED is a global scope status, see MS-ASCMD 2.2.2.8
			return PingResponse.builder()
					.syncCollections(ImmutableSet.<SyncCollectionResponse>of())
					.pingStatus(PingStatus.CHANGES_OCCURED)
					.build();
		}
		
		if (sendHierarchyChange) {
			throw new FolderSyncRequiredException();
		}
		
		final Set<SyncCollectionResponse> changes = backend.getChangesSyncCollections(continuation.getCollectionChangeListener());
		if (changes.isEmpty()) {
			return PingResponse.builder()
				.syncCollections(changes)
				.pingStatus(PingStatus.NO_CHANGES)
				.build();
		} else {
			return PingResponse.builder()
				.syncCollections(changes)
				.pingStatus(PingStatus.CHANGES_OCCURED)
				.build();
		}
	}

	@Override
	public void sendError(Device device, Responder responder, String errorStatus, IContinuation continuation) {
		Document document = protocol.buildError(errorStatus);
		sendResponse(responder, document);
	}

	private void sendError(Device device, Responder responder, PingStatus serverError) {
		sendError(device, responder, serverError.asSpecificationValue(), null);
	}
	
}
