/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.client.tests;

import java.io.InputStream;
import java.util.Map;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.obm.push.client.tests.SyncKeyUtils.fillSyncKey;
import static org.obm.push.client.tests.SyncKeyUtils.processCollection;

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
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
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
