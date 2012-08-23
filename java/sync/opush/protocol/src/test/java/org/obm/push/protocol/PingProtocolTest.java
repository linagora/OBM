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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncCollection;
import org.obm.push.protocol.bean.PingRequest;
import org.obm.push.protocol.bean.PingResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableSet;

@RunWith(SlowFilterRunner.class)
public class PingProtocolTest {

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
		
		PingRequest expectedPingRequest = new PingRequest();
		SyncCollection syncCollection1 = new SyncCollection();
		syncCollection1.setCollectionId(1);
		syncCollection1.setDataClass("Calendar");
		SyncCollection syncCollection2 = new SyncCollection();
		syncCollection2.setCollectionId(4);
		syncCollection2.setDataClass("Contacts");
		
		expectedPingRequest.setSyncCollections(ImmutableSet.of(
				syncCollection1,
				syncCollection2));
		
		assertThat(new PingProtocol().decodeRequest(document)).isEqualTo(expectedPingRequest);
	}
	
	@Test
	public void encodeNoChangesWithFolders() throws TransformerException {
		SyncCollection syncCollection1 = new SyncCollection();
		syncCollection1.setCollectionId(1);
		syncCollection1.setDataClass("Calendar");
		
		SyncCollection syncCollection2 = new SyncCollection();
		syncCollection2.setCollectionId(4);
		syncCollection2.setDataClass("Contacts");

		PingResponse pingResponse = new PingResponse(
				ImmutableSet.of(syncCollection1,syncCollection2),
				PingStatus.NO_CHANGES);
		
		assertThat(DOMUtils.serialize(new PingProtocol().encodeResponse(pingResponse))).isEqualTo(
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
		PingResponse pingResponse = new PingResponse(
				ImmutableSet.<SyncCollection>of(),
				PingStatus.CHANGES_OCCURED);
		
		assertThat(DOMUtils.serialize(new PingProtocol().encodeResponse(pingResponse))).isEqualTo(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Ping>" +
					"<Status>2</Status>" +
				"</Ping>");
	}
}
