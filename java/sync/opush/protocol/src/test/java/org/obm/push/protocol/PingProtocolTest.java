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
package org.obm.push.protocol;

import static org.fest.assertions.api.Assertions.assertThat;

import javax.xml.transform.TransformerException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncCollectionRequest;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncKey;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableSet;

@RunWith(SlowFilterRunner.class)
public class PingProtocolTest {
	
	private PingProtocol pingProtocol;
	
	@Before
	public void init() {
		pingProtocol = new PingProtocol(null);
	}
	
	@Test
	public void testLoopWithinRequestProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<Ping>" +
				"<HeartbeatInterval>470</HeartbeatInterval>" +
				"<Folders>" +
				"<Folder>" +
				"<Id>19</Id>" +
				"<Class>Contacts</Class>" +
				"</Folder>" +
				"<Folder>" +
				"<Id>22</Id>" +
				"<Class>Email</Class>" +
				"</Folder>" +
				"<Folder>" +
				"<Id>27</Id>" +
				"<Class>Calendar</Class>" +
				"</Folder>" +
				"</Folders>" +
				"</Ping>";
		
		PingRequest pingRequest = pingProtocol.decodeRequest(DOMUtils.parse(initialDocument));
		Document encodeRequest = pingProtocol.encodeRequest(pingRequest);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeRequest));
	}
	
	@Test
	public void testLoopWithinResponseProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<Ping>" +
				"<Status>1</Status>" +
				"<Folders>" +
				"<Folder>19</Folder>" +
				"<Folder>22</Folder>" +
				"<Folder>27</Folder>" +
				"</Folders>" +
				"</Ping>";
		
		PingResponse pingResponse = pingProtocol.decodeResponse(DOMUtils.parse(initialDocument));
		Document encodeResponse = pingProtocol.encodeResponse(pingResponse);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
	
	@Test
	public void decodeRequest() throws Exception {
		Document document = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Ping>" +
					"<Folders>" +
						"<Folder>" +
							"<Id>1</Id>" +
							"<Class>Calendar</Class>" +
						"</Folder>" +
						"<Folder>" +
							"<Id>4</Id>" +
							"<Class>Contacts</Class>" +
						"</Folder>" +
					"</Folders>" +
				"</Ping>");

		assertThat(pingProtocol.decodeRequest(document)).isEqualTo(PingRequest.builder()
				.syncCollections(ImmutableSet.of(
					SyncCollectionRequest.builder()
						.collectionId(1)
						.dataType(PIMDataType.CALENDAR)
						.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
						.build(),
					SyncCollectionRequest.builder()
						.collectionId(4)
						.dataType(PIMDataType.CONTACTS)
						.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
						.build()))
				.heartbeatInterval(null)
				.build());
	}
	
	@Test
	public void encodeNoChangesWithFolders() throws TransformerException {
		PingResponse pingResponse = PingResponse.builder()
				.syncCollections(ImmutableSet.of(
					SyncCollectionResponse.builder()
						.collectionId(1)
						.dataType(PIMDataType.CALENDAR)
						.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
						.build(),
					SyncCollectionResponse.builder()
						.collectionId(4)
						.dataType(PIMDataType.CONTACTS)
						.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
						.build()))
				.pingStatus(PingStatus.NO_CHANGES)
				.build();
		
		assertThat(DOMUtils.serialize(pingProtocol.encodeResponse(pingResponse))).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Ping>" +
					"<Status>1</Status>" +
					"<Folders>" +
						"<Folder>1</Folder>" +
						"<Folder>4</Folder>" +
					"</Folders>" +
				"</Ping>");
	}
	
	@Test
	public void encodeChangesWithoutFolder() throws TransformerException {
		PingResponse pingResponse = PingResponse.builder()
			.syncCollections(ImmutableSet.<SyncCollectionResponse>of())
			.pingStatus(PingStatus.CHANGES_OCCURED)
			.build();
		
		assertThat(DOMUtils.serialize(pingProtocol.encodeResponse(pingResponse))).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Ping>" +
					"<Status>2</Status>" +
				"</Ping>");
	}

	@Test
	public void testDecodeDataClass() throws Exception {
		Document document = DOMUtils.parse(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Ping>" +
					"<Folders>" +
						"<Folder>" +
							"<Id>1</Id>" +
						"</Folder>" +
						"<Folder>" +
							"<Id>2</Id>" +
							"<Class>Music</Class>" +
						"</Folder>" +
						"<Folder>" +
							"<Id>3</Id>" +
							"<Class>Contacts</Class>" +
						"</Folder>" +
					"</Folders>" +
				"</Ping>");

		PingRequest decoded = pingProtocol.decodeRequest(document);
		
		assertThat(decoded.getSyncCollections()).containsOnly(
			SyncCollectionRequest.builder()
				.collectionId(1)
				.dataType(null)
				.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
				.build(), 
			SyncCollectionRequest.builder()
				.collectionId(2)
				.dataType(PIMDataType.UNKNOWN)
				.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
				.build(), 
			SyncCollectionRequest.builder()
				.collectionId(3)
				.dataType(PIMDataType.CONTACTS)
				.syncKey(SyncKey.INITIAL_FOLDER_SYNC_KEY)
				.build());
	}
}
