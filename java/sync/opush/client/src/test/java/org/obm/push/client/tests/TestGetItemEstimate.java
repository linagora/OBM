package org.obm.push.client.tests;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.obm.push.client.tests.SyncKeyUtils.fillSyncKey;
import static org.obm.push.client.tests.SyncKeyUtils.processCollection;
@Ignore
public class TestGetItemEstimate extends AbstractPushTest {

	public void testGetItemEstimate() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("FullSyncRequest.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);
		Map<String, String> sks = processCollection(ret.getDocumentElement());
		
		in = loadDataFile("GetItemEstimateRequest.xml");
		doc = DOMUtils.parse(in);
		fillSyncKey(doc.getDocumentElement(), sks);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	}

	public void testGetItemEstimateBadCollectionId() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("FullSyncRequest.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		Map<String, String> sks = processCollection(ret.getDocumentElement());
		in = loadDataFile("GetItemEstimateRequestErrorBadCollectionId.xml");
		doc = DOMUtils.parse(in);
		NodeList nl = doc.getDocumentElement().getElementsByTagName(
				"Collection");
		Iterator<String> vals = sks.values().iterator();
		for (int i = 0; i < nl.getLength(); i++) {
			Element col = (Element) nl.item(i);
			if (vals.hasNext()) {
				String syncKey = vals.next();
				Element synckeyElem = DOMUtils.getUniqueElement(col, "SyncKey");
				if (synckeyElem == null) {
					synckeyElem = DOMUtils.getUniqueElement(col,
							"AirSync:SyncKey");
				}
				synckeyElem.setTextContent(syncKey);
			}
		}

		DOMUtils.logDom(doc);
		ret = postXml25("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml120("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	}

	public void testGetItemEstimateBadSyncKey() throws Exception {
		InputStream in = loadDataFile("GetItemEstimateRequestBadSyncKey.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml120("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		ret = postXml25("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

	}
}
