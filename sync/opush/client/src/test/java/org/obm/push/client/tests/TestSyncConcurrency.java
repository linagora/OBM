package org.obm.push.client.tests;

import java.io.InputStream;
import java.util.Map;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.obm.sync.push.client.utils.SyncKeyUtils.fillSyncKey;
import static org.obm.sync.push.client.utils.SyncKeyUtils.processCollection;

@Ignore("It's necessary to do again all tests")
public class TestSyncConcurrency extends AbstractPushTest {

	public void testConcurrencySync() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);
		String folderSyncKey = DOMUtils.getUniqueElement(ret.getDocumentElement(), "SyncKey").getTextContent();
		
		in = loadDataFile("ConcurrencySyncRequest.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);
		
		Map<String,String> sks = processCollection(ret.getDocumentElement());
		in = loadDataFile("ConcurrencyGetEstimateRequest.xml");
		doc = DOMUtils.parse(in);
		fillSyncKey(doc.getDocumentElement(), sks);
		DOMUtils.logDom(doc);
		ret = postXml("ItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("ConcurrencySyncRequest2.xml");
		doc = DOMUtils.parse(in);
		fillSyncKey(doc.getDocumentElement(), sks);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		SyncRequest syncRequest = new SyncRequest(ret, sks);
		FolderSyncRequest folderSyncRequest = new FolderSyncRequest(folderSyncKey);  
		
		syncRequest.start();
		Thread.sleep(1000);
		folderSyncRequest.start();
		
		while(!syncRequest.hasResp() || folderSyncRequest.hasResp() ){
		}
	}
	
	private class SyncRequest extends Thread{
		private Document lastResponse;
		private Boolean resp;
		private Map<String,String> lastSyncKey;
		
		public SyncRequest(Document lastResponse, Map<String,String> lastSyncKey){
			this.lastResponse = lastResponse;
			this.resp = false;
			this.lastSyncKey = lastSyncKey;
		}

		@Override
		public void run() {
			super.run();
			lastSyncKey.putAll(processCollection(lastResponse.getDocumentElement()));
			InputStream in = loadDataFile("ConcurrencySyncRequest3.xml");
			Document doc;
			try {
				doc = DOMUtils.parse(in);
				fillSyncKey(doc.getDocumentElement(), lastSyncKey);
				DOMUtils.logDom(doc);
				postXml("AirSync", doc, "Sync");
			} catch (Exception e) {
				fail(e.getMessage());
			} finally {
				resp = true;
			}
		}
		
		public Boolean hasResp(){
			return resp;
		}
	}
	
	private class FolderSyncRequest extends Thread{
		private String lastFolderSyncKey;
		private Boolean resp;
		
		public FolderSyncRequest(String lastFolderSyncKey){
			this.lastFolderSyncKey =lastFolderSyncKey;
			this.resp = false;
		}

		@Override
		public void run() {
			super.run();
			
			try {
				InputStream in = loadDataFile("FolderSyncRequest.xml");
				Document doc = DOMUtils.parse(in);
				Element se = DOMUtils.getUniqueElement(doc.getDocumentElement(), "SyncKey");
				se.setTextContent(lastFolderSyncKey);
				Document ret = postXml("FolderHierarchy", doc, "FolderSync");
				assertNotNull(ret);
			} catch (Exception e) {
				fail(e.getMessage());
			} finally {
				resp = true;
			}
		}
		public Boolean hasResp(){
			return resp;
		}
	}
}
