package org.obm.push.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.obm.annotations.transactional.Propagation;
import org.obm.annotations.transactional.Transactional;
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
import org.obm.push.bean.Sync;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncCollectionChange;
import org.obm.push.bean.SyncState;
import org.obm.push.bean.SyncStatus;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NoDocumentException;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.exception.PartialException;
import org.obm.push.exception.ProtocolException;
import org.obm.push.exception.WaitIntervalOutOfRangeException;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.SyncProtocol;
import org.obm.push.protocol.bean.SyncRequest;
import org.obm.push.protocol.bean.SyncResponse;
import org.obm.push.protocol.bean.SyncResponse.SyncCollectionResponse;
import org.obm.push.protocol.data.EncoderFactory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;
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

	static {
		waitContinuationCache = new HashMap<Integer, IContinuation>();
	}

	@Inject SyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, IContentsExporter contentsExporter,
			StateMachine stMachine, UnsynchronizedItemDao unSynchronizedItemCache,
			MonitoredCollectionDao monitoredCollectionService, SyncProtocol SyncProtocol,
			CollectionDao collectionDao) {
		
		super(backend, encoderFactory, contentsImporter, contentsExporter, stMachine, collectionDao);
		this.unSynchronizedItemCache = unSynchronizedItemCache;
		this.monitoredCollectionService = monitoredCollectionService;
		this.syncProtocol = SyncProtocol;
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
				responder.sendResponse("AirSync", syncProtocol.endcodeResponse(syncResponse));
			} 
			
		} catch (ProtocolException convExpt) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR.asXmlValue(), null);
		} catch (PartialException pe) {
			sendError(responder, SyncStatus.PARTIAL_REQUEST.asXmlValue(), null);
		} catch (CollectionNotFoundException ce) {
			sendError(responder, SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation);
		} catch (ObjectNotFoundException oe) {
			sendError(responder, SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation);
		} catch (ActiveSyncException e) {
			logger.error(e.getMessage(), e);
		} catch (ContinuationThrowable e) {
			throw e;
		} catch (NoDocumentException e) {
			sendError(responder, SyncStatus.PARTIAL_REQUEST.asXmlValue(), null);
		} catch (WaitIntervalOutOfRangeException e) {
			try {
				responder.sendResponse("AirSync", syncProtocol.encodeResponse() );
			} catch (IOException e1) {
				logger.error(e.getMessage(), e);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional(propagation=Propagation.NESTED)
	private void registerWaitingSync(IContinuation continuation, BackendSession bs, Sync sync)
			throws CollectionNotFoundException, ActiveSyncException, WaitIntervalOutOfRangeException, DaoException {
		
		logger.info("suspend for {} seconds", sync.getWaitInSecond());
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
		
		continuation.suspend(sync.getWaitInSecond() * 1000);
	}

	private Date doUpdates(BackendSession bs, SyncCollection c,	Map<String, String> processedClientIds, SyncCollectionResponse syncCollectionResponse) 
			throws ActiveSyncException, DaoException {

		DataDelta delta = null;
		Date lastSync = null;
		
		int unSynchronizedItemNb = unSynchronizedItemCache.listItemToAdd(bs.getCredentials(), bs.getDevice(), c.getCollectionId()).size();
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
		
		Set<ItemChange> unSyncdeleted = unSynchronizedItemCache.listItemToRemove(bs.getCredentials(), bs.getDevice(), c.getCollectionId());
		
		if (delta != null && delta.getDeletions() != null && unSyncdeleted != null) {
			delta.getDeletions().addAll(unSyncdeleted);
			unSynchronizedItemCache.clearItemToRemove(bs.getCredentials(), bs.getDevice(), c.getCollectionId());
		}

		final List<ItemChange> itemChangesDeletion = new ArrayList<ItemChange>();
		if (delta != null && delta.getDeletions() != null) {
			for (ItemChange ic: delta.getDeletions()) {
				if (processedClientIds.containsKey(ic.getServerId())) {
					unSynchronizedItemCache.storeItemToRemove(bs.getCredentials(), bs.getDevice(), c.getCollectionId(), ic);
				} else {
					itemChangesDeletion.add(ic);
				}
			}
		}
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
		
		changed.addAll(unSynchronizedItemCache.listItemToAdd(credentials, device, collectionId));
		unSynchronizedItemCache.clearItemToAdd(credentials, device,collectionId);
		
		if (changed.size() <= windowSize) {
			return changed;
		}
		
		logger.info("should send {} change(s)", changed.size());
		int changeItem = changed.size() - c.getWindowSize();
		logger.info("WindowsSize value is {} , {} changes will not send", 
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

		int changedSize = changed.size();
		for (int i = windowSize; i < changedSize; i++) {
			ItemChange ic = changed.get(changed.size() - 1);
			unSynchronizedItemCache.storeItemToAdd(credentials, device, collectionId, ic);
			changed.remove(ic);
		}

		changed.addAll(changeByMobile);
		c.setMoreAvailable(true);
		
		return changed;
	}

	private ModificationStatus processCollections(BackendSession bs, Sync sync)
			throws ActiveSyncException, DaoException {
		ModificationStatus modificationStatus = new ModificationStatus();

		for (SyncCollection collection : sync.getCollections()) {
			// get our sync state for this collection
			SyncState collectionState = stMachine.getSyncState(collection.getCollectionId(),
					collection.getSyncKey());
			collection.setSyncState(collectionState);
			// Disables last push request
			IContinuation cont = waitContinuationCache.get(collection
					.getCollectionId());
			if (cont != null) {
				cont.error(SyncStatus.NEED_RETRY.asXmlValue());
			}

			if (collectionState.isValid()) {

				Map<String, String> processedClientIds = processModification(bs, collection);
				modificationStatus.processedClientIds.putAll(processedClientIds);
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
	 * @return 
	 */
	private Map<String, String> processModification(BackendSession bs,
			SyncCollection collection) throws ActiveSyncException, DaoException {
		
		Map<String, String> processedClientIds = new HashMap<String, String>();
		
		for (SyncCollectionChange change : collection.getChanges()) {

			if (change.getModType().equals("Modify")) {
				contentsImporter.importMessageChange(bs, collection.getCollectionId(),
						change.getServerId(), change.getClientId(),
						change.getData());
			} else if (change.getModType().equals("Add")
					|| change.getModType().equals("Change")) {
				logger.info("processing Add/Change (srv = {} | cli = {} )",
						new Object[]{ change.getServerId(), change.getClientId() });
				
				String obmId = contentsImporter.importMessageChange(bs,
						collection.getCollectionId(), change.getServerId(),
						change.getClientId(), change.getData());
				if (obmId != null) {
					if (change.getClientId() != null) {
						processedClientIds.put(obmId, change.getClientId());
					}
					if (change.getServerId() != null) {
						processedClientIds.put(obmId, null);
					}
				}
			} else if (change.getModType().equals("Delete")) {
				contentsImporter.importMessageDeletion(bs, change.getType(),
						collection.getCollectionId(), change.getServerId(),
						collection.getOptions().isDeletesAsMoves());
			}
		}
		return processedClientIds;
	}

	@Override
	public void sendResponseWithoutHierarchyChanges(BackendSession bs, Responder responder, IContinuation continuation) {
		sendResponse(bs, responder, false, continuation);
	}

	@Override
	public void sendResponse(BackendSession bs, Responder responder, boolean sendHierarchyChange, IContinuation continuation) {
		try {
			SyncResponse syncResponse = doTheJob(bs, monitoredCollectionService.list(bs.getCredentials(), bs.getDevice()),
					ImmutableMap.<String, String> of(), continuation);
			responder.sendResponse("AirSync", syncProtocol.endcodeResponse(syncResponse));
		} catch (DaoException e) {
			logger.error(e.getMessage(), e);
		} catch (ActiveSyncException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional
	public SyncResponse doTheJob(BackendSession bs, Collection<SyncCollection> changedFolders, 
			Map<String, String> processedClientIds, IContinuation continuation) throws DaoException, ActiveSyncException {

		final List<SyncCollectionResponse> syncCollectionResponses = new ArrayList<SyncResponse.SyncCollectionResponse>();
		for (SyncCollection c : changedFolders) {
			
			SyncCollectionResponse syncCollectionResponse = new SyncCollectionResponse(c);
			
			if ("0".equals(c.getSyncKey())) {
				backend.resetCollection(bs, c.getCollectionId());
			}
			SyncState st = stMachine.getSyncState(c.getCollectionId(), c.getSyncKey());
			boolean syncStateValid = st.isValid();
			syncCollectionResponse.setSyncStateValid(syncStateValid);
			if (syncStateValid) {

				Date syncDate = null;
				if (!c.getSyncKey().equals("0")) {
					if (c.getFetchIds().size() == 0) {
						syncDate = doUpdates(bs, c, processedClientIds, syncCollectionResponse);
					} else {
						List<ItemChange> itemChanges = contentsExporter.fetch(bs, c.getSyncState().getDataType(), c.getFetchIds());
						syncCollectionResponse.setItemChanges(itemChanges);
					}
				}
				syncCollectionResponse.setNewSyncKey(stMachine.allocateNewSyncKey(bs, c.getCollectionId(), syncDate));
			}
			syncCollectionResponses.add(syncCollectionResponse);
		}
		logger.info("Resp for requestId = {}", continuation.getReqId());
		return new SyncResponse(syncCollectionResponses, bs, getEncoders(), processedClientIds);
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
