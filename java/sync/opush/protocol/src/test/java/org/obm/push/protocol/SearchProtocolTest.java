package org.obm.push.protocol;

import static org.fest.assertions.Assertions.assertThat;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.bean.StoreName;
import org.obm.push.exception.activesync.XMLValidationException;
import org.obm.push.protocol.bean.SearchRequest;
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
	public void parseSearchRequestTest() throws SAXException, IOException, FactoryConfigurationError, XMLValidationException {
		SearchRequest searchRequest = getSearchRequest("search.xml");
		verifyObject(searchRequest, "Bill", StoreName.Mailbox, 0, 99);
	}
	
	@Test
	public void parseSearchRequestWithEmptyQueryElementTest() throws SAXException, IOException, FactoryConfigurationError, XMLValidationException {
		SearchRequest searchRequest = getSearchRequest("search-with-empty-query-element.xml");
		verifyObject(searchRequest, "", StoreName.Mailbox, 0, 99);
	}
	
	@Test
	public void parseSearchRequestStoreGAL() throws SAXException, IOException, FactoryConfigurationError, XMLValidationException {
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
	
	private SearchRequest getSearchRequest(String filename) throws SAXException, IOException, FactoryConfigurationError, XMLValidationException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("file/search/" + filename);
		Document document = DOMUtils.parse(inputStream);
		return searchProtocol.getRequest(document.getDocumentElement());
	}
	
}
