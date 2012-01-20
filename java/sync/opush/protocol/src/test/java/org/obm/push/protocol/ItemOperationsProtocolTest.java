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

import static org.obm.push.TestUtils.getXml;

import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;

import org.easymock.EasyMock;
import org.eclipse.jetty.http.HttpHeaders;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.protocol.bean.ItemOperationsRequest;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ItemOperationsProtocolTest {

	private ItemOperationsProtocol itemOperationsProtocol;

	@Before
	public void setup() {
		itemOperationsProtocol = new ItemOperationsProtocol(null);
	}
	
	@Test
	public void test() throws SAXException, IOException, FactoryConfigurationError {
		Document document = getXml(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<ItemOperations>" +
				"<Fetch>" +
				"<Store>Mailbox</Store>" +
				"<CollectionId>1400</CollectionId>" +
				"<ServerId>1400:350025</ServerId>" +
				"<Options>" +
				"<BodyPreference>" +
				"<Type>2</Type>" +
				"</BodyPreference>" +
				"</Options>" +
				"</Fetch>" +
				"</ItemOperations>");
		ActiveSyncRequest request = createDefaultActiveSyncRequestMock();
		EasyMock.replay(request);
		ItemOperationsRequest decodedRequest = itemOperationsProtocol.getRequest(request, document);
		EasyMock.verify(request);
		Assertions.assertThat(decodedRequest).isNotNull();
		Assertions.assertThat(decodedRequest.getFetch().getCollectionId()).isEqualTo("1400");
		Assertions.assertThat(decodedRequest.getFetch().getServerId()).isEqualTo("1400:350025");
		Assertions.assertThat(decodedRequest.getFetch().getType()).isEqualTo(MSEmailBodyType.HTML);
	}

	@Test
	public void testNoOptions() throws SAXException, IOException, FactoryConfigurationError {
		Document document = getXml(
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<ItemOperations>" +
				"<Fetch>" +
				"<Store>Mailbox</Store>" +
				"<CollectionId>1400</CollectionId>" +
				"<ServerId>1400:350025</ServerId>" +
				"</Fetch>" +
				"</ItemOperations>");
		ActiveSyncRequest request = createDefaultActiveSyncRequestMock();
		EasyMock.replay(request);
		ItemOperationsRequest decodedRequest = itemOperationsProtocol.getRequest(request, document);
		EasyMock.verify(request);
		Assertions.assertThat(decodedRequest).isNotNull();
		Assertions.assertThat(decodedRequest.getFetch().getCollectionId()).isEqualTo("1400");
		Assertions.assertThat(decodedRequest.getFetch().getServerId()).isEqualTo("1400:350025");
		Assertions.assertThat(decodedRequest.getFetch().getType()).isNull();
	}

	private ActiveSyncRequest createDefaultActiveSyncRequestMock() {
		ActiveSyncRequest request = EasyMock.createMock(ActiveSyncRequest.class);
		EasyMock.expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn("T");
		EasyMock.expect(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).andReturn(null);
		return request;
	}

	
}
