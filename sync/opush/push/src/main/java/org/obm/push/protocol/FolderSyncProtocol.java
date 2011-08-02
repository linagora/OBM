package org.obm.push.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;

import org.obm.push.bean.FolderSyncRequest;
import org.obm.push.bean.FolderSyncResponse;
import org.obm.push.bean.ItemChange;
import org.obm.push.exception.NoDocumentException;
import org.obm.push.impl.FolderSyncStatus;
import org.obm.push.impl.Responder;
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

	public Document encodeResponse(FolderSyncResponse folderSyncResponse)
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

	public void answerOpushIsAlive(Responder responder) {
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

	public void sendError(Responder resp, FolderSyncStatus status) {
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
