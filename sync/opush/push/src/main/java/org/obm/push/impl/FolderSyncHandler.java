package org.obm.push.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.ItemChange;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.data.EncoderFactory;
import org.obm.push.exception.InvalidSyncKeyException;
import org.obm.push.exception.NoDocumentException;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ActiveSyncException;
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

	private static class FolderSyncRequest {
		
		private final String syncKey;
		
		public FolderSyncRequest(String syncKey) {
			this.syncKey = syncKey;
		}
		
		public String getSyncKey() {
			return syncKey;
		}
	}
	
	private static class FolderSyncResponse {
		
		private final Collection<ItemChange> itemChanges;
		private final String newSyncKey;
		
		public FolderSyncResponse(Collection<ItemChange> itemChanges, String newSyncKey) {
			this.itemChanges = itemChanges;
			this.newSyncKey = newSyncKey;
		}
		
		public int getCount() {
			return itemChanges.size();
		}

		public Collection<ItemChange> getItemChanges() {
			return itemChanges;
		}
		
		public String getNewSyncKey() {
			return newSyncKey;
		}
		
	}
	
	private final IHierarchyExporter hierarchyExporter;

	@Inject
	protected FolderSyncHandler(IBackend backend, EncoderFactory encoderFactory,
			IContentsImporter contentsImporter, ISyncStorage storage,
			IHierarchyExporter hierarchyExporter,
			IContentsExporter contentsExporter, StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
		
		this.hierarchyExporter = hierarchyExporter;
	}

	@Override
	@Transactional
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		
		try {
			FolderSyncRequest folderSyncRequest = getRequest(doc);
			FolderSyncResponse folderSyncResponse = doTheJob(bs, folderSyncRequest);
			Document ret = encodeResponse(folderSyncResponse);
			responder.sendResponse("FolderHierarchy", ret);
			
		} catch (CollectionNotFoundException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
		} catch (InvalidSyncKeyException e) {
			logger.error(e.getMessage(), e);
			sendError(responder, FolderSyncStatus.INVALID_SYNC_KEY);
		} catch (NoDocumentException e) {
			answerOpushIsAlive(responder);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

	}

	private Document encodeResponse(FolderSyncResponse folderSyncResponse)
			throws FactoryConfigurationError {
		Document ret = DOMUtils.createDoc(null, "FolderSync");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", "1");
		Element sk = DOMUtils.createElement(root, "SyncKey");
		Element changes = DOMUtils.createElement(root, "Changes");
			DOMUtils.createElementAndText(changes, "Count", String.valueOf(folderSyncResponse.getCount()));

			for (ItemChange sf : folderSyncResponse.getItemChanges()) {
				Element add = DOMUtils.createElement(changes, "Add");
				addItemChange(add, sf);
			}
		sk.setTextContent(folderSyncResponse.getNewSyncKey());
		return ret;
	}

	private FolderSyncResponse doTheJob(BackendSession bs,
			FolderSyncRequest folderSyncRequest) throws SQLException,
			InvalidSyncKeyException, CollectionNotFoundException,
			ActiveSyncException {
		
		// FIXME we know that we do not monitor hierarchy, so just respond
		// that nothing changed
		
		SyncState state = stMachine.getFolderSyncState(bs.getLoginAtDomain(), bs.getDevId(),
				hierarchyExporter.getRootFolderUrl(bs), folderSyncRequest.getSyncKey());
		
		if (!state.isValid()) {
			throw new InvalidSyncKeyException();
		}
		hierarchyExporter.configure(state, null, null, 0, 0);
								
		List<ItemChange> changed = hierarchyExporter.getChanged(bs);
		
		if (!isSynckeyValid(folderSyncRequest.getSyncKey(), changed)) {
			throw new InvalidSyncKeyException();
		}
		
		String newSyncKey = stMachine.allocateNewSyncKey(bs,
				hierarchyExporter.getRootFolderId(bs), null);
		
		FolderSyncResponse folderSyncResponse = new FolderSyncResponse(changed, newSyncKey);
		return folderSyncResponse;
	}

	private boolean isSynckeyValid(String syncKey, List<ItemChange> changed) {
		if (!"0".equals(syncKey)) {
			for (ItemChange sf : changed) {
				if (sf.isNew()) {
					return false;
				}
			}
		}
		return true;
	}

	private FolderSyncRequest getRequest(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException();
		}
		String syncKey = DOMUtils.getElementText(doc.getDocumentElement(), "SyncKey");
		return new FolderSyncRequest(syncKey);
	}
	
	private void answerOpushIsAlive(Responder responder) {
		try {
			responder
					.sendResponseFile(
							"text/plain",
							new ByteArrayInputStream("OPUSH IS ALIVE\n"
									.getBytes()));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void addItemChange(Element add, ItemChange sf) {
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
