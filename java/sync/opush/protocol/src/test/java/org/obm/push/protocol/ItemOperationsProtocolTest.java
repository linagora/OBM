package org.obm.push.protocol;

import static org.obm.push.TestUtils.getXml;

import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;

import org.easymock.EasyMock;
import org.eclipse.jetty.http.HttpHeaders;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
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
		ActiveSyncRequest request = EasyMock.createMock(ActiveSyncRequest.class);
		EasyMock.expect(request.getHeader("MS-ASAcceptMultiPart")).andReturn("T");
		EasyMock.expect(request.getHeader(HttpHeaders.ACCEPT_ENCODING)).andReturn(null);
		EasyMock.replay(request);
		ItemOperationsRequest decodedRequest = itemOperationsProtocol.getRequest(request, document);
		EasyMock.verify(request);
		Assertions.assertThat(decodedRequest).isNotNull();
	}

}
