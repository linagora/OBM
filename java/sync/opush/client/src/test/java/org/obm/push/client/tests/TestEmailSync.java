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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.beans.Collection;
import org.obm.sync.push.client.beans.Folder;
import org.obm.sync.push.client.beans.FolderSyncResponse;
import org.obm.sync.push.client.beans.FolderType;
import org.obm.sync.push.client.beans.SyncResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Ignore("It's necessary to do again all tests")
public class TestEmailSync extends OPClientTests {

	public void testMailSync() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder inbox = fsr.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncResp = testInitialSync(inbox);
		
		InputStream in = null;
		Document doc = null;
		
		in = loadDataFile("GetItemEstimateRequestEmail.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		Document estimateRet = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(estimateRet);
		
		
		in = loadDataFile("EmailSyncRequest.xml");
		Document docFirst = DOMUtils.parse(in);
		replace(docFirst, inbox, syncResp);
		syncResp = testSync(docFirst);
		Collection colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		assertTrue(colInbox.getAdds().size() > 0);
		
		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		assertTrue(colInbox.getAdds().size() > 0);
		
		syncResp = testSync(docFirst);
		colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		assertTrue(colInbox.getAdds().size() > 0);
	}

	public void testMailSync2() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder inbox = fsr.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncResp = testInitialSync(inbox);
		
		InputStream in = null;
		Document doc = null;
		
		in = loadDataFile("GetItemEstimateRequestEmail.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		Document estimateRet = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(estimateRet);
		
		
		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		Collection colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		assertTrue(colInbox.getAdds().size() > 0);

		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		assertEquals(0, colInbox.getAdds().size());

	}
	
	public void testMailChangeRead() throws Exception {
		testOptions();
		FolderSyncResponse fsr = testInitialFolderSync();
		Folder inbox = fsr.getFolders().get(FolderType.DEFAULT_INBOX_FOLDER);
		SyncResponse syncResp = testInitialSync(inbox);
		
		InputStream in = null;
		Document doc = null;
		Document ret = null;
		
		in = loadDataFile("GetItemEstimateRequestEmail.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		ret = postXml("GetItemEstimate", doc, "GetItemEstimate");
		assertNotNull(ret);
		
		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		Collection colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		assertTrue(colInbox.getAdds().size() > 0);
		String serverId = colInbox.getAdds().get(0).getServerId();
		
		in = loadDataFile("EmailSyncReadRequest.xml");
		doc = DOMUtils.parse(in);
		replace(doc, inbox, syncResp);
		DOMUtils.getUniqueElement(doc.getDocumentElement(), "ServerId").setTextContent(serverId);
		syncResp = testSync(doc);
		syncResp = testSync(doc);
		assertNotNull(syncResp);
		colInbox = syncResp.getCollection(inbox.getServerId());
		assertNotNull(colInbox);
		assertEquals(0, colInbox.getAdds().size());

	}

	public void testMailSyncMultiBodyPref() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("EmailSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();
		in = loadDataFile("EmailSyncRequestMultipleBodyPref.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

	}
}
