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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.obm.push.ContinuationService;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.CollectionPathHelper;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.client.SyncClientCommands.Add;
import org.obm.push.bean.change.client.SyncClientCommands.Change;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.CollectionPathException;
import org.obm.push.exception.ConversionException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.exception.UnsupportedBackendFunctionException;
import org.obm.push.exception.WaitIntervalOutOfRangeException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.HierarchyChangedException;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.exception.activesync.ServerErrorException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.protocol.SyncProtocol;
import org.obm.push.protocol.bean.AnalysedSyncRequest;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.service.DateService;
import org.obm.push.state.StateMachine;
import org.obm.push.state.SyncKeyFactory;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SyncHandler extends WbxmlRequestHandler implements IContinuationHandler {

	private final SyncProtocol.Factory syncProtocolFactory;
	private final UnsynchronizedItemDao unSynchronizedItemCache;
	private final MonitoredCollectionDao monitoredCollectionService;
	private final ItemTrackingDao itemTrackingDao;
	private final CollectionPathHelper collectionPathHelper;
	private final ResponseWindowingService responseWindowingProcessor;
	private final ContinuationService continuationService;
	private final boolean enablePush;
	private final SyncKeyFactory syncKeyFactory;
	private final DateService dateService;

	@Inject SyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IContentsExporter contentsExporter,
			StateMachine stMachine, UnsynchronizedItemDao unSynchronizedItemCache,
			MonitoredCollectionDao monitoredCollectionService, SyncProtocol.Factory syncProtocolFactory,
			CollectionDao collectionDao, ItemTrackingDao itemTrackingDao,
			WBXMLTools wbxmlTools, DOMDumper domDumper, CollectionPathHelper collectionPathHelper,
			ResponseWindowingService responseWindowingProcessor,
			ContinuationService continuationService,
			@Named("enable-push") boolean enablePush,
			SyncKeyFactory syncKeyFactory,
			DateService dateService) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, 
				stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.unSynchronizedItemCache = unSynchronizedItemCache;
		this.monitoredCollectionService = monitoredCollectionService;
		this.syncProtocolFactory = syncProtocolFactory;
		this.itemTrackingDao = itemTrackingDao;
		this.collectionPathHelper = collectionPathHelper;
		this.responseWindowingProcessor = responseWindowingProcessor;
		this.continuationService = continuationService;
		this.enablePush = enablePush;
		this.syncKeyFactory = syncKeyFactory;
		this.dateService = dateService;
	}

	@Override
	public void process(IContinuation continuation, UserDataRequest udr, Document doc, ActiveSyncRequest request, Responder responder) {
		SyncProtocol syncProtocol = syncProtocolFactory.create(udr);
		try {
			SyncRequest syncRequest = syncProtocol.decodeRequest(doc);
			AnalysedSyncRequest analyzedSyncRequest = syncProtocol.analyzeRequest(udr, syncRequest);
			
			continuationService.cancel(udr.getDevice(), SyncStatus.NEED_RETRY.asSpecificationValue());
			SyncClientCommands clientCommands = processClientCommands(udr, analyzedSyncRequest.getSync());
			if (analyzedSyncRequest.getSync().getWaitInSecond() > 0) {
				registerWaitingSync(continuation, udr, analyzedSyncRequest.getSync());
			} else {
				SyncResponse syncResponse = doTheJob(udr, analyzedSyncRequest.getSync().getCollections(), 
						clientCommands, continuation);
				sendResponse(responder, syncProtocol.encodeResponse(syncResponse));
			}
		} catch (InvalidServerId e) {
			sendError(udr.getDevice(), responder, SyncStatus.PROTOCOL_ERROR, continuation, e);
		} catch (ProtocolException convExpt) {
			sendError(udr.getDevice(), responder, SyncStatus.PROTOCOL_ERROR, continuation, convExpt);
		} catch (PartialException pe) {
			sendError(udr.getDevice(), responder, SyncStatus.PARTIAL_REQUEST, continuation, pe);
		} catch (CollectionNotFoundException ce) {
			sendError(udr.getDevice(), responder, SyncStatus.OBJECT_NOT_FOUND, continuation, ce);
		} catch (ContinuationThrowable e) {
			throw e;
		} catch (NoDocumentException e) {
			sendError(udr.getDevice(), responder, SyncStatus.PARTIAL_REQUEST, continuation, e);
		} catch (WaitIntervalOutOfRangeException e) {
			sendResponse(responder, syncProtocol.encodeResponse());
		} catch (WaitSyncFolderLimitException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR.asSpecificationValue(), continuation);
		} catch (DaoException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (UnexpectedObmSyncServerException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (ProcessingEmailException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (CollectionPathException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (ConversionException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (UnsupportedBackendFunctionException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (ServerErrorException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (HierarchyChangedException e) {
			sendError(udr.getDevice(), responder, SyncStatus.HIERARCHY_CHANGED, continuation, e);
		} catch (ItemNotFoundException e) {
			//This case is specific to email sync race-condition
			sendError(udr.getDevice(), responder, SyncStatus.NEED_RETRY, continuation, e);
		}
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendWBXMLResponse("AirSync", document);
	}
	
	private void registerWaitingSync(IContinuation continuation, UserDataRequest udr, Sync sync) 
			throws CollectionNotFoundException, WaitIntervalOutOfRangeException, DaoException, CollectionPathException, WaitSyncFolderLimitException {
		
		if (!enablePush) {
			throw new WaitSyncFolderLimitException(0);
		}
		
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
		
		continuationService.suspend(udr, continuation, sync.getWaitInSecond());
	}

	private ItemSyncState doUpdates(UserDataRequest udr, SyncCollection c,	SyncClientCommands clientCommands, 
			SyncCollectionResponse syncCollectionResponse) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException, FilterTypeChangedException, HierarchyChangedException {

		DataDelta delta = null;
		Date lastSync = null;
		SyncKey treatmentSyncKey = null;
		
		int unSynchronizedItemNb = unSynchronizedItemCache.listItemsToAdd(udr.getCredentials(), udr.getDevice(), c.getCollectionId()).size();
		if (unSynchronizedItemNb == 0) {
			treatmentSyncKey = syncKeyFactory.randomSyncKey();
			delta = contentsExporter.getChanged(udr, c, treatmentSyncKey);
			lastSync = delta.getSyncDate();
		} else {
			treatmentSyncKey = c.getSyncKey();
			lastSync = c.getItemSyncState().getSyncDate();
			delta = DataDelta.newEmptyDelta(lastSync);
		}

		List<ItemChange> changed = responseWindowingProcessor.windowChanges(c, delta, udr, clientCommands);
		syncCollectionResponse.setItemChanges(changed);
	
		List<ItemDeletion> itemChangesDeletion = responseWindowingProcessor.windowDeletions(c, delta, udr, clientCommands);
		syncCollectionResponse.setItemChangesDeletion(itemChangesDeletion);
		
		return ItemSyncState.builder()
				.syncKey(treatmentSyncKey)
				.syncDate(lastSync)
				.build();
	}

	private SyncClientCommands processClientCommands(UserDataRequest udr, Sync sync) throws CollectionNotFoundException, DaoException, 
		UnexpectedObmSyncServerException, ProcessingEmailException, UnsupportedBackendFunctionException, ConversionException, HierarchyChangedException {
		
		SyncClientCommands.Builder clientCommandsBuilder = SyncClientCommands.builder();

		for (SyncCollection collection : sync.getCollectionsValidToProcess()) {

			// get our sync state for this collection
			ItemSyncState collectionState = stMachine.getItemSyncState(collection.getSyncKey());

			if (collectionState != null) {
				collection.setItemSyncState(collectionState);
				clientCommandsBuilder.merge(processClientModification(udr, collection));
			} else {
				ItemSyncState syncState = ItemSyncState.builder()
						.syncDate(dateService.getEpochPlusOneSecondDate())
						.syncKey(collection.getSyncKey())
						.build();
				collection.setItemSyncState(syncState);
			}
		}
		return clientCommandsBuilder.build();
	}
	
	@VisibleForTesting SyncClientCommands processClientModification(UserDataRequest udr, SyncCollection collection)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, UnsupportedBackendFunctionException, ConversionException, HierarchyChangedException {

		SyncClientCommands.Builder clientCommandsBuilder = SyncClientCommands.builder();
		for (SyncCollectionChange change: collection.getChanges()) {
			try {
				switch (change.getCommand()) {
				case FETCH:
					break;
				case MODIFY:
				case CHANGE:
					clientCommandsBuilder.putChange(updateServerItem(udr, collection, change));
					break;
				case DELETE:
					clientCommandsBuilder.putChange(deleteServerItem(udr, collection, change));
					break;
				case ADD:
					clientCommandsBuilder.putAdd(addServerItem(udr, collection, change));
					break;
				}
			} catch (ItemNotFoundException e) {
				logger.warn("Item with server id {} not found.", change.getServerId());
			}
		}
		return clientCommandsBuilder.build();
	}

	private Change updateServerItem(UserDataRequest udr, SyncCollection collection, SyncCollectionChange change) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ItemNotFoundException, ConversionException, HierarchyChangedException {

		return new SyncClientCommands.Change(contentsImporter.importMessageChange(
				udr, collection.getCollectionId(), change.getServerId(), change.getClientId(), change.getData()));
	}

	private Add addServerItem(UserDataRequest udr, SyncCollection collection, SyncCollectionChange change)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ItemNotFoundException, ConversionException, HierarchyChangedException {

		return new SyncClientCommands.Add(change.getClientId(), contentsImporter.importMessageChange(
				udr, collection.getCollectionId(), change.getServerId(), change.getClientId(), change.getData()));
	}
	
	private Change deleteServerItem(UserDataRequest udr, SyncCollection collection, SyncCollectionChange change)
			throws CollectionNotFoundException, DaoException,
			UnexpectedObmSyncServerException, ProcessingEmailException, ItemNotFoundException, UnsupportedBackendFunctionException {

		String serverId = change.getServerId();
		contentsImporter.importMessageDeletion(udr, change.getType(), collection.getCollectionId(), serverId,
				collection.getOptions().isDeletesAsMoves());
		return new SyncClientCommands.Change(serverId);
	}

	@Override
	public void sendResponseWithoutHierarchyChanges(UserDataRequest udr, Responder responder, IContinuation continuation) {
		sendResponse(udr, responder, false, continuation);
	}

	@Override
	public void sendResponse(UserDataRequest udr, Responder responder, boolean sendHierarchyChange, IContinuation continuation) {
		try {
			if (enablePush) {
				SyncProtocol syncProtocol = syncProtocolFactory.create(udr);
				SyncResponse syncResponse = doTheJob(udr, monitoredCollectionService.list(udr.getCredentials(), udr.getDevice()),
						SyncClientCommands.empty(), continuation);
				sendResponse(responder, syncProtocol.encodeResponse(syncResponse));
			} else {
				//Push is not supported, after the heartbeat interval is over, we ask the phone to retry
				sendError(udr.getDevice(), responder, SyncStatus.NEED_RETRY.asSpecificationValue(), continuation);
			}
		} catch (DaoException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (CollectionNotFoundException e) {
			sendError(udr.getDevice(), responder, SyncStatus.OBJECT_NOT_FOUND, continuation, e);
		} catch (UnexpectedObmSyncServerException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (ProcessingEmailException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (InvalidServerId e) {
			sendError(udr.getDevice(), responder, SyncStatus.PROTOCOL_ERROR, continuation, e);			
		} catch (ConversionException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (ServerErrorException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (HierarchyChangedException e) {
			sendError(udr.getDevice(), responder, SyncStatus.HIERARCHY_CHANGED, continuation, e);
		}
	}

	public SyncResponse doTheJob(UserDataRequest udr, Collection<SyncCollection> changedFolders, 
			SyncClientCommands clientCommands, IContinuation continuation) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, InvalidServerId, ConversionException, HierarchyChangedException {

		List<SyncCollectionResponse> syncCollectionResponses = new ArrayList<SyncResponse.SyncCollectionResponse>();
		for (SyncCollection c : changedFolders) {
			SyncCollectionResponse syncCollectionResponse = computeSyncState(udr, clientCommands, c);
			syncCollectionResponses.add(syncCollectionResponse);
		}
		logger.info("Resp for requestId = {}", continuation.getReqId());
		return new SyncResponse(syncCollectionResponses, clientCommands);
	}
	
	private SyncCollectionResponse computeSyncState(UserDataRequest udr,
			SyncClientCommands clientCommands, SyncCollection syncCollection)
			throws DaoException, CollectionNotFoundException, InvalidServerId,
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException, HierarchyChangedException {

		SyncCollectionResponse syncCollectionResponse = new SyncCollectionResponse(syncCollection);
		if (syncCollection.getStatus() != SyncStatus.OK) {
			handleErrorSync(syncCollection, syncCollectionResponse);
		} else if (SyncKey.INITIAL_FOLDER_SYNC_KEY.equals(syncCollection.getSyncKey())) {
			handleInitialSync(udr, syncCollection, syncCollectionResponse);
		} else {
			try {
				handleDataSync(udr, clientCommands, syncCollection, syncCollectionResponse);
			} catch (FilterTypeChangedException e) {
				syncCollectionResponse.getSyncCollection().setStatus(SyncStatus.INVALID_SYNC_KEY);
			}
		}
		return syncCollectionResponse;
	}

	@VisibleForTesting void handleErrorSync(SyncCollection syncCollection, SyncCollectionResponse syncCollectionResponse) {
		if (syncCollection.getStatus() == SyncStatus.OBJECT_NOT_FOUND) {
			syncCollectionResponse.setCollectionValidity(false);
		}
	}

	private void handleDataSync(UserDataRequest udr, SyncClientCommands clientCommands, SyncCollection syncCollection,
			SyncCollectionResponse syncCollectionResponse) throws CollectionNotFoundException, DaoException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, InvalidServerId, ConversionException, FilterTypeChangedException, HierarchyChangedException {

		syncCollectionResponse.setCollectionValidity(true);
		
		ItemSyncState st = stMachine.getItemSyncState(syncCollection.getSyncKey());
		if (st == null) {
			syncCollectionResponse.getSyncCollection().setStatus(SyncStatus.INVALID_SYNC_KEY);
		} else if (syncCollection.isValidToProcess()) {
			syncCollection.setItemSyncState(st);
			Date syncDate = null;
			SyncKey treatmentSyncKey = null;
			if (syncCollection.getFetchIds().isEmpty()) {
				ItemSyncState itemSyncState = doUpdates(udr, syncCollection, clientCommands, syncCollectionResponse);
				syncDate = itemSyncState.getSyncDate();
				treatmentSyncKey = itemSyncState.getSyncKey();
			} else {
				treatmentSyncKey = syncKeyFactory.randomSyncKey();
				syncDate = st.getSyncDate();
				syncCollectionResponse.setItemChanges(
						contentsExporter.fetch(udr, syncCollection, treatmentSyncKey));
			}
			
			identifyNewItems(syncCollectionResponse, st);
			allocateSyncStateIfNew(udr, syncCollection, syncCollectionResponse, syncDate, treatmentSyncKey);
			syncCollectionResponse.setNewSyncKey(treatmentSyncKey);
		}
	}

	private void allocateSyncStateIfNew(UserDataRequest udr, SyncCollection syncCollection, SyncCollectionResponse syncCollectionResponse, Date syncDate, SyncKey treatmentSyncKey) 
				throws DaoException, InvalidServerId {

		if (!Objects.equal(syncCollection.getSyncKey(), treatmentSyncKey)) {
			stMachine.allocateNewSyncState(udr, syncCollection.getCollectionId(), syncDate, 
					syncCollectionResponse.getItemChanges(), syncCollectionResponse.getItemChangesDeletion(), treatmentSyncKey);
		}
	}

	private void handleInitialSync(UserDataRequest udr, SyncCollection syncCollection, SyncCollectionResponse syncCollectionResponse) 
			throws DaoException, InvalidServerId {
		
		backend.resetCollection(udr, syncCollection.getCollectionId());
		syncCollectionResponse.setCollectionValidity(true);
		List<ItemChange> changed = ImmutableList.of();
		List<ItemDeletion> deleted = ImmutableList.of();
		SyncKey newSyncKey = syncKeyFactory.randomSyncKey();
		stMachine.allocateNewSyncState(udr, 
				syncCollection.getCollectionId(), 
				dateService.getEpochPlusOneSecondDate(), 
				changed,
				deleted,
				newSyncKey);
		syncCollectionResponse.setNewSyncKey(newSyncKey);
	}

	private void identifyNewItems(
			SyncCollectionResponse syncCollectionResponse, ItemSyncState st)
			throws DaoException, InvalidServerId {
		
		for (ItemChange change: syncCollectionResponse.getItemChanges()) {
			boolean isItemAddition = st.getSyncKey().equals(SyncKey.INITIAL_FOLDER_SYNC_KEY) || 
					!itemTrackingDao.isServerIdSynced(st, new ServerId(change.getServerId()));
			change.setNew(isItemAddition);
		}
	}
	
	private void sendError(Device device, Responder responder, SyncStatus errorStatus,
			IContinuation continuation, Exception exception) {
		logError(errorStatus, exception);
		sendError(device, responder, errorStatus.asSpecificationValue(), continuation);
	}

	private void logError(SyncStatus errorStatus, Exception exception) {
		if (errorStatus == SyncStatus.SERVER_ERROR) {
			logger.error(exception.getMessage(), exception);
		} else {
			logger.warn(exception.getMessage(), exception);
		}
	}

	@Override
	public void sendError(Device device, Responder responder, String errorStatus, IContinuation continuation) {
		try {
			logger.info("Resp for requestId = {}", continuation.getReqId());
			SyncProtocol syncProtocol = syncProtocolFactory.create(continuation.getUserDataRequest());
			responder.sendWBXMLResponse("AirSync", syncProtocol.encodeResponse(errorStatus) );
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
