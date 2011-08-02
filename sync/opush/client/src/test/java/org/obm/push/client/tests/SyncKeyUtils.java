package org.obm.push.client.tests;

import java.util.HashMap;
import java.util.Map;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SyncKeyUtils {
	
	public static String getFolderSyncKey(Document doc){
		return DOMUtils.getElementText(doc.getDocumentElement(), "SyncKey");
	}
	
	public static void appendFolderSyncKey(Document doc, String syncKey){
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "SyncKey").setTextContent(syncKey);
	}
	
	public static void fillSyncKey(Element root, Map<String, String> sks) {
		NodeList nl = root.getElementsByTagName("Collection");

		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			String collectionId = DOMUtils.getElementText(col, "CollectionId");
			String syncKey = sks.get(collectionId);
			Element synckeyElem = DOMUtils.getUniqueElement(col, "SyncKey");
			if (synckeyElem == null) {
				synckeyElem = DOMUtils.getUniqueElement(col, "AirSync:SyncKey");
			}
			synckeyElem.setTextContent(syncKey);
		}

	}

	public static Map<String, String> processCollection(Element root) {
		Map<String, String> ret = new HashMap<String, String>();
		NodeList nl = root.getElementsByTagName("Collection");

		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			String collectionId = DOMUtils.getElementText(col, "CollectionId");
			String syncKey = DOMUtils.getElementText(col, "SyncKey");
			ret.put(collectionId, syncKey);
		}
		return ret;
	}

}
