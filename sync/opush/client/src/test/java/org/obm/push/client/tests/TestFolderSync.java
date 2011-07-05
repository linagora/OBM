package org.obm.push.client.tests;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
@Ignore
public class TestFolderSync extends AbstractPushTest {

	public void testFolderSync() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);
	}
	
	public void testFolderSyncBadSyncKey() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequestBadSyncKey.xml");
		Document doc = DOMUtils.parse(in);
		
		Document ret = postXml25("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);
		
		ret = postXml120("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);
		
		ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);
	}

}
