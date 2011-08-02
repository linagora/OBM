package org.obm.push.protocol;

import java.util.HashSet;

import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncCollection;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PingProtocol {

	public PingRequest getRequest(Document doc) {
		PingRequest pingRequest = new PingRequest();
		if (doc == null) {
			return pingRequest;
		}
		Element pr = doc.getDocumentElement();
		Element hb = DOMUtils.getUniqueElement(pr, "HeartbeatInterval");
		if (hb != null) {
			pingRequest.setHeartbeatInterval(Long.valueOf(hb.getTextContent()));
		}
		HashSet<SyncCollection> syncCollections = new HashSet<SyncCollection>();
		NodeList folders = pr.getElementsByTagName("Folder");
		for (int i = 0; i < folders.getLength(); i++) {
			SyncCollection syncCollection = new SyncCollection();
			Element f = (Element) folders.item(i);
			syncCollection.setDataClass(DOMUtils.getElementText(f, "Class"));
			int id = Integer.valueOf(DOMUtils.getElementText(f, "Id"));
			syncCollection.setCollectionId(id);
			syncCollections.add(syncCollection);
		}
		pingRequest.setSyncCollections(syncCollections);
		return pingRequest;
	}
	
	public Document encodeResponse(PingResponse pingResponse) {
		Document document = DOMUtils.createDoc(null, "Ping");
		Element root = document.getDocumentElement();
		
		DOMUtils.createElementAndText(root, "Status", pingResponse.getPingStatus().asXmlValue());
		Element folders = DOMUtils.createElement(root, "Folders");
		for (SyncCollection sc : pingResponse.getSyncCollections()) {
			DOMUtils.createElementAndText(folders, "Folder", sc.getCollectionId().toString());
		}
		return document;
	}

	public Document buildError(PingStatus status) {
		return buildError(status.asXmlValue());
	}

	public Document buildError(String errorStatus) {
		Document document = DOMUtils.createDoc(null, "Ping");
		Element root = document.getDocumentElement();
		DOMUtils.createElementAndText(root, "Status", errorStatus);
		return document;
	}
	
}
