package org.obm.push.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.obm.push.ActiveSyncServlet;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.CollectionChangeListener;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IListenerRegistration;
import org.obm.push.data.EncoderFactory;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.SyncCollection;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Handles the Provision cmd
 * 
 */
@Singleton
public class PingHandler extends WbxmlRequestHandler implements
		IContinuationHandler {

	@Inject
	private PingHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, ISyncStorage storage,
			IContentsExporter contentsExporter, StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		long intervalSeconds = 0;
		if (doc == null) {
			logger
					.info("Empty Ping, reusing cached heartbeat & monitored folders");

			intervalSeconds = storage.findLastHearbeat(bs.getDevId());

			// 5sec, why not ? a mobile device asking for <5sec is just stupid
			if (bs.getLastMonitored() == null
					|| bs.getLastMonitored().isEmpty() || intervalSeconds < 5) {

				logger.error("Don't know what to monitor, " + "interval: "
						+ intervalSeconds + " toMonitor: "
						+ bs.getLastMonitored());
				sendError(responder, PingStatus.MISSING_REQUEST_PARAMS, continuation);
				return;
			}
		} else {
			Element pr = doc.getDocumentElement();
			Element hb = DOMUtils.getUniqueElement(pr, "HeartbeatInterval");
			if (hb != null) {
				intervalSeconds = Long.parseLong(hb.getTextContent());
			} else {
				intervalSeconds = storage.findLastHearbeat(bs.getDevId());
			}

			Set<SyncCollection> toMonitor = new HashSet<SyncCollection>();
			NodeList folders = pr.getElementsByTagName("Folder");
			for (int i = 0; i < folders.getLength(); i++) {
				Element f = (Element) folders.item(i);
				SyncCollection sc = new SyncCollection();
				sc.setDataClass(DOMUtils.getElementText(f, "Class"));
				int id = Integer.parseInt(DOMUtils.getElementText(f, "Id"));
				sc.setCollectionId(id);
				toMonitor.add(sc);
				if ("email".equalsIgnoreCase(sc.getDataClass())) {
					try {
						backend.startEmailMonitoring(bs, id);
					} catch (CollectionNotFoundException e) {
						sendError(responder, PingStatus.FOLDER_SYNC_REQUIRED, continuation);
					} catch (ActiveSyncException e) {
						sendError(responder, PingStatus.SERVER_ERROR, continuation);
					}
				}
			}
			// pda is allowed to only send the folder list on the first ping
			if (folders.getLength() > 0) {
				logger.warn("=========== setting monitored to "
						+ toMonitor.size());
				bs.setLastMonitored(toMonitor);
			}
			storage.updateLastHearbeat(bs.getDevId(),
					intervalSeconds);
		}

		if (intervalSeconds > 0 && bs.getLastMonitored() != null) {
			bs.setLastContinuationHandler(ActiveSyncServlet.PING_HANDLER);
			CollectionChangeListener l = new CollectionChangeListener(bs,
					continuation, bs.getLastMonitored());
			IListenerRegistration reg = backend.addChangeListener(l);
			continuation.setListenerRegistration(reg);
			continuation.setCollectionChangeListener(l);
			logger.info("suspend for " + intervalSeconds + " seconds");
			synchronized (bs) {
				// logger
				// .warn("for testing purpose, we will only suspend for 40sec (to monitor: "
				// + bs.getLastMonitored() + ")");
				// continuation.suspend(40 * 1000);
				continuation.suspend(intervalSeconds * 1000);
			}
		} else {
			logger.error("Don't know what to monitor, interval is null");
			sendError(responder, PingStatus.MISSING_REQUEST_PARAMS,continuation);
			// sendResponse(bs, responder, null, true);
		}
	}

	@Override
	public void sendResponseWithoutHierarchyChanges(BackendSession bs, Responder responder,
			Collection<SyncCollection> changedFolders,
			IContinuation continuation) {
		sendResponse(bs, responder, changedFolders, false, continuation);
	}
	
	@Override
	public void sendResponse(BackendSession bs, Responder responder,
			Collection<SyncCollection> changedFolders, boolean sendHierarchyChange,IContinuation continuation) {
		Document ret = DOMUtils.createDoc(null, "Ping");

		fillResponse(ret.getDocumentElement(), changedFolders,
				sendHierarchyChange);
		try {
			responder.sendResponse("Ping", ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void fillResponse(Element element,
			Collection<SyncCollection> changedFolders, boolean sendHierarchyChange) {
		if (sendHierarchyChange) {
			DOMUtils.createElementAndText(element, "Status",
					PingStatus.FOLDER_SYNC_REQUIRED.asXmlValue());
		} else if (changedFolders == null || changedFolders.isEmpty()) {
			DOMUtils.createElementAndText(element, "Status",
					PingStatus.NO_CHANGES.asXmlValue());
		} else {
			DOMUtils.createElementAndText(element, "Status",
					PingStatus.CHANGES_OCCURED.asXmlValue());
			Element folders = DOMUtils.createElement(element, "Folders");
			for (SyncCollection sc : changedFolders) {
				DOMUtils.createElementAndText(folders, "Folder", sc
						.getCollectionId().toString());
			}
		}
	}

	private void sendError(Responder responder, PingStatus status, IContinuation continuation) {
		sendError(responder, new HashSet<SyncCollection>(), status.asXmlValue(),continuation);
	}

	@Override
	public void sendError(Responder responder,
			Set<SyncCollection> changedFolders, String errorStatus, IContinuation continuation) {
		Document ret = DOMUtils.createDoc(null, "Ping");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", errorStatus);

		try {
			responder.sendResponse("Ping", ret);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
