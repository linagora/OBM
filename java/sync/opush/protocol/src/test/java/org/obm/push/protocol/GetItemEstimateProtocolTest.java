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
import org.obm.push.bean.AnalysedSyncCollection;
import org.obm.push.bean.PIMDataType;
import org.obm.push.protocol.bean.GetItemEstimateRequest;
import org.obm.push.protocol.bean.GetItemEstimateResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;

import com.google.common.collect.Iterables;


public class GetItemEstimateProtocolTest {
	
	private GetItemEstimateProtocol getItemEstimateProtocol;
	
	@Before
	public void init() {
		getItemEstimateProtocol = new GetItemEstimateProtocol();
	}
	
	@Test
	public void testLoopWithinResponseProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<GetItemEstimate>" +
				"<Response>" +
				"<Status>1</Status>" +
				"<Collection>" +
				"<CollectionId>2</CollectionId>" +
				"<Estimate>10</Estimate>" +
				"</Collection>" +
				"</Response>" +
				"<Response>" +
				"<Status>1</Status>" +
				"<Collection>" +
				"<CollectionId>0</CollectionId>" +
				"<Estimate>20</Estimate>" +
				"</Collection>" +
				"</Response>" +
				"</GetItemEstimate>";

		
		GetItemEstimateResponse getItemEstimateResponse = getItemEstimateProtocol.decodeResponse(DOMUtils.parse(initialDocument));
		Document encodeResponse = getItemEstimateProtocol.encodeResponse(getItemEstimateResponse);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
	
	@Test
	public void testLoopWithinRequestProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<GetItemEstimate>" +
				"<Collection>" +
				"<Class>Email</Class>" +
				"<FilterType>0</FilterType>" +
				"<SyncKey>123-456</SyncKey>" +
				"<CollectionId>1</CollectionId>" +
				"</Collection>" +
				"<Collection>" +
				"<Class>Contacts</Class>" +
				"<FilterType>8</FilterType>" +
				"<SyncKey>789-012</SyncKey>" +
				"<CollectionId>2</CollectionId>" +
				"</Collection>" +
				"</GetItemEstimate>";
		
		GetItemEstimateRequest getItemEstimateRequest = getItemEstimateProtocol.decodeRequest(DOMUtils.parse(initialDocument));
		Document encodeResponse = getItemEstimateProtocol.encodeRequest(getItemEstimateRequest);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
	
	@Test
	public void testNoDataClass() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<GetItemEstimate>" +
					"<Collection>" +
						"<SyncKey>123-456</SyncKey>" +
						"<CollectionId>1</CollectionId>" +
					"</Collection>" +
				"</GetItemEstimate>";
		
		GetItemEstimateRequest request = getItemEstimateProtocol.decodeRequest(DOMUtils.parse(initialDocument));

		AnalysedSyncCollection syncCollection = Iterables.getOnlyElement(request.getSyncCollections());
		assertThat(syncCollection.getDataType()).isNull();
		assertThat(syncCollection.getDataClass()).isNull();
	}
	
	@Test
	public void testUnkownDataClass() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<GetItemEstimate>" +
					"<Collection>" +
						"<Class>music</Class>" +
						"<SyncKey>123-456</SyncKey>" +
						"<CollectionId>1</CollectionId>" +
					"</Collection>" +
				"</GetItemEstimate>";
		
		GetItemEstimateRequest request = getItemEstimateProtocol.decodeRequest(DOMUtils.parse(initialDocument));

		AnalysedSyncCollection syncCollection = Iterables.getOnlyElement(request.getSyncCollections());
		assertThat(syncCollection.getDataType()).isEqualTo(PIMDataType.UNKNOWN);
		assertThat(syncCollection.getDataClass()).isNull();
	}
	
	@Test
	public void testEmailDataClass() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<GetItemEstimate>" +
					"<Collection>" +
						"<Class>Email</Class>" +
						"<SyncKey>123-456</SyncKey>" +
						"<CollectionId>1</CollectionId>" +
					"</Collection>" +
				"</GetItemEstimate>";
		
		GetItemEstimateRequest request = getItemEstimateProtocol.decodeRequest(DOMUtils.parse(initialDocument));

		AnalysedSyncCollection syncCollection = Iterables.getOnlyElement(request.getSyncCollections());
		assertThat(syncCollection.getDataType()).isEqualTo(PIMDataType.EMAIL);
		assertThat(syncCollection.getDataClass()).isEqualTo("Email");
	}
}
