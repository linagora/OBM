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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.obm.push.client.tests.SyncKeyUtils.fillSyncKey;
import static org.obm.push.client.tests.SyncKeyUtils.processCollection;

import java.io.InputStream;
import java.util.Map;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

@Ignore("It's necessary to do again all tests")
public class TestFullSync extends AbstractPushTest {

	public void testFullSync() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);
		
		in = loadDataFile("FullSyncRequest.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);
		
		Map<String,String> sks = processCollection(ret.getDocumentElement());
		in = loadDataFile("FullGetEstimateRequest.xml");
		doc = DOMUtils.parse(in);
		fillSyncKey(doc.getDocumentElement(), sks);
		DOMUtils.logDom(doc);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);

		in = loadDataFile("FullSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		fillSyncKey(doc.getDocumentElement(), sks);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		SyncPush push1 = new SyncPush(ret, sks);		
		push1.start();
		
		while(!push1.hasResp() /*|| !push2.hasResp() ||!push3.hasResp() || !push4.hasResp()*/ ){
		}
	}
	
	private class SyncPush extends Thread {
		
		private Document lastResponse;
		private Boolean resp;
		private Map<String,String> lastSyncKey;
		
		public SyncPush(Document lastResponse, Map<String,String> lastSyncKey){
			this.lastResponse = lastResponse;
			this.resp = false;
			this.lastSyncKey = lastSyncKey;
		}

		@Override
		public void run() {
			super.run();
			lastSyncKey.putAll(SyncKeyUtils.processCollection(lastResponse.getDocumentElement()));
			InputStream in = loadDataFile("FullSyncRequest3.xml");
			Document doc;
			try {
				doc = DOMUtils.parse(in);
				SyncKeyUtils.fillSyncKey(doc.getDocumentElement(), lastSyncKey);
				DOMUtils.logDom(doc);
				postXml("AirSync", doc, "Sync");
			} catch (Exception e) {
				fail(e.getMessage());
			} finally {
				resp = true;
			}
		}
		
		public synchronized Boolean hasResp() {
			return resp;
		}
		
	}
	
	@SuppressWarnings("unused")
	private class PingPush extends Thread{
		
		private int num;
		private Boolean resp;
		
		public PingPush(int num){
			this.num = num;
			this.resp = false;
		}

		@Override
		public void run() {
			super.run();
			InputStream in = loadDataFile("FullPingRequest.xml");
			Document doc;
			try {
				doc = DOMUtils.parse(in);
				DOMUtils.logDom(doc);
				postXml("Ping", doc, "Ping");
			} catch (Exception e) {
				fail(e.getMessage());
			} finally {
				resp = true;
			}
		}
		
		public synchronized Boolean hasResp() {
			return resp;
		}
		
	}
}
