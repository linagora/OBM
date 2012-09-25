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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.obm.push.ContinuationTransactionMap;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.ElementNotFoundException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.WaitIntervalOutOfRangeException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.SyncProtocol;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

//<?xml version="1.0" encoding="UTF-8"?>
//<Sync>
//<Collections>
//<Collection>
//<Class>Contacts</Class>
//<SyncKey>ff16677f-ee9c-42dc-a562-709f899c8d31</SyncKey>
//<CollectionId>obm://contacts/user@domain</CollectionId>
//<DeletesAsMoves/>
//<GetChanges/>
//<WindowSize>100</WindowSize>
//<Options>
//<Truncation>4</Truncation>
//<RTFTruncation>4</RTFTruncation>
//<Conflict>1</Conflict>
//</Options>
//</Collection>
//</Collections>
//</Sync>
@Singleton
public class SyncHandler extends WbxmlRequestHandler implements IContinuationHandler {

	private static class ModificationStatus {
		public Map<String, String> processedClientIds = new HashMap<String, String>();
	}
	
	private final SyncProtocol syncProtocol;
	private final UnsynchronizedItemDao unSynchronizedItemCache;
	private final MonitoredCollectionDao monitoredCollectionService;
	private final ItemTrackingDao itemTrackingDao;
	private final CollectionPathHelper collectionPathHelper;
	private final ResponseWindowingService responseWindowingProcessor;
	private final ContinuationTransactionMap continuationTransactionMap;

	@Inject SyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IContentsExporter contentsExporter,
			StateMachine stMachine, UnsynchronizedItemDao unSynchronizedItemCache,
			MonitoredCollectionDao monitoredCollectionService, SyncProtocol SyncProtocol,
			CollectionDao collectionDao, ItemTrackingDao itemTrackingDao,
			WBXMLTools wbxmlTools, DOMDumper domDumper, CollectionPathHelper collectionPathHelper,
			ResponseWindowingService responseWindowingProcessor,
			ContinuationTransactionMap continuationTransactionMap) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, 
				stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.unSynchronizedItemCache = unSynchronizedItemCache;
		this.monitoredCollectionService = monitoredCollectionService;
		this.syncProtocol = SyncProtocol;
		this.itemTrackingDao = itemTrackingDao;
		this.collectionPathHelper = collectionPathHelper;
		this.responseWindowingProcessor = responseWindowingProcessor;
		this.continuationTransactionMap = continuationTransactionMap;
	}

	@Override
	public void process(IContinuation continuation, UserDataRequest udr, Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			SyncRequest syncRequest = syncProtocol.getRequest(doc, udr);
			
			cancelPreviousContinuation(udr.getDevice());
			
			ModificationStatus modificationStatus = processCollections(udr, syncRequest.getSync());
			if (syncRequest.getSync().getWaitInSecond() > 0) {
				registerWaitingSync(continuation, udr, syncRequest.getSync());
			} else {
				SyncResponse syncResponse = doTheJob(udr, syncRequest.getSync().getCollections(), 
						 modificationStatus.processedClientIds, continuation);
				sendResponse(responder, syncProtocol.endcodeResponse(syncResponse));
			}
		} catch (InvalidServerId e) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR, e);
		} catch (ProtocolException convExpt) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR, convExpt);
		} catch (PartialException pe) {
			sendError(responder, SyncStatus.PARTIAL_REQUEST, pe);
		} catch (CollectionNotFoundException ce) {
			sendError(responder, SyncStatus.OBJECT_NOT_FOUND, continuation, ce);
		} catch (ContinuationThrowable e) {
			throw e;
		} catch (NoDocumentException e) {
			sendError(responder, SyncStatus.PARTIAL_REQUEST, e);
		} catch (WaitIntervalOutOfRangeException e) {
			sendResponse(responder, syncProtocol.encodeResponse());
		} catch (DaoException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (UnexpectedObmSyncServerException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (ProcessingEmailException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (CollectionPathException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (ConversionException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (UnsupportedBackendFunctionException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (IOException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		}
	}

	private void cancelPreviousContinuation(Device device) {
		try {
			IContinuation continuation = continuationTransactionMap.getContinuationForDevice(device);
			continuation.resume();
		}
		catch (ElementNotFoundException e) {
		}
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendWBXMLResponse("AirSync", document);
	}
	
	private void registerWaitingSync(IContinuation continuation, UserDataRequest udr, Sync sync) 
			throws CollectionNotFoundException, WaitIntervalOutOfRangeException, DaoException, CollectionPathException {
		
		if (sync.getWaitInSecond() > 3540) {
			throw new WaitIntervalOutOfRangeException();
		}
		
		for (SyncCollection sc: sync.getCollections()) {
			String collectionPath = collectionDao.getCollectionPath(sc.getCollectionId());
			sc.setCollectionPath(collectionPath);
			PIMDataType dataClass = collectionPathHelper.recognizePIMDataType(collectionPath);
			if (dataClass == PIMDataType.EMAIL) {
				backend.startEmailMonitoring(udr, sc.getCollectionId());
				break;
			}
		}
		
		continuation.error(SyncStatus.NEED_RETRY.asSpecificationValue());
		
		continuation.setLastContinuationHandler(this);
		monitoredCollectionService.put(udr.getCredentials(), udr.getDevice(), sync.getCollections());
		CollectionChangeListener l = new CollectionChangeListener(udr,
				continuation, sync.getCollections());
		IListenerRegistration reg = backend.addChangeListener(l);
		continuation.setListenerRegistration(reg);
		continuation.setCollectionChangeListener(l);
		continuationTransactionMap.putContinuationForDevice(udr.getDevice(), continuation);
		
		continuation.suspend(udr, sync.getWaitInSecond());
	}

	private Date doUpdates(UserDataRequest udr, SyncCollection c,	Map<String, String> processedClientIds, 
			SyncCollectionResponse syncCollectionResponse) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException {

		DataDelta delta = null;
		Date lastSync = null;
		
		int unSynchronizedItemNb = unSynchronizedItemCache.listItemsToAdd(udr.getCredentials(), udr.getDevice(), c.getCollectionId()).size();
		if (unSynchronizedItemNb == 0) {
			delta = contentsExporter.getChanged(udr, c);
			
			lastSync = delta.getSyncDate();
		} else {
			lastSync = c.getSyncState().getLastSync();
			delta = new DataDelta(ImmutableList.<ItemChange>of(), ImmutableList.<ItemChange>of(), lastSync);
		}

		List<ItemChange> changed = responseWindowingProcessor.windowChanges(c, delta, udr, processedClientIds);
		syncCollectionResponse.setItemChanges(changed);
	
		List<ItemChange> itemChangesDeletion = responseWindowingProcessor.windowDeletions(c, delta, udr, processedClientIds);
		syncCollectionResponse.setItemChangesDeletion(itemChangesDeletion);
		
		return lastSync;
	}

	private ModificationStatus processCollections(UserDataRequest udr, Sync sync) throws CollectionNotFoundException, DaoException, 
		UnexpectedObmSyncServerException, ProcessingEmailException, UnsupportedBackendFunctionException, ConversionException {
		
		ModificationStatus modificationStatus = new ModificationStatus();

		setContinuationAsNeedRetryIfAny(udr);
		
		for (SyncCollection collection : sync.getCollections()) {

			// get our sync state for this collection
			ItemSyncState collectionState = stMachine.getItemSyncState(collection.getSyncKey());

			if (collectionState != null) {
				collection.setItemSyncState(collectionState);
				Map<String, String> processedClientIds = processModification(udr, collection);
				modificationStatus.processedClientIds.putAll(processedClientIds);
			} else {
				ItemSyncState syncState = new ItemSyncState(collection.getSyncKey());
				collection.setItemSyncState(syncState);
			}
		}
		return modificationStatus;
	}

	private void setContinuationAsNeedRetryIfAny(UserDataRequest udr) {
		// Disables last push request
		try {
			IContinuation cont= continuationTransactionMap.getContinuationForDevice(udr.getDevice());
			cont.error(SyncStatus.NEED_RETRY.asSpecificationValue());
		}
		catch (ElementNotFoundException e) {
		}
	}
	
	/**
	 * Handles modifications requested by mobile device
	 */
	private Map<String, String> processModification(UserDataRequest udr, SyncCollection collection) throws CollectionNotFoundException, 
		DaoException, UnexpectedObmSyncServerException, ProcessingEmailException, UnsupportedBackendFunctionException, ConversionException {
		
		Map<String, String> processedClientIds = new HashMap<String, String>();
		for (SyncCollectionChange change: collection.getChanges()) {
			try {
				if (change.getModType().equals("Modify")) {
					updateServerItem(udr, collection, change);
					
				} else if (change.getModType().equals("Add") || change.getModType().equals("Change")) {
					addServerItem(udr, collection, processedClientIds, change);
					
				} else if (change.getModType().equals("Delete")) {
					deleteServerItem(udr, collection, processedClientIds, change);
				}
			} catch (ItemNotFoundException e) {
				logger.warn("Item with server id {} not found.", change.getServerId());
			}
		}
		return processedClientIds;
	}

	private void updateServerItem(UserDataRequest udr, SyncCollection collection, SyncCollectionChange change) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ItemNotFoundException, ConversionException {

		contentsImporter.importMessageChange(udr, collection.getCollectionId(), change.getServerId(), change.getClientId(), 
				change.getData());
	}

	private void addServerItem(UserDataRequest udr, SyncCollection collection, 
			Map<String, String> processedClientIds, SyncCollectionChange change) throws CollectionNotFoundException, DaoException,
			UnexpectedObmSyncServerException, ProcessingEmailException, ItemNotFoundException, ConversionException {

		String obmId = contentsImporter.importMessageChange(udr, collection.getCollectionId(), change.getServerId(),
				change.getClientId(), change.getData());
		if (obmId != null) {
			if (change.getClientId() != null) {
				processedClientIds.put(obmId, change.getClientId());
			}
			if (change.getServerId() != null) {
				processedClientIds.put(obmId, null);
			}
		}
	}
	
	private void deleteServerItem(UserDataRequest udr, SyncCollection collection,
			Map<String, String> processedClientIds, SyncCollectionChange change) throws CollectionNotFoundException, DaoException,
			UnexpectedObmSyncServerException, ProcessingEmailException, ItemNotFoundException, UnsupportedBackendFunctionException {

		String serverId = change.getServerId();
		contentsImporter.importMessageDeletion(udr, change.getType(), collection.getCollectionId(), serverId, collection
				.getOptions().isDeletesAsMoves());
		if (serverId != null) {
			processedClientIds.put(serverId, null);
		}
	}

	@Override
	public void sendResponseWithoutHierarchyChanges(UserDataRequest udr, Responder responder, IContinuation continuation) {
		sendResponse(udr, responder, false, continuation);
	}

	@Override
	public void sendResponse(UserDataRequest udr, Responder responder, boolean sendHierarchyChange, IContinuation continuation) {
		try {
			SyncResponse syncResponse = doTheJob(udr, monitoredCollectionService.list(udr.getCredentials(), udr.getDevice()),
					Collections.EMPTY_MAP, continuation);
			sendResponse(responder, syncProtocol.endcodeResponse(syncResponse));
		} catch (DaoException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (CollectionNotFoundException e) {
			sendError(responder, SyncStatus.OBJECT_NOT_FOUND, continuation, e);
		} catch (UnexpectedObmSyncServerException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (ProcessingEmailException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (InvalidServerId e) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR, e);			
		} catch (ConversionException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		} catch (IOException e) {
			sendError(responder, SyncStatus.SERVER_ERROR, e);
		}
	}

	public SyncResponse doTheJob(UserDataRequest udr, Collection<SyncCollection> changedFolders, 
			Map<String, String> processedClientIds, IContinuation continuation) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, InvalidServerId, ConversionException {

		List<SyncCollectionResponse> syncCollectionResponses = new ArrayList<SyncResponse.SyncCollectionResponse>();
		for (SyncCollection c : changedFolders) {
			SyncCollectionResponse syncCollectionResponse = computeSyncState(udr, processedClientIds, c);
			syncCollectionResponses.add(syncCollectionResponse);
		}
		logger.info("Resp for requestId = {}", continuation.getReqId());
		return new SyncResponse(syncCollectionResponses, udr, getEncoders(), processedClientIds);
	}

	private SyncCollectionResponse computeSyncState(UserDataRequest udr,
			Map<String, String> processedClientIds, SyncCollection syncCollection)
			throws DaoException, CollectionNotFoundException, InvalidServerId,
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException {

		SyncCollectionResponse syncCollectionResponse = new SyncCollectionResponse(syncCollection);
		if ("0".equals(syncCollection.getSyncKey())) {
			handleInitialSync(udr, syncCollection, syncCollectionResponse);
		} else {
			handleDataSync(udr, processedClientIds, syncCollection, syncCollectionResponse);
		}
		return syncCollectionResponse;
	}

	private void handleDataSync(UserDataRequest udr, Map<String, String> processedClientIds, SyncCollection syncCollection,
			SyncCollectionResponse syncCollectionResponse) throws CollectionNotFoundException, DaoException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, InvalidServerId, ConversionException {
		
		ItemSyncState st = stMachine.getItemSyncState(syncCollection.getSyncKey());
		if (st == null) {
			syncCollectionResponse.setSyncStateValid(false);
		} else {
			syncCollection.setItemSyncState(st);
			syncCollectionResponse.setSyncStateValid(true);
			Date syncDate = null;
			if (syncCollection.getFetchIds().isEmpty()) {
				syncDate = doUpdates(udr, syncCollection, processedClientIds, syncCollectionResponse);
			} else {
				syncCollectionResponse.setItemChanges(
						contentsExporter.fetch(udr, syncCollection));
			}
			identifyNewItems(syncCollectionResponse, st);
			String newSyncKey = 
					stMachine.allocateNewSyncKey(udr, syncCollection.getCollectionId(), syncDate, 
							syncCollectionResponse.getItemChanges(), syncCollectionResponse.getItemChangesDeletion());
			syncCollectionResponse.setNewSyncKey(newSyncKey);
		}
	}

	private void handleInitialSync(UserDataRequest udr, SyncCollection syncCollection, SyncCollectionResponse syncCollectionResponse) 
			throws DaoException, InvalidServerId {
		
		backend.resetCollection(udr, syncCollection.getCollectionId());
		syncCollectionResponse.setSyncStateValid(true);
		ImmutableList<ItemChange> changed = ImmutableList.<ItemChange>of();
		ImmutableList<ItemChange> deleted = ImmutableList.<ItemChange>of();
		String newSyncKey = stMachine.allocateNewSyncKey(udr, syncCollection.getCollectionId(), null, changed, deleted);
		syncCollectionResponse.setNewSyncKey(newSyncKey);
	}

	private void identifyNewItems(
			SyncCollectionResponse syncCollectionResponse, SyncState st)
			throws DaoException, InvalidServerId {
		
		for (ItemChange change: syncCollectionResponse.getItemChanges()) {
			boolean isItemAddition = st.getKey().equals("0") || 
					!itemTrackingDao.isServerIdSynced(st, new ServerId(change.getServerId()));
			change.setNew(isItemAddition);
		}
	}
	
	private void sendError(Responder responder, SyncStatus errorStatus, Exception exception) {
		sendError(responder, errorStatus, null, exception);
	}
	
	private void sendError(Responder responder, SyncStatus errorStatus, IContinuation continuation, Exception exception) {
		logError(errorStatus, exception);
		sendError(responder, errorStatus.asSpecificationValue(), continuation);
	}

	private void logError(SyncStatus errorStatus, Exception exception) {
		if (errorStatus == SyncStatus.SERVER_ERROR) {
			logger.error(exception.getMessage(), exception);
		} else {
			logger.warn(exception.getMessage(), exception);
		}
	}

	@Override
	public void sendError(Responder responder, String errorStatus, IContinuation continuation) {
		try {
			if (continuation != null) {
				logger.info("Resp for requestId = {}", continuation.getReqId());
			}
			responder.sendWBXMLResponse("AirSync", syncProtocol.encodeResponse(errorStatus) );
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
