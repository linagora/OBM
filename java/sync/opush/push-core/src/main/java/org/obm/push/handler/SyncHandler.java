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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.naming.NoPermissionException;

import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.obm.push.ContinuationService;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.Device;
import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.ItemSyncState;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollectionCommand;
import org.obm.push.bean.SyncCollectionCommands;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.SyncStatus;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.client.SyncClientCommands.Add;
import org.obm.push.bean.change.client.SyncClientCommands.Deletion;
import org.obm.push.bean.change.client.SyncClientCommands.Update;
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
import org.obm.push.exception.activesync.InvalidSyncKeyException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.exception.activesync.ServerErrorException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.mail.exception.FilterTypeChangedException;
import org.obm.push.mail.exception.ImapTimeoutException;
import org.obm.push.protocol.SyncProtocol;
import org.obm.push.protocol.bean.AnalysedSyncRequest;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.service.DateService;
import org.obm.push.state.StateMachine;
import org.obm.push.state.SyncKeyFactory;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.wbxml.WBXMLTools;
import org.w3c.dom.Document;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SyncHandler extends WbxmlRequestHandler implements IContinuationHandler {

	private final SyncProtocol.Factory syncProtocolFactory;
	private final MonitoredCollectionDao monitoredCollectionService;
	private final ItemTrackingDao itemTrackingDao;
	private final ICollectionPathHelper collectionPathHelper;
	private final ContinuationService continuationService;
	private final boolean enablePush;
	private final SyncKeyFactory syncKeyFactory;
	private final DateService dateService;

	@Inject SyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IContentsExporter contentsExporter,
			StateMachine stMachine,
			MonitoredCollectionDao monitoredCollectionService, SyncProtocol.Factory syncProtocolFactory,
			CollectionDao collectionDao, ItemTrackingDao itemTrackingDao,
			WBXMLTools wbxmlTools, DOMDumper domDumper, ICollectionPathHelper collectionPathHelper,
			ContinuationService continuationService,
			@Named("enable-push") boolean enablePush,
			SyncKeyFactory syncKeyFactory,
			DateService dateService) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, 
				stMachine, collectionDao, wbxmlTools, domDumper);
		
		this.monitoredCollectionService = monitoredCollectionService;
		this.syncProtocolFactory = syncProtocolFactory;
		this.itemTrackingDao = itemTrackingDao;
		this.collectionPathHelper = collectionPathHelper;
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
			
			continuationService.cancel(udr.getDevice());
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
			sendError(udr.getDevice(), responder, SyncStatus.CONVERSATION_ERROR_OR_INVALID_ITEM, continuation, e);
		} catch (InvalidSyncKeyException e) {
			sendError(udr.getDevice(), responder, SyncStatus.INVALID_SYNC_KEY, continuation, e);
		} catch (ImapTimeoutException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} catch (RuntimeException e) {
			sendError(udr.getDevice(), responder, SyncStatus.SERVER_ERROR, continuation, e);
		} 
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendWBXMLResponse("AirSync", document);
	}
	
	private void registerWaitingSync(IContinuation continuation, UserDataRequest udr, Sync syncRequest) 
			throws CollectionNotFoundException, WaitIntervalOutOfRangeException, DaoException, CollectionPathException, WaitSyncFolderLimitException {
		
		if (!enablePush) {
			throw new WaitSyncFolderLimitException(0);
		}
		
		if (syncRequest.getWaitInSecond() > 3540) {
			throw new WaitIntervalOutOfRangeException();
		}
		
		for (AnalysedSyncCollection sc: syncRequest.getCollections()) {
			String collectionPath = collectionDao.getCollectionPath(sc.getCollectionId());
			PIMDataType dataClass = collectionPathHelper.recognizePIMDataType(collectionPath);
			if (dataClass == PIMDataType.EMAIL) {
				backend.startEmailMonitoring(udr, sc.getCollectionId());
				break;
			}
		}
		
		continuation.setLastContinuationHandler(this);
		monitoredCollectionService.put(udr.getCredentials(), udr.getDevice(), syncRequest.getCollections());
		CollectionChangeListener l = new CollectionChangeListener(udr, continuation, syncRequest.getCollections());
		IListenerRegistration reg = backend.addChangeListener(l);
		continuation.setListenerRegistration(reg);
		continuation.setCollectionChangeListener(l);
		
		continuationService.suspend(udr, continuation, syncRequest.getWaitInSecond(), SyncStatus.NEED_RETRY.asSpecificationValue());
	}

	private Date doUpdates(UserDataRequest udr, AnalysedSyncCollection request, SyncClientCommands clientCommands, 
			SyncCollectionResponse.Builder responseBuilder, ItemSyncState syncState, SyncKey newSyncKey) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException, FilterTypeChangedException, HierarchyChangedException, InvalidServerId {

		DataDelta delta = contentsExporter.getChanged(udr, syncState, request, clientCommands, newSyncKey);
		
		responseBuilder
			.responses(SyncCollectionCommands.Response.builder()
					.fetchs(fetchItems(udr, request, syncState))
					.changes(identifyNewItems(delta.getChanges(), syncState), clientCommands)
					.deletions(delta.getDeletions())
					.build())
			.moreAvailable(delta.hasMoreAvailable());
		
		return delta.getSyncDate();
	}

	private List<ItemChange> fetchItems(UserDataRequest udr, AnalysedSyncCollection request, ItemSyncState syncState) {
		if (!request.getFetchIds().isEmpty()) {
			try {
				return identifyNewItems(contentsExporter.fetch(udr, syncState, request), syncState);
			} catch (ItemNotFoundException e) {
				logger.warn("At least one item to fetch can not be found", e);
			}
		}
		return ImmutableList.of();
	}

	private SyncClientCommands processClientCommands(UserDataRequest udr, Sync syncRequest) throws CollectionNotFoundException, DaoException, 
		UnexpectedObmSyncServerException, ProcessingEmailException, UnsupportedBackendFunctionException, ConversionException, HierarchyChangedException {
		
		SyncClientCommands.Builder clientCommandsBuilder = SyncClientCommands.builder();

		for (AnalysedSyncCollection collection : collectionsValidToProcess(syncRequest.getCollections())) {

			// we don't merge modifications on unknown collections
			ItemSyncState collectionState = stMachine.getItemSyncState(collection.getSyncKey());
			if (collectionState != null) {
				clientCommandsBuilder.merge(processClientModification(udr, collection));
			}
		}
		return clientCommandsBuilder.build();
	}
	
	private Set<AnalysedSyncCollection> collectionsValidToProcess(Set<AnalysedSyncCollection> collections) {
		return FluentIterable
				.from(collections)
				.filter(new Predicate<AnalysedSyncCollection>() {

					@Override
					public boolean apply(AnalysedSyncCollection input) {
						return isDataTypeKnown(input.getDataType());
					}
				}).toSet();
	}

	private boolean isDataTypeKnown(PIMDataType dataType) {
		return dataType != PIMDataType.UNKNOWN;
	}

	@VisibleForTesting SyncClientCommands processClientModification(UserDataRequest udr, AnalysedSyncCollection collection)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, UnsupportedBackendFunctionException, ConversionException, HierarchyChangedException {

		SyncClientCommands.Builder clientCommandsBuilder = SyncClientCommands.builder();
		SyncCollectionCommands.Response commands = collection.getCommands();
		for (SyncCollectionCommand.Response change: commands.getCommands()) {
			try {
				switch (change.getType()) {
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
			} catch (NoPermissionException e) {
				logger.warn("Client is not allowed to perform the command: {}", change);
			}
		}
		return clientCommandsBuilder.build();
	}

	private Update updateServerItem(UserDataRequest udr, AnalysedSyncCollection collection, SyncCollectionCommand.Response change) 
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ItemNotFoundException, ConversionException, HierarchyChangedException, NoPermissionException {

		return new SyncClientCommands.Update(contentsImporter.importMessageChange(
				udr, collection.getCollectionId(), change.getServerId(), change.getClientId(), change.getApplicationData()));
	}

	private Add addServerItem(UserDataRequest udr, AnalysedSyncCollection collection, SyncCollectionCommand.Response change)
			throws CollectionNotFoundException, DaoException, UnexpectedObmSyncServerException,
			ProcessingEmailException, ItemNotFoundException, ConversionException, HierarchyChangedException, NoPermissionException {

		return new SyncClientCommands.Add(change.getClientId(), contentsImporter.importMessageChange(
				udr, collection.getCollectionId(), change.getServerId(), change.getClientId(), change.getApplicationData()));
	}
	
	private Deletion deleteServerItem(UserDataRequest udr, AnalysedSyncCollection collection, SyncCollectionCommand.Response change)
			throws CollectionNotFoundException, DaoException,
			UnexpectedObmSyncServerException, ProcessingEmailException, ItemNotFoundException, UnsupportedBackendFunctionException {

		String serverId = change.getServerId();
		contentsImporter.importMessageDeletion(udr, collection.getDataType(), collection.getCollectionId(), serverId,
				collection.getOptions().isDeletesAsMoves());
		return new SyncClientCommands.Deletion(serverId);
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
		} catch (InvalidSyncKeyException e) {
			sendError(udr.getDevice(), responder, SyncStatus.INVALID_SYNC_KEY, continuation, e);
		}
	}

	public SyncResponse doTheJob(UserDataRequest udr, Collection<AnalysedSyncCollection> changedFolders, 
			SyncClientCommands clientCommands, IContinuation continuation) throws DaoException, CollectionNotFoundException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, InvalidServerId, ConversionException, HierarchyChangedException {

		SyncResponse.Builder builder = SyncResponse.builder();
		
		for (AnalysedSyncCollection c : changedFolders) {
			builder.addResponse(computeSyncState(udr, clientCommands, c));
		}
		logger.info("Resp for requestId = {}", continuation.getReqId());
		return builder.build();
	}
	
	private SyncCollectionResponse computeSyncState(UserDataRequest udr,
			SyncClientCommands clientCommands, AnalysedSyncCollection syncCollectionRequest)
			throws DaoException, CollectionNotFoundException, InvalidServerId,
			UnexpectedObmSyncServerException, ProcessingEmailException, ConversionException, HierarchyChangedException  {

		PIMDataType dataType = syncCollectionRequest.getDataType();
		SyncCollectionResponse.Builder builder = SyncCollectionResponse.builder()
				.collectionId(syncCollectionRequest.getCollectionId())
				.dataType(dataType);
			
		ItemSyncState newItemSyncState = null;
		if (syncCollectionRequest.getStatus() != SyncStatus.OK) {
			builder.status(syncCollectionRequest.getStatus());
		} else if (isDataTypeKnown(dataType)) {
			if (SyncKey.INITIAL_FOLDER_SYNC_KEY.equals(syncCollectionRequest.getSyncKey())) {
				handleInitialSync(udr, syncCollectionRequest, builder);
			} else {
				try {
					newItemSyncState = handleDataSync(udr, clientCommands, syncCollectionRequest, builder);
				} catch (FilterTypeChangedException e) {
					builder.status(SyncStatus.INVALID_SYNC_KEY);
				}
			}
		} else {
			builder.status(SyncStatus.OBJECT_NOT_FOUND);
		}
		
		SyncCollectionResponse syncCollectionResponse = builder.build();
		allocateSyncStateIfNew(udr, syncCollectionRequest, syncCollectionResponse, newItemSyncState);
		return syncCollectionResponse;
	}

	private ItemSyncState handleDataSync(UserDataRequest udr, SyncClientCommands clientCommands, AnalysedSyncCollection request,
			SyncCollectionResponse.Builder builder) throws CollectionNotFoundException, DaoException, 
			UnexpectedObmSyncServerException, ProcessingEmailException, InvalidServerId, ConversionException, FilterTypeChangedException, HierarchyChangedException {

		ItemSyncState st = stMachine.getItemSyncState(request.getSyncKey());
		if (st == null) {
			builder.status(SyncStatus.INVALID_SYNC_KEY);
			return null;
		} else {
			SyncKey newSyncKey = syncKeyFactory.randomSyncKey();
			Date newSyncDate = doUpdates(udr, request, clientCommands, builder, st, newSyncKey);
			builder.syncKey(newSyncKey).status(SyncStatus.OK);
			
			return ItemSyncState.builder()
					.syncDate(newSyncDate)
					.syncKey(newSyncKey)
					.build();
		}
	}

	private void allocateSyncStateIfNew(UserDataRequest udr, AnalysedSyncCollection request, SyncCollectionResponse syncCollectionResponse, ItemSyncState itemSyncState) 
				throws DaoException, InvalidServerId {

		if (itemSyncState != null && !Objects.equal(request.getSyncKey(), itemSyncState.getSyncKey())) {
			stMachine.allocateNewSyncState(udr, request.getCollectionId(), itemSyncState.getSyncDate(), 
					syncCollectionResponse.getItemChanges(), syncCollectionResponse.getItemChangesDeletion(), itemSyncState.getSyncKey());
		}
	}

	private void handleInitialSync(UserDataRequest udr, AnalysedSyncCollection syncCollectionRequest, SyncCollectionResponse.Builder builder) 
			throws DaoException, InvalidServerId {
		
		backend.resetCollection(udr, syncCollectionRequest.getCollectionId());
		List<ItemChange> changed = ImmutableList.of();
		List<ItemDeletion> deleted = ImmutableList.of();
		SyncKey newSyncKey = syncKeyFactory.randomSyncKey();
		stMachine.allocateNewSyncState(udr, 
				syncCollectionRequest.getCollectionId(), 
				dateService.getEpochPlusOneSecondDate(), 
				changed,
				deleted,
				newSyncKey);
		builder.syncKey(newSyncKey)
			.status(SyncStatus.OK);
	}

	private List<ItemChange> identifyNewItems(List<ItemChange> itemChanges, ItemSyncState st)
			throws DaoException, InvalidServerId {

		for (ItemChange change: itemChanges) {
			boolean isItemAddition = st.getSyncKey().equals(SyncKey.INITIAL_FOLDER_SYNC_KEY) || 
					!itemTrackingDao.isServerIdSynced(st, new ServerId(change.getServerId()));
			change.setNew(isItemAddition);
		}
		return itemChanges;
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
