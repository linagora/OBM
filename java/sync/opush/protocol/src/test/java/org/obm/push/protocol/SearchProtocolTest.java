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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.StoreName;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.XMLValidationException;
import org.obm.push.protocol.bean.SearchRequest;
import org.obm.push.protocol.bean.SearchResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class SearchProtocolTest {

	private SearchProtocol searchProtocol;
	
	@Before
	public void init() {
		this.searchProtocol = new SearchProtocol();
	}
	
	@Test
	public void parseSearchRequestTest() throws Exception {
		SearchRequest searchRequest = getSearchRequest("search.xml");
		verifyObject(searchRequest, "Bill", StoreName.Mailbox, 0, 99);
	}
	
	@Test
	public void parseSearchRequestWithEmptyQueryElementTest() throws Exception {
		SearchRequest searchRequest = getSearchRequest("search-with-empty-query-element.xml");
		verifyObject(searchRequest, "", StoreName.Mailbox, 0, 99);
	}
	
	@Test
	public void parseSearchRequestStoreGAL() throws Exception {
		SearchRequest searchRequest = getSearchRequest("search-store-GAL.xml");
		verifyObject(searchRequest, "Jobs", StoreName.GAL, 0, 50);
	}
	
	private void verifyObject(SearchRequest searchRequest, String query, StoreName storeName, Integer rangeLower, Integer rangeUpper) {
		Assert.assertNotNull(searchRequest);
		assertThat(searchRequest.getQuery()).as("FreeText element").isEqualTo(query);
		assertThat(searchRequest.getStoreName()).as("Store name").isEqualTo(storeName);
		assertThat(searchRequest.getRangeLower()).as("Range lower").isEqualTo(rangeLower);
		assertThat(searchRequest.getRangeUpper()).as("Range upper").isEqualTo(rangeUpper);
	}
	
	private SearchRequest getSearchRequest(String filename) 
			throws SAXException, IOException, FactoryConfigurationError, XMLValidationException, NoDocumentException {
		
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("file/search/" + filename);
		Document document = DOMUtils.parse(inputStream);
		return searchProtocol.decodeRequest(document);
	}
	
	@Test
	public void testLoopWithinRequestProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<Search xmlns:GAL=\"GAL\">" +
				"<Name>Document Library</Name>" +
				"<Query>" +
				"<FreeText>query</FreeText>" +
				"</Query>" +
				"<Range>10-100</Range>" +
				"</Search>";
		
		SearchRequest searchRequest = searchProtocol.decodeRequest(DOMUtils.parse(initialDocument));
		Document encodeRequest = searchProtocol.encodeRequest(searchRequest);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeRequest));
	}
	
	@Test
	public void testLoopWithinResponseProtocolMethods() throws Exception {
		String initialDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + 
				"<Search xmlns:GAL=\"GAL\">" +
				"<Status>1</Status>" +
				"<Response>" +
				"<Store>" +
				"<Status>1</Status>" +
				"<Result>" +
				"<Properties>" +
				"<GAL:DisplayName>name</GAL:DisplayName>" +
				"<GAL:Alias>alias</GAL:Alias>" +
				"<GAL:FirstName>first</GAL:FirstName>" +
				"<GAL:LastName>last</GAL:LastName>" +
				"<GAL:EmailAddress>email</GAL:EmailAddress>" +
				"<GAL:Company>company</GAL:Company>" +
				"<GAL:HomePhone>home</GAL:HomePhone>" +
				"<GAL:MobilePhone>mobile</GAL:MobilePhone>" +
				"<GAL:Office>office</GAL:Office>" +
				"<GAL:Phone>phone</GAL:Phone>" +
				"<GAL:Title>title</GAL:Title>" +
				"</Properties>" +
				"</Result>" +
				"<Result>" +
				"<Properties>" +
				"<GAL:DisplayName>name2</GAL:DisplayName>" +
				"<GAL:Alias>alias2</GAL:Alias>" +
				"<GAL:FirstName>first2</GAL:FirstName>" +
				"<GAL:LastName>last2</GAL:LastName>" +
				"<GAL:EmailAddress>email2</GAL:EmailAddress>" +
				"<GAL:Company>company2</GAL:Company>" +
				"<GAL:HomePhone>home2</GAL:HomePhone>" +
				"<GAL:MobilePhone>mobile2</GAL:MobilePhone>" +
				"<GAL:Office>office2</GAL:Office>" +
				"<GAL:Phone>phone2</GAL:Phone>" +
				"<GAL:Title>title2</GAL:Title>" +
				"</Properties>" +
				"</Result>" +
				"<Range>10-1</Range>" +
				"<Total>2</Total>" +
				"</Store>" +
				"</Response>" +
				"</Search>";
		
		SearchResponse searchResponse = searchProtocol.decodeResponse(DOMUtils.parse(initialDocument));
		Document encodeResponse = searchProtocol.encodeResponse(searchResponse);
		
		assertThat(initialDocument).isEqualTo(DOMUtils.serialize(encodeResponse));
	}
}
