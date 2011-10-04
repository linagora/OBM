package org.obm.push.client.tests;

import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.SyncResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Ignore("It's necessary to do again all tests")
public class TestCalendarSync extends OPClientTests {

	public void testSync() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder calendarFolder = fsr.getFolders().get(
				FolderType.DEFAULT_CALENDAR_FOLDER);
		SyncResponse syncResp = testInitialSync(calendarFolder);

		InputStream in = null;
		Document doc = null;
		Document ret = null;

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		Collection colInbox = syncResp.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colInbox);
		assertTrue(colInbox.getAdds().size() > 0);
	}

	public void testSyncOldSyncKey() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder calendarFolder = fsr.getFolders().get(
				FolderType.DEFAULT_CALENDAR_FOLDER);
		SyncResponse syncResp1 = testInitialSync(calendarFolder);

		InputStream in = null;
		Document doc = null;
		Document ret = null;

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		SyncResponse syncResp2 = testSync(doc);
		assertNotNull(syncResp2);
		Collection colCal2 = syncResp2.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal2);
		assertTrue(colCal2.getAdds().size() > 0);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		SyncResponse syncResp3 = testSync(doc);
		assertNotNull(syncResp3);
		Collection colCal3 = syncResp3.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal3);
		assertEquals(0, colCal3.getAdds().size());

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		SyncResponse syncResp4 = testSync(doc);
		assertNotNull(syncResp4);
		Collection colCal4 = syncResp4.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal4);
		assertTrue(colCal4.getAdds().size() > 0);

	}

	public void testCalAdd() throws Exception {
		testOptions();
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder calendarFolder = fsr.getFolders().get(
				FolderType.DEFAULT_CALENDAR_FOLDER);
		SyncResponse syncResp1 = testInitialSync(calendarFolder);

		InputStream in = null;
		Document doc = null;
		Document ret = null;

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp1);
		SyncResponse syncResp2 = testSync(doc);
		assertNotNull(syncResp2);
		Collection colCal2 = syncResp2.getCollection(calendarFolder
				.getServerId());
		assertNotNull(colCal2);
		assertTrue(colCal2.getAdds().size() > 0);

		in = loadDataFile("GetItemEstimateRequestEvent.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
	
		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		ret = postXml("AirSync", doc, "Sync");

		in = loadDataFile("CalSyncAdd.xml");
		doc = DOMUtils.parse(in);
		replace(doc, calendarFolder, syncResp2);
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "ClientId")
				.setTextContent(UUID.randomUUID().toString());
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "Calendar:UID")
				.setTextContent(UUID.randomUUID().toString());
		SyncResponse syncRespAdd = testSync(doc);
		Collection colCalAdd = syncRespAdd.getCollection(calendarFolder
				.getServerId());
		assertNotNull(syncRespAdd);
		assertTrue(colCalAdd.getAdds().size() > 0);

	}

	public void testCalTwoAdd() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(
				doc.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
				.getTextContent();

		in = loadDataFile("CalSyncAdd.xml");
		doc = DOMUtils.parse(in);
		Element cliidElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"ClientId");
		cliidElem.setTextContent("999999999");
		for (int i = 0; i < 2; i++) {
			synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
					"SyncKey");
			synckeyElem.setTextContent(sk);
			DOMUtils.logDom(doc);
			ret = postXml("AirSync", doc, "Sync");
			assertNotNull(ret);
			sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
					.getTextContent();
		}

		NodeList nl = ret.getDocumentElement().getElementsByTagName(
				"ApplicationData");
		assertTrue(nl.getLength() > 0);

	}

	public void testCalDelete() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml25("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(
				doc.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();

		in = loadDataFile("CalSyncDelete1.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		Element cliidElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"ClientId");
		String clientId = "" + new Random().nextInt(999999999);
		cliidElem.setTextContent(clientId);
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

		NodeList nl = ret.getDocumentElement().getElementsByTagName("Add");
		String servId = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element elem = (Element) nl.item(i);
				String cliId = DOMUtils.getElementText(elem, "ClientId");
				if (clientId.equals(cliId)) {
					servId = DOMUtils.getElementText(elem, "ServerId");
					break;
				}
			}
		}
		if (servId == null) {
			fail();
		}
		sk = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey")
				.getTextContent();

		in = loadDataFile("CalSyncDelete2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		Element servIdElem = DOMUtils.getUniqueElement(
				doc.getDocumentElement(), "ServerId");
		servIdElem.setTextContent(servId);
		DOMUtils.logDom(doc);
		ret = postXml25("AirSync", doc, "Sync");
		assertNotNull(ret);

	}
}
