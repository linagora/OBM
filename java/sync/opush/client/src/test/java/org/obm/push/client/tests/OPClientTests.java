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
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Ignore;
import org.obm.push.bean.SyncKey;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.AccountInfos;
import org.obm.sync.push.client.Collection;
import org.obm.sync.push.client.Folder;
import org.obm.sync.push.client.FolderSyncResponse;
import org.obm.sync.push.client.FolderType;
import org.obm.sync.push.client.SyncResponse;
import org.obm.sync.push.client.commands.DocumentProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Ignore("it needs a server to be usefull")
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
			FolderSyncResponse resp = opc.folderSync(SyncKey.INITIAL_FOLDER_SYNC_KEY);
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

	public SyncResponse testSync(final Document doc) throws Exception {
		SyncResponse resp = opc.sync(new DocumentProvider() {
			@Override
			public Document get(AccountInfos accountInfos) {
				return doc;
			}
		});
		assertNotNull(resp);
		assertNotNull(resp.getCollections());
		assertTrue(resp.getCollections().size() > 0);
		return resp;
	}

	public SyncKey getSyncKey(String collectionId, Map<String, Collection> cols) {
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
			{
				FolderType type = FolderType.valueOf(syncKey.getTextContent());
				if (folder.getType().equals(type)) {
					syncKey.setTextContent(getSyncKey(folder.getServerId(),
							syncResp.getCollections()).getSyncKey());
				}
			}
			{
				Element collectionId = DOMUtils.getUniqueElement(e,
						"CollectionId");
				FolderType type = FolderType.valueOf(collectionId
						.getTextContent());
				if (folder.getType().equals(type)) {
					collectionId.setTextContent(folder.getServerId());
				}
			}
		}
	}
}
