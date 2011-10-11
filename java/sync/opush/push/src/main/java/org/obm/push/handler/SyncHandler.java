package org.obm.push.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.SyncStatus;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.WaitIntervalOutOfRangeException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.PartialException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ProtocolException;
import org.obm.push.exception.activesync.ServerItemNotFoundException;
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
		public boolean hasChanges = false;
	}
	
	private static Map<Integer, IContinuation> waitContinuationCache;
	private final SyncProtocol syncProtocol;
	private final UnsynchronizedItemDao unSynchronizedItemCache;
	private final MonitoredCollectionDao monitoredCollectionService;
	private final ItemTrackingDao itemTrackingDao;

	static {
		waitContinuationCache = new HashMap<Integer, IContinuation>();
	}

	@Inject SyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IContentsExporter contentsExporter,
			StateMachine stMachine, UnsynchronizedItemDao unSynchronizedItemCache,
			MonitoredCollectionDao monitoredCollectionService, SyncProtocol SyncProtocol,
			CollectionDao collectionDao, ItemTrackingDao itemTrackingDao) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, stMachine, collectionDao);
		this.unSynchronizedItemCache = unSynchronizedItemCache;
		this.monitoredCollectionService = monitoredCollectionService;
		this.syncProtocol = SyncProtocol;
		this.itemTrackingDao = itemTrackingDao;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs, Document doc, ActiveSyncRequest request, Responder responder) {
		try {
			SyncRequest syncRequest = syncProtocol.getRequest(doc, bs);
			
			ModificationStatus modificationStatus = processCollections(bs, syncRequest.getSync());
			if (!modificationStatus.hasChanges && syncRequest.getSync().getWaitInSecond() > 0) {
				registerWaitingSync(continuation, bs, syncRequest.getSync());
			} else {
				
				SyncResponse syncResponse = doTheJob(bs, syncRequest.getSync().getCollections(), 
						 modificationStatus.processedClientIds, continuation);
				sendResponse(responder, syncProtocol.endcodeResponse(syncResponse));
			} 
		} catch (InvalidServerId e) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR.asXmlValue(), e);
		} catch (ProtocolException convExpt) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR.asXmlValue(), convExpt);
		} catch (PartialException pe) {
			sendError(responder, SyncStatus.PARTIAL_REQUEST.asXmlValue(), pe);
		} catch (CollectionNotFoundException ce) {
			sendError(responder, SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation, ce);
		} catch (ContinuationThrowable e) {
			throw e;
		} catch (NoDocumentException e) {
			sendError(responder, SyncStatus.PARTIAL_REQUEST.asXmlValue(), e);
		} catch (WaitIntervalOutOfRangeException e) {
			sendResponse(responder, syncProtocol.encodeResponse());
		} catch (DaoException e) {
			sendError(responder, SyncStatus.SERVER_ERROR.asXmlValue(), e);
		} catch (UnknownObmSyncServerException e) {
			sendError(responder, SyncStatus.SERVER_ERROR.asXmlValue(), e);
		} catch (ProcessingEmailException e) {
			sendError(responder, SyncStatus.SERVER_ERROR.asXmlValue(), e);
		}
	}

	private void sendResponse(Responder responder, Document document) {
		responder.sendResponse("AirSync", document);
	}
	
	private void registerWaitingSync(IContinuation continuation, BackendSession bs, Sync sync)
			throws CollectionNotFoundException, WaitIntervalOutOfRangeException, DaoException {
		
		if (sync.getWaitInSecond() > 3540) {
			throw new WaitIntervalOutOfRangeException();
		}
		
		for (SyncCollection sc : sync.getCollections()) {
			String collectionPath = collectionDao.getCollectionPath(sc.getCollectionId());
			sc.setCollectionPath(collectionPath);
			PIMDataType dataClass = PIMDataType.getPIMDataType(collectionPath);
			if ("email".equalsIgnoreCase(dataClass.toString())) {
				backend.startEmailMonitoring(bs, sc.getCollectionId());
				break;
			}
		}
		continuation.setLastContinuationHandler(this);
		monitoredCollectionService.put(bs.getCredentials(), bs.getDevice(), sync.getCollections());
		CollectionChangeListener l = new CollectionChangeListener(bs,
				continuation, sync.getCollections());
		IListenerRegistration reg = backend.addChangeListener(l);
		continuation.setListenerRegistration(reg);
		continuation.setCollectionChangeListener(l);
		for (SyncCollection sc : sync.getCollections()) {
			waitContinuationCache.put(sc.getCollectionId(),
					continuation);
		}
		
		continuation.suspend(bs, sync.getWaitInSecond());
	}

	private Date doUpdates(BackendSession bs, SyncCollection c,	Map<String, String> processedClientIds, 
			SyncCollectionResponse syncCollectionResponse) throws DaoException, CollectionNotFoundException, 
			UnknownObmSyncServerException, ProcessingEmailException {

		DataDelta delta = null;
		Date lastSync = null;
		
		int unSynchronizedItemNb = unSynchronizedItemCache.listItemsToAdd(bs.getCredentials(), bs.getDevice(), c.getCollectionId()).size();
		if (unSynchronizedItemNb == 0) {
			delta = contentsExporter.getChanged(bs, c.getSyncState(), c.getOptions().getFilterType(), c.getCollectionId());
			lastSync = delta.getSyncDate();
		}

		List<ItemChange> changed = processWindowSize(c, delta, bs, processedClientIds);
		syncCollectionResponse.setItemChanges(changed);
	
		List<ItemChange> itemChangesDeletion = serializeDeletion(bs, c, processedClientIds, delta);
		syncCollectionResponse.setItemChangesDeletion(itemChangesDeletion);
		
		return lastSync;
	}

	private List<ItemChange> serializeDeletion(BackendSession bs, SyncCollection c, Map<String, String> processedClientIds, 
			DataDelta delta) {
		
		Set<ItemChange> unSyncdeleted = unSynchronizedItemCache.listItemsToRemove(bs.getCredentials(), bs.getDevice(), c.getCollectionId());
		
		if (delta != null && delta.getDeletions() != null && unSyncdeleted != null) {
			delta.getDeletions().addAll(unSyncdeleted);
			unSynchronizedItemCache.clearItemsToRemove(bs.getCredentials(), bs.getDevice(), c.getCollectionId());
		}

		ArrayList<ItemChange> toKeepForLaterSync = new ArrayList<ItemChange>();
		
		final List<ItemChange> itemChangesDeletion = new ArrayList<ItemChange>();
		if (delta != null && delta.getDeletions() != null) {
			for (ItemChange ic: delta.getDeletions()) {
				if (processedClientIds.containsKey(ic.getServerId())) {
					toKeepForLaterSync.add(ic);
				} else {
					itemChangesDeletion.add(ic);
				}
			}
		}
		unSynchronizedItemCache.storeItemsToRemove(bs.getCredentials(), bs.getDevice(), c.getCollectionId(), toKeepForLaterSync);
		
		return itemChangesDeletion;
	}

	private List<ItemChange> processWindowSize(SyncCollection c, DataDelta delta, 
			BackendSession backendSession, Map<String, String> processedClientIds) {
		
		final Credentials credentials = backendSession.getCredentials();
		final Device device = backendSession.getDevice();
		final Integer collectionId = c.getCollectionId();
		final Integer windowSize = c.getWindowSize();	
		
		final List<ItemChange> changed = new ArrayList<ItemChange>();
		if (delta != null) {
			changed.addAll(delta.getChanges());
		}
		
		changed.addAll(unSynchronizedItemCache.listItemsToAdd(credentials, device, collectionId));
		unSynchronizedItemCache.clearItemsToAdd(credentials, device,collectionId);
		
		if (changed.size() <= windowSize) {
			return changed;
		}
		
		logger.info("should send {} change(s)", changed.size());
		int changeItem = changed.size() - c.getWindowSize();
		logger.info("WindowsSize value is {} , {} changes will not be sent", 
				new Object[]{ c.getWindowSize(), (changeItem < 0 ? 0 : changeItem) });

		final Set<ItemChange> changeByMobile = new HashSet<ItemChange>();
		// Find changes ask by the device
		for (Iterator<ItemChange> it = changed.iterator(); it.hasNext();) {
			ItemChange ic = it.next();
			if (processedClientIds.get(ic.getServerId()) != null
					|| processedClientIds.keySet().contains(ic.getServerId())) {
				changeByMobile.add(ic);
				it.remove();
			}
			if (processedClientIds.size() == changeByMobile.size()) {
				break;
			}
		}

		ArrayList<ItemChange> toKeepForLaterSync = new ArrayList<ItemChange>();
		
		int changedSize = changed.size();
		for (int i = windowSize; i < changedSize; i++) {
			ItemChange ic = changed.get(changed.size() - 1);
			toKeepForLaterSync.add(ic);
			changed.remove(ic);
		}

		unSynchronizedItemCache.storeItemsToAdd(credentials, device, collectionId, toKeepForLaterSync);
		changed.addAll(changeByMobile);
		c.setMoreAvailable(true);
		
		return changed;
	}

	private ModificationStatus processCollections(BackendSession bs, Sync sync) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException, ProcessingEmailException {
		
		ModificationStatus modificationStatus = new ModificationStatus();

		for (SyncCollection collection : sync.getCollections()) {

			// Disables last push request
			IContinuation cont = waitContinuationCache.get(collection
					.getCollectionId());
			if (cont != null) {
				cont.error(SyncStatus.NEED_RETRY.asXmlValue());
			}

			// get our sync state for this collection
			SyncState collectionState = stMachine.getSyncState(collection.getSyncKey());

			if (collectionState != null) {
				collection.setSyncState(collectionState);
				Map<String, String> processedClientIds = processModification(bs, collection);
				modificationStatus.processedClientIds.putAll(processedClientIds);
			} else {
				collection.setSyncState(
						new SyncState(
								collectionDao.getCollectionPath(collection.getCollectionId()), 
																collection.getSyncKey()));
			}
			
			modificationStatus.hasChanges |= haveFilteredItemToSync(bs, collection);
		}
		return modificationStatus;
	}

	private boolean haveFilteredItemToSync(BackendSession bs,
			SyncCollection collection) throws DaoException {
		return contentsExporter.getFilterChanges(bs, collection);
	}
	
	/**
	 * Handles modifications requested by mobile device
	 */
	private Map<String, String> processModification(BackendSession bs, SyncCollection collection) throws CollectionNotFoundException, 
		DaoException, UnknownObmSyncServerException, ProcessingEmailException {
		
		Map<String, String> processedClientIds = new HashMap<String, String>();
		for (SyncCollectionChange change: collection.getChanges()) {

			if (change.getModType().equals("Modify")) {
				updateServerItem(bs, collection, change);

			} else if (change.getModType().equals("Add") || change.getModType().equals("Change")) {
				addServerItem(bs, collection, processedClientIds, change);
			
			} else if (change.getModType().equals("Delete")) {
				deleteServerItem(bs, collection, processedClientIds, change);
			}
		}
		return processedClientIds;
	}

	private void updateServerItem(BackendSession bs, SyncCollection collection, SyncCollectionChange change) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException, ProcessingEmailException {
		
		try {
			contentsImporter.importMessageChange(bs, collection.getCollectionId(), change.getServerId(), change.getClientId(),
					change.getData());
		} catch (ServerItemNotFoundException e) {
			logger.info("Item {} not exist on server. The client has sent a malformed or invalid item. Stop sending the item !", 
					e.getServerItemId());
		}
	}
	
	private void addServerItem(BackendSession bs, SyncCollection collection, Map<String, String> processedClientIds, 
			SyncCollectionChange change) throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException, ProcessingEmailException {
		
		try {
			String obmId = contentsImporter.importMessageChange(bs,	collection.getCollectionId(), change.getServerId(),
					change.getClientId(), change.getData());
		
			if (obmId != null) {
				
				if (change.getClientId() != null) {
					processedClientIds.put(obmId, change.getClientId());
				}
				
				if (change.getServerId() != null) {
					processedClientIds.put(obmId, null);
				}
				
			}
			
		} catch (ServerItemNotFoundException e) {
			logger.info("Item {} not exist on server. The client has sent a malformed or invalid item. Stop sending the item !", 
					e.getServerItemId());
		}
	}
	
	private void deleteServerItem(BackendSession bs, SyncCollection collection, Map<String, String> processedClientIds, 
			SyncCollectionChange change) throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException, ProcessingEmailException {
	
		try {
			
			contentsImporter.importMessageDeletion(bs, change.getType(), collection.getCollectionId(), change.getServerId(),
					collection.getOptions().isDeletesAsMoves());
			
			processedClientIds.put(change.getServerId(), null);
		} catch (ServerItemNotFoundException e) {
			logger.info("Item {} not exist on server. The client has sent a malformed or invalid item. Stop sending the item !", 
					e.getServerItemId());
		}
	}

	@Override
	public void sendResponseWithoutHierarchyChanges(BackendSession bs, Responder responder, IContinuation continuation) {
		sendResponse(bs, responder, false, continuation);
	}

	@Override
	public void sendResponse(BackendSession bs, Responder responder, boolean sendHierarchyChange, IContinuation continuation) {
		try {
			SyncResponse syncResponse = doTheJob(bs, monitoredCollectionService.list(bs.getCredentials(), bs.getDevice()),
					Collections.EMPTY_MAP, continuation);
			sendResponse(responder, syncProtocol.endcodeResponse(syncResponse));
		} catch (DaoException e) {
			sendError(responder, SyncStatus.SERVER_ERROR.asXmlValue(), e);
		} catch (CollectionNotFoundException e) {
			sendError(responder, SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation, e);
		} catch (UnknownObmSyncServerException e) {
			sendError(responder, SyncStatus.SERVER_ERROR.asXmlValue(), e);
		} catch (ProcessingEmailException e) {
			sendError(responder, SyncStatus.SERVER_ERROR.asXmlValue(), e);
		} catch (InvalidServerId e) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR.asXmlValue(), e);			
		}
	}

	public SyncResponse doTheJob(BackendSession bs, Collection<SyncCollection> changedFolders, 
			Map<String, String> processedClientIds, IContinuation continuation) throws DaoException, 
			CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException, InvalidServerId {

		final List<SyncCollectionResponse> syncCollectionResponses = new ArrayList<SyncResponse.SyncCollectionResponse>();
		for (SyncCollection c : changedFolders) {
			SyncCollectionResponse syncCollectionResponse = computeSyncState(bs, processedClientIds, c);
			syncCollectionResponses.add(syncCollectionResponse);
		}
		logger.info("Resp for requestId = {}", continuation.getReqId());
		return new SyncResponse(syncCollectionResponses, bs, getEncoders(), processedClientIds);
	}

	private SyncCollectionResponse computeSyncState(BackendSession bs,
			Map<String, String> processedClientIds, SyncCollection syncCollection)
			throws DaoException, CollectionNotFoundException, InvalidServerId,
			UnknownObmSyncServerException, ProcessingEmailException {

		SyncCollectionResponse syncCollectionResponse = new SyncCollectionResponse(syncCollection);
		
		if ("0".equals(syncCollection.getSyncKey())) {
			handleInitialSync(bs, syncCollection, syncCollectionResponse);
		} else {
			handleDataSync(bs, processedClientIds, syncCollection, syncCollectionResponse);
		}
		return syncCollectionResponse;
	}

	private void handleDataSync(BackendSession bs,
			Map<String, String> processedClientIds,
			SyncCollection syncCollection,
			SyncCollectionResponse syncCollectionResponse)
			throws CollectionNotFoundException, DaoException,
			UnknownObmSyncServerException, ProcessingEmailException,
			InvalidServerId {
		
		SyncState st = stMachine.getSyncState(syncCollection.getSyncKey());
		if (st == null) {
			syncCollectionResponse.setSyncStateValid(false);
		} else {
			syncCollection.setSyncState(st);
			syncCollectionResponse.setSyncStateValid(true);
			Date syncDate = null;
			if (syncCollection.getFetchIds().isEmpty()) {
				syncDate = doUpdates(bs, syncCollection, processedClientIds, syncCollectionResponse);
			} else {
				List<ItemChange> itemChanges = contentsExporter.fetch(bs, syncCollection.getSyncState().getDataType(), syncCollection.getFetchIds());
				syncCollectionResponse.setItemChanges(itemChanges);
			}
			identifyNewItems(syncCollectionResponse, st);
			String newSyncKey = stMachine.allocateNewSyncKey(bs, syncCollection.getCollectionId(), syncDate, syncCollectionResponse.getItemChanges());
			syncCollectionResponse.setNewSyncKey(newSyncKey);
		}
	}

	private void handleInitialSync(BackendSession bs,
			SyncCollection syncCollection,
			SyncCollectionResponse syncCollectionResponse) throws DaoException,
			CollectionNotFoundException, InvalidServerId {
		
		backend.resetCollection(bs, syncCollection.getCollectionId());
		syncCollectionResponse.setSyncStateValid(true);
		String newSyncKey = stMachine.allocateNewSyncKey(bs, syncCollection.getCollectionId(), null, ImmutableList.<ItemChange>of());
		syncCollectionResponse.setNewSyncKey(newSyncKey);
	}

	private void identifyNewItems(
			SyncCollectionResponse syncCollectionResponse, SyncState st)
			throws DaoException, InvalidServerId {
		
		for (ItemChange change: syncCollectionResponse.getItemChanges()) {
			boolean isItemAddition = st.getKey().equals("0") || 
					!itemTrackingDao.isServerIdSynced(st, new ServerId(change.getServerId()));
			change.setIsNew(isItemAddition);
		}
	}

	
	private void sendError(Responder responder, String errorStatus, Exception exception) {
		sendError(responder, errorStatus, null, exception);
	}
	
	private void sendError(Responder responder, String errorStatus, IContinuation continuation, Exception exception) {
		logger.error(exception.getMessage(), exception);
		sendError(responder, errorStatus, continuation);
	}

	@Override
	public void sendError(Responder responder, String errorStatus, IContinuation continuation) {
		try {
			if (continuation != null) {
				logger.info("Resp for requestId = {}", continuation.getReqId());
			}
			responder.sendResponse("AirSync", syncProtocol.encodeResponse(errorStatus) );
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
