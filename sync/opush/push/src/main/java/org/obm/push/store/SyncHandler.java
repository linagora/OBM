package org.obm.push.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.obm.push.ActiveSyncServlet;
import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.backend.Sync;
import org.obm.push.data.EncoderFactory;
import org.obm.push.data.IDataEncoder;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.exception.PartialException;
import org.obm.push.exception.ProtocolException;
import org.obm.push.impl.ActiveSyncRequest;
import org.obm.push.impl.IContinuationHandler;
import org.obm.push.impl.Responder;
import org.obm.push.impl.SyncDecoder;
import org.obm.push.impl.WbxmlRequestHandler;
import org.obm.push.state.StateMachine;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class SyncHandler extends WbxmlRequestHandler implements
		IContinuationHandler {

	public static final Integer SYNC_TRUNCATION_ALL = 9;
	private static Map<Integer, IContinuation> waitContinuationCache;
	private SyncDecoder syncDecoder;

	static {
		waitContinuationCache = new HashMap<Integer, IContinuation>();
	}

	@Inject
	private SyncHandler(IBackend backend, EncoderFactory encoderFactory,
			SyncDecoder syncDecoder, IContentsImporter contentsImporter,
			ISyncStorage storage, IContentsExporter contentsExporter,
			StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
		
		this.syncDecoder = syncDecoder;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		try {
			Map<String, String> processedClientIds = new HashMap<String, String>();
			Sync sync = null;
			if (doc != null) {
				sync = syncDecoder.decodeSync(doc, bs);
				bs.setLastSyncProcessedClientIds(processedClientIds);
			} else {
				if (bs.getLastSync() == null) {
					throw new PartialException();
				}
				sync = bs.getLastSync();
			}
			boolean modif = processCollections(bs, sync, processedClientIds);
			if (sync.getWaitInSecond() > 0 && !modif) {
				logger.info("suspend for " + sync.getWaitInSecond()
						+ " seconds");
				if (sync.getWaitInSecond() > 3540) {
					sendWaitIntervalOutOfRangeError(responder);
					return;
				}
				for (SyncCollection sc : sync.getCollections()) {
					String collectionPath = storage.getCollectionPath(sc.getCollectionId());
					sc.setCollectionPath(collectionPath);
					PIMDataType dataClass = storage.getDataClass(
							collectionPath);
					if ("email".equalsIgnoreCase(dataClass.toString())) {
						backend.startEmailMonitoring(bs, sc.getCollectionId());
						break;
					}
				}
				bs.setLastContinuationHandler(ActiveSyncServlet.SYNC_HANDLER);
				bs.setLastMonitored(sync.getCollectionById());
				CollectionChangeListener l = new CollectionChangeListener(bs,
						continuation, sync.getCollections());
				IListenerRegistration reg = backend.addChangeListener(l);
				continuation.setListenerRegistration(reg);
				continuation.setCollectionChangeListener(l);
				for (SyncCollection sc : sync.getCollections()) {
					waitContinuationCache.put(sc.getCollectionId(),
							continuation);
				}

				synchronized (bs) {
					// logger
					// .warn("for testing purpose, we will only suspend for 40sec (to monitor: "
					// + bs.getLastMonitored() + ")");
					// continuation.suspend(10 * 1000);
					continuation.suspend(sync.getWaitInSecond() * 1000);
				}
			} else {
				processResponse(bs, responder, sync.getCollections(),
						processedClientIds, continuation);
			}

		} catch (ProtocolException convExpt) {
			sendError(responder, SyncStatus.PROTOCOL_ERROR.asXmlValue());
		} catch (PartialException pe) {
			// Status 13
			// The client sent an empty or partial Sync request,
			// but the server is unable to process it. Please
			// resend the request with the full XML
			sendError(responder, SyncStatus.PARTIAL_REQUEST.asXmlValue());
		} catch (CollectionNotFoundException ce) {
			sendError(responder, new HashSet<SyncCollection>(),
					SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation);
		} catch (ObjectNotFoundException oe) {
			sendError(responder, new HashSet<SyncCollection>(),
					SyncStatus.OBJECT_NOT_FOUND.asXmlValue(), continuation);
		} catch (ActiveSyncException e) {
			// sendError(responder, new HashSet<SyncCollection>(),
			// SyncStatus.SERVER_ERROR.asXmlValue(), continuation);
			logger.error(e.getMessage(), e);

		} catch (ContinuationThrowable e) {
			// used by org.mortbay.util.ajax.Continuation
			throw e;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void sendWaitIntervalOutOfRangeError(Responder responder) {
		try {
			Document reply = null;
			reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();
			DOMUtils.createElementAndText(root, "Status",
					SyncStatus.WAIT_INTERVAL_OUT_OF_RANGE.asXmlValue());
			DOMUtils.createElementAndText(root, "Limit", "59");
			responder.sendResponse("AirSync", reply);
		} catch (Exception e) {
			logger.error("Error creating Sync response", e);
		}
	}

	private void doUpdates(BackendSession bs, SyncCollection c, Element ce,
			Map<String, String> processedClientIds,
			Collection<FolderType> allSyncFolderType)
			throws ActiveSyncException {

		DataDelta delta = null;
		if (bs.getUnSynchronizedItemChange(c.getCollectionId()).size() == 0) {
			delta = contentsExporter.getChanged(bs, c.getSyncState(), c.getFilterType(),
					c.getCollectionId(), allSyncFolderType);
		}

		List<ItemChange> changed = processWindowSize(c, delta, bs,
				processedClientIds);

		Element responses = DOMUtils.createElement(ce, "Responses");
		if (c.isMoreAvailable()) {
			// MoreAvailable has to be before Commands
			DOMUtils.createElement(ce, "MoreAvailable");
		}
		Element commands = DOMUtils.createElement(ce, "Commands");

		StringBuilder processedIds = new StringBuilder();
		for (Entry<String, String> k : processedClientIds.entrySet()) {
			processedIds.append(' ').append(k.getValue());
		}

		Set<ItemChange> unSyncdeleted = bs.getUnSynchronizedDeletedItemChange(c
				.getCollectionId());
		if (delta != null && delta.getDeletions() != null
				&& unSyncdeleted != null) {
			delta.getDeletions().addAll(unSyncdeleted);
			unSyncdeleted.clear();
		}

		if (delta != null && delta.getDeletions() != null) {
			for (ItemChange ic : delta.getDeletions()) {
				if (processedClientIds.containsKey(ic.getServerId())) {
					// changed.add(ic);
					bs.addUnSynchronizedDeletedItemChange(c.getCollectionId(),
							ic);
				} else {
					serializeDeletion(commands, ic);
				}
			}
		}

		for (ItemChange ic : changed) {
			String clientId = processedClientIds.get(ic.getServerId());
			logger.info("processedIds:" + processedIds.toString()
					+ " ic.clientId: " + clientId + " ic.serverId: "
					+ ic.getServerId());

			if (clientId != null) {
				// Acks Add done by pda
				Element add = DOMUtils.createElement(responses, "Add");
				DOMUtils.createElementAndText(add, "ClientId", clientId);
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status",
						SyncStatus.OK.asXmlValue());
			} else if (processedClientIds.keySet().contains(ic.getServerId())) {
				// Change asked by device
				Element add = DOMUtils.createElement(responses, "Change");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				DOMUtils.createElementAndText(add, "Status",
						SyncStatus.OK.asXmlValue());
			} else {
				// New change done on server
				Element add = DOMUtils.createElement(commands, "Add");
				DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
				serializeChange(bs, add, c, ic);
			}
			processedClientIds.remove(ic.getServerId());
		}

		// Send error for the remaining entry in the Map because the
		// client has requested the addition of a resource that already exists
		// on the server
		Set<Entry<String, String>> entries = new HashSet<Map.Entry<String, String>>(
				processedClientIds.entrySet());
		for (Entry<String, String> entry : entries) {
			if (entry.getKey() != null) {
				if (entry.getKey().startsWith(c.getCollectionId().toString())) {
					Element add = null;
					if (entry.getValue() != null) {
						add = DOMUtils.createElement(responses, "Add");
						DOMUtils.createElementAndText(add, "ClientId",
								entry.getValue());
					} else {
						add = DOMUtils.createElement(responses, "Change");
					}
					DOMUtils.createElementAndText(add, "ServerId",
							entry.getKey());
					// need send ok since we do not synchronize event with
					// ParticipationState need-action
					DOMUtils.createElementAndText(add, "Status",
							SyncStatus.OK.asXmlValue());
				}
				processedClientIds.remove(entry.getKey());
			}
		}
		if (responses.getChildNodes().getLength() == 0) {
			responses.getParentNode().removeChild(responses);
		}
		if (commands.getChildNodes().getLength() == 0) {
			commands.getParentNode().removeChild(commands);
		}

	}

	private List<ItemChange> processWindowSize(SyncCollection c,
			DataDelta delta, BackendSession bs,
			Map<String, String> processedClientIds) {
		List<ItemChange> changed = new ArrayList<ItemChange>();
		if (delta != null) {
			changed.addAll(delta.getChanges());
		}
		changed.addAll(bs.getUnSynchronizedItemChange(c.getCollectionId()));

		bs.getUnSynchronizedItemChange(c.getCollectionId()).clear();
		logger.info("should send " + changed.size() + " change(s)");
		int changeItem = changed.size() - c.getWindowSize();
		logger.info("WindowsSize value is " + c.getWindowSize() + ", "
				+ (changeItem < 0 ? 0 : changeItem) + " changes will not send");

		if (changed.size() <= c.getWindowSize()) {
			return changed;
		}

		int nbChangeByMobile = processedClientIds.size();
		Set<ItemChange> changeByMobile = new HashSet<ItemChange>();
		// Find changes ask by the device
		for (Iterator<ItemChange> it = changed.iterator(); it.hasNext();) {
			ItemChange ic = it.next();
			if (processedClientIds.get(ic.getServerId()) != null
					|| processedClientIds.keySet().contains(ic.getServerId())) {
				changeByMobile.add(ic);
				it.remove();
			}
			if (nbChangeByMobile == changeByMobile.size()) {
				break;
			}
		}

		int changedSize = changed.size();
		for (int i = c.getWindowSize(); i < changedSize; i++) {
			ItemChange ic = changed.get(changed.size() - 1);
			bs.addUnSynchronizedItemChange(c.getCollectionId(), ic);
			changed.remove(ic);
		}

		changed.addAll(changeByMobile);
		c.setMoreAvailable(true);
		return changed;
	}

	private void doFetch(BackendSession bs, SyncCollection c, Element ce)
			throws ActiveSyncException {
		
		List<ItemChange> changed;
		changed = contentsExporter
				.fetch(bs, c.getSyncState().getDataType(), c.getFetchIds());
		Element commands = DOMUtils.createElement(ce, "Responses");
		for (ItemChange ic : changed) {
			Element add = DOMUtils.createElement(commands, "Fetch");
			DOMUtils.createElementAndText(add, "ServerId", ic.getServerId());
			DOMUtils.createElementAndText(add, "Status",
					SyncStatus.OK.asXmlValue());
			c.setTruncation(null);
			for (BodyPreference bp : c.getBodyPreferences().values()) {
				bp.setTruncationSize(null);
			}
			serializeChange(bs, add, c, ic);
		}
	}

	private void serializeDeletion(Element commands, ItemChange ic) {
		Element del = DOMUtils.createElement(commands, "Delete");
		DOMUtils.createElementAndText(del, "ServerId", ic.getServerId());
	}

	private void serializeChange(BackendSession bs, Element col,
			SyncCollection c, ItemChange ic) {
		IApplicationData data = ic.getData();
		IDataEncoder encoder = getEncoders().getEncoder(data);
		Element apData = DOMUtils.createElement(col, "ApplicationData");
		encoder.encode(bs, apData, data, c, true);
	}

	private boolean processCollections(BackendSession bs,
			Sync sync, Map<String, String> processedClientIds)
			throws ActiveSyncException {
		boolean changes = false;
		for (SyncCollection collection : sync.getCollections()) {
			SyncState colState = stMachine.getSyncState(collection.getCollectionId(),
					collection.getSyncKey());
			collection.setSyncState(colState);
			// Disables last push request
			IContinuation cont = waitContinuationCache.get(collection
					.getCollectionId());
			if (cont != null) {
				cont.error(SyncStatus.NEED_RETRY.asXmlValue());
			}

			if (colState.isValid()) {
				// get our sync state for this collection
				processModification(bs, collection, processedClientIds);
			}
			changes = changes || haveFilteredItemToSync(bs, collection);
		}
		return changes;
	}

	private boolean haveFilteredItemToSync(BackendSession bs,
			SyncCollection collection) {
		return contentsExporter.getFilterChanges(bs, collection);
	}

	/**
	 * Handles modifications requested by mobile device
	 * 
	 * @param bs
	 * @param collection
	 * @param importer
	 * @param modification
	 * @param processedClientIds
	 * @throws ActiveSyncException
	 */
	private void processModification(BackendSession bs,
			SyncCollection collection, Map<String, String> processedClientIds) throws ActiveSyncException {
		for (SyncCollectionChange change : collection.getChanges()) {

			if (change.getModType().equals("Modify")) {
				contentsImporter.importMessageChange(bs, collection.getCollectionId(),
						change.getServerId(), change.getClientId(),
						change.getData());
			} else if (change.getModType().equals("Add")
					|| change.getModType().equals("Change")) {
				logger.info("processing Add/Change (srv: "
						+ change.getServerId() + ", cli:"
						+ change.getClientId() + ")");

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
						collection.isDeletesAsMoves());
			}
		}
	}

	@Override
	public void sendResponseWithoutHierarchyChanges(BackendSession bs,
			Responder responder, Collection<SyncCollection> changedFolders,
			IContinuation continuation) {
		sendResponse(bs, responder, changedFolders, false, continuation);
	}

	@Override
	public void sendResponse(BackendSession bs, Responder responder,
			Collection<SyncCollection> changedFolders,
			boolean sendHierarchyChange, IContinuation continuation) {
		Map<String, String> processedClientIds = new HashMap<String, String>(
				bs.getLastSyncProcessedClientIds());
		bs.setLastSyncProcessedClientIds(new HashMap<String, String>());
		processResponse(bs, responder, bs.getLastMonitored(),
				processedClientIds, continuation);

	}

	public void processResponse(BackendSession bs, Responder responder,
			Collection<SyncCollection> changedFolders,
			Map<String, String> processedClientIds, IContinuation continuation) {

		Collection<FolderType> allSyncFolderType = contentsExporter
				.getSyncFolderType(changedFolders);

		Document reply = null;
		try {
			reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();

			Element cols = DOMUtils.createElement(root, "Collections");

			for (SyncCollection c : changedFolders) {
				Element ce = DOMUtils.createElement(cols, "Collection");
				try {
					if ("0".equals(c.getSyncKey())) {
						backend.resetCollection(bs, c.getCollectionId());
					}

					String syncKey = c.getSyncKey();
					SyncState st = stMachine
							.getSyncState(c.getCollectionId(), syncKey);

					SyncState oldClientSyncKey = bs.getLastClientSyncState(c
							.getCollectionId());
					if (oldClientSyncKey != null
							&& oldClientSyncKey.getKey().equals(syncKey)) {
						st.setLastSync(oldClientSyncKey.getLastSync());
					}

					if (c.getDataClass() != null) {
						DOMUtils.createElementAndText(ce, "Class",
								c.getDataClass());
					}

					if (!st.isValid()) {
						DOMUtils.createElementAndText(ce, "CollectionId", c
								.getCollectionId().toString());
						DOMUtils.createElementAndText(ce, "Status",
								SyncStatus.INVALID_SYNC_KEY.asXmlValue());
						DOMUtils.createElementAndText(ce, "SyncKey", "0");
					} else {
						Element sk = DOMUtils.createElement(ce, "SyncKey");
						DOMUtils.createElementAndText(ce, "CollectionId", c
								.getCollectionId().toString());
						DOMUtils.createElementAndText(ce, "Status",
								SyncStatus.OK.asXmlValue());

						if (!syncKey.equals("0")) {
							if (c.getFetchIds().size() == 0) {
								doUpdates(bs, c, ce, processedClientIds,
										allSyncFolderType);
							} else {
								doFetch(bs, c, ce);
							}
						}
						bs.addLastClientSyncState(c.getCollectionId(), st);
						sk.setTextContent(stMachine.allocateNewSyncKey(bs,
								c.getCollectionId()));
					}
				} catch (CollectionNotFoundException e) {
					sendError(responder, new HashSet<SyncCollection>(),
							SyncStatus.OBJECT_NOT_FOUND.asXmlValue(),
							continuation);
				}
			}
			logger.info("Resp for requestId: " + continuation.getReqId());
			responder.sendResponse("AirSync", reply);
		} catch (Exception e) {
			logger.error("Error creating Sync response", e);
		}
	}

	public void sendError(Responder responder, String errorStatus) {
		try {
			Document reply = null;
			reply = DOMUtils.createDoc(null, "Sync");
			Element root = reply.getDocumentElement();
			DOMUtils.createElementAndText(root, "Status", errorStatus);
			responder.sendResponse("AirSync", reply);
		} catch (Exception e) {
			logger.error("Error creating Sync response", e);
		}
	}

	@Override
	public void sendError(Responder responder,
			Set<SyncCollection> changedFolders, String errorStatus,
			IContinuation continuation) {
		Document ret = DOMUtils.createDoc(null, "Sync");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", errorStatus.toString());

		try {
			logger.info("Resp for requestId: " + continuation.getReqId());
			responder.sendResponse("AirSync", ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}
}
