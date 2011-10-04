package org.obm.push.client.tests;

import java.util.Map;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.SyncResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class OPClientTests extends AbstractPushTest {

	public void testOptions() {
		try {
			opc.options();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	public FolderSyncResponse testInitialFolderSync() {
		try {
			FolderSyncResponse resp = opc.folderSync("0");
			assertNotNull(resp);
			assertNotNull(resp.getFolders());
			assertTrue(resp.getFolders().size() > 0);
			return resp;
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return null;
	}

	public SyncResponse testInitialSync(Folder... folders) {
		try {
			SyncResponse resp = opc.initialSync(folders);
			assertNotNull(resp);
			assertNotNull(resp.getCollections());
			assertEquals(folders.length, resp.getCollections().size());
			return resp;
		} catch (Exception e) {
			fail(e.getMessage());
		}
		return null;
	}

	public SyncResponse testSync(Document doc) throws Exception {
		SyncResponse resp = opc.sync(doc);
		assertNotNull(resp);
		assertNotNull(resp.getCollections());
		assertTrue(resp.getCollections().size() > 0);
		return resp;
	}

	public String getSyncKey(String collectionId, Map<String, Collection> cols) {
		Collection col = cols.get(collectionId);
		assertNotNull("Collection[" + collectionId + "] not found", col);
		assertNotNull(col.getSyncKey());
		return col.getSyncKey();
	}

	protected void replace(Document doc, Folder folder, SyncResponse syncResp) {
		NodeList nl = doc.getElementsByTagName("Collection");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			Element syncKey = DOMUtils.getUniqueElement(e, "SyncKey");
			if (syncKey == null) {
				syncKey = DOMUtils.getUniqueElement(e, "AirSync:SyncKey");
			}
			try {
				FolderType type = FolderType.valueOf(syncKey.getTextContent());
				if (folder.getType().equals(type)) {
					syncKey.setTextContent(getSyncKey(folder.getServerId(),
							syncResp.getCollections()));
				}
			} catch (Throwable t) {
			}
			try {
				Element collectionId = DOMUtils.getUniqueElement(e,
						"CollectionId");
				FolderType type = FolderType.valueOf(collectionId
						.getTextContent());
				if (folder.getType().equals(type)) {
					collectionId.setTextContent(folder.getServerId());
				}
			} catch (Throwable t) {
			}
		}
	}
}
