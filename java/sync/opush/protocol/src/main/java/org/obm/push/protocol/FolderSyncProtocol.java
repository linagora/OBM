package org.obm.push.protocol;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.ItemChange;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.protocol.bean.FolderSyncRequest;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FolderSyncProtocol {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	public FolderSyncRequest getRequest(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException();
		}
		String syncKey = DOMUtils.getElementText(doc.getDocumentElement(), "SyncKey");
		return new FolderSyncRequest(syncKey);
	}

	public Document encodeResponse(FolderSyncResponse folderSyncResponse) throws FactoryConfigurationError {
		Document ret = DOMUtils.createDoc(null, "FolderSync");
		Element root = ret.getDocumentElement();
		
		DOMUtils.createElementAndText(root, "Status", "1");
		
		Element sk = DOMUtils.createElement(root, "SyncKey");
		sk.setTextContent(folderSyncResponse.getNewSyncKey());

		Element changes = DOMUtils.createElement(root, "Changes");
		DOMUtils.createElementAndText(changes, "Count", String.valueOf(folderSyncResponse.getCount()));
		
		for (ItemChange itemChange: folderSyncResponse.getItemsAddedAndUpdated()) {
			Element addedOrUpdated;
			if (itemChange.isNew()) {
				addedOrUpdated = DOMUtils.createElement(changes, "Add");
			} else {
				addedOrUpdated = DOMUtils.createElement(changes, "Update");
			}
			addItemChange(addedOrUpdated, itemChange);
		}
		
		for (ItemChange itemChange: folderSyncResponse.getItemsDeleted()) {
			Element deleted = DOMUtils.createElement(changes, "Delete");
			DOMUtils.createElementAndText(deleted, "ServerId", itemChange.getServerId());
		}
		
		return ret;
	}

	private void addItemChange(Element addedOrUpdated, ItemChange itemChange) {
		DOMUtils.createElementAndText(addedOrUpdated, "ServerId", itemChange.getServerId());
		DOMUtils.createElementAndText(addedOrUpdated, "ParentId", itemChange.getParentId());
		DOMUtils.createElementAndText(addedOrUpdated, "DisplayName", itemChange.getDisplayName());
		DOMUtils.createElementAndText(addedOrUpdated, "Type", itemChange.getItemType()
				.asIntString());
	}

	public Document encodeErrorResponse(FolderSyncStatus status) {
		Document ret = DOMUtils.createDoc(null, "FolderSync");
		Element root = ret.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", status.asXmlValue());
		return ret;
	}
	
}
