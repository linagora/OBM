/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.FolderSyncStatus;
import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.hierarchy.HierarchyCollectionChanges;
import org.obm.push.protocol.bean.FolderSyncRequest;
import org.obm.push.protocol.bean.FolderSyncResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;


public class FolderSyncProtocolTest {
	
	private FolderSyncProtocol folderSyncProtocol;
	
	@Before
	public void init() {
		folderSyncProtocol = new FolderSyncProtocol();
	}

	@Test
	public void testLoopWithinRequestProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<FolderSync>" +
				"<SyncKey>2f70eaa5-a95a-4f4e-af94-e062955be19b</SyncKey>" +
				"</FolderSync>";
		
		FolderSyncRequest folderSyncRequest = folderSyncProtocol.decodeRequest(DOMUtils.parse(initialDocument));
		Document encodeRequest = folderSyncProtocol.encodeRequest(folderSyncRequest);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeRequest));
	}
	
	@Test
	public void testLoopWithinResponseProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<FolderSync>" +
				"<Status>1</Status>" +
				"<SyncKey>2056e98e-be0e-4d1a-8f39-328614a32f3a</SyncKey>" +
				"<Changes>" +
				"<Count>3</Count>" +
				"<Add>" +
				"<ServerId>11:3</ServerId>" +
				"<ParentId>11</ParentId>" +
				"<DisplayName>displayName</DisplayName>" +
				"<Type>18</Type>" +
				"</Add>" +
				"<Update>" +
				"<ServerId>12:4</ServerId>" +
				"<ParentId>12</ParentId>" +
				"<DisplayName>nameDisplayed</DisplayName>" +
				"<Type>17</Type>" +
				"</Update>" +
				"<Delete>" +
				"<ServerId>13:5</ServerId>" +
				"</Delete>" +
				"</Changes>" +
				"</FolderSync>";
		
		FolderSyncResponse folderSyncResponse = folderSyncProtocol.decodeResponse(DOMUtils.parse(initialDocument));
		Document encodeResponse = folderSyncProtocol.encodeResponse(folderSyncResponse);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
	
	@Test
	public void testDecodeStatusOK() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<FolderSync>" +
				"<Status>1</Status>" +
				"<SyncKey>2056e98e-be0e-4d1a-8f39-328614a32f3a</SyncKey>" +
				"<Changes>" +
				"<Count>0</Count>" +
				"</Changes>" +
				"</FolderSync>";
		
		FolderSyncResponse folderSyncResponse = folderSyncProtocol.decodeResponse(DOMUtils.parse(initialDocument));

		assertThat(folderSyncResponse.getStatus()).isEqualTo(FolderSyncStatus.OK);
	}
	
	@Test
	public void testDecodeStatusInvalidSyncKey() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<FolderSync>" +
				"<Status>9</Status>" +
				"<SyncKey>2056e98e-be0e-4d1a-8f39-328614a32f3a</SyncKey>" +
				"<Changes>" +
				"<Count>0</Count>" +
				"</Changes>" +
				"</FolderSync>";
		
		FolderSyncResponse folderSyncResponse = folderSyncProtocol.decodeResponse(DOMUtils.parse(initialDocument));

		assertThat(folderSyncResponse.getStatus()).isEqualTo(FolderSyncStatus.INVALID_SYNC_KEY);
	}
	
	@Test
	public void testEncodeStatusOK() throws Exception {
		Document encodedDocument = folderSyncProtocol.encodeResponse(FolderSyncResponse.builder()
				.newSyncKey(new SyncKey("1234"))
				.status(FolderSyncStatus.OK)
				.hierarchyItemsChanges(HierarchyCollectionChanges.builder().build())
				.build());

		assertThat(DOMUtils.serialize(encodedDocument)).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<FolderSync>" +
				"<Status>1</Status>" +
				"<SyncKey>1234</SyncKey>" +
				"<Changes>" +
				"<Count>0</Count>" +
				"</Changes>" +
				"</FolderSync>");
	}
	
	@Test
	public void testEncodeStatusInvalidSyncKey() throws Exception {
		Document encodedDocument = folderSyncProtocol.encodeResponse(FolderSyncResponse.builder()
				.newSyncKey(new SyncKey("1234"))
				.status(FolderSyncStatus.INVALID_SYNC_KEY)
				.hierarchyItemsChanges(HierarchyCollectionChanges.builder().build())
				.build());

		assertThat(DOMUtils.serialize(encodedDocument)).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<FolderSync>" +
				"<Status>9</Status>" +
				"<SyncKey>1234</SyncKey>" +
				"<Changes>" +
				"<Count>0</Count>" +
				"</Changes>" +
				"</FolderSync>");
	}
}
