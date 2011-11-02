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

import org.junit.Ignore;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
@Ignore
public class TestItemOperation extends AbstractPushTest {

	public void testItemOperationFileReference() throws Exception {
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
		in = loadDataFile("EmailSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		in = loadDataFile("ItemOperationFileReference.xml");
		doc = DOMUtils.parse(in);
		String fileRef = DOMUtils.getElementText(ret.getDocumentElement(),
				"FileReference");
		Element refElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSyncBase:FileReference");
		refElem.setTextContent(fileRef);
		DOMUtils.logDom(doc);
		ret = postMultipartXml("ItemOperations", doc, "ItemOperations");
	}

	public void testItemOperationMail() throws Exception {
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
		in = loadDataFile("EmailSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		in = loadDataFile("ItemOperationMail.xml");
		doc = DOMUtils.parse(in);
		String serverId = DOMUtils.getElementText(ret.getDocumentElement(),
				"ServerId");
		String collectionId = serverId.split(":")[0];
		Element serIdElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:ServerId");
		serIdElem.setTextContent(serverId);
		Element colIdElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:CollectionId");
		colIdElem.setTextContent(collectionId);
		DOMUtils.logDom(doc);

		ret = postMultipartXml("ItemOperations", doc, "ItemOperations");
	}

	public void testItemOperationContact() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("contactSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		String sk = DOMUtils.getUniqueElement(ret.getDocumentElement(),
				"SyncKey").getTextContent();
		in = loadDataFile("contactSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		in = loadDataFile("ItemOperationContact.xml");
		doc = DOMUtils.parse(in);
		Element add = DOMUtils
				.getUniqueElement(ret.getDocumentElement(), "Add");
		String serverId = DOMUtils.getElementText(add, "ServerId");
		String collectionId = serverId.split(":")[0];
		Element serIdElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:ServerId");
		serIdElem.setTextContent(serverId);
		Element colIdElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:CollectionId");
		colIdElem.setTextContent(collectionId);
		DOMUtils.logDom(doc);

		ret = postMultipartXml("ItemOperations", doc, "ItemOperations");
	}

	public void testItemOperationCalendrier() throws Exception {
		InputStream in = loadDataFile("FolderSyncRequest.xml");
		Document doc = DOMUtils.parse(in);
		Document ret = postXml("FolderHierarchy", doc, "FolderSync");
		assertNotNull(ret);

		in = loadDataFile("CalSyncRequest.xml");
		doc = DOMUtils.parse(in);
		Element synckeyElem = DOMUtils.getUniqueElement(doc
				.getDocumentElement(), "SyncKey");
		synckeyElem.setTextContent("0");
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

		in = loadDataFile("ItemOperationCalendar.xml");
		doc = DOMUtils.parse(in);
		Element add = DOMUtils
				.getUniqueElement(ret.getDocumentElement(), "Add");
		String serverId = DOMUtils.getElementText(add, "ServerId");
		String collectionId = serverId.split(":")[0];
		Element serIdElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:ServerId");
		serIdElem.setTextContent(serverId);
		Element colIdElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:CollectionId");
		colIdElem.setTextContent(collectionId);
		DOMUtils.logDom(doc);

		ret = postMultipartXml("ItemOperations", doc, "ItemOperations");
	}

	public void testItemOperationMailServerIdError() throws Exception {
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
		in = loadDataFile("EmailSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		in = loadDataFile("ItemOperationMail.xml");
		doc = DOMUtils.parse(in);
		String serverId = DOMUtils.getElementText(ret.getDocumentElement(),
				"ServerId");
		String collectionId = serverId.split(":")[0];
		// Element serIdElem =
		// DOMUtils.getUniqueElement(doc.getDocumentElement(),
		// "AirSync:ServerId");
		// serIdElem.setTextContent(serverId);
		Element colIdElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"AirSync:CollectionId");
		colIdElem.setTextContent(collectionId);
		DOMUtils.logDom(doc);
		ret = postXml("ItemOperations", doc, "ItemOperations");
	}

	public void testEmptyFolderOperation() throws Exception {
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
		in = loadDataFile("EmailSyncRequest2.xml");
		doc = DOMUtils.parse(in);
		synckeyElem = DOMUtils.getUniqueElement(doc.getDocumentElement(),
				"SyncKey");
		synckeyElem.setTextContent(sk);
		DOMUtils.logDom(doc);
		ret = postXml("AirSync", doc, "Sync");
		assertNotNull(ret);

		in = loadDataFile("ItemOperationEmptyFolderContents.xml");
		doc = DOMUtils.parse(in);
		DOMUtils.logDom(doc);
		ret = postMultipartXml("ItemOperations", doc, "ItemOperations");
	}
}
