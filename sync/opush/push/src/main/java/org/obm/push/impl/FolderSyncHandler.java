package org.obm.push.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.data.EncoderFactory;
import org.obm.push.state.StateMachine;
import org.obm.push.store.CollectionNotFoundException;
import org.obm.push.store.ISyncStorage;
import org.obm.push.store.SyncState;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FolderSyncHandler extends WbxmlRequestHandler {

	private final IHierarchyExporter hierarchyExporter;

	@Inject
	private FolderSyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, ISyncStorage storage,
			IHierarchyExporter hierarchyExporter,
			IContentsExporter contentsExporter, StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
		
		this.hierarchyExporter = hierarchyExporter;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		
		logger.info("devType = {}", bs.getDevType());
		if (doc == null) {
			try {
				responder
						.sendResponseFile(
								"text/plain",
								new ByteArrayInputStream("OPUSH IS ALIVE\n"
										.getBytes()));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return;
		}

		String syncKey = DOMUtils.getElementText(doc.getDocumentElement(),
				"SyncKey");

		try {
			SyncState state = stMachine.getFolderSyncState(bs.getLoginAtDomain(), bs.getDevId(),
					hierarchyExporter.getRootFolderUrl(bs), syncKey);
			
			if (!state.isValid()) {
				sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
				return;
			}
			// look for Add, Modify, Remove

			Element changes = DOMUtils.getUniqueElement(doc.getDocumentElement(),
					"Changes");

			// dataClass, filterType, state, int, int
			hierarchyExporter.configure(state, null, null, 0, 0);

			
			Document ret = DOMUtils.createDoc(null, "FolderSync");
			Element root = ret.getDocumentElement();
			DOMUtils.createElementAndText(root, "Status", "1");
			Element sk = DOMUtils.createElement(root, "SyncKey");
			changes = DOMUtils.createElement(root, "Changes");

			// FIXME we know that we do not monitor hierarchy, so just respond
			// that nothing changed
			List<ItemChange> changed = hierarchyExporter.getChanged(bs);
			if ("0".equals(syncKey)) {
				int cnt = hierarchyExporter.getCount(bs);
				DOMUtils.createElementAndText(changes, "Count", cnt + "");

				for (ItemChange sf : changed) {
					Element add = DOMUtils.createElement(changes, "Add");
					encode(add, sf);
				}
				List<ItemChange> deleted = hierarchyExporter.getDeleted(bs);
				for (ItemChange sf : deleted) {
					Element remove = DOMUtils.createElement(changes, "Remove");
					encode(remove, sf);
				}
			} else {
				DOMUtils.createElementAndText(changes, "Count", "0");
				for (ItemChange sf : changed) {
					if (sf.isNew()) {
						sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
						return;
					}
				}
			}
			
			String newSyncKey = stMachine.allocateNewSyncKey(bs,
					hierarchyExporter.getRootFolderId(bs));
			
			sk.setTextContent(newSyncKey);
			responder.sendResponse("FolderHierarchy", ret);
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	private void encode(Element add, ItemChange sf) {
		DOMUtils.createElementAndText(add, "ServerId", sf.getServerId());
		DOMUtils.createElementAndText(add, "ParentId", sf.getParentId());
		DOMUtils.createElementAndText(add, "DisplayName", sf.getDisplayName());
		DOMUtils.createElementAndText(add, "Type", sf.getItemType()
				.asIntString());
	}

	private void sendError(Responder resp, FolderSyncStatus status) {
		Document ret = DOMUtils.createDoc(null, "FolderSync");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", status.asXmlValue());
		try {
			resp.sendResponse("FolderHierarchy", ret);
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}
	}
}
